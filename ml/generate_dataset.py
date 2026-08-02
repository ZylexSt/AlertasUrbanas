import random
from pathlib import Path

import pandas as pd


OUTPUT_DIR = Path(__file__).parent / "data"
OUTPUT_FILE = OUTPUT_DIR / "urban_alerts_training.csv"

TYPES = ["Bache", "Tránsito", "Incendio", "Iluminación", "Seguridad", "Residuos"]
URGENCIES = ["Baja", "Media", "Alta"]


def risk_score(alert_type: str, urgency: str, hour: int, nearby_reports: int, recent_reports: int) -> int:
    score = 0

    if urgency == "Media":
        score += 2
    elif urgency == "Alta":
        score += 4

    if alert_type in ["Incendio", "Tránsito", "Seguridad"]:
        score += 2
    elif alert_type in ["Bache", "Iluminación"]:
        score += 1

    if 6 <= hour <= 9 or 17 <= hour <= 20:
        score += 2

    score += min(nearby_reports, 5)
    score += min(recent_reports, 4)

    if score >= 9:
        return 2
    if score >= 5:
        return 1
    return 0


def main() -> None:
    rows = []
    target_per_class = 600
    counts = {0: 0, 1: 0, 2: 0}

    while min(counts.values()) < target_per_class:
        alert_type = random.choice(TYPES)
        urgency = random.choice(URGENCIES)
        hour = random.randint(0, 23)
        weekday = random.randint(0, 6)
        nearby_reports = random.randint(0, 8)
        recent_reports = random.randint(0, 6)
        rain_probability = round(random.random(), 2)

        risk = risk_score(
            alert_type=alert_type,
            urgency=urgency,
            hour=hour,
            nearby_reports=nearby_reports,
            recent_reports=recent_reports,
        )

        if rain_probability > 0.7 and alert_type in ["Bache", "Tránsito"]:
            risk = min(2, risk + 1)

        if counts[risk] >= target_per_class:
            continue

        counts[risk] += 1
        rows.append(
            {
                "alert_type": alert_type,
                "urgency": urgency,
                "hour": hour,
                "weekday": weekday,
                "nearby_reports": nearby_reports,
                "recent_reports": recent_reports,
                "rain_probability": rain_probability,
                "risk_level": risk,
            }
        )

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    pd.DataFrame(rows).to_csv(OUTPUT_FILE, index=False)
    print(f"Dataset creado: {OUTPUT_FILE}")
    print(f"Distribución: {counts}")


if __name__ == "__main__":
    main()
