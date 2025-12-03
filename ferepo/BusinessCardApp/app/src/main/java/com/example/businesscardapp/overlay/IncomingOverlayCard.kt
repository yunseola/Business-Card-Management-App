// 같은 패키지에 파일 하나 더: IncomingOverlayCard.kt
package com.example.businesscardapp.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.businesscardapp.data.model.CardCallInfoResponse

@Composable
fun IncomingOverlayCard(
    phone: String?,
    cardInfo: CardCallInfoResponse?,
    onClose: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF101317))
                    .padding(16.dp)
            ) {
                Text("통화 정보", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(phone ?: "번호 확인 중", color = Color(0xFFB5C0D0))
                Spacer(Modifier.height(10.dp))
                if (cardInfo != null) {
                    Text("${cardInfo.name} • ${cardInfo.company} • ${cardInfo.position}",
                        color = Color(0xFFEDEFF5))
                    if (!cardInfo.memoSummary.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(cardInfo.memoSummary, color = Color(0xFFBAC2CF))
                    }
                } else {
                    Text("명함 정보를 불러오는 중...", color = Color(0xFFBAC2CF))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onClose) { Text("닫기") }
                }
            }
        }
    }
}
