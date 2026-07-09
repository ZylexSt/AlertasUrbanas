package com.example.alertasurbanas.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class AutomotiveAlertDetailScreen(
    carContext: CarContext
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle("Bache peligroso")
                    .addText("Av. Siempre Viva 742")
                    .addText("Urgencia alta")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Recomendación")
                    .addText("Usa una ruta alternativa para evitar esta zona.")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Distancia")
                    .addText("A 120 m de tu ubicación")
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Volver al mapa")
                    .setOnClickListener {
                        screenManager.pop()
                    }
                    .build()
            )
            .build()

        return PaneTemplate.Builder(pane)
            .setTitle("Detalle de alerta")
            .setHeaderAction(Action.BACK)
            .build()
    }
}