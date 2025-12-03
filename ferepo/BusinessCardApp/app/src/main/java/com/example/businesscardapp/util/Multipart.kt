package com.example.businesscardapp.util

//class Multipart {
//}

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.source

/** content:// Uri 를 메모리 복사 없이 스트리밍으로 MultipartBody.Part로 변환 */
fun Context.uriToPart(partName: String, uri: Uri): MultipartBody.Part? = try {
    val cr: ContentResolver = contentResolver
    val mime = cr.getType(uri) ?: "image/png"
    val displayName = cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    val safeName = (displayName ?: "${partName}_${System.currentTimeMillis()}.png")
        .let { if ('.' in it) it else "$it.png" }

    val body = object : RequestBody() {
        override fun contentType() = mime.toMediaTypeOrNull()
        override fun writeTo(sink: okio.BufferedSink) {
            cr.openInputStream(uri)?.use { input -> sink.writeAll(input.source()) }
                ?: throw IllegalStateException("Cannot open input stream for $uri")
        }
    }
    MultipartBody.Part.createFormData(partName, safeName, body)
} catch (_: Exception) {
    null
}
