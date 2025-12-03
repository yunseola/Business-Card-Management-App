//AddCardDialog.kt
package com.example.businesscardapp.ui.screen.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.businesscardapp.ui.theme.pretendardRegular

// ✅ Ripple 제거 확장 함수
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    navController: NavController
) {
    var selected by remember { mutableStateOf<String?>(null) }

    val options = listOf(
//        "카메라로 촬영하기" to {
//            onDismiss()
//            navController.navigate("camera")
//        },
        "카메라로 촬영하기" to {
            onDismiss()
            navController.navigate("camera?from=paper")   // ← 명시
        },

        "앨범에서 선택하기" to {
            // TODO: 앨범 연결
            onDismiss()
            // 아래 추가: 앨범 연결
            navController.navigate("album_guide?from=ocr&max=2")

        },
        "수기로 작성하기" to {
            onDismiss()
            navController.navigate("add")
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000)) // 반투명 블랙 배경
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .width(287.dp)
                .height(190.dp)
                .padding(bottom = 28.dp)
                .clickable(enabled = false) {}, // Card 클릭 시 닫히지 않게
            shape = RoundedCornerShape(4.dp),
            elevation = 8.dp,
            backgroundColor = Color(0xFFF6F3ED)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                options.forEachIndexed { index, (label, action) ->
                    val isSelected = selected == label
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFFC6B9A4) else Color.Transparent)
                            .noRippleClickable {
                                selected = label
                                action()
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            fontFamily = pretendardRegular,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF4C3924),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (index < options.lastIndex) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}