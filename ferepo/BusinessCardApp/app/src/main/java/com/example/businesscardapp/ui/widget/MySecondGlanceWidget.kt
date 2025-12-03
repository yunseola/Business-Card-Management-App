package com.example.businesscardapp.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.businesscardapp.MainActivity
import com.example.businesscardapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MySecondGlanceWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    private fun qrIntent(context: Context, cardId: Int): Intent =
        Intent(context, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra("route", "mycard/qr")
            putExtra("cardId", cardId)
        }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val manager = GlanceAppWidgetManager(context)
        val myAppWidgetId = manager.getAppWidgetId(id)

        // 1) Glance prefs 읽기 전에, Pending 저장소가 있으면 Glance에 먼저 주입 (레이스 방지)
        runCatching {
            val (pendingId, pendingImg) = PendingWidgetPref.pop(context, myAppWidgetId)
            if (pendingId != null || !pendingImg.isNullOrBlank()) {
                updateAppWidgetState(context, id) { st ->
                    pendingId?.let { st[intPreferencesKey("cardId")] = it }
                    pendingImg?.let { st[stringPreferencesKey("cardImg")] = it }
                }
            }
        }.onFailure { Log.e("Glance", "pending_apply_failed", it) }

        // 2) 저장소에서 최신 상태 읽기
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val cardId: Int? = prefs[intPreferencesKey("cardId")]
        val imgUrl: String? = prefs[stringPreferencesKey("cardImg")]
        Log.d("Glance", "cardId=$cardId, imgUrl=$imgUrl")

        // 3) 이미지 로드 (다운샘플링으로 바인더 한도 회피)
        val bitmap: Bitmap? = if (!imgUrl.isNullOrBlank()) {
            try {
                withContext(Dispatchers.IO) {
                    val targetWidthPx = 600
                    val loader = ImageLoader(context)
                    val req = ImageRequest.Builder(context)
                        .data(imgUrl)
                        .size(targetWidthPx)                 // 다운샘플
                        .allowHardware(false)                // toBitmap 위해 HW 비활성화
                        .bitmapConfig(Bitmap.Config.RGB_565) // 메모리 절약
                        .build()
                    when (val result = loader.execute(req)) {
                        is SuccessResult -> {
                            var bmp = result.drawable.toBitmap()
                            if (bmp.width > targetWidthPx) {
                                val h = (bmp.height * targetWidthPx) / bmp.width
                                bmp = Bitmap.createScaledBitmap(bmp, targetWidthPx, h, true)
                            }
                            Log.d("Glance/Bitmap", "bmp=${bmp.width}x${bmp.height}")
                            bmp
                        }

                        is ErrorResult -> {
                            Log.e("Glance/Coil", "url=$imgUrl, ${result.throwable}")
                            null
                        }

                        else -> null
                    }
                }
            } catch (t: Throwable) {
                Log.e("Glance", "Coil exception", t); null
            }
        } else null

        // 4) UI
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(ColorProvider(R.color.basic))
                    .padding(12.dp)
                    .let { m ->
                        if (cardId != null) m.clickable(actionStartActivity(qrIntent(context, cardId))) else m
                    },
                verticalAlignment = Alignment.Vertical.Top,
                horizontalAlignment = Alignment.Horizontal.Start
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.Start
                ) {
                    Text("명함.zip", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    Spacer(GlanceModifier.width(6.dp))
                    Text("공유", style = TextStyle(fontSize = 12.sp))
                }

                Spacer(GlanceModifier.height(14.dp))

                Image(
                    provider = if (bitmap != null)
                        ImageProvider(bitmap) else ImageProvider(R.drawable.ic_card_placeholder),
                    contentDescription = if (cardId != null) "선택된 명함 QR 열기" else "명함을 선택해 주세요",
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }
}

/**
 * ConfigActivity가 glanceId를 못 찾는 레이스를 대비하기 위한 임시 저장소.
 * 위젯 쪽에서 provideGlance 시작 시 가장 먼저 pop 해서 Glance state에 주입합니다.
 */
internal object PendingWidgetPref {
    private const val NAME = "pending_widget_prefs"
    fun save(context: Context, appWidgetId: Int, cardId: Int, img: String) {
        val sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putInt("cardId_$appWidgetId", cardId)
            .putString("cardImg_$appWidgetId", img)
            .apply()
    }

    fun pop(context: Context, appWidgetId: Int): Pair<Int?, String?> {
        val sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        val idKey = "cardId_$appWidgetId"
        val imgKey = "cardImg_$appWidgetId"
        val id = if (sp.contains(idKey)) sp.getInt(idKey, -1) else null
        val img = sp.getString(imgKey, null)
        sp.edit().remove(idKey).remove(imgKey).apply()
        return Pair(if (id == -1) null else id, img)
    }
}
