package com.example.alertasurbanas.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class AutomotiveRouteAlertsScreen(
    carContext: CarContext
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle("Ruta segura activa")
                    .addText("12 min · 4.2 km")
                    .addText("2 alertas importantes durante el recorrido")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Evita Av. Reforma")
                    .addText("Choque reportado a 250 m")
                    .addText("Riesgo alto")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Precaución en Eje Central")
                    .addText("Calle parcialmente bloqueada")
                    .addText("Riesgo medio")
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Ver detalle")
                    .setOnClickListener {
                        carContext
                            .getCarService(ScreenManager::class.java)
                            .push(AutomotiveAlertDetailScreen(carContext))
                    }
                    .build()
            )
            .build()

        return PaneTemplate.Builder(pane)
            .setTitle("Alertas en ruta")
            .setHeaderAction(Action.BACK)
            .build()
    }
}