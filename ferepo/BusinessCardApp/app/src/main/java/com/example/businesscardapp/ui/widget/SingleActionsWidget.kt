package com.example.businesscardapp.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.businesscardapp.MainActivity
import com.example.businesscardapp.R
import androidx.glance.unit.ColorProvider

class SingleActionsWidget : GlanceAppWidget() {

    private fun toMain(context: Context, route: String) =
        Intent(context, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra("route", route)
        }

    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        // 바깥 Row: 좌/우 1:1
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(ColorProvider(R.color.back)) // 전체 배경색
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ───────── 왼쪽 절반 ───────── (이 안에서 1:1로 나눔 → 등록, 공유)
            Row(
                modifier = GlanceModifier
                    .defaultWeight()         // 바깥 Row 기준 왼쪽 1
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 등록 (왼쪽 절반의 1)
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .clickable(actionStartActivity(toMain(context, "camera")))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_mycard_create),
                        contentDescription = "명함 등록",
                        modifier = GlanceModifier.size(24.dp)
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    Text("명함 등록", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium))
                }

                Spacer(GlanceModifier.width(10.dp))

                // 공유 (왼쪽 절반의 1)
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .clickable(actionStartActivity(toMain(context, "mycard/share")))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_share),
                        contentDescription = "내 명함 공유",
                        modifier = GlanceModifier.size(24.dp)
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    Text("명함 공유", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium))
                }
            }

            Spacer(GlanceModifier.width(10.dp))

            // ───────── 오른쪽 절반 ───────── (전체의 2 몫)
            Box(
                modifier = GlanceModifier
                    .defaultWeight()         // 바깥 Row 기준 오른쪽 1  → 결과적으로 전체의 1/2(=2 몫)
                    .fillMaxHeight()
                    .clickable(actionStartActivity(toMain(context, "mycard/shake")))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.mycard_mock),
                    contentDescription = "내 명함 열기",
                    modifier = GlanceModifier
                        .fillMaxWidth()      // 가로를 박스에 맞춤
                        .fillMaxHeight()     // 세로도 박스에 맞춤
                )
            }
        }
    }
}

class SingleActionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SingleActionsWidget()
}
