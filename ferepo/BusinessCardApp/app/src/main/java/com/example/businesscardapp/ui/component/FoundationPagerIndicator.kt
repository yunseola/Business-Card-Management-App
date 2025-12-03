// FoundationPagerIndicator.kt
package com.example.businesscardapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape

@Composable
fun FoundationPagerIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotSpacing: Dp = 8.dp,
    activeColor: Color = Color(0xFF4C3924),
    inactiveColor: Color = Color.LightGray,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(
                        color = if (index == selectedIndex) activeColor else inactiveColor,
                        shape = CircleShape
                    )
            )
        }
    }
}
