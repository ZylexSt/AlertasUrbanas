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
    userLatitude: float | None = None
    userLongitude: float | None = None
    analysisRadiusMeters: float = 15000.0


class AlertRecommendationRequest(BaseModel):
    selectedReport: ReportIn
    reports: list[ReportIn] = []


class AlertRecommendationOut(BaseModel):
    recommendation: str
    alternatives: list[str]


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


def street_name(value: str) -> str:
    value = value.strip()
    if not value:
        return "esta zona"
    return value.split(",")[0].strip() or "esta zona"


def normalize_urgency(value: str) -> str:
    value = value.strip().capitalize()
    if value in {"Alta", "Media", "Baja"}:
        return value
    return "Media"


def urgency_to_risk_label(value: str) -> RiskLabel:
    urgency = normalize_urgency(value)
    if urgency == "Alta":
        return "Riesgo alto"
    if urgency == "Media":
        return "Riesgo medio"
    return "Riesgo bajo"


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


def make_empty_summary(
    model_available: bool,
    has_user_location: bool = False,
    radius_meters: float = 15000.0,
) -> AISummaryOut:
    radius_km = round(radius_meters / 1000, 1)

    return AISummaryOut(
        title="Sin zonas de riesgo cercanas" if has_user_location else "Esperando ubicación",
        description=(
            f"No hay reportes validados dentro de un rango aproximado de {radius_km} km alrededor de tu ubicación actual."
            if has_user_location
            else "La IA necesita tu ubicación actual para analizar zonas cercanas en tiempo real."
        ),
        analyzedZones=0,
        highRiskZones=0,
        mediumRiskZones=0,
        lowRiskZones=0,
        modelAvailable=model_available,
        generatedAt=datetime.now().isoformat(timespec="seconds"),
        zones=[],
        recommendations=[
            AIRecommendationOut(
                title="Sin reportes cercanos validados" if has_user_location else "Activa la ubicación",
                description=(
                    "Cuando se validen reportes cerca de ti, esta sección mostrará patrones, zonas y recomendaciones útiles."
                    if has_user_location
                    else "Permite el acceso a ubicación para que la IA no analice reportes lejanos."
                ),
                label="Datos insuficientes",
                colorKey="primary",
                iconKey="lightbulb",
            )
        ],
    )


def sort_zones_for_user(
    zones: list[AIRiskZoneOut],
    user_latitude: float | None,
    user_longitude: float | None,
) -> list[AIRiskZoneOut]:
    risk_weight = {"Alta": 3, "Media": 2, "Baja": 1}

    if user_latitude is None or user_longitude is None:
        return sorted(
            zones,
            key=lambda zone: risk_weight[zone.riskLevel],
            reverse=True,
        )

    return sorted(
        zones,
        key=lambda zone: (
            haversine_meters(
                user_latitude,
                user_longitude,
                zone.latitude,
                zone.longitude,
            ),
            -risk_weight[zone.riskLevel],
        ),
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
    has_user_location = request.userLatitude is not None and request.userLongitude is not None
    valid_reports = [
        report
        for report in request.reports
        if report.status.lower() == "approved"
        and report.latitude is not None
        and report.longitude is not None
        and (
            request.userLatitude is None
            or request.userLongitude is None
            or haversine_meters(
                request.userLatitude,
                request.userLongitude,
                report.latitude,
                report.longitude,
            )
            <= request.analysisRadiusMeters
        )
    ]

    model = load_model()
    model_available = model is not None

    if not valid_reports:
        return make_empty_summary(
            model_available=model_available,
            has_user_location=has_user_location,
            radius_meters=request.analysisRadiusMeters,
        )

    predictions = [
        (report, predict_report_risk(model, report, valid_reports))
        for report in valid_reports
    ]

    risk_counter = Counter(urgency_to_risk_label(report.urgency) for report in valid_reports)
    category_counter = Counter(normalize_type(report.type) for report, _ in predictions)

    zones: dict[str, list[tuple[ReportIn, RiskLabel]]] = defaultdict(list)
    for report, label in predictions:
        zones[group_zone_key(report)].append((report, label))

    zone_levels = []
    zones_out: list[AIRiskZoneOut] = []

    for zone_id, values in zones.items():
        labels = [label for _, label in values]
        predicted_strongest = max(labels, key=lambda label: LABEL_PRIORITY[label])
        actual_strongest = max(
            (urgency_to_risk_label(report.urgency) for report, _ in values),
            key=lambda label: LABEL_PRIORITY[label],
        )
        strongest = min(
            predicted_strongest,
            actual_strongest,
            key=lambda label: LABEL_PRIORITY[label],
        )
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
    dominant_category_count = category_counter[dominant_category]
    urgency_counter = Counter(normalize_urgency(report.urgency) for report in valid_reports)
    dominant_urgency = urgency_counter.most_common(1)[0][0]
    dominant_urgency_count = urgency_counter[dominant_urgency]

    recommendations_out = [
        AIRecommendationOut(
            title="Resumen de riesgo cercano",
            description=(
                f"En tu rango cercano hay {len(valid_reports)} reporte(s) validado(s): "
                f"{risk_counter['Riesgo alto']} alta(s), {risk_counter['Riesgo medio']} media(s) "
                f"y {risk_counter['Riesgo bajo']} baja(s)."
            ),
            label="Predicción IA",
            colorKey="high" if risk_counter["Riesgo alto"] else "medium",
            iconKey="warning",
        ),
        AIRecommendationOut(
            title=f"Tipo más repetido: {dominant_category}",
            description=(
                f"Este tipo aparece {dominant_category_count} vez/veces dentro del área analizada. "
                "Úsalo como señal para revisar esas calles antes de iniciar una ruta."
            ),
            label="Patrón de categoría",
            colorKey="primary",
            iconKey="graph",
        ),
        AIRecommendationOut(
            title=f"Urgencia más común: {dominant_urgency}",
            description=(
                f"La urgencia {dominant_urgency.lower()} se repite {dominant_urgency_count} vez/veces. "
                f"Se detectaron {len(zones_out)} zona(s) con reportes alrededor de tu ubicación."
            ),
            label="Análisis de datos",
            colorKey="terracotta",
            iconKey="lightbulb",
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
        zones=sort_zones_for_user(zones_out, request.userLatitude, request.userLongitude),
        recommendations=recommendations_out,
    )


@app.post("/alert-recommendation", response_model=AlertRecommendationOut)
def alert_recommendation(request: AlertRecommendationRequest):
    selected = request.selectedReport
    valid_reports = [
        report
        for report in request.reports
        if report.status.lower() == "approved" and report.id != selected.id
    ]

    model = load_model()
    selected_label = predict_report_risk(model, selected, valid_reports + [selected])
    selected_street = street_name(selected.locationName)

    nearby_reports = sorted(
        [
            report
            for report in valid_reports
            if report.latitude is not None
            and report.longitude is not None
            and selected.latitude is not None
            and selected.longitude is not None
        ],
        key=lambda report: haversine_meters(
            selected.latitude or 0,
            selected.longitude or 0,
            report.latitude or 0,
            report.longitude or 0,
        ),
    )

    alternatives = []
    for report in nearby_reports:
        candidate = street_name(report.locationName)
        if not candidate or candidate.lower() == selected_street.lower():
            continue
        if report.urgency.lower() == "alta" and selected.urgency.lower() != "alta":
            continue
        if candidate not in alternatives:
            alternatives.append(candidate)
        if len(alternatives) >= 3:
            break

    if selected_label == "Riesgo alto" or selected.urgency.lower() == "alta":
        base = f"La red neuronal detecta riesgo alto en {selected_street or 'esta zona'}."
        action = "Evita cruzar directamente por ese punto y compara una ruta cercana antes de iniciar el recorrido."
    elif selected_label == "Riesgo medio" or selected.urgency.lower() == "media":
        base = f"La red neuronal detecta riesgo moderado en {selected_street or 'esta zona'}."
        action = "Puedes pasar con precaución, pero revisa si hay una calle paralela con menos reportes."
    else:
        base = f"La red neuronal detecta riesgo bajo en {selected_street or 'esta zona'}."
        action = "Mantén el mapa activo por si aparecen reportes nuevos durante el recorrido."

    if alternatives:
        alternative_text = "Calles cercanas para comparar: " + ", ".join(alternatives) + "."
    else:
        alternative_text = "No hay suficientes calles alternativas con menor riesgo en los reportes actuales."

    return AlertRecommendationOut(
        recommendation=f"{base} {action} {alternative_text}",
        alternatives=alternatives,
    )
