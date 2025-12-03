package com.example.businesscardapp.util

import android.content.Context
import android.util.Log
import java.io.File
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun downloadImageAndSave(context: Context, imageUrl: String): File? = withContext(Dispatchers.IO) {
    try {
        Log.d("ImageDownload", "이미지 다운로드 시작: $imageUrl")

        val input = URL(imageUrl).openStream() // 여기에서 IOException 가능
        val file = File(context.cacheDir, "shared_image.jpg")

        Log.d("ImageDownload", "파일 경로: ${file.absolutePath}")

        file.outputStream().use { output ->
            input.copyTo(output)
        }

        Log.d("ImageDownload", "이미지 다운로드 성공")
        file
    } catch (e: Exception) {
        Log.e("ImageDownload", "이미지 다운로드 실패", e)
        null
    }
}

