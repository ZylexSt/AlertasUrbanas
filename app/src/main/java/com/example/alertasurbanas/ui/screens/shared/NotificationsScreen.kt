package com.example.alertasurbanas.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.model.AppNotification
import com.example.alertasurbanas.ui.theme.UrbanColors
import java.util.concurrent.TimeUnit

private val NotificationsBackground = UrbanColors.Background
private val NotificationsPrimary = UrbanColors.Primary
private val NotificationsText = UrbanColors.TextPrimary
private val NotificationsDanger = UrbanColors.HighUrgency
private val NotificationsSuccess = UrbanColors.Success

@Composable
fun NotificationsScreen(
    notifications: List<AppNotification> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String = "",
    onBack: () -> Unit = {},
    onMarkAsRead: (AppNotification) -> Unit = {}
) {
    Scaffold(
        containerColor = NotificationsBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                NotificationsHeader(onBack = onBack)
            }

            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NotificationsPrimary)
                        }
                    }
                }

                errorMessage.isNotBlank() -> {
                    item {
                        Text(
                            text = errorMessage,
                            color = NotificationsDanger,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                notifications.isEmpty() -> {
                    item {
                        EmptyNotifications()
                    }
                }

                else -> {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onMarkAsRead = { onMarkAsRead(notification) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Regresar",
                tint = NotificationsText
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Notificaciones",
                color = NotificationsText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Actualizaciones sobre reportes y revisiones.",
                color = NotificationsText.copy(alpha = 0.62f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    onMarkAsRead: () -> Unit
) {
    val color = notificationColor(notification.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.read) 1.dp else 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(52.dp)
                        .background(color, RoundedCornerShape(100.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = if (notification.read) 0.10f else 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notificationIcon(notification.type),
                    contentDescription = null,
                    tint = color
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.title,
                        color = NotificationsText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.read) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = color.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Nuevo",
                                color = color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = notification.message,
                    color = NotificationsText.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatNotificationDate(notification.createdAt),
                    color = NotificationsText.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )

                if (!notification.read) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onMarkAsRead,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NotificationsPrimary)
                    ) {
                        Text(text = "Marcar como leída", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotifications() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = NotificationsPrimary.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = NotificationsPrimary,
                modifier = Modifier.padding(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Sin notificaciones",
            color = NotificationsText,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Text(
            text = "Cuando haya cambios importantes apareceran aqui.",
            color = NotificationsText.copy(alpha = 0.62f),
            fontSize = 12.sp
        )
    }
}

private fun notificationIcon(type: String): ImageVector {
    return when (type) {
        "report_approved" -> Icons.Outlined.CheckCircle
        "report_rejected" -> Icons.Outlined.ReportProblem
        "report_created" -> Icons.Outlined.Notifications
        else -> Icons.Outlined.Notifications
    }
}

private fun notificationColor(type: String): Color {
    return when (type) {
        "report_approved" -> NotificationsSuccess
        "report_rejected" -> NotificationsDanger
        "report_created" -> NotificationsPrimary
        else -> NotificationsPrimary
    }
}

private fun formatNotificationDate(createdAt: Long): String {
    if (createdAt <= 0L) return "Reciente"

    val elapsed = System.currentTimeMillis() - createdAt
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsed)
    val days = TimeUnit.MILLISECONDS.toDays(elapsed)

    return when {
        minutes < 1 -> "Hace un momento"
        minutes < 60 -> "Hace $minutes min"
        hours < 24 -> "Hace $hours h"
        days == 1L -> "Ayer"
        else -> "Hace $days dias"
    }
}
