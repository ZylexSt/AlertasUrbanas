package com.example.alertasurbanas.car

import androidx.car.app.CarAppService
import androidx.car.app.validation.HostValidator
import androidx.car.app.Session

class UrbanAlertsCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        // Permitido solamente durante el desarrollo.
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return UrbanAlertsCarSession()
    }
}