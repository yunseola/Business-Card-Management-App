package com.example.businesscardapp.ui.component

//class DigitalPreviewCardExport {
//}

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businesscardapp.R

@Composable
fun DigitalPreviewCardExport(
    photo: ImageBitmap?,             // 이미 로드된 프로필 사진 (null 가능)
    bgColor: Color,                  // 패턴이면 투명, 아니면 단색 배경
    patternResId: Int?,              // null이면 단색, 아니면 패턴 리소스
    useDarkText: Boolean,
    name: String,
    company: String,
    phoneDisplay: String,
    position: String,
    email: String,
    extras: List<Pair<String, String>>,
    orientation: CardOrientation
) {
    val textPrimary = if (useDarkText) Color.Black else Color.White
    val textSecondary = if (useDarkText) Color(0xFF666666) else Color(0xFFEFEFEF)

    val cardModifier =
        if (orientation == CardOrientation.Landscape)
            Modifier.fillMaxWidth().aspectRatio(9f / 5f)
        else
            Modifier.fillMaxWidth().aspectRatio(5f / 9f)

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = patternResId?.let { Color.Transparent } ?: bgColor),
        border = CardDefaults.outlinedCardBorder(true)
    ) {
        if (patternResId != null) {
            Image(
                painter = painterResource(patternResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (orientation == CardOrientation.Landscape) {
            Box(Modifier.fillMaxSize()) {
                if (company.isNotBlank()) {
                    Text(
                        text = company,
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 16.dp)
                    )
                }
                if (photo != null) {
                    Image(
                        bitmap = photo,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp).size(76.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp, end = 120.dp)
                ) {
                    if (name.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            if (position.isNotBlank()) {
                                Spacer(Modifier.width(6.dp))
                                Text(position, color = textSecondary, fontSize = 7.sp)
                            }
                        }
                    }
                    if (phoneDisplay.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(phoneDisplay, color = textPrimary, fontSize = 8.sp)
                    }
                    if (email.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(email, color = textPrimary, fontSize = 8.sp)
                    }
                    if (extras.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        extras.take(5).forEach { (label, value) ->
                            if (value.isNotBlank()) Text("$label: $value", color = textSecondary, fontSize = 7.sp)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (company.isNotBlank()) {
                    Text(company, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
                }
                if (photo != null) {
                    Spacer(Modifier.height(10.dp))
                    Image(
                        bitmap = photo,
                        contentDescription = null,
                        modifier = Modifier.size(96.dp),
                        contentScale = ContentScale.Crop
                    )
                } else Spacer(Modifier.height(12.dp))

                if (name.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        if (position.isNotBlank()) {
                            Spacer(Modifier.width(6.dp))
                            Text(position, color = textSecondary, fontSize = 7.sp)
                        }
                    }
                }
                if (phoneDisplay.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(phoneDisplay, color = textPrimary, fontSize = 8.sp)
                }
                if (email.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(email, color = textPrimary, fontSize = 8.sp)
                }
                Spacer(Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp)) {
                    extras.take(5).forEach { (label, value) ->
                        if (value.isNotBlank()) Text("$label: $value", color = textSecondary, fontSize = 7.sp)
                    }
                }
            }
        }
    }
}

