package com.example.businesscardapp.ui.screen.myinfo

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.businesscardapp.ui.theme.pretendardMedium
import com.example.businesscardapp.R
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.businesscardapp.util.PrefUtil

@Composable
fun MyInfoScreen(rootNavController: NavHostController, navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "내 정보",
                        fontSize = 20.sp,
                        fontFamily = pretendardMedium
                    )
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
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .background(Color.White)
            ) {

                val items = listOf(
                    R.drawable.ic_myinfo_safety to "계정 보안",
                    R.drawable.ic_myinfo_company to "회사 인증하기",
                    R.drawable.ic_myinfo_settings to "설정",
                    R.drawable.ic_myinfo_faq to "자주하는 질문(FAQ)",
                    R.drawable.ic_myinfo_voice to "공지사항",
                    R.drawable.ic_myinfo_customer to "고객센터",
                    R.drawable.ic_myinfo_app to "앱 버전",
                    R.drawable.ic_myinfo_app to "로그아웃"
                )

                val settingsRoute = "settings"
                val context = LocalContext.current

                items.forEach { (iconRes, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp) // 글자 간 간격
                            .clickable(
                                indication = null, // 🔹 Ripple 제거
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                when (label) {
                                    "설정" -> navController.navigate("settings")
                                    "자주하는 질문(FAQ)" -> navController.navigate("faq")
                                    "공지사항" -> navController.navigate("notice")
                                    "앱 버전" -> navController.navigate("app_version")
                                    "고객센터" -> navController.navigate("customer_service")
                                    "회사 인증하기" -> navController.navigate("company_verification")
                                    "로그아웃" -> {
                                        PrefUtil.clear(context) // 저장된 JWT, 유저 ID 등 삭제
                                        rootNavController.navigate("intro") {
                                            popUpTo("main") { inclusive = true } // 루트 그래프의 main 스택 제거
                                            launchSingleTop = true
                                        }
                                    }
                                    // TODO: 추후 나머지 항목 라우팅 추가 가능
                                }
                                // TODO: 나중에 다른 label들 라우팅 추가 가능
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = label,
                            modifier = Modifier
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(15.dp)) // 아이콘과 글자 간 간격
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            fontFamily = pretendardMedium,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    )
}
