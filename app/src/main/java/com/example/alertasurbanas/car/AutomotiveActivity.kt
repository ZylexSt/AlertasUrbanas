package com.example.alertasurbanas.car

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

class AutomotiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AlertasUrbanasTheme {
                AutomotiveMapDashboardScreen()
            }
        }
    }
}