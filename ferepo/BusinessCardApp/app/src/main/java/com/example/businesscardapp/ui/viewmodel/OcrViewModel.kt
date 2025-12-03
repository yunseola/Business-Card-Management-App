package com.example.businesscardapp.ui.viewmodel

// AndroidX 및 Compose 관련
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel

// Android 관련
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log

// OpenCV 관련
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.exifinterface.media.ExifInterface
import com.example.businesscardapp.util.BusinessCardInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OcrViewModel : ViewModel() {
    private val _corners = mutableStateOf<List<Offset>>(emptyList())
    val corners: State<List<Offset>> = _corners

    val fullBitmap = MutableStateFlow<Bitmap?>(null)
    val cardRect = MutableStateFlow<Rect?>(null)

    private var isCornersInitialized = false

    private val _businessCardInfo = MutableStateFlow(BusinessCardInfo())
    val businessCardInfo: StateFlow<BusinessCardInfo> = _businessCardInfo

    private val _croppedBitmap = MutableStateFlow<Bitmap?>(null)
    val croppedBitmap: StateFlow<Bitmap?> = _croppedBitmap

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    fun updateCorners(updated: List<Offset>) {
        _corners.value = updated
    }

    fun updateCorner(index: Int, newOffset: Offset) {
        val current = _corners.value.toMutableList()
        if (index in current.indices) {
            current[index] = newOffset
            _corners.value = current
        }
    }


    fun resetCorners() {
        isCornersInitialized = false
        _corners.value = emptyList()
    }

    fun rotateBitmapIfNeeded(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        val exif = inputStream?.let { ExifInterface(it) }
        val orientation =
            exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun loadAndDetectCard(context: Context, uri: Uri) {
        Log.d("CornerDetection", ">> loadAndDetectCard start: isCornersInitialized=$isCornersInitialized, uri=$uri")

        val bitmapRaw = decodeBitmapFromUri(context, uri, maxSize = 2048)
        val rotatedBitmap = rotateBitmapIfNeeded(context, uri, bitmapRaw)

        val detectBmp = resizeBitmap(rotatedBitmap, maxSize = 2048)
        Log.d("CornerDetection", ">> detectBmp size: ${detectBmp.width}×${detectBmp.height}")

        val displayBmp = Bitmap.createScaledBitmap(
            rotatedBitmap,
            (rotatedBitmap.width * 1.2f).toInt(),
            (rotatedBitmap.height * 1.2f).toInt(),
            true
        )
        Log.d("CornerDetection", ">> displayBmp size: ${displayBmp.width}×${displayBmp.height}")

        fullBitmap.value = displayBmp

        if (!isCornersInitialized) {
            Log.d("CornerDetection", ">> about to call detectCornersFromBitmap")
            val rawCorners = detectCornersFromBitmap(detectBmp)
            Log.d("CornerDetection", "<< detectCornersFromBitmap returned ${rawCorners.size} points")

            val scaleX = displayBmp.width.toFloat() / detectBmp.width
            val scaleY = displayBmp.height.toFloat() / detectBmp.height
            val mapped = rawCorners.map { pf ->
                val x = pf.x * scaleX
                val y = pf.y * scaleY
                Offset(x, y)
            }

            _corners.value = mapped
            Log.d("CornerDetection", "Mapped Offsets (Compose 기준):")
            mapped.forEachIndexed { i, off ->
                Log.d("CornerDetection", "  Corner $i: x=${off.x}, y=${off.y}")
            }

            isCornersInitialized = true

            val rect = calculateBoundingBox(mapped)
            cardRect.value = rect
            Log.d("CornerDetection", "BoundingBox set to $rect")
        }
    }

    fun runOcrAndCrop(
        context: Context,
        uri: Uri,
        bitmap: Bitmap,
        cornersDetected: List<Offset>
    ) {
        val rect = calculateBoundingBox(cornersDetected)
        cardRect.value = rect

        _isProcessing.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val businessCardInfo = com.example.businesscardapp.util.runMultiOCR(uri, context)
                withContext(Dispatchers.Main) {
                    _businessCardInfo.value = businessCardInfo
                    _isProcessing.value = false
                }

                rect?.let { cropRect ->
                    val croppedBitmap = Bitmap.createBitmap(
                        bitmap,
                        cropRect.left,
                        cropRect.top,
                        cropRect.width(),
                        cropRect.height()
                    )
                    withContext(Dispatchers.Main) {
                        _croppedBitmap.value = croppedBitmap
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        _croppedBitmap.value = bitmap
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _businessCardInfo.value = BusinessCardInfo()
                    _croppedBitmap.value = bitmap
                    _isProcessing.value = false
                }
            }
        }
    }

    fun calculateBoundingBox(corners: List<Offset>): Rect? {
        if (corners.size < 4) return null
        val minX = corners.minOf { it.x }
        val minY = corners.minOf { it.y }
        val maxX = corners.maxOf { it.x }
        val maxY = corners.maxOf { it.y }
        return Rect(minX.toInt(), minY.toInt(), maxX.toInt(), maxY.toInt())
    }

    fun decodeBitmapFromUri(context: Context, uri: Uri, maxSize: Int = 2048): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri).use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        val originalWidth = options.outWidth
        val originalHeight = options.outHeight
        var sampleSize = 1
        while ((originalWidth / sampleSize > maxSize) || (originalHeight / sampleSize > maxSize)) {
            sampleSize *= 2
        }
        val finalOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, finalOptions)!!
        } ?: throw Exception("이미지 디코딩 실패")
    }

    fun resizeBitmap(bitmap: Bitmap, maxSize: Int = 2048): Bitmap {
        Log.d("ResizeBitmap", "Original size: ${bitmap.width}x${bitmap.height}")
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        return if (bitmap.width >= bitmap.height) {
            Bitmap.createScaledBitmap(bitmap, maxSize, (maxSize / ratio).toInt(), true)
        } else {
            Bitmap.createScaledBitmap(bitmap, (maxSize * ratio).toInt(), maxSize, true)
        }
    }

    fun detectCornersFromBitmap(bitmap: Bitmap): List<PointF> {
        Log.d("CornerDetection", "-- detectCornersFromBitmap start: ${bitmap.width}×${bitmap.height}")

        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // 1. 흑백 변환 + 블러 + Canny 에지 검출
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(mat, mat, Size(5.0, 5.0), 0.0)
        Imgproc.Canny(mat, mat, 50.0, 150.0)

        // 2. 윤곽선 찾기
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
        Log.d("CornerDetection", "-- total contours found = ${contours.size}")

        val imageArea = bitmap.width * bitmap.height
        var largestQuad: MatOfPoint2f? = null
        var maxArea = 0.0

        for (contour in contours) {
            val approx = MatOfPoint2f()
            val contour2f = MatOfPoint2f(*contour.toArray())
            val peri = Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, approx, 0.02 * peri, true)

            // 3. 사각형 조건 + 면적 조건
            if (approx.total() == 4L) {
                val area = Imgproc.contourArea(approx)

                fun distance(p1: Point, p2: Point): Double {
                    return kotlin.math.hypot(p1.x - p2.x, p1.y - p2.y)
                }

                // 너무 작거나 너무 큰 사각형 무시 (전체 이미지 대비)
                if (area > 10000 && area < imageArea * 0.9) {
                    val points = approx.toArray()
                    val w = distance(points[0], points[1])
                    val h = distance(points[1], points[2])

                    val ratio = maxOf(w, h).toDouble() / minOf(w, h).toDouble()

                    Log.d("CornerDetection", "approxArea=$area, ratio=$ratio, corners=${approx.total()}")
                    Log.d(
                        "CornerDetection",
                        "Contour approx: area=${area.toInt()}, ratio=${"%.2f".format(ratio)}, corners=${approx.total()}"
                    )

                    // ⭐ 명함 비율인 경우만 통과 (예: 1.3 ~ 3.0 비율)
                    if (ratio in 1.0..4.0 && area > maxArea) {
                        largestQuad = approx
                        maxArea = area
                    }
                }
            }
        }

        // 4. 결과 리턴
        if (largestQuad != null) {
            val points = sortCorners(largestQuad.toArray().map { PointF(it.x.toFloat(), it.y.toFloat()) })
            Log.d("CornerDetection", "<< Detected corners: ${points.size}")
            return points
        }

        // 5. 실패 시 fallback
        val fallback = listOf(
            PointF(100f, 100f),
            PointF(bitmap.width - 100f, 100f),
            PointF(bitmap.width - 100f, bitmap.height - 100f),
            PointF(100f, bitmap.height - 100f)
        )

        Log.d("CornerDetection", "Fallback 4 corners used!")
        return fallback
    }

    private fun sortCorners(points: List<PointF>): List<PointF> {
        if (points.size != 4) return points

        val centerX = points.map { it.x }.average().toFloat()
        val centerY = points.map { it.y }.average().toFloat()

        return points.sortedBy {
            Math.atan2((it.y - centerY).toDouble(), (it.x - centerX).toDouble())
        }
    }
}
