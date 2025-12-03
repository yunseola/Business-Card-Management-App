package com.example.businesscardapp.ui.screen.myinfo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.pretendardMedium
import com.example.businesscardapp.ui.theme.pretendardSemiBold
import androidx.compose.material3.Button
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape


@Composable
fun CompanyVerificationScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4C3924)) // 배경색 (진한 갈색)
            .padding(24.dp)
    ) {
        // 🔙 뒤로가기 버튼
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "뒤로가기",
            tint = Color.White,
            modifier = Modifier
                .statusBarsPadding() // ← 상태바 높이만큼 padding
                .padding(start = 0.dp, top = 0.dp) // ← 실제 내용과의 간격 조정
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    navController.popBackStack()
                }
        )

        // 🔳 본문 컨텐츠
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter) // Center → TopCenter
                .padding(top = 150.dp, start = 16.dp, end = 16.dp), // 원하는 만큼 top padding 추가
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_myinfo_safety_two), // 인증 아이콘
                contentDescription = "회사 인증 아이콘",
                modifier = Modifier
                    .size(110.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "회사인증",
                fontFamily = pretendardSemiBold,
                fontSize = 36.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "디지털 명함을 선택하고\n회사 인증을 진행하세요",
                fontFamily = pretendardMedium,
                fontSize = 24.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // ⬇ 하단 버튼
        Button(
            onClick = {
                navController.navigate("myCardsSelectForVerify")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF3EEE7)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "+ 명함 선택",
                fontFamily = pretendardMedium,
                fontSize = 16.sp,
                color = Color(0xFF4C3924)
            )
        }
    }
}

