package com.example.businesscardapp.ocr

import android.content.Context
import android.net.Uri
import com.example.businesscardapp.ui.screen.mycard.MyCardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun runMyCardOcr(
    scope: CoroutineScope,
    context: Context,
    uri: Uri,
    vm: MyCardViewModel,
    onDone: () -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        try {
            val text = recognizeTextFromUri(context, uri)
            val lines = text.textBlocks.flatMap { b -> b.lines.map { it.text } }
            val parsed = parseBusinessCard(lines)

            parsed.name?.let { vm.updateOrAddField("이름", it) }
            parsed.company?.let { vm.updateOrAddField("회사", it) }
            parsed.phone?.let { vm.updateOrAddField("연락처", it) }
            parsed.position?.let { vm.updateOrAddField("직책", it) }
            parsed.department?.let { vm.updateOrAddField("부서", it) }
            parsed.email?.let { vm.updateOrAddField("이메일", it) }

            vm.setPhotoUri(uri)
            onDone()
        } catch (e: Exception) {
            onError(e.message ?: "OCR 실패")
        }
    }
}
