package com.example.alertasurbanas.car

import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.MapWithContentTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.alertasurbanas.BuildConfig

class AndroidAutoMapScreen(
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
                    .setTitle("Bache peligroso")
                    .addText("Av. Siempre Viva 742 · Urgencia alta")
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("2 alertas cercanas")
                    .addText("Conduce con precaución")
                    .build()
            )
            .build()

        val content = PaneTemplate.Builder(pane)
            .setTitle("Mapa de alertas")
            .setHeaderAction(Action.BACK)
            .build()

        return MapWithContentTemplate.Builder()
            .setContentTemplate(content)
            .build()
    }
}