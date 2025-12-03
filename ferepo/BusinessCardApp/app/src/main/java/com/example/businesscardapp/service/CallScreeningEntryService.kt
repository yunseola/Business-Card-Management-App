package com.example.businesscardapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.businesscardapp.R
import com.example.businesscardapp.data.model.CardCallInfoResponse
import com.example.businesscardapp.data.network.RetrofitClient
import com.example.businesscardapp.overlay.CallOverlayService
import com.example.businesscardapp.ui.screen.incoming.IncomingActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "CallScreeningSvc"

class CallScreeningEntryService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())

        val raw = callDetails.handle?.schemeSpecificPart
        val phone = raw?.replace(Regex("[^0-9]"), "")
        Log.d(TAG, "onScreenCall raw=$raw normalized=$phone")

        // ① 통화 뜨자마자 빈 정보로 오버레이 먼저 띄움
        startOverlay(phone, null)

        // ② 네트워크로 카드 정보 받아서 오버레이 갱신
        CoroutineScope(Dispatchers.IO).launch {
            var card: CardCallInfoResponse? = null
            try {
                val res = RetrofitClient.apiService.getCardInfoOnCall(phone ?: "")
                Log.d(TAG, "HTTP=${res.code()} ok=${res.isSuccessful}")
                card = res.body()?.result
            } catch (t: Throwable) {
                Log.e(TAG, "API error", t)
            }
            sendOverlayUpdate(phone, card) // → 아래 함수
        }
    }

    private fun sendOverlayUpdate(phone: String?, card: CardCallInfoResponse?) {
        val json = card?.let { Gson().toJson(it) }
        sendBroadcast(Intent(IncomingOverlayService.ACTION_UPDATE).apply {
            `package` = packageName
            putExtra("phone", phone)
            if (json != null) putExtra("cardInfoJson", json)
        })
    }

    private fun showOverlayPermissionNudge() {
        val ctx = applicationContext
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chId = "overlay_perm"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(chId, "Overlay permission", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:$packageName")
        )
        val pi = PendingIntent.getActivity(
            ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val noti = NotificationCompat.Builder(ctx, chId)
            .setSmallIcon(R.drawable.ic_launcher_foreground2)
            .setContentTitle("전화 오버레이 권한이 필요합니다")
            .setContentText("설정 > '다른 앱 위에 표시'에서 허용해 주세요.")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        nm.notify(5551, noti)
    }

    private fun startOverlay(phone: String?, card: CardCallInfoResponse?) {
        // 오버레이 권한 없으면 안내하고 종료
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "overlay permission off, skip overlay")
            showOverlayPermissionNudge()
            return
        }

        val intent = Intent(this, IncomingOverlayService::class.java).apply {
            putExtra("phone", phone)
            card?.let { putExtra("cardInfoJson", Gson().toJson(it)) }
        }
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
    }

    /** 오버레이 권한이 꺼져 있을 때만 표시하는 안내 알림 (풀스크린 아님) */
    private fun postOverlayPermissionHint(phone: String?) {
        val ctx = applicationContext
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "incoming_call_channel_v2"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    channelId, "Incoming Calls",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        // 권한 설정 이동 액션
        val settingsIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${packageName}")
        )
        val settingsPi = PendingIntent.getActivity(
            ctx, 1002, settingsIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // “자세히 보기”는 풀스크린 없이 일반 액티비티로만 이동
        val detailIntent = Intent(ctx, IncomingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val detailPi = PendingIntent.getActivity(
            ctx, 1003, detailIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val noti = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground2)
            .setContentTitle("전화가 왔어요")
            .setContentText(phone ?: "발신자 확인 중")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(0, "오버레이 권한 켜기", settingsPi)
            .setContentIntent(detailPi)
            // ❌ setFullScreenIntent는 쓰지 않음 (정책/요구사항)
            .build()

        nm.notify(1001, noti)
    }
}
