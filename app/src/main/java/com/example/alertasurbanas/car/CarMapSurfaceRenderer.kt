package com.example.alertasurbanas.car

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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

    override fun onSurfaceDestroyed(
        surfaceContainer: SurfaceContainer
    ) {
        if (currentSurface === surfaceContainer.surface) {
            currentSurface?.release()
            currentSurface = null
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
            connection.setRequestProperty("User-Agent", "AlertasUrbanas/1.0")
            connection.setRequestProperty("Accept", "image/png")
            connection.connect()

            if (connection.responseCode in 200..299) {
                connection.inputStream.use {
                    BitmapFactory.decodeStream(it)
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
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

            if (bitmap != null) {
                canvas.drawBitmap(
                    bitmap,
                    null,
                    Rect(0, 0, canvas.width, canvas.height),
                    Paint(Paint.ANTI_ALIAS_FLAG)
                )
            } else {
                drawFallbackMap(canvas)
            }

            drawRoute(canvas)
            drawUserLocation(canvas)
            drawAlertMarkers(canvas)
            drawAttribution(canvas)
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

    private fun drawFallbackMap(canvas: Canvas) {
        canvas.drawColor(Color.rgb(241, 240, 236))

        val streetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = 14f
            style = Paint.Style.STROKE
        }

        val minorStreetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 224, 219)
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }

        val parkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(221, 233, 220)
            style = Paint.Style.FILL
        }

        canvas.drawCircle(
            canvas.width * 0.18f,
            canvas.height * 0.72f,
            120f,
            parkPaint
        )

        canvas.drawCircle(
            canvas.width * 0.78f,
            canvas.height * 0.28f,
            90f,
            parkPaint
        )

        for (i in 1..8) {
            val x = canvas.width * i / 9f
            canvas.drawLine(x, 0f, x, canvas.height.toFloat(), streetPaint)
        }

        for (i in 1..5) {
            val y = canvas.height * i / 6f
            canvas.drawLine(0f, y, canvas.width.toFloat(), y, streetPaint)
        }

        canvas.drawLine(
            0f,
            canvas.height * 0.75f,
            canvas.width.toFloat(),
            canvas.height * 0.25f,
            streetPaint
        )

        canvas.drawLine(
            canvas.width * 0.20f,
            0f,
            canvas.width * 0.55f,
            canvas.height.toFloat(),
            minorStreetPaint
        )

        canvas.drawLine(
            canvas.width * 0.70f,
            0f,
            canvas.width * 0.35f,
            canvas.height.toFloat(),
            minorStreetPaint
        )
    }

    private fun drawRoute(canvas: Canvas) {
        val routePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(63, 104, 98)
            strokeWidth = 11f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val routeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = 18f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val path = Path().apply {
            moveTo(canvas.width * 0.22f, canvas.height * 0.60f)
            lineTo(canvas.width * 0.38f, canvas.height * 0.48f)
            lineTo(canvas.width * 0.52f, canvas.height * 0.48f)
            lineTo(canvas.width * 0.64f, canvas.height * 0.56f)
            lineTo(canvas.width * 0.78f, canvas.height * 0.50f)
        }

        canvas.drawPath(path, routeBorderPaint)
        canvas.drawPath(path, routePaint)
    }

    private fun drawUserLocation(canvas: Canvas) {
        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(70, 66, 133, 244)
            style = Paint.Style.FILL
        }

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(66, 133, 244)
            style = Paint.Style.FILL
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        val x = canvas.width * 0.22f
        val y = canvas.height * 0.60f

        canvas.drawCircle(x, y, 42f, haloPaint)
        canvas.drawCircle(x, y, 17f, dotPaint)
        canvas.drawCircle(x, y, 17f, borderPaint)
    }

    private fun drawAlertMarkers(canvas: Canvas) {
        drawMarker(
            canvas = canvas,
            x = canvas.width * 0.43f,
            y = canvas.height * 0.36f,
            color = Color.rgb(217, 83, 79),
            label = "!"
        )

        drawMarker(
            canvas = canvas,
            x = canvas.width * 0.64f,
            y = canvas.height * 0.54f,
            color = Color.rgb(231, 163, 62),
            label = "!"
        )

        drawMarker(
            canvas = canvas,
            x = canvas.width * 0.78f,
            y = canvas.height * 0.50f,
            color = Color.rgb(217, 83, 79),
            label = "!"
        )
    }

    private fun drawMarker(
        canvas: Canvas,
        x: Float,
        y: Float,
        color: Int,
        label: String
    ) {
        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }

        val pinPath = Path().apply {
            addCircle(x, y, 34f, Path.Direction.CW)
            moveTo(x - 14f, y + 26f)
            lineTo(x, y + 56f)
            lineTo(x + 14f, y + 26f)
            close()
        }

        canvas.drawPath(pinPath, markerPaint)
        canvas.drawCircle(x, y, 34f, borderPaint)

        val textY = y - ((textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(label, x, textY, textPaint)
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

    fun close() {
        currentSurface?.release()
        currentSurface = null
        executor.shutdownNow()
    }
}