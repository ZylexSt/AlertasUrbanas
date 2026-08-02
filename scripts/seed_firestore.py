from __future__ import annotations

import argparse
import os
from pathlib import Path

import firebase_admin
from firebase_admin import auth, credentials, firestore


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SERVICE_ACCOUNT = PROJECT_ROOT / "serviceAccountKey.json"

SEED_USERS = [
    {
        "email": "admin@alertasurbanas.test",
        "password": "Admin123!",
        "name": "Administrador Urbano",
        "role": "admin",
    },
    {
        "email": "ana@alertasurbanas.test",
        "password": "Ciudadano123!",
        "name": "Ana Martínez",
        "role": "citizen",
    },
    {
        "email": "luis@alertasurbanas.test",
        "password": "Ciudadano123!",
        "name": "Luis Herrera",
        "role": "citizen",
    },
]

SEED_REPORTS = [
    {
        "type": "Choque",
        "description": "Choque sobre avenida principal con tráfico lento.",
        "urgency": "Alta",
        "locationName": "Universidad Tecnológica de Ciudad Juárez",
        "latitude": 31.59935,
        "longitude": -106.40780,
        "status": "approved",
    },
    {
        "type": "Bache",
        "description": "Bache profundo que representa riesgo para vehículos.",
        "urgency": "Alta",
        "locationName": "Av. Universidad Tecnológica, Ciudad Juárez",
        "latitude": 31.60015,
        "longitude": -106.40835,
        "status": "approved",
    },
    {
        "type": "Calle bloqueada",
        "description": "Obras bloqueando un carril, manejar con precaución.",
        "urgency": "Media",
        "locationName": "Calle cercana a UTCJ",
        "latitude": 31.59870,
        "longitude": -106.40695,
        "status": "approved",
    },
    {
        "type": "Luminaria",
        "description": "Luminaria apagada en zona peatonal.",
        "urgency": "Media",
        "locationName": "Zona habitacional cercana a UTCJ",
        "latitude": 31.60210,
        "longitude": -106.40590,
        "status": "approved",
    },
    {
        "type": "Incendio",
        "description": "Reporte de humo en lote baldío.",
        "urgency": "Alta",
        "locationName": "Sector suroriente de Ciudad Juárez",
        "latitude": 31.59590,
        "longitude": -106.41070,
        "status": "pending",
    },
    {
        "type": "Tránsito",
        "description": "Congestión por vehículo detenido.",
        "urgency": "Baja",
        "locationName": "Cruce vial cercano a UTCJ",
        "latitude": 31.60155,
        "longitude": -106.41110,
        "status": "rejected",
        "rejectionReason": "Falta evidencia clara del incidente.",
    },
]


def initialize_firebase(service_account_path: Path) -> None:
    if not service_account_path.exists():
        raise FileNotFoundError(
            f"No se encontró la llave de servicio: {service_account_path}\n"
            "Descárgala desde Firebase Console > Project settings > Service accounts."
        )

    if not firebase_admin._apps:
        cred = credentials.Certificate(str(service_account_path))
        firebase_admin.initialize_app(cred)


def get_or_create_user(email: str, password: str, name: str) -> auth.UserRecord:
    try:
        user = auth.get_user_by_email(email)
        print(f"Usuario existente: {email}")
        return user
    except auth.UserNotFoundError:
        user = auth.create_user(
            email=email,
            password=password,
            display_name=name,
        )
        print(f"Usuario creado: {email}")
        return user


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Inserta usuarios y reportes de prueba en Firebase."
    )
    parser.add_argument(
        "--service-account",
        default=os.getenv("FIREBASE_SERVICE_ACCOUNT", str(DEFAULT_SERVICE_ACCOUNT)),
        help="Ruta al serviceAccountKey.json.",
    )

    args = parser.parse_args()
    service_account_path = Path(args.service_account).expanduser().resolve()

    initialize_firebase(service_account_path)
    db = firestore.client()

    users_by_role = {"admin": [], "citizen": []}

    for seed_user in SEED_USERS:
        user = get_or_create_user(
            email=seed_user["email"],
            password=seed_user["password"],
            name=seed_user["name"],
        )

        users_by_role[seed_user["role"]].append(user)

        db.collection("users").document(user.uid).set(
            {
                "name": seed_user["name"],
                "email": seed_user["email"],
                "role": seed_user["role"],
            },
            merge=True,
        )

    citizen_users = users_by_role["citizen"]
    if not citizen_users:
        raise RuntimeError("No hay usuarios ciudadanos para asignar reportes.")

    batch = db.batch()

    for index, seed_report in enumerate(SEED_REPORTS):
        user = citizen_users[index % len(citizen_users)]
        report_ref = db.collection("reports").document()

        batch.set(
            report_ref,
            {
                **seed_report,
                "userId": user.uid,
                "userName": user.display_name or "Ciudadano",
                "createdAt": int(1785513600000 + (index * 900000)),
                "photoUrl": "",
                "rejectionReason": seed_report.get("rejectionReason", ""),
            },
        )

    batch.commit()

    print("Datos insertados correctamente.")
    print("Usuarios de prueba:")
    for seed_user in SEED_USERS:
        print(f"- {seed_user['email']} / {seed_user['password']} / {seed_user['role']}")
    print(f"Reportes creados: {len(SEED_REPORTS)}")


if __name__ == "__main__":
    main()
