package com.example.alertasurbanas.data

object AlertRepository {
    val publicAlerts = listOf(
        UrbanAlert(
            id = "alert-bache-001",
            title = "Bache peligroso",
            category = "Vía pública",
            address = "Av. Siempre Viva 742",
            description = "Bache profundo que representa riesgo para vehículos y motocicletas.",
            urgency = "Alta",
            distanceText = "120 m",
            timeText = "Hace 15 min",
            latitude = 31.7556,
            longitude = -106.4902
        ),
        UrbanAlert(
            id = "alert-choque-002",
            title = "Choque en Av. Reforma",
            category = "Tránsito",
            address = "Av. Reforma 1200",
            description = "Choque con reducción de carriles y tránsito lento.",
            urgency = "Alta",
            distanceText = "250 m",
            timeText = "Hace 20 min",
            latitude = 31.7642,
            longitude = -106.4851
        ),
        UrbanAlert(
            id = "alert-calle-003",
            title = "Calle bloqueada",
            category = "Vía pública",
            address = "Eje Central Lázaro Cárdenas",
            description = "Obstrucción temporal por trabajos y vehículos detenidos.",
            urgency = "Media",
            distanceText = "600 m",
            timeText = "Hace 35 min",
            latitude = 31.7583,
            longitude = -106.4754
        ),
        UrbanAlert(
            id = "alert-incendio-004",
            title = "Incendio reportado",
            category = "Incendio",
            address = "Calz. de Tlalpan 550",
            description = "Reporte ciudadano de humo visible en la zona.",
            urgency = "Alta",
            distanceText = "1.2 km",
            timeText = "Hace 45 min",
            latitude = 31.7487,
            longitude = -106.4818
        ),
        UrbanAlert(
            id = "alert-luminaria-005",
            title = "Luminaria apagada",
            category = "Iluminación",
            address = "Calle 26 Norte 115",
            description = "Zona con baja visibilidad durante la noche.",
            urgency = "Media",
            distanceText = "350 m",
            timeText = "Hace 1 h",
            latitude = 31.7674,
            longitude = -106.4934
        ),
        UrbanAlert(
            id = "alert-riesgo-006",
            title = "Zona de riesgo",
            category = "Seguridad",
            address = "Parque Central, acceso norte",
            description = "Zona marcada por reportes recurrentes durante la noche.",
            urgency = "Baja",
            distanceText = "900 m",
            timeText = "Hace 2 h",
            latitude = 31.7521,
            longitude = -106.4693
        )
    )
}
