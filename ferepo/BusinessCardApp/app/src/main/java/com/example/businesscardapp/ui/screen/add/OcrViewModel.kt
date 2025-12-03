//OcrViewModel.kt
// ui/screen/add/OcrViewModel.kt
package com.example.businesscardapp.ui.screen.add

import android.graphics.ImageDecoder
import android.provider.MediaStore
import android.os.Build
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect

import android.net.Uri


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlinx.coroutines.suspendCancellableCoroutine

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.businesscardapp.util.BusinessCardInfo
import com.example.businesscardapp.util.runMultiOCR


data class OcrResult(
    val full: ImageBitmap,
    val cropped: ImageBitmap,
    val rect: Rect,
    val text: String
)

class OcrViewModel : ViewModel() {


    private val _fullBitmap = MutableStateFlow<Bitmap?>(null)
    val fullBitmap: StateFlow<Bitmap?> = _fullBitmap

    private val _cardRect = MutableStateFlow<Rect?>(null)
    val cardRect: StateFlow<Rect?> = _cardRect

    private val _croppedBitmap = MutableStateFlow<ImageBitmap?>(null)
    val croppedBitmap: StateFlow<ImageBitmap?> = _croppedBitmap

    private val _ocrText = MutableStateFlow("")
    val ocrText: StateFlow<String> = _ocrText

    private val _businessCardInfo = MutableStateFlow<BusinessCardInfo?>(null)
    val businessCardInfo: StateFlow<BusinessCardInfo?> = _businessCardInfo

    private var lastUri: Uri? = null


    fun loadAndDetectCard(context: Context, uri: Uri) {
        if (lastUri == uri) {
            Log.d("OcrViewModel", "같은 URI는 재처리하지 않음: $uri")
            return
        }
        lastUri = uri

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }

                }

                Log.d("OcrViewModel", "Bitmap is null? ${bitmap == null}")
                Log.d("OcrViewModel", "Loaded bitmap size: ${bitmap?.width} x ${bitmap?.height}")


                if (bitmap == null) {
                    Log.e("OcrViewModel", "이미지를 로드하지 못했습니다.")
                    _fullBitmap.value = null
                    _cardRect.value = null
                    _croppedBitmap.value = null
                    _ocrText.value = ""
                    _businessCardInfo.value = null
                    return@launch
                }

                _fullBitmap.value = bitmap

                // ✅ OCRUtil의 고급 OCR 기능 사용 (Uri 전달)
                Log.d("OcrViewModel", "=== 고급 OCR 처리 시작 ===")

                val businessCardInfo = runMultiOCR(uri, context)
                Log.d("OcrViewModel", "OCR 결과: $businessCardInfo")

                // 결과 업데이트

                _businessCardInfo.value = businessCardInfo
                _ocrText.value = businessCardInfo.rawText

                // 카드 영역 감지 (기존 로직 유지)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = recognizeTextSuspend(recognizer.process(image))
                val boundingBoxes = result.textBlocks.mapNotNull { it.boundingBox }
                val union = if (boundingBoxes.isNotEmpty()) {
                    boundingBoxes.reduce { acc, rect -> acc.union(rect); acc }

                } else {
                    Rect(0, 0, bitmap.width, bitmap.height)
                }

                _cardRect.value = union
                val cropped = Bitmap.createBitmap(
                    bitmap,
                    union.left.coerceAtLeast(0),
                    union.top.coerceAtLeast(0),
                    union.width().coerceAtMost(bitmap.width - union.left),
                    union.height().coerceAtMost(bitmap.height - union.top)

                )
                _croppedBitmap.value = cropped.asImageBitmap()

                Log.d("OcrViewModel", "=== OCR 처리 완료 ===")
                Log.d("OcrViewModel", "인식된 텍스트: ${businessCardInfo.rawText}")
                Log.d(
                    "OcrViewModel",
                    "분류된 정보: 이름=${businessCardInfo.name}, 회사=${businessCardInfo.company}, 직책=${businessCardInfo.position}"
                )


            } catch (e: Exception) {
                Log.e("OcrViewModel", "OCR 처리 중 오류 발생", e)
                _businessCardInfo.value = null
                _ocrText.value = ""
            }
        }
    }

    // ✅ suspend로 안전하게 ML Kit OCR 호출
    private suspend fun recognizeTextSuspend(task: Task<com.google.mlkit.vision.text.Text>) =
        suspendCancellableCoroutine<com.google.mlkit.vision.text.Text> { continuation ->
            task
                .addOnSuccessListener { result ->
                    continuation.resume(result)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
            // ✅ BusinessCardInfo 업데이트 함수
            fun updateBusinessCardInfo(info: BusinessCardInfo) {
                _businessCardInfo.value = info
            }

            // ✅ 초기화 함수
            fun reset() {
                _fullBitmap.value = null
                _cardRect.value = null
                _croppedBitmap.value = null
                _ocrText.value = ""
                _businessCardInfo.value = null
                lastUri = null
            }


        }
}