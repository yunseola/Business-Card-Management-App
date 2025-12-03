package com.example.businesscardapp.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.CardOrientation
import com.example.businesscardapp.ui.component.DigitalPreviewCardExport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color

/** dp → px */
private fun dpToPx(context: Context, dp: Float): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density).roundToInt()
}

/** Context에서 Activity 찾기 */
private tailrec fun findActivity(ctx: Context?): Activity? = when (ctx) {
    is Activity -> ctx
    is android.content.ContextWrapper -> findActivity(ctx.baseContext)
    else -> null
}

/**
 * Compose 오프스크린 렌더 → PNG 저장 (ComposeView를 잠시 window에 attach)
 */
suspend fun exportDigitalCardToPng(
    context: Context,
    photoUri: String?,
    bgHex: String?,
    patternCode: String?,
    useDarkText: Boolean,
    name: String,
    company: String,
    phoneDisplay: String,
    position: String,
    email: String,
    extras: List<Pair<String, String>>,
    orientation: CardOrientation,
    widthDp: Float = if (orientation == CardOrientation.Landscape) 360f else 300f,
    heightDp: Float = if (orientation == CardOrientation.Landscape) 200f else 540f,
): File = withContext(Dispatchers.Main) {
    val activity = findActivity(context)
        ?: throw IllegalStateException("Activity를 찾을 수 없습니다 (exportDigitalCardToPng는 Activity 컨텍스트 필요).")

    // 1) 사진 비트맵 선로드
    val photoBitmap = withContext(Dispatchers.IO) {
        photoUri?.let { loadBitmapWithCoil(context, it) }?.asImageBitmap()
    }

    // 2) 배경/패턴
    val bgColor = runCatching { Color(android.graphics.Color.parseColor(bgHex ?: "#FFFFFF")) }
        .getOrElse { Color.White }
    val patternResId = when (patternCode) {
        "pattern1" -> R.drawable.pattern1
        "pattern2" -> R.drawable.pattern2
        "pattern3" -> R.drawable.pattern3
        "pattern4" -> R.drawable.pattern4
        "pattern5" -> R.drawable.pattern5
        "pattern6" -> R.drawable.pattern6
        "pattern7" -> R.drawable.pattern7
        "pattern8" -> R.drawable.pattern8
        "pattern9" -> R.drawable.pattern9
        "pattern10" -> R.drawable.pattern10
        "pattern11" -> R.drawable.pattern11
        "pattern12" -> R.drawable.pattern12
        else -> null
    }

    val wPx = dpToPx(context, widthDp)
    val hPx = dpToPx(context, heightDp)

    // 3) window에 붙일 임시 컨테이너 생성
    val root: ViewGroup = activity.window?.decorView as ViewGroup
    val host = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(wPx, hPx)
        // 화면에 보이지 않게
        isVisible = false
    }
    root.addView(host)

    // 4) ComposeView를 host에 attach 하고 setContent
    val composeView = ComposeView(context).apply {
        layoutParams = FrameLayout.LayoutParams(wPx, hPx)
        setContent {
            DigitalPreviewCardExport(
                photo = photoBitmap,
                bgColor = if (patternResId != null) Color.Transparent else bgColor,
                patternResId = patternResId,
                useDarkText = useDarkText,
                name = name,
                company = company,
                phoneDisplay = phoneDisplay,
                position = position,
                email = email,
                extras = extras,
                orientation = orientation
            )
        }
    }
    host.addView(composeView)

    // 5) measure/layout (attach된 상태이므로 WindowRecomposer 사용 가능)
    val wSpec = View.MeasureSpec.makeMeasureSpec(wPx, View.MeasureSpec.EXACTLY)
    val hSpec = View.MeasureSpec.makeMeasureSpec(hPx, View.MeasureSpec.EXACTLY)
    host.measure(wSpec, hSpec)
    host.layout(0, 0, wPx, hPx)

    // 6) draw to bitmap
    val bitmap = Bitmap.createBitmap(wPx, hPx, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).apply { host.draw(this) }

    // 7) 파일 저장 및 정리
    val file = File(context.cacheDir, "card_${orientation.name.lowercase()}_${System.currentTimeMillis()}.png")
    try {
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    } finally {
        // 반드시 제거 (메모리/뷰 누수 방지)
        host.removeAllViews()
        root.removeView(host)
    }
    file
}
