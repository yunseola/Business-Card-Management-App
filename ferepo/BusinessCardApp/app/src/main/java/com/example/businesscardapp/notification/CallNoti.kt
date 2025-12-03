package com.example.businesscardapp.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.screen.incoming.IncomingActivity

object CallNoti {
    private const val CH_ID_INCOMING = "incoming_fullscreen"
    private const val CH_ID_SILENT   = "service_silent"

    fun ensureChannels(ctx: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 풀스크린(High + heads-up)
        if (nm.getNotificationChannel(CH_ID_INCOMING) == null) {
            val ch = NotificationChannel(
                CH_ID_INCOMING,
                "Incoming Call",
                NotificationManager.IMPORTANCE_HIGH
            )
            ch.enableLights(true)
            ch.lightColor = Color.WHITE
            ch.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            nm.createNotificationChannel(ch)
        }

        // 조용한(서비스용)
        if (nm.getNotificationChannel(CH_ID_SILENT) == null) {
            val ch = NotificationChannel(
                CH_ID_SILENT,
                "Background",
                NotificationManager.IMPORTANCE_MIN
            )
            ch.setSound(null, null)
            nm.createNotificationChannel(ch)
        }
    }

    fun buildSilentServiceNotification(ctx: Context): Notification {
        ensureChannels(ctx)
        return NotificationCompat.Builder(ctx, CH_ID_SILENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground2)
            .setContentTitle("백그라운드 동작 중")
            .setOngoing(true)
            .build()
    }

    fun showIncomingFullscreen(
        ctx: Context,
        phone: String?,
        notiId: Int = 7001
    ) {
        ensureChannels(ctx)

        val intent = Intent(ctx, IncomingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("phone", phone ?: "")
        }

        val pi = PendingIntent.getActivity(
            ctx, 100, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nb = NotificationCompat.Builder(ctx, CH_ID_INCOMING)
            .setSmallIcon(R.drawable.ic_launcher_foreground2)
            .setContentTitle("전화 정보 표시")
            .setContentText(phone ?: "발신번호 확인 중")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setFullScreenIntent(pi, true)

        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notiId, nb.build())
    }
}
