package com.example.businesscardapp.ui.screen.myinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication


@Composable
fun SettingScreen(navController: NavHostController) {
    var alarmEnabled by remember { mutableStateOf(true) }
    var memoPopupEnabled by remember { mutableStateOf(false) }
    var photoEnabled by remember { mutableStateOf(false) }

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
                            text = "설정",
                            fontSize = 20.sp,
                            fontFamily = pretendardMedium
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp,
                modifier = Modifier.statusBarsPadding()
            )
        }
        ,
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                SettingItem("알림 허용", alarmEnabled) { alarmEnabled = !alarmEnabled }
                SettingItem("전화 메모 팝업 허용", memoPopupEnabled) { memoPopupEnabled = !memoPopupEnabled }
                SettingItem("사진 허용", photoEnabled) { photoEnabled = !photoEnabled }
            }
        }
    )
}

@Composable
fun SettingItem(label: String, isChecked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontFamily = pretendardRegular
        )
        Icon(
            painter = painterResource(
                id = if (isChecked) R.drawable.ic_toggle_on else R.drawable.ic_toggle_off
            ),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clickable(
                    indication = null, // ✅ Ripple 제거
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onToggle()
                },
            tint = Color.Unspecified // 원본 색상 유지
        )
    }
}
