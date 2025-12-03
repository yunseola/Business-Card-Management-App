package com.example.businesscardapp.ui.screen.myinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.pretendardMedium
import com.example.businesscardapp.ui.theme.pretendardRegular
import com.example.businesscardapp.ui.theme.pretendardSemiBold

@Composable
fun FAQScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    indication = null, // 🔹 Ripple 제거
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    navController.popBackStack()
                                }
                                .padding(end = 8.dp),
                            tint = Color.Unspecified // 원본 색상 유지
                        )
                        Text(
                            text = "자주하는 질문(FAQ)",
                            fontSize = 20.sp,
                            fontFamily = pretendardMedium
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp,
                modifier = Modifier.statusBarsPadding()
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // 첫 번째 QA
                Text(
                    text = "Q.",
                    fontSize = 15.sp,
                    fontFamily = pretendardSemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "저장한 명함은 어디에서 확인할 수 있나요?",
                    fontSize = 15.sp,
                    fontFamily = pretendardRegular,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "A.",
                    fontSize = 15.sp,
                    fontFamily = pretendardSemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "저장된 명함은 ‘명함첩’ 탭에서 확인 가능합니다.\n하단 맨 왼쪽 탭을 클릭하시면 디지털 명함 목록을 확인하실 수 있습니다.",
                    fontSize = 14.sp,
                    fontFamily = pretendardMedium,
                    color = Color.Black,
                    letterSpacing = 0.7.sp
                )
                Spacer(modifier = Modifier.height(30.dp))

                Divider(
                    color = Color(0xFFC6B9A4), // ← 변경된 구분선 색상
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 두 번째 QA
                Text(
                    text = "Q.",
                    fontSize = 15.sp,
                    fontFamily = pretendardSemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "회사 이메일 인증 메일이 오지 않아요.",
                    fontSize = 15.sp,
                    fontFamily = pretendardRegular,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "A.",
                    fontSize = 15.sp,
                    fontFamily = pretendardSemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "스팸 메일함을 확인하시거나 이메일 주소를\n한 번 더 검토 부탁드립니다. 그래도 인증 메일이 오지 않는다면 고객센터로 문의 및 연락 부탁드립니다.",
                    fontSize = 14.sp,
                    fontFamily = pretendardMedium,
                    color = Color.Black,
                    letterSpacing = 0.7.sp
                )
            }
        }
    )
}
