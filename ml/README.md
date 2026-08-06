# Módulo de Inteligencia Artificial - GeoNav

Este módulo representa la parte de innovación tecnológica del proyecto.

Objetivo:

- Analizar reportes urbanos.
- Estimar nivel de riesgo por zona, horario y tipo de incidente.
- Generar recomendaciones útiles para ciudadanos.

Modelo propuesto:

- Red neuronal multicapa en Python con `scikit-learn` usando `MLPClassifier`.
- Entrada: tipo de alerta, urgencia, hora, día de semana, reportes cercanos y reportes recientes.
- Salida: nivel de riesgo estimado:
  - 0 = bajo
  - 1 = medio
  - 2 = alto

Flujo local:

1. Ejecutar `generate_dataset.py` para crear datos sintéticos iniciales.
2. Ejecutar `train_risk_model.py` para entrenar la red neuronal.
3. Usar `predict_risk.py` para probar predicciones.

Flujo con Android:

1. Ejecutar `py -m pip install -r requirements.txt`.
2. Ejecutar `py generate_dataset.py`.
3. Ejecutar `py train_risk_model.py`.
4. Levantar la API:

```bash
py -m uvicorn api:app --reload --host 0.0.0.0 --port 8000
```

5. En Android, usar esta URL en `local.properties`:

```properties
AI_API_BASE_URL=http://10.0.2.2:8000
```

`10.0.2.2` es la forma en que el emulador Android accede al servidor local de tu computadora.

Endpoints:

- `GET /health`: revisa si la API está encendida y si el modelo existe.
- `POST /recommendations`: recibe reportes aprobados de Firebase y devuelve recomendaciones generadas por la red neuronal.

Nota: Se usa `scikit-learn` porque funciona correctamente con Python moderno en Windows. TensorFlow no siempre publica paquetes para versiones muy recientes como Python 3.14.
