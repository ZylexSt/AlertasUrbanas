package com.example.alertasurbanas.car

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class CarMapSurfaceRenderer(
    private val mapTilerKey: String
) : SurfaceCallback {

    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var currentSurface: android.view.Surface? = null

    @Volatile
    private var surfaceWidth: Int = 0

    @Volatile
    private var surfaceHeight: Int = 0

    override fun onSurfaceAvailable(
        surfaceContainer: SurfaceContainer
    ) {
        val newSurface = surfaceContainer.surface ?: return

        if (currentSurface !== newSurface) {
            currentSurface?.release()
        }

        currentSurface = newSurface
        surfaceWidth = surfaceContainer.width
        surfaceHeight = surfaceContainer.height

        executor.execute {
            val map = downloadMap()
            drawMap(map)
        }
    }

    private fun downloadMap(): Bitmap? {
        if (mapTilerKey.isBlank()) {
            return null
        }

        // Centro provisional: Ciudad Juárez.
        val longitude = -106.485
        val latitude = 31.761
        val zoom = 13

        val url = URL(
            "https://api.maptiler.com/maps/streets-v2/static/" +
                    "$longitude,$latitude,$zoom/1280x720.png" +
                    "?key=$mapTilerKey"
        )

        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode in 200..299) {
                connection.inputStream.use {
                    BitmapFactory.decodeStream(it)
                }
            } else {
                null
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun drawMap(bitmap: Bitmap?) {
        val surface = currentSurface ?: return

        if (!surface.isValid) {
            return
        }

        var canvas: Canvas? = null

        try {
            canvas = surface.lockCanvas(null)
            canvas.drawColor(Color.rgb(35, 42, 44))

            if (bitmap != null) {
                canvas.drawBitmap(
                    bitmap,
                    null,
                    Rect(0, 0, canvas.width, canvas.height),
                    Paint(Paint.ANTI_ALIAS_FLAG)
                )

                drawAlertMarkers(canvas)
                drawAttribution(canvas)
            } else {
                drawError(canvas)
            }
        } catch (_: Exception) {
            // La superficie puede cambiar mientras se dibuja.
        } finally {
            if (canvas != null && surface.isValid) {
                try {
                    surface.unlockCanvasAndPost(canvas)
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun drawAlertMarkers(canvas: Canvas) {
        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(217, 83, 79)
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        val markers = listOf(
            canvas.width * 0.43f to canvas.height * 0.42f,
            canvas.width * 0.67f to canvas.height * 0.56f
        )

        markers.forEach { (x, y) ->
            canvas.drawCircle(x, y, 27f, markerPaint)
            canvas.drawCircle(x, y, 27f, borderPaint)
        }
    }

    private fun drawAttribution(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        canvas.drawText(
            "© MapTiler © OpenStreetMap contributors",
            18f,
            canvas.height - 18f,
            paint
        )
    }

    private fun drawError(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f
        }

        canvas.drawText(
            "No se pudo cargar el mapa",
            50f,
            canvas.height / 2f,
            paint
        )
    }

    override fun onSurfaceDestroyed(
        surfaceContainer: SurfaceContainer
    ) {
        if (currentSurface === surfaceContainer.surface) {
            currentSurface?.release()
            currentSurface = null
        }
    }

    fun close() {
        currentSurface?.release()
        currentSurface = null
        executor.shutdownNow()
    }
}