// components/GuideOverlay.kt
package com.example.businesscardapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businesscardapp.ui.theme.pretendardSemiBold
import com.example.businesscardapp.ui.theme.pretendardMedium


@Composable
fun GuideOverlay(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 반투명 배경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(160.dp))

            Text(
                text = "명함 촬영 가이드",
                color = Color.White,
                fontFamily = pretendardSemiBold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            GuideItem("명함 전체가 프레임 안에 배치되도록 하세요")
            GuideItem("밝은 조명 아래에서 촬영하세요")
            GuideItem("반사광이 없도록 조절하세요")
            GuideItem("명함과 비슷한 색의 배경은 주의해 주세요")

            Spacer(modifier = Modifier.height(160.dp))

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF6F3ED)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "닫기", 
                    color = Color.Black,
                    fontFamily = pretendardMedium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun GuideItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "✓",
            color = Color(0xFFF6F3ED),
            fontFamily = pretendardMedium,
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            color = Color.White,
            fontFamily = pretendardMedium,
            fontSize = 14.sp
        )
    }
}
