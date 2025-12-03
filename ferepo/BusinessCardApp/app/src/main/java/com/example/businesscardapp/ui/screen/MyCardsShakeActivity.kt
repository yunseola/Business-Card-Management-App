package com.example.businesscardapp.ui.screen

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.example.businesscardapp.data.network.MyCardRepository
import com.example.businesscardapp.data.network.RetrofitClient
import com.example.businesscardapp.ui.viewmodel.MyCardsShakeViewModel
import com.example.businesscardapp.util.ShakeForegroundService

class MyCardsShakeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(
            "MyCardsShakeActivity",
            "onCreate taskId=$taskId isTaskRoot=$isTaskRoot " +
                    "fromNotif=${intent?.getBooleanExtra("from_notification", false)} flags=${intent?.flags}"
        )

        // 알림 중복 방지: 들어오자마자 감지 일시정지
        sendShakeServiceAction(ShakeForegroundService.ACTION_PAUSE_SHAKE)

        // 인텐트 파라미터(선택 사용)
        val selectOnlyExtra = intent?.getBooleanExtra("select_only", false) ?: false
        val startCardIdExtra = intent?.getIntExtra("start_card_id", -1)?.takeIf { it != -1 }
        val forceQrExtra = intent?.getBooleanExtra("force_qr", false) ?: false

        setContent {
            MaterialTheme {
                val vm = remember {
                    MyCardsShakeViewModel(
                        repo = MyCardRepository(RetrofitClient.apiService)
                    )
                }
                MyCardsShakeScreen(
                    viewModel = vm,
                    selectOnly = selectOnlyExtra,
                    startCardId = startCardIdExtra,
                    forceQr = forceQrExtra
                )
            }
        }
    }

    // 알림을 탭했을 때(이미 떠있는 상태) 재호출
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d(
            "MyCardsShakeActivity",
            "onNewIntent taskId=$taskId fromNotif=${intent.getBooleanExtra("from_notification", false)} flags=${intent.flags}"
        )
        // 다시 들어온 경우에도 감지 일시정지 유지
        sendShakeServiceAction(ShakeForegroundService.ACTION_PAUSE_SHAKE)
    }

    override fun onResume() {
        super.onResume()
        // (선택) NFC 꺼져 있으면 설정 패널 열기 — HCE로 링크 전달 시 편의
        val nfc = NfcAdapter.getDefaultAdapter(this)
        if (nfc != null && !nfc.isEnabled) {
            try {
                startActivity(Intent(Settings.Panel.ACTION_NFC))
            } catch (_: Exception) {
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // 화면에서 벗어나면 흔들기 감지 재개 (백그라운드에서 다시 알림 뜨도록)
        sendShakeServiceAction(ShakeForegroundService.ACTION_RESUME_SHAKE)
    }

    private fun sendShakeServiceAction(action: String) {
        ContextCompat.startForegroundService(
            this,
            Intent(this, ShakeForegroundService::class.java).setAction(action)
        )
    }
}
