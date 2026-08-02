from __future__ import annotations

import argparse
import json
import os
from datetime import datetime
from pathlib import Path
from typing import Any

import firebase_admin
from firebase_admin import auth, credentials, firestore


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SERVICE_ACCOUNT = PROJECT_ROOT / "serviceAccountKey.json"
DEFAULT_BACKUP_DIR = PROJECT_ROOT / "backups"


def initialize_firebase(service_account_path: Path) -> None:
    if not service_account_path.exists():
        raise FileNotFoundError(
            f"No se encontró la llave de servicio: {service_account_path}\n"
            "Descárgala desde Firebase Console > Project settings > Service accounts."
        )

    if not firebase_admin._apps:
        cred = credentials.Certificate(str(service_account_path))
        firebase_admin.initialize_app(cred)


def serialize_value(value: Any) -> Any:
    if hasattr(value, "isoformat"):
        return value.isoformat()

    if isinstance(value, dict):
        return {key: serialize_value(item) for key, item in value.items()}

    if isinstance(value, list):
        return [serialize_value(item) for item in value]

    return value


def export_collection(db: firestore.Client, collection_name: str) -> list[dict[str, Any]]:
    documents = []

    for document in db.collection(collection_name).stream():
        documents.append(
            {
                "id": document.id,
                "data": serialize_value(document.to_dict() or {}),
            }
        )

    return documents


def export_auth_users() -> list[dict[str, Any]]:
    users = []

    for user in auth.list_users().iterate_all():
        users.append(
            {
                "uid": user.uid,
                "email": user.email,
                "displayName": user.display_name,
                "disabled": user.disabled,
                "customClaims": user.custom_claims or {},
                "createdAt": user.user_metadata.creation_timestamp,
                "lastSignInAt": user.user_metadata.last_sign_in_timestamp,
            }
        )

    return users


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Respalda colecciones principales de Firestore en un archivo JSON local."
    )
    parser.add_argument(
        "--service-account",
        default=os.getenv("FIREBASE_SERVICE_ACCOUNT", str(DEFAULT_SERVICE_ACCOUNT)),
        help="Ruta al serviceAccountKey.json.",
    )
    parser.add_argument(
        "--out",
        default=str(DEFAULT_BACKUP_DIR),
        help="Carpeta donde se guardará el respaldo.",
    )
    parser.add_argument(
        "--collections",
        default="users,reports",
        help="Colecciones separadas por coma. Default: users,reports.",
    )
    parser.add_argument(
        "--include-auth",
        action="store_true",
        help="Incluye usuarios de Firebase Auth sin contraseñas.",
    )

    args = parser.parse_args()

    service_account_path = Path(args.service_account).expanduser().resolve()
    output_dir = Path(args.out).expanduser().resolve()
    collections = [item.strip() for item in args.collections.split(",") if item.strip()]

    initialize_firebase(service_account_path)
    db = firestore.client()

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_dir.mkdir(parents=True, exist_ok=True)
    output_file = output_dir / f"firestore_backup_{timestamp}.json"

    backup: dict[str, Any] = {
        "createdAt": datetime.now().isoformat(timespec="seconds"),
        "collections": {},
    }

    for collection_name in collections:
        backup["collections"][collection_name] = export_collection(db, collection_name)

    if args.include_auth:
        backup["authUsers"] = export_auth_users()

    output_file.write_text(
        json.dumps(backup, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    print(f"Respaldo creado: {output_file}")
    for collection_name, documents in backup["collections"].items():
        print(f"- {collection_name}: {len(documents)} documentos")

    if args.include_auth:
        print(f"- authUsers: {len(backup['authUsers'])} usuarios")


if __name__ == "__main__":
    main()
