package com.example.businesscardapp.util

//class FileProviderExt {
//}

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** cacheDir 등에 저장된 File을 FileProvider 기반 content:// Uri로 변환 */
fun Context.fileToContentUri(file: File): Uri =
    FileProvider.getUriForFile(this, "${packageName}.file_provider", file)