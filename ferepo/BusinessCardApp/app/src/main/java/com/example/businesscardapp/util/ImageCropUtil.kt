// 경로: com.example.businesscardapp.util.ImageCropUtil.kt

package com.example.businesscardapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.compose.ui.geometry.Offset
import java.io.File

// ✅ 1. 사각형 영역 기준으로 Bitmap 자르기
fun cropBitmapWithCorners(bitmap: Bitmap, corners: List<Offset>): Bitmap {
    val rect = Rect(
        corners.minOf { it.x }.toInt(),
        corners.minOf { it.y }.toInt(),
        corners.maxOf { it.x }.toInt(),
        corners.maxOf { it.y }.toInt()
    )

    return Bitmap.createBitmap(
        bitmap,
        rect.left.coerceAtLeast(0),
        rect.top.coerceAtLeast(0),
        rect.width().coerceAtMost(bitmap.width - rect.left),
        rect.height().coerceAtMost(bitmap.height - rect.top)
    )
}

// ✅ 2. 잘라낸 Bitmap을 내부 저장소에 저장하고 파일명을 리턴
fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
    return try {
        val fileName = "CROPPED_${System.currentTimeMillis()}.jpg"
        val file = File(context.getExternalFilesDir(null), fileName)
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        fileName
    } catch (e: Exception) {
        Log.e("SaveBitmap", "이미지 저장 실패", e)
        null
    }
}
