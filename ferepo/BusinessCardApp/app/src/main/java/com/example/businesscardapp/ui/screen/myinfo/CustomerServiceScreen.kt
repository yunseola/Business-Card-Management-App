package com.example.businesscardapp.ui.screen.myinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.pretendardMedium

@Composable
fun CustomerServiceScreen(navController: NavHostController) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var inquiry by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    navController.popBackStack()
                                }
                                .padding(end = 8.dp),
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "고객센터",
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
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // 안내 문구 (가운데 정렬)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FAQ를 통해",
                        fontSize = 16.sp,
                        fontFamily = pretendardMedium,
                        color = Color.Black
                    )
                    Text(
                        text = "문제를 해결하지 못하셨나요?",
                        fontSize = 16.sp,
                        fontFamily = pretendardMedium,
                        color = Color.Black
                    )
                }

                // 답변 받을 이메일
                Text(
                    text = "답변 받을 이메일 주소",
                    fontSize = 14.sp,
                    fontFamily = pretendardMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            text = "example@sample.com",
                            fontSize = 14.sp,
                            fontFamily = pretendardMedium,
                            color = Color(0xFF9E9E9E)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 문의 내용
                Text(
                    text = "문의 내용",
                    fontSize = 14.sp,
                    fontFamily = pretendardMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = inquiry,
                    onValueChange = { inquiry = it },
                    placeholder = {
                        Text(
                            text = "",
                            fontSize = 14.sp,
                            fontFamily = pretendardMedium
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 문의 접수 버튼
                Button(
                    onClick = { /* TODO: 문의 접수 로직 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF4C3924),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "문의 내용 접수",
                        fontSize = 14.sp,
                        fontFamily = pretendardMedium
                    )
                }
            }
        }
    )
}
