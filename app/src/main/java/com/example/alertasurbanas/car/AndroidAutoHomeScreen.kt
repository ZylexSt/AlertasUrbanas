package com.example.alertasurbanas.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.ScreenManager

class AndroidAutoHomeScreen(
    carContext: CarContext
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle("Ruta segura")
                    .addText("Consulta alertas durante tu recorrido")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("2 alertas cercanas")
                    .addText("Bache peligroso y calle bloqueada")
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Ver mapa")
                    .setOnClickListener {
                        carContext
                            .getCarService(ScreenManager::class.java)
                            .push(AndroidAutoMapScreen(carContext))
                    }
                    .build()
            )
            .build()

        return PaneTemplate.Builder(pane)
            .setTitle("Alerta Urbana")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}