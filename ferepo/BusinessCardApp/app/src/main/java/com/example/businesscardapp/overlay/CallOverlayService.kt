package com.example.businesscardapp.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import com.example.businesscardapp.R
import com.example.businesscardapp.data.model.CardCallInfoResponse
import com.google.gson.Gson

class CallOverlayService : Service() {

    companion object {
        private const val NOTI_CHANNEL_ID = "incoming_overlay"
        private const val NOTI_ID = 2002
        const val EXTRA_PHONE = "phone"
        const val EXTRA_CARD_INFO_JSON = "cardInfoJson"
    }

    private lateinit var wm: WindowManager
    private var overlayView: View? = null
    private var composeView: ComposeView? = null

    private var telMgr: TelephonyManager? = null
    private var listener: PhoneStateListener? = null

    private var latestPhone: String? = null
    private var latestCard: CardCallInfoResponse? = null

    override fun onCreate() {
        super.onCreate()
        createChannelIfNeeded()
        startForeground(NOTI_ID, buildNotification())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        telMgr = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        latestPhone = intent?.getStringExtra(EXTRA_PHONE)
        latestCard = intent?.getStringExtra(EXTRA_CARD_INFO_JSON)
            ?.let { runCatching { Gson().fromJson(it, CardCallInfoResponse::class.java) }.getOrNull() }

        if (overlayView == null) {
            showOverlay()
            listenCallStateForAutoClose()
            // 안전장치: 60초 뒤 자동 종료(필요 없으면 주석 처리)
            overlayView?.postDelayed({ stopSelf() }, 60_000)
        } else {
            // 이미 떠 있으면 내용만 갱신
            render()
        }

        return START_NOT_STICKY
    }

    private fun showOverlay() {
        composeView = ComposeView(this)
        render()

        val container = FrameLayout(this).apply {
            addView(
                composeView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            // 포커스는 뺏지 않되, 화면 최상단에 배치
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = dp(72)
        }

        wm.addView(container, params)
        overlayView = container
    }

    private fun render() {
        composeView?.setContent {
            IncomingOverlayCard(
                phone = latestPhone,
                cardInfo = latestCard,
                onClose = { stopSelf() }
            )
        }
    }

    private fun listenCallStateForAutoClose() {
        runCatching {
            listener = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, incomingNumber: String?) {
                    if (state == TelephonyManager.CALL_STATE_IDLE ||
                        state == TelephonyManager.CALL_STATE_OFFHOOK
                    ) {
                        stopSelf()
                    }
                }
            }
            @Suppress("DEPRECATION")
            telMgr?.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.let { telMgr?.listen(it, PhoneStateListener.LISTEN_NONE) }
        listener = null
        overlayView?.let { wm.removeViewImmediate(it) }
        overlayView = null
        composeView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, NOTI_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground2)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("통화 정보 오버레이 표시 중")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(NOTI_CHANNEL_ID) == null) {
                mgr.createNotificationChannel(
                    NotificationChannel(
                        NOTI_CHANNEL_ID,
                        "Incoming Overlay",
                        NotificationManager.IMPORTANCE_MIN
                    )
                )
            }
        }
    }

    private fun dp(v: Int): Int = (resources.displayMetrics.density * v).toInt()
}
