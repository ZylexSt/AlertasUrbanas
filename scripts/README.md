# Scripts de Firebase

Estos scripts sirven para respaldar datos y sembrar usuarios/reportes de prueba.

## 1. Instalar dependencias

```powershell
cd C:\Users\angel\AndroidStudioProjects\AlertasUrbanas\scripts
py -m pip install -r requirements.txt
```

## 2. Descargar llave de servicio

En Firebase Console:

1. Project settings.
2. Service accounts.
3. Generate new private key.
4. Guarda el archivo como:

```text
C:\Users\angel\AndroidStudioProjects\AlertasUrbanas\serviceAccountKey.json
```

No subas ese archivo a GitHub. Ya está en `.gitignore`.

## 3. Hacer respaldo local antes de insertar datos

```powershell
cd C:\Users\angel\AndroidStudioProjects\AlertasUrbanas\scripts
py backup_firestore.py --include-auth
```

El respaldo queda en:

```text
C:\Users\angel\AndroidStudioProjects\AlertasUrbanas\backups
```

## 4. Insertar datos de prueba

Ejecuta esto solo después de crear el respaldo:

```powershell
py seed_firestore.py
```

Usuarios de prueba:

- `admin@alertasurbanas.test` / `Admin123!`
- `ana@alertasurbanas.test` / `Ciudadano123!`
- `luis@alertasurbanas.test` / `Ciudadano123!`

## Respaldo oficial de Firestore

Firebase también permite exportar Firestore a Cloud Storage usando el servicio administrado:

```powershell
gcloud config set project alertasurbanas-21964
gcloud firestore export gs://NOMBRE_DEL_BUCKET/backups/alertasurbanas --database="(default)"
```

Ese método requiere Cloud Storage y normalmente el plan Blaze.
