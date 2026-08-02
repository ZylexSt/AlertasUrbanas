from __future__ import annotations

from collections import Counter, defaultdict
from datetime import datetime
from math import atan2, cos, radians, sin, sqrt
from pathlib import Path
from typing import Literal

import joblib
import pandas as pd
from fastapi import FastAPI
from pydantic import BaseModel


BASE_DIR = Path(__file__).parent
MODEL_PATH = BASE_DIR / "model" / "risk_model.joblib"

RiskLabel = Literal["Riesgo bajo", "Riesgo medio", "Riesgo alto"]

RISK_LABELS: dict[int, RiskLabel] = {
    0: "Riesgo bajo",
    1: "Riesgo medio",
    2: "Riesgo alto",
}

LABEL_PRIORITY = {
    "Riesgo bajo": 0,
    "Riesgo medio": 1,
    "Riesgo alto": 2,
}


class ReportIn(BaseModel):
    id: str = ""
    type: str = ""
    description: str = ""
    urgency: str = "Media"
    locationName: str = ""
    latitude: float | None = None
    longitude: float | None = None
    status: str = "approved"
    createdAt: int | None = None


class AIRequest(BaseModel):
    reports: list[ReportIn] = []


class AIRecommendationOut(BaseModel):
    title: str
    description: str
    label: str
    colorKey: Literal["primary", "high", "medium", "low", "terracotta"]
    iconKey: Literal["warning", "route", "graph", "lightbulb"]


class AIRiskZoneOut(BaseModel):
    id: str
    title: str
    riskLevel: Literal["Alta", "Media", "Baja"]
    dominantCategory: str
    reportCount: int
    latitude: float
    longitude: float
    recommendation: str


class AISummaryOut(BaseModel):
    title: str
    description: str
    analyzedZones: int
    highRiskZones: int
    mediumRiskZones: int
    lowRiskZones: int
    modelAvailable: bool
    generatedAt: str
    zones: list[AIRiskZoneOut]
    recommendations: list[AIRecommendationOut]


app = FastAPI(
    title="Alertas Urbanas IA",
    description="API de recomendaciones urbanas generadas con Machine Learning.",
    version="1.0.0",
)


def load_model():
    if not MODEL_PATH.exists():
        return None
    return joblib.load(MODEL_PATH)


def haversine_meters(
    lat1: float,
    lon1: float,
    lat2: float,
    lon2: float,
) -> float:
    earth_radius = 6_371_000
    d_lat = radians(lat2 - lat1)
    d_lon = radians(lon2 - lon1)
    r_lat1 = radians(lat1)
    r_lat2 = radians(lat2)

    a = (
        sin(d_lat / 2) * sin(d_lat / 2)
        + cos(r_lat1) * cos(r_lat2) * sin(d_lon / 2) * sin(d_lon / 2)
    )
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earth_radius * c


def normalize_type(value: str) -> str:
    value = value.strip()
    if not value:
        return "Vía pública"
    return value


def normalize_urgency(value: str) -> str:
    value = value.strip().capitalize()
    if value in {"Alta", "Media", "Baja"}:
        return value
    return "Media"


def group_zone_key(report: ReportIn) -> str:
    if report.latitude is None or report.longitude is None:
        return "sin-ubicacion"

    return f"{round(report.latitude, 3)}:{round(report.longitude, 3)}"


def count_nearby_reports(report: ReportIn, reports: list[ReportIn], radius_meters: float = 850) -> int:
    if report.latitude is None or report.longitude is None:
        return 0

    return sum(
        1
        for other in reports
        if other.id != report.id
        and other.latitude is not None
        and other.longitude is not None
        and haversine_meters(
            report.latitude,
            report.longitude,
            other.latitude,
            other.longitude,
        )
        <= radius_meters
    )


def count_recent_reports(report: ReportIn, reports: list[ReportIn], hours: int = 6) -> int:
    if report.createdAt is None:
        return 0

    window_ms = hours * 60 * 60 * 1000

    return sum(
        1
        for other in reports
        if other.id != report.id
        and other.createdAt is not None
        and 0 <= abs(report.createdAt - other.createdAt) <= window_ms
    )


def risk_from_rules(report: ReportIn, nearby_reports: int, recent_reports: int) -> RiskLabel:
    urgency = normalize_urgency(report.urgency)
    score = 0

    if urgency == "Alta":
        score += 3
    elif urgency == "Media":
        score += 2
    else:
        score += 1

    score += min(nearby_reports, 4)
    score += min(recent_reports, 3)

    if score >= 6:
        return "Riesgo alto"
    if score >= 4:
        return "Riesgo medio"
    return "Riesgo bajo"


def predict_report_risk(model, report: ReportIn, reports: list[ReportIn]) -> RiskLabel:
    timestamp = report.createdAt or int(datetime.now().timestamp() * 1000)
    created_at = datetime.fromtimestamp(timestamp / 1000)
    nearby_reports = count_nearby_reports(report, reports)
    recent_reports = count_recent_reports(report, reports)

    if model is None:
        return risk_from_rules(report, nearby_reports, recent_reports)

    sample = pd.DataFrame(
        [
            {
                "alert_type": normalize_type(report.type),
                "urgency": normalize_urgency(report.urgency),
                "hour": created_at.hour,
                "weekday": created_at.weekday(),
                "nearby_reports": nearby_reports,
                "recent_reports": recent_reports,
                "rain_probability": 0.0,
            }
        ]
    )

    prediction = int(model.predict(sample)[0])
    return RISK_LABELS.get(prediction, "Riesgo medio")


def make_empty_summary(model_available: bool) -> AISummaryOut:
    return AISummaryOut(
        title="Riesgo bajo en tu zona",
        description="Todavía no hay reportes suficientes para detectar patrones de riesgo.",
        analyzedZones=0,
        highRiskZones=0,
        mediumRiskZones=0,
        lowRiskZones=0,
        modelAvailable=model_available,
        generatedAt=datetime.now().isoformat(timespec="seconds"),
        zones=[],
        recommendations=[
            AIRecommendationOut(
                title="Genera más reportes para mejorar la IA",
                description="La red neuronal necesita datos reales de la aplicación para entregar recomendaciones más precisas.",
                label="Datos insuficientes",
                colorKey="primary",
                iconKey="lightbulb",
            )
        ],
    )


@app.get("/health")
def health():
    return {
        "ok": True,
        "modelAvailable": MODEL_PATH.exists(),
        "modelPath": str(MODEL_PATH),
    }


@app.post("/recommendations", response_model=AISummaryOut)
def recommendations(request: AIRequest):
    valid_reports = [
        report
        for report in request.reports
        if report.status.lower() == "approved"
    ]

    model = load_model()
    model_available = model is not None

    if not valid_reports:
        return make_empty_summary(model_available)

    predictions = [
        (report, predict_report_risk(model, report, valid_reports))
        for report in valid_reports
    ]

    risk_counter = Counter(label for _, label in predictions)
    category_counter = Counter(normalize_type(report.type) for report, _ in predictions)

    zones: dict[str, list[tuple[ReportIn, RiskLabel]]] = defaultdict(list)
    for report, label in predictions:
        zones[group_zone_key(report)].append((report, label))

    zone_levels = []
    zones_out: list[AIRiskZoneOut] = []

    for zone_id, values in zones.items():
        labels = [label for _, label in values]
        strongest = max(labels, key=lambda label: LABEL_PRIORITY[label])
        zone_levels.append(strongest)
        reports_in_zone = [report for report, _ in values]
        reports_with_location = [
            report
            for report in reports_in_zone
            if report.latitude is not None and report.longitude is not None
        ]

        if not reports_with_location:
            continue

        dominant_zone_category = Counter(normalize_type(report.type) for report in reports_in_zone).most_common(1)[0][0]
        risk_level = {
            "Riesgo alto": "Alta",
            "Riesgo medio": "Media",
            "Riesgo bajo": "Baja",
        }[strongest]

        zones_out.append(
            AIRiskZoneOut(
                id=zone_id,
                title=f"Zona {risk_level.lower()} · {dominant_zone_category}",
                riskLevel=risk_level,
                dominantCategory=dominant_zone_category,
                reportCount=len(reports_in_zone),
                latitude=sum(report.latitude or 0 for report in reports_with_location) / len(reports_with_location),
                longitude=sum(report.longitude or 0 for report in reports_with_location) / len(reports_with_location),
                recommendation={
                    "Alta": "Evita atravesar esta zona si hay una ruta alternativa disponible.",
                    "Media": "Pasa con precaución y compara rutas cercanas antes de salir.",
                    "Baja": "Zona con baja concentración de reportes activos.",
                }[risk_level],
            )
        )

    high_zones = zone_levels.count("Riesgo alto")
    medium_zones = zone_levels.count("Riesgo medio")
    low_zones = zone_levels.count("Riesgo bajo")

    if risk_counter["Riesgo alto"] > 0 or high_zones > 0:
        title = "Riesgo alto en tu zona"
    elif risk_counter["Riesgo medio"] > 0 or medium_zones > 0:
        title = "Riesgo moderado en tu zona"
    else:
        title = "Riesgo bajo en tu zona"

    dominant_category = category_counter.most_common(1)[0][0]

    recommendations_out = [
        AIRecommendationOut(
            title="Evita zonas con mayor concentración de riesgo",
            description=f"La red neuronal detectó {risk_counter['Riesgo alto']} reporte(s) de riesgo alto y {risk_counter['Riesgo medio']} de riesgo medio.",
            label="Predicción IA",
            colorKey="high" if risk_counter["Riesgo alto"] else "medium",
            iconKey="warning",
        ),
        AIRecommendationOut(
            title="Prioriza rutas con menos reportes cercanos",
            description="Antes de iniciar un recorrido, compara las rutas y elige la que tenga menor cantidad de incidentes activos.",
            label="Ruta sugerida",
            colorKey="primary",
            iconKey="route",
        ),
        AIRecommendationOut(
            title=f"Patrón recurrente: {dominant_category}",
            description="El modelo encontró que este tipo de incidente aparece con mayor frecuencia en los reportes recientes.",
            label="Análisis de datos",
            colorKey="terracotta",
            iconKey="graph",
        ),
    ]

    return AISummaryOut(
        title=title,
        description=f"Se analizaron {len(valid_reports)} reportes reales de la aplicación con una red neuronal en Python.",
        analyzedZones=len(zones),
        highRiskZones=high_zones,
        mediumRiskZones=medium_zones,
        lowRiskZones=low_zones,
        modelAvailable=model_available,
        generatedAt=datetime.now().isoformat(timespec="seconds"),
        zones=sorted(
            zones_out,
            key=lambda zone: {"Alta": 3, "Media": 2, "Baja": 1}[zone.riskLevel],
            reverse=True,
        ),
        recommendations=recommendations_out,
    )
