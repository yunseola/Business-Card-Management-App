
//AlbumGuideScreen.kt
package com.example.businesscardapp.ui.screen.add

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.ConfirmButton
import com.example.businesscardapp.ui.theme.BackgroundColor
import com.example.businesscardapp.ui.theme.GrayColor
import com.example.businesscardapp.ui.theme.MainColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumGuideScreen(navController: NavController, from: String, max: Int, cardId: String? = null) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ✅ AlbumImagePickerScreen 과 동일한 TopAppBar 구성
            TopAppBar(
                title = { /* 제목 없이 비워두기 */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = null,
                            tint = MainColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )

            // 본문 영역
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(64.dp))

                // 🟤 제목
                Text(
                    text = "명함 사진을 선택해주세요",
                    style = MaterialTheme.typography.titleLarge.copy(color = MainColor),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 설명 텍스트
                Text(
                    text = "명함 사진을 최대 2장까지 등록할 수 있습니다.\n등록 순서대로 1, 2로 표시됩니다.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = GrayColor),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                // ✅ 앨범 이미지 선택 화면으로 이동
                ConfirmButton(
                    text = "확인",
                    onClick = {
                        val cardIdParam = if (cardId != null) "&cardId=$cardId" else ""
                        navController.navigate("album_image_picker?max=$max&from=$from$cardIdParam")
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
