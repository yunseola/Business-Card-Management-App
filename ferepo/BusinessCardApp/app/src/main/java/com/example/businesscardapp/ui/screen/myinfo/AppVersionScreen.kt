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
import androidx.compose.foundation.Image


@Composable
fun AppVersionScreen(navController: NavHostController) {
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
                            text = "앱 버전",
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
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "버전정보",
                    fontSize = 13.sp,
                    fontFamily = pretendardRegular,
                    color = Color(0xFF9E9E9E)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 앱 아이콘 자리 (회색 박스 대체)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground2), // 임시 아이콘
                            contentDescription = "앱 아이콘",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column {
                        Text(
                            text = "명함.zip",
                            fontSize = 18.sp,
                            fontFamily = pretendardSemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1.0.0",
                            fontSize = 14.sp,
                            fontFamily = pretendardRegular,
                            color = Color.Black
                        )
                    }
                }
            }
        },
        backgroundColor = Color.White
    )
}
