package com.example.businesscardapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class ShakeBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("ShakeBootReceiver", "onReceive: $action")

        when (action) {
            // 부팅 직후 (Direct Boot 구간 포함)
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_BOOT_COMPLETED,
                // 기기 잠금 해제 (파일 시스템 완전 접근 가능 시점)
            Intent.ACTION_USER_UNLOCKED,
                // 앱이 업데이트/재설치 되었을 때
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val svc = Intent(context, ShakeForegroundService::class.java)
                // 반드시 Foreground 로 시작 (onCreate()에서 startForeground 호출함)
                ContextCompat.startForegroundService(context, svc)
            }
        }
    }
}
