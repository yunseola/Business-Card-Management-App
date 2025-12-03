package com.example.businesscardapp.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.abs
import kotlin.math.sqrt

class ShakeForegroundService : Service(), SensorEventListener {

    companion object {
        const val ACTION_RESUME_SHAKE = "com.example.businesscardapp.action.RESUME_SHAKE"
        const val ACTION_PAUSE_SHAKE  = "com.example.businesscardapp.action.PAUSE_SHAKE"

        private const val CH_SERVICE = "shake_service_bg"   // 상주 알림(이 채널만 사용)
    }

    private val ongoingId = 1001

    private var sm: SensorManager? = null
    private var accel: Sensor? = null

    private var isListening = false
    private var lastShake = 0L
    private var lastAboveTs = 0L
    private var serviceStartTs = 0L

    // 감도/타이밍(잘 터지게 튜닝)
    private val gForceThreshold = 2.6f      // 기본 임계치
    private val strongThreshold = 3.3f      // 아주 강한 한 방이면 2연속 없이 허용
    private val confirmWithinMs = 900L      // 2연속 허용 간격
    private val minIntervalMs   = 1000L      // 트리거 쿨다운
    private val idleBand        = 0.10f     // g≈1.0 무시 대역
    private val warmupMs        = 800L      // 서비스 시작 직후 무시
    private val autoResumeMs    = 2500L     // 알림 문구 원복 및 리스닝 자동 재개

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d("ShakeFGS", "서비스 시작됨")
        createChannels()
        startForeground(ongoingId, buildOngoingNotification()) // 기본 문구로 시작

        serviceStartTs = System.currentTimeMillis()

        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel == null) {
            Log.w("ShakeFGS", "No accelerometer")
            stopSelf(); return
        }
        startListeningIfNeeded()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RESUME_SHAKE -> startListeningIfNeeded()
            ACTION_PAUSE_SHAKE  -> stopListeningIfNeeded()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListeningIfNeeded()
        handler.removeCallbacksAndMessages(null)
        Log.d("ShakeFGS", "listener unregistered")
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(e: SensorEvent?) {
        if (e?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val now = System.currentTimeMillis()
        if (now - serviceStartTs < warmupMs) return  // 워밍업 구간 무시

        val gX = e.values[0] / SensorManager.GRAVITY_EARTH
        val gY = e.values[1] / SensorManager.GRAVITY_EARTH
        val gZ = e.values[2] / SensorManager.GRAVITY_EARTH
        val g  = sqrt(gX*gX + gY*gY + gZ*gZ)

        // idle 대역(중력 근처) 무시
        if (abs(g - 1f) < idleBand) {
            return
        }

        // 디버그(선택): 문턱 근처 확인
        if (g > gForceThreshold * 0.85f) {
            Log.d("ShakeFGS", "near g=$g lastAboveTs=$lastAboveTs lastShake=$lastShake")
        }

        val coolOk = now - lastShake > minIntervalMs

        // 아주 강한 한 방 → 바로 트리거
        if (g > strongThreshold && coolOk) {
            confirmShake(now, g, reason = "strong")
            return
        }

        // 일반: 2연속 스파이크 요구
        if (g > gForceThreshold) {
            if (lastAboveTs == 0L) {
                lastAboveTs = now
                Log.d("ShakeFGS", "first spike g=$g @ $now")
                return
            }
            val dt = now - lastAboveTs
            if (dt <= confirmWithinMs && coolOk) {
                confirmShake(now, g, reason = "double(dt=$dt)")
            } else {
                lastAboveTs = now
                Log.d("ShakeFGS", "reset spike window g=$g dt=$dt coolOk=$coolOk")
            }
        }
    }

    private fun confirmShake(now: Long, g: Float, reason: String) {
        lastShake = now
        lastAboveTs = 0L
        Log.d("ShakeFGS", "CONFIRMED reason=$reason g=$g now=$now isListening=$isListening")

        // 1) 기존 상주 알림을 "감지됨" 문구로, 배너+진동으로 잠깐 갱신
        updateOngoingNotification(
            title = "Shake 감지됨",
            text = "탭하면 내 디지털 명함을 열어요",
            alertNow = true
        )

        // 2) 알림을 탭해야만 네비게이션이 수행됨 (PendingIntent는 그대로 유지)
        // 3) 잠시 후 원래 문구로 복구 + 리스닝 자동 재개
        handler.postDelayed({
            updateOngoingNotification() // 기본 문구로 복원
            if (!isListening) startListeningIfNeeded()
        }, autoResumeMs)
    }

    private fun startListeningIfNeeded() {
        if (!isListening && accel != null) {
            val ok = sm?.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME) == true
            isListening = ok
            Log.d("ShakeFGS", "리스너 등록됨 ok=$ok sensor=${accel?.name}")
        }
    }

    private fun stopListeningIfNeeded() {
        if (isListening) {
            sm?.unregisterListener(this)
            isListening = false
            Log.d("ShakeFGS", "stopListening")
        }
    }

    /** 채널: 배너/진동을 위해 HIGH 로 생성 (사용자 설정에 종속) */
    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // 중요: 기존 LOW → HIGH 로 변경
            nm.createNotificationChannel(
                NotificationChannel(
                    CH_SERVICE,
                    "Shake Service (foreground)",
                    NotificationManager.IMPORTANCE_HIGH  // 배너/소리/진동 허용
                ).apply {
                    description = "Shake detection service (heads-up on update)"
                    enableVibration(true)
                }
            )
        }
    }

    /** 상주 알림 기본 빌더 */
    private fun buildOngoingNotification(
        title: String = "Shake 감지 켜짐",
        text: String  = "폰을 흔들면 내 디지털 명함을 열 수 있어요",
        alertNow: Boolean = false
    ): Notification {
        val intent = Intent(this, com.example.businesscardapp.MainActivity::class.java).apply {
            action = "OPEN_MY_CARDS_PICK_" + System.currentTimeMillis()
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra("dest", "myCardsPick") // 목록으로 이동
        }
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0) or
                    PendingIntent.FLAG_CANCEL_CURRENT
        )

        val b = NotificationCompat.Builder(this, CH_SERVICE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)                     // 상주 유지
            .setContentIntent(pi)                 // 탭 시 이동
            .setOnlyAlertOnce(!alertNow)          // alertNow=true일 때만 다시 알림
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (alertNow) {
            // 채널이 HIGH일 때 배너/소리/진동을 유도
            b.setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        return b.build()
    }

    /** 같은 ID(ongoingId)로 상주 알림을 갱신 */
    private fun updateOngoingNotification(
        title: String = "Shake 감지 켜짐",
        text: String  = "폰을 흔들면 내 디지털 명함을 열 수 있어요",
        alertNow: Boolean = false
    ) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(ongoingId, buildOngoingNotification(title, text, alertNow))
        Log.d("ShakeFGS", "notify(ongoing) -> title='$title' alertNow=$alertNow")
    }
}
