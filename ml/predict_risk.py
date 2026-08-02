from pathlib import Path

import joblib
import pandas as pd


BASE_DIR = Path(__file__).parent
MODEL_DIR = BASE_DIR / "model"

LABELS = {
    0: "Riesgo bajo",
    1: "Riesgo medio",
    2: "Riesgo alto",
}


def predict(sample: dict) -> str:
    model = joblib.load(MODEL_DIR / "risk_model.joblib")

    frame = pd.DataFrame([sample])
    prediction = int(model.predict(frame)[0])

    return LABELS[prediction]


if __name__ == "__main__":
    example = {
        "alert_type": "Tránsito",
        "urgency": "Alta",
        "hour": 18,
        "weekday": 4,
        "nearby_reports": 5,
        "recent_reports": 3,
        "rain_probability": 0.2,
    }

    print(predict(example))
