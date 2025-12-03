package com.example.businesscardapp.ui.screen.incoming

import IncomingCallScreen
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.businesscardapp.data.model.CardCallInfoResponse
import com.google.gson.Gson

const val ACTION_CARD_INFO_UPDATE = "CARD_INFO_UPDATE"

class IncomingActivity : ComponentActivity() {

    companion object {
        private const val TAG = "IncomingActivity"
        const val EXTRA_PHONE = "phone"
        const val EXTRA_CARD_INFO_JSON = "cardInfoJson"
    }

    private var cardInfoState by mutableStateOf<CardCallInfoResponse?>(null)

    /** 수신 정보 갱신 브로드캐스트 수신 */
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val json = intent?.getStringExtra(EXTRA_CARD_INFO_JSON)
            Log.d(TAG, "[BR] jsonLen=${json?.length}")
            parseAndSet(json)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금 화면 위로 띄우기 + 화면 켜기
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            // 잠금화면 해제(선택)
            val km = getSystemService(KeyguardManager::class.java)
            if (km?.isKeyguardLocked == true) {
                km.requestDismissKeyguard(this, null)
            }
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        dumpIntent("onCreate", intent)
        parseAndSet(intent.getStringExtra(EXTRA_CARD_INFO_JSON))

        setContent {
            val data = cardInfoState
            if (data != null) {
                // 앱에 이미 있는 커스텀 수신 UI
                IncomingCallScreen(cardInfo = data)
            } else {
                // 파싱 전/실패 시의 아주 기본적인 화면
                IncomingBasicScreen(
                    number = intent.getStringExtra(EXTRA_PHONE),
                    onDismiss = { finishAndRemoveTask() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dumpIntent("onNewIntent", intent)
        parseAndSet(intent.getStringExtra(EXTRA_CARD_INFO_JSON))
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                updateReceiver,
                IntentFilter(ACTION_CARD_INFO_UPDATE),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(
                updateReceiver,
                IntentFilter(ACTION_CARD_INFO_UPDATE)
            )
        }
    }

    override fun onStop() {
        super.onStop()
        runCatching { unregisterReceiver(updateReceiver) }
            .onFailure { Log.w(TAG, "unregisterReceiver fail: ${it.message}") }
    }

    private fun parseAndSet(json: String?) {
        cardInfoState = json?.let {
            runCatching { Gson().fromJson(it, CardCallInfoResponse::class.java) }
                .onFailure { Log.e(TAG, "Gson parse error", it) }
                .getOrNull()
        }
        Log.d(TAG, "parsed? ${cardInfoState != null}")
    }

    private fun dumpIntent(tag: String, i: Intent) {
        val keys = i.extras?.keySet()?.joinToString()
        val len = i.getStringExtra(EXTRA_CARD_INFO_JSON)?.length
        val phone = i.getStringExtra(EXTRA_PHONE)
        Log.d(TAG, "[$tag] phone=$phone jsonLen=$len keys=$keys")
    }
}

@Composable
private fun IncomingBasicScreen(number: String?, onDismiss: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF101317))
            .padding(24.dp)
    ) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "전화가 왔어요",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(Modifier.height(12.dp))
            Text(number ?: "발신자 확인 중", color = Color(0xFFB5C0D0))
            Spacer(Modifier.height(24.dp))
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Text("닫기")
        }
    }
}
