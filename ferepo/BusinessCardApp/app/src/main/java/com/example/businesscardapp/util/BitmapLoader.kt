package com.example.businesscardapp.util

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** URL/URI/String -> Bitmap (동기처럼 코루틴에서 로드) */
suspend fun loadBitmapWithCoil(context: Context, model: Any): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val req = ImageRequest.Builder(context)
                .data(model)
                .allowHardware(false) // Bitmap 변환을 위해
                .build()
            val res = loader.execute(req)
            if (res is SuccessResult) res.drawable.toBitmap() else null
        } catch (_: Exception) { null }
    }
