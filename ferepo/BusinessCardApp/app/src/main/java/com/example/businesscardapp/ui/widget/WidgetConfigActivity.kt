package com.example.businesscardapp.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import coil.compose.AsyncImage
import com.example.businesscardapp.R
import com.example.businesscardapp.data.network.ApiService
import com.example.businesscardapp.data.network.RetrofitClient
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish(); return
        }

        setContent {
            MaterialTheme {
                val scope = rememberCoroutineScope()
                val manager = remember { GlanceAppWidgetManager(this) }
                val api = remember { RetrofitClient.retrofit.create(ApiService::class.java) }

                var loading by remember { mutableStateOf(true) }
                var error by remember { mutableStateOf<String?>(null) }

                // 서버 응답 DTO 매핑용 간이 모델
                data class UiCard(val id: Int, val img: String?)

                var cards by remember { mutableStateOf<List<UiCard>>(emptyList()) }

                // 1) 내 명함 목록 로드
                LaunchedEffect(Unit) {
                    loading = true; error = null
                    try {
                        val res = api.getMyCardList()
                        if (res.isSuccessful) {
                            val body = res.body()
                            val list = body?.result ?: emptyList()
                            cards = list.map { item -> UiCard(item.cardId, item.imageUrlVertical) }
                            if (cards.isEmpty()) error = "등록된 명함이 없습니다."
                        } else {
                            error = "서버 오류 (${res.code()})"
                        }
                    } catch (e: Exception) {
                        error = "네트워크 오류: ${e.message}"
                    }
                    loading = false
                }

                // 2) UI
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("위젯에 표시할 명함 선택") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.White,
                                titleContentColor = Color.Black
                            )
                        )
                    },
                    containerColor = Color.White
                ) { inner ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(inner)
                    ) {
                        when {
                            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                            error != null -> Text(error ?: "", Modifier.align(Alignment.Center))
                            cards.isNotEmpty() -> {
                                val pagerState = rememberPagerState(pageCount = { cards.size })

                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp)
                                        .padding(top = 16.dp, bottom = 0.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 좌우 스와이프
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    ) { page ->
                                        val c = cards[page]
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = c.img ?: R.drawable.ic_card_placeholder,
                                                contentDescription = "명함 미리보기",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(0.62f)
                                                    .clip(RoundedCornerShape(16.dp))
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    PagerDots(
                                        totalDots = cards.size,
                                        selectedIndex = pagerState.currentPage,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(vertical = 4.dp),
                                        dotSize = 8.dp,
                                        dotSpacing = 10.dp,
                                        activeColor = Color.Black,
                                        inactiveColor = Color.Black.copy(alpha = 0.25f)
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            val selected = cards[pagerState.currentPage]
                                            val selectedId = selected.id
                                            val selectedImg = (selected.img ?: "").trim()

                                            scope.launch {
                                                // 1) 현재 appWidgetId에 해당하는 glanceId 찾기 (최대 1초 재시도)
                                                var glanceId = manager.findGlanceIdFor<MySecondGlanceWidget>(appWidgetId)
                                                var tries = 0
                                                while (glanceId == null && tries < 10) {
                                                    kotlinx.coroutines.delay(100)
                                                    glanceId = manager.findGlanceIdFor<MySecondGlanceWidget>(appWidgetId)
                                                    tries++
                                                }

                                                if (glanceId != null) {
                                                    // 2-A) Glance state 저장 + 즉시 업데이트
                                                    updateAppWidgetState(
                                                        context = this@WidgetConfigActivity,
                                                        glanceId = glanceId
                                                    ) { prefs ->
                                                        prefs[intPreferencesKey("cardId")] = selectedId
                                                        prefs[stringPreferencesKey("cardImg")] = selectedImg
                                                    }
                                                    MySecondGlanceWidget().update(this@WidgetConfigActivity, glanceId)
                                                } else {
                                                    // 2-B) 레이스: glanceId 없음 → 임시 저장 + 브로드캐스트
                                                    PendingWidgetPref.save(
                                                        this@WidgetConfigActivity,
                                                        appWidgetId,
                                                        selectedId,
                                                        selectedImg
                                                    )
                                                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                                                        component = ComponentName(
                                                            this@WidgetConfigActivity,
                                                            MySecondWidgetReceiver::class.java
                                                        )
                                                        putExtra(
                                                            AppWidgetManager.EXTRA_APPWIDGET_IDS,
                                                            intArrayOf(appWidgetId)
                                                        )
                                                    }
                                                    sendBroadcast(intent)
                                                }

                                                // 3) 결과 반환 후 종료
                                                setResult(
                                                    RESULT_OK,
                                                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                                )
                                                finish()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .padding(bottom = 24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C3924)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("이 명함으로 설정", color = Color(0xFFFFFFFF))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Pager indicator */
@Composable
private fun PagerDots(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotSpacing: Dp = 8.dp,
    activeColor: Color = Color.Black,
    inactiveColor: Color = Color.Black.copy(alpha = 0.25f)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(
                        color = if (index == selectedIndex) activeColor else inactiveColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

/** GlanceId 찾기 헬퍼: 특정 AppWidgetId에 매핑된 GlanceId를 검색 */
private suspend inline fun <reified T : GlanceAppWidget>
        GlanceAppWidgetManager.findGlanceIdFor(appWidgetId: Int): GlanceId? {
    val ids = getGlanceIds(T::class.java) // suspend 호출 가능
    for (gid in ids) {
        try {
            val id = getAppWidgetId(gid)
            if (id == appWidgetId) return gid
        } catch (t: Throwable) {
            Log.w("Glance", "getAppWidgetId failed for $gid", t)
        }
    }
    return null
}
