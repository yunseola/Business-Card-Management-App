// CameraScreen.kt
package com.example.businesscardapp.ui.screen.camera

import android.net.Uri
import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.businesscardapp.ui.component.CameraPreviewScreen
import com.example.businesscardapp.ui.component.GuideOverlay
import com.google.accompanist.permissions.*
import android.util.Log
import java.io.File
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.material3.CardDefaults
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.*
import androidx.compose.ui.unit.sp
import com.example.businesscardapp.ui.screen.mycard.MyCardViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.font.FontWeight




@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    navController: NavController,
    from: String = "paper",                        // "paper" | "mycard" | "edit"
    cardId: String? = null,                        // 편집 모드일 때 cardId
    myCardViewModel: MyCardViewModel               // ← 추가
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // 상태 관리
    var showGuide by remember { mutableStateOf(true) }
    var showBackSideDialog by remember { mutableStateOf(false) }
    var showLoadingScreen by remember { mutableStateOf(false) }
    var frontImageUri by remember { mutableStateOf<String?>(null) }
    var backImageUri by remember { mutableStateOf<String?>(null) }
    var hasNavigated by remember { mutableStateOf(false) }



    LaunchedEffect(cameraPermissionState.status) {
        Log.d("CameraScreen", "권한 상태: ${cameraPermissionState.status}")
        Log.d("CameraScreen", "isGranted: ${cameraPermissionState.status.isGranted}")
    }

    // 권한이 없으면 요청
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            Log.d("CameraScreen", "카메라 권한 요청 시작")
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // 권한이 없는 상태면 메시지만 표시
    if (!cameraPermissionState.status.isGranted) {
        Log.d("CameraScreen", "카메라 권한이 없음")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("카메라 권한이 필요합니다.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    // 화면 상태에 따른 렌더링
    when {
        showLoadingScreen -> {
            LoadingScreen(
                onProcessingComplete = {
                    Log.d("CameraScreen", "로딩 완료, 결과 화면으로 이동")
                    showLoadingScreen = false

                    // 중복 네비게이션 방지
                    if (hasNavigated) {
                        Log.d("CameraScreen", "이미 네비게이션이 완료됨, 중복 방지")
                        return@LoadingScreen
                    }

                    // 안전한 네비게이션
                    try {
                        val safeUri = frontImageUri ?: ""
                        Log.d("CameraScreen", "결과 화면으로 이동: $safeUri")
                        Log.d("CameraScreen", "네비게이션 시도 전 hasNavigated: $hasNavigated")
                        if (safeUri.isNotEmpty()) {
                            // 네비게이션을 더 안전하게 처리
                            try {
                                hasNavigated = true
                                Log.d("CameraScreen", "네비게이션 실행: camera_result/$safeUri")
                                val resultBase = if (from == "mycard") "camera_result_mycard" else "camera_result"
                                val front = android.net.Uri.encode(safeUri)
                                val back  = android.net.Uri.encode(backImageUri ?: "")
                                val base = if (from == "mycard") "camera_result_mycard" else "camera_result" // ✅ 분기
                                
                                // 편집 모드일 때 cardId 파라미터 추가
                                val cardIdParam = if (from == "edit" && cardId != null) "&from=edit&cardId=$cardId" else ""
                                navController.navigate("$base/$safeUri?back=${backImageUri ?: ""}$cardIdParam") {
//                                navController.navigate("$resultBase/$front?back=$back") {
                                    popUpTo("camera") { inclusive = true }
                                    launchSingleTop = true
                                }

                                Log.d("CameraScreen", "네비게이션 성공")
                            } catch (e: Exception) {
                                Log.e("CameraScreen", "네비게이션 실패", e)
                                hasNavigated = false
                                Toast.makeText(context, "화면 이동 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        } else {
                            Log.e("CameraScreen", "이미지 URI가 비어있습니다")
                            Toast.makeText(context, "촬영된 이미지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    } catch (e: Exception) {
                        Log.e("CameraScreen", "네비게이션 처리 실패", e)
                        Toast.makeText(context, "화면 이동 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }
            )
        }
        showBackSideDialog -> {
            BackSideDialog(
                onYes = {
                    showBackSideDialog = false
                    // 뒷면 촬영을 위해 카메라 화면 유지
                },
                onNo = {
                    showBackSideDialog = false
                    showLoadingScreen = true
                }
            )
        }
        showGuide -> {
            Log.d("CameraScreen", "가이드 화면 표시")
            GuideOverlay(onClose = {
                Log.d("CameraScreen", "가이드 닫기")
                showGuide = false
            })
        }
        else -> {
            Log.d("CameraScreen", "카메라 프리뷰 화면 표시")
            CameraPreviewScreen(
                onImageCaptured = { uri ->
                    Log.d("CameraScreen", "이미지 캡처됨: $uri")
                    try {
                        // 파일 존재 확인
                        val path = uri.path ?: run {
                            Toast.makeText(context, "잘못된 이미지 경로입니다.", Toast.LENGTH_SHORT).show()
                            return@CameraPreviewScreen
                        }
                        val file = File(path)
                        if (!file.exists()) {
                            Log.e("CameraScreen", "촬영된 파일이 존재하지 않습니다: ${file.absolutePath}")
                            Toast.makeText(context, "촬영된 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            return@CameraPreviewScreen
                        }

                        val fileName = file.name

                        if (frontImageUri == null) {
                            // 앞면 촬영 완료 → 뒷면 촬영 여부 다이얼로그 표시
                            frontImageUri = fileName
                            showBackSideDialog = true
                            Log.d("CameraScreen", "앞면 촬영 완료, 뒷면 다이얼로그 표시")
                        } else {
                            // 뒷면 촬영 완료 → 로딩 화면으로 전환(여기서 OCR 후 결과 화면으로 이동)
                            backImageUri = fileName
                            showLoadingScreen = true
                            Log.d("CameraScreen", "뒷면 촬영 완료, 로딩 화면 표시")
                        }
                    } catch (e: Exception) {
                        Log.e("CameraScreen", "이미지 처리 실패", e)
                        Toast.makeText(context, "이미지 처리 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },

                        onError = { e ->
                    Log.e("CameraScreen", "카메라 오류", e)
                    Toast.makeText(context, "촬영 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                onBack = {
                    Log.d("CameraScreen", "뒤로가기 버튼 클릭됨")
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun LoadingScreen(
    onProcessingComplete: () -> Unit
) {
    var rotation by remember { mutableStateOf(0f) }
    var hasCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 2초 후에 처리 완료 (더 짧게)
        delay(2000)
        if (!hasCompleted) {
            Log.d("LoadingScreen", "로딩 완료, 콜백 호출")
            hasCompleted = true
            onProcessingComplete()
        }
    }

    LaunchedEffect(Unit) {
        // 로딩 애니메이션
        while (true) {
            delay(50)
            rotation += 5f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F3ED))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 로딩 아이콘 (피그마에서 가져온 디자인)
            Icon(
                painter = painterResource(id = R.drawable.ic_loading),
                contentDescription = "로딩",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(
                        rotationZ = rotation
                    ),
                tint = Color(0xFF4C3924)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 로딩 메시지
            Text(
                text = "정보를 인식 중입니다",
                fontFamily = pretendardRegular,
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color(0xFF4C3924)
            )

            Text(
                text = "잠시만 기다려 주세요",
                fontFamily = pretendardRegular,
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color(0xFF4C3924)
            )
        }
    }
}

@Composable
fun BackSideDialog(
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
                .fillMaxWidth()
                .height(160.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF6F3ED)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxHeight(), // 전체 높이 사용
                verticalArrangement = Arrangement.SpaceBetween // 위 아래로 정렬
            ) {
                Text(
                    text = "명함의 뒷면도 촬영하시겠습니까?",
                    fontFamily = pretendardMedium,
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onYes,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("예", fontFamily = pretendardMedium, fontSize = 14.sp, color = Color(0xFF4C3924))
                    }

                    TextButton(
                        onClick = onNo
                    ) {
                        Text("아니오", fontFamily = pretendardMedium, fontSize = 14.sp, color = Color(0xFF4C3924))
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewCard(
    photoUri: String?,           // ex) viewModel.photoUri.value?.toString()
    backgroundHex: String?,      // ex) "#FFFFFF"
    name: String,
    company: String,
    phone: String
) {
    val bg = remember(backgroundHex) {
        try {
            backgroundHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.White
        } catch (_: Exception) { Color.White }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = CardDefaults.outlinedCardBorder(true)
    ) {
        Box(Modifier.fillMaxSize()) {

            // 배경 이미지(선택)
            if (!photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 오른쪽 하단 정렬로 필드 "값" 오버레이
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (name.isNotBlank()) {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
                if (company.isNotBlank()) {
                    Text(
                        text = company,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
                if (phone.isNotBlank()) {
                    Text(
                        text = phone,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
