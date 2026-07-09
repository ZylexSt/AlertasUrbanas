package com.example.alertasurbanas.car

import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.MapWithContentTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.alertasurbanas.BuildConfig

class AutomotiveMapScreen(
    carContext: CarContext
) : Screen(carContext) {

    private val mapRenderer = CarMapSurfaceRenderer(
        mapTilerKey = BuildConfig.MAPTILER_API_KEY
    )

    init {
        carContext
            .getCarService(AppManager::class.java)
            .setSurfaceCallback(mapRenderer)

        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    carContext
                        .getCarService(AppManager::class.java)
                        .setSurfaceCallback(null)

                    mapRenderer.close()
                }
            }
        )
    }

    override fun onGetTemplate(): Template {
        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle("Choque en Av. Reforma")
                    .addText("Av. Reforma 1200")
                    .addText("Alta · 250 m")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Calle bloqueada")
                    .addText("Eje Central Lázaro Cárdenas")
                    .addText("Media · 600 m")
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Ruta segura")
                    .setOnClickListener {
                        carContext
                            .getCarService(ScreenManager::class.java)
                            .push(AutomotiveRouteAlertsScreen(carContext))
                    }
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

        val content = PaneTemplate.Builder(pane)
            .setTitle("Mapa de alertas")
            .setHeaderAction(Action.APP_ICON)
            .build()

        return MapWithContentTemplate.Builder()
            .setContentTemplate(content)
            .build()
    }
}