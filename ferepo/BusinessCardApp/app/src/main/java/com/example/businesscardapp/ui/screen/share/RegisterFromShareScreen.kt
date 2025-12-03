package com.example.businesscardapp.ui.screen.share

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.LoadingDialog

@Composable
fun RegisterFromShareScreen(
    navController: NavController,
    imageUri: Uri?,          // 딥링크로 넘어온 이미지 (옵션)
    displayName: String,     // 딥링크로 넘어온 이름 (옵션)
    cardId: Int,             // 팔로우 대상 카드 ID (필수)
    viewModel: RegisterFromShareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current

    // 진입 시 공개 미리보기 로딩
    LaunchedEffect(cardId) {
        if (cardId > 0) viewModel.loadPreview(cardId)
    }

    // 에러 토스트
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show() }
    }

    // 성공 시 상세로 이동 (라우트 일치 주의)
    LaunchedEffect(uiState.successCardId) {
        uiState.successCardId?.let { id ->
            navController.navigate("digital_card_detail/$id") {
                // popUpTo를 쓰려면 존재하는 목적지/ID만 지정
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val titleText = when {
        displayName.isNotBlank() -> "${displayName}님의 명함"
        uiState.previewName.isNotBlank() -> "${uiState.previewName}님의 명함"
        else -> "디지털 명함"
    }

    val previewUri: Uri? = when {
        imageUri != null -> imageUri
        uiState.previewImage != null -> Uri.parse(uiState.previewImage)
        else -> null
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F2))
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp)
            .padding(bottom = 28.dp)
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            // 제목
            Text(
                text = titleText,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2C2620),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            )

            // 이미지 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFE7E7E7), RoundedCornerShape(14.dp))
                ) {
                    if (previewUri != null) {
                        AsyncImage(
                            model = previewUri,
                            contentDescription = "명함 이미지",
                            contentScale = ContentScale.Crop, // 꽉 채움
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_person_placeholder),
                                contentDescription = null,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("이미지 미리보기 없음", color = Color(0xFF8B857E))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 하단 버튼
            Button(
                onClick = { viewModel.register(cardId.toString()) }, // ViewModel.register(String)
                enabled = !uiState.loading && cardId > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A331F))
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp),
                        color = Color.White
                    )
                } else {
                    Text(text = "+ 등록하기", color = Color.White, fontSize = 16.sp)
                }
            }
        }

        // 전체 화면 로딩 다이얼로그 (최소 2초 유지 로직은 ViewModel에서 처리)
        if (uiState.loading) {
            LoadingDialog(message = "등록 중입니다...")
        }
    }
}
