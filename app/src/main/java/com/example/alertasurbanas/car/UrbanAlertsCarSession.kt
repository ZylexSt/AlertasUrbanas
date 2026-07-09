package com.example.alertasurbanas.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class UrbanAlertsCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return AutomotiveMapScreen(carContext)
    }
}