package com.example.businesscardapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businesscardapp.ui.theme.MainColor // Color.kt에 정의된 메인 컬러

@Composable
fun ConfirmButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MainColor,        // 항상 MainColor
            disabledContainerColor = MainColor, // 비활성화 시도 MainColor
            contentColor = Color.White,
            disabledContentColor = Color.White  // 비활성화 시 텍스트 색도 유지
        ),
        modifier = modifier
            .width(288.dp)
            .height(53.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                color = Color.White
            )
        )
    }
}

