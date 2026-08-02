from pathlib import Path

import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.metrics import classification_report
from sklearn.model_selection import train_test_split
from sklearn.neural_network import MLPClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler


BASE_DIR = Path(__file__).parent
DATA_FILE = BASE_DIR / "data" / "urban_alerts_training.csv"
MODEL_DIR = BASE_DIR / "model"


def main() -> None:
    if not DATA_FILE.exists():
        raise FileNotFoundError("Primero ejecuta generate_dataset.py")

    df = pd.read_csv(DATA_FILE)

    x = df.drop(columns=["risk_level"])
    y = df["risk_level"]

    categorical_features = ["alert_type", "urgency"]
    numeric_features = ["hour", "weekday", "nearby_reports", "recent_reports", "rain_probability"]

    preprocessor = ColumnTransformer(
        transformers=[
            ("categorical", OneHotEncoder(handle_unknown="ignore"), categorical_features),
            ("numeric", StandardScaler(), numeric_features),
        ]
    )

    x_train, x_test, y_train, y_test = train_test_split(
        x,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y,
    )

    model = Pipeline(
        steps=[
            ("preprocessor", preprocessor),
            (
                "neural_network",
                MLPClassifier(
                    hidden_layer_sizes=(32, 16),
                    activation="relu",
                    solver="adam",
                    max_iter=600,
                    random_state=42,
                    early_stopping=True,
                    validation_fraction=0.15,
                ),
            ),
        ]
    )

    model.fit(x_train, y_train)

    predictions = model.predict(x_test)
    print(classification_report(y_test, predictions))

    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    joblib.dump(model, MODEL_DIR / "risk_model.joblib")
    print(f"Modelo guardado en: {MODEL_DIR}")


if __name__ == "__main__":
    main()
