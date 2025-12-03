// components/CameraPreviewScreen.kt
package com.example.businesscardapp.ui.component

import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.io.File
import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import com.example.businesscardapp.ui.theme.pretendardMedium
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize

@Composable
fun CameraPreviewScreen(
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    onBack: () -> Unit,
    showControls: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setTargetRotation(android.view.Surface.ROTATION_0)
            .build()
    }

    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    var frameBounds by remember { mutableStateOf<Rect?>(null) }
    var screenSize by remember { mutableStateOf<IntSize?>(null) }
    var previewViewBounds by remember { mutableStateOf<Rect?>(null) }
    val density = LocalDensity.current

    var zoomLevel by remember { mutableStateOf(1f) } // 기본 1x
    var camera by remember { mutableStateOf<Camera?>(null) }


    LaunchedEffect(lensFacing) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            preview.setSurfaceProvider(previewView.surfaceProvider)

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraPreviewScreen", "카메라 바인딩 실패", e)
                onError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "카메라 초기화 실패", e))
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(lifecycleOwner) {
        onDispose { cameraExecutor.shutdown() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                screenSize = IntSize(coordinates.size.width, coordinates.size.height)
                Log.d("CameraPreviewScreen", "화면 크기: $screenSize")
            }
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    val bounds = coordinates.boundsInWindow()
                    previewViewBounds = Rect(
                        bounds.left.toInt(),
                        bounds.top.toInt(),
                        bounds.right.toInt(),
                        bounds.bottom.toInt()
                    )
                    Log.d("CameraPreviewScreen", "프리뷰 뷰 좌표: $previewViewBounds")
                }
        )

        if (showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp)
            ) {
                // 촬영 프레임
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (- 60).dp)
                        .size(280.dp, 180.dp)
                        .border(2.dp, Color.White)
                        .onGloballyPositioned { coordinates ->
                            val bounds = coordinates.boundsInWindow()
                            frameBounds = Rect(
                                bounds.left.toInt(),
                                bounds.top.toInt(),
                                bounds.right.toInt(),
                                bounds.bottom.toInt()
                            )
                            Log.d("CameraPreviewScreen", "프레임 좌표: $frameBounds")
                        }
                )

                // 상단 안내 문구
                Text(
                    text = "명함 전체가 프레임 안에 배치되도록 하세요",
                    color = Color.White,
                    fontFamily = pretendardMedium,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 120.dp)
                )

                // 프레임 아래 안내 문구
                Text(
                    text = "영역 안에 명함이 꼭 차도록 배치 후\n하단 버튼을 누르면 촬영됩니다",
                    color = Color.White,
                    fontFamily = pretendardMedium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 120.dp + 180.dp / 2 + 16.dp) // 프레임 기준 아래로 조금 띄움
                )

                // 1x / 2x 버튼
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp), // 촬영 버튼 위로 띄움
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ZoomButton(text = "1x", selected = zoomLevel == 1f) {
                        zoomLevel = 1f
                        camera?.cameraControl?.setZoomRatio(1f)
                    }
                    ZoomButton(text = "2x", selected = zoomLevel == 2f) {
                        zoomLevel = 2f
                        camera?.cameraControl?.setZoomRatio(2f)
                    }
                }

                // 가운데 촬영 버튼만 배치
                Button(
                    onClick = {
                        val outputDir = context.getExternalFilesDir(null)
                        if (outputDir != null) {
                            takePhoto(
                                context = context,           //추가
                                imageCapture = imageCapture,
                                outputDirectory = outputDir,
                                executor = cameraExecutor,
                                frameBounds = frameBounds,
                                previewViewBounds = previewViewBounds,
                                onImageCaptured = onImageCaptured,
                                onError = onError
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {}
            }
        }

    }
}

@Composable
fun ZoomButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) Color.DarkGray else Color.LightGray
    val textColor = if (selected) Color.White else Color.Black

    Button(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp
        )
    }
}


private fun takePhoto(
    context: Context,                       // ⬅ 추가
    imageCapture: ImageCapture,
    outputDirectory: File,
    executor: ExecutorService,
    frameBounds: Rect?,
    previewViewBounds: Rect?,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(outputDirectory, "IMG_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    val mainExecutor = ContextCompat.getMainExecutor(context)   // ⬅ 추가

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                try {
                    val savedUri = Uri.fromFile(photoFile)
                    val rotationDegrees = 90 // 명시적으로 가로 촬영 기준 회전 설정
                    val croppedFile = cropImageToFrame(photoFile, frameBounds, previewViewBounds, rotationDegrees)
                    val croppedUri = Uri.fromFile(croppedFile)
                    // ✅ 메인스레드에서 콜백
                    mainExecutor.execute { onImageCaptured(croppedUri) }
//                    onImageCaptured(croppedUri)
                } catch (e: Exception) {
                    // ✅ 메인스레드에서 콜백
                    mainExecutor.execute { onImageCaptured(Uri.fromFile(photoFile)) }
                }
            }
//                    Log.e("CameraDebug", "크롭 실패, 원본 사용", e)
//                    onImageCaptured(Uri.fromFile(photoFile))
//                }
//            }

            override fun onError(exception: ImageCaptureException) {
                // ✅ 메인스레드에서 콜백
                mainExecutor.execute { onError(exception) }
            }
//                onError(exception)
//            }
        }
    )
}

private fun cropImageToFrame(
    originalFile: File,
    frameBounds: Rect?,
    previewViewBounds: Rect?,
    rotationDegrees: Int
): File {
    val originalBitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    val rotatedBitmap = Bitmap.createBitmap(
        originalBitmap,
        0, 0,
        originalBitmap.width,
        originalBitmap.height,
        matrix,
        true
    )

    if (frameBounds == null || previewViewBounds == null) return originalFile

    val imageWidth = rotatedBitmap.width
    val imageHeight = rotatedBitmap.height

    val widthRatio = imageWidth.toFloat() / previewViewBounds.width()
    val heightRatio = imageHeight.toFloat() / previewViewBounds.height()

    val cropLeft = ((frameBounds.left - previewViewBounds.left) * widthRatio).toInt().coerceIn(0, imageWidth - 1)
    val cropTop = ((frameBounds.top - previewViewBounds.top) * heightRatio).toInt().coerceIn(0, imageHeight - 1)
    val cropWidth = (frameBounds.width() * widthRatio).toInt().coerceIn(1, imageWidth - cropLeft)
    val cropHeight = (frameBounds.height() * heightRatio).toInt().coerceIn(1, imageHeight - cropTop)

    val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, cropLeft, cropTop, cropWidth, cropHeight)

    // 🔧 대비/밝기 향상 보정
    val enhancedBitmap = enhanceForOCR(croppedBitmap)

    val croppedFile = File(originalFile.parent, "CROPPED_IMG_${System.currentTimeMillis()}.jpg")
    croppedFile.outputStream().use {
        enhancedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
    }

    return croppedFile
}

private fun enhanceForOCR(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val contrast = 1.1f  // 대비 약간 향상
    val brightness = 15  // 밝기 약간 상승

    for (x in 0 until width) {
        for (y in 0 until height) {
            val pixel = bitmap.getPixel(x, y)
            val a = (pixel shr 24) and 0xFF
            var r = (pixel shr 16) and 0xFF
            var g = (pixel shr 8) and 0xFF
            var b = pixel and 0xFF

            // 밝기/대비 조절 공식: New = ((Old - 128) * contrast + 128) + brightness
            r = ((r - 128) * contrast + 128 + brightness).toInt().coerceIn(0, 255)
            g = ((g - 128) * contrast + 128 + brightness).toInt().coerceIn(0, 255)
            b = ((b - 128) * contrast + 128 + brightness).toInt().coerceIn(0, 255)

            val newPixel = (a shl 24) or (r shl 16) or (g shl 8) or b
            result.setPixel(x, y, newPixel)
        }
    }

    return result
}