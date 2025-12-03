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
fun NoticeScreen(navController: NavHostController) {
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
                            text = "공지사항",
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
            val notices = listOf(
                "명함.zip 운영정책 기정 안내",
                "업데이트 안내",
                "신고 정책 안내"
            )

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                notices.forEachIndexed { index, notice ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                // TODO: 클릭 시 상세 공지로 이동
                            }
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = notice,
                            fontSize = 16.sp,
                            fontFamily = pretendardMedium,
                            color = Color.Black
                        )
                    }

                    if (index < notices.lastIndex) {
                        Divider(
                            color = Color(0xFFC6B9A4),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()

                        )
                    }
                }
            }
        }
    )
}

