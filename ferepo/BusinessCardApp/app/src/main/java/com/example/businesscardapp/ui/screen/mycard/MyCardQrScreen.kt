package com.example.businesscardapp.ui.screen.mycard

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun MyCardQrScreen(
    viewModel: MyCardQrViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Scaffold(
        containerColor = Color.White
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // 👈 뒤로가기 아이콘 (좌측 상단에서 살짝 아래로)
            IconButton(
                onClick = { backDispatcher?.onBackPressed() },
                modifier = Modifier
                    .padding(start = 8.dp, top = 16.dp) // ← top 값으로 원하는 만큼 내리기
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.Black
                )
            }

            // 👇 QR은 중앙
            when {
                ui.loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                ui.errorMessage != null -> Text(
                    ui.errorMessage!!,
                    modifier = Modifier.align(Alignment.Center)
                )

                ui.qrCodeUrl != null -> Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .align(Alignment.Center) // 중앙 정렬 그대로
                ) {
                    AsyncImage(
                        model = ui.qrCodeUrl,
                        contentDescription = "QR 코드",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

