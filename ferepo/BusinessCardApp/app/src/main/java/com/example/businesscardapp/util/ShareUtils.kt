package com.example.businesscardapp.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File

data class BizCard(
    val name: String,
    val company: String? = null,
    val title: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null
)

private fun BizCard.toVCard(): String = buildString {
    appendLine("BEGIN:VCARD")
    appendLine("VERSION:3.0")
    appendLine("FN:${name}")
    company?.let { appendLine("ORG:$it") }
    title?.let { appendLine("TITLE:$it") }
    phone?.let { appendLine("TEL;TYPE=CELL:$it") }
    email?.let { appendLine("EMAIL;TYPE=INTERNET:$it") }
    website?.let { appendLine("URL:$it") }
    appendLine("END:VCARD")
}

/** vCard(.vcf) 공유 (필요 시 사용) */
fun shareAsVCard(context: Context, card: BizCard) {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, "my_card.vcf").apply {
        writeText(card.toVCard(), Charsets.UTF_8)
    }
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.file_provider",   // ← 여기!
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/x-vcard"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_TITLE, "내 명함 공유")
    }
    context.startActivity(Intent.createChooser(intent, "공유하기"))
}

/** 명함 이미지(Bitmap)만 공유 */
fun shareCardImage(context: Context, bitmap: Bitmap) {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, "my_card.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.file_provider",   // ← 여기!
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_TITLE, "내 명함 이미지 공유")
    }
    context.startActivity(Intent.createChooser(intent, "공유하기"))
}
