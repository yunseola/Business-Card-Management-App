package com.example.businesscardapp.ui.screen.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import com.example.businesscardapp.R
import androidx.compose.ui.res.colorResource


@Composable
fun IntroScreen(
    onGoogleSignInClick: () -> Unit,
    onLoginResult: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.main)),
        color = colorResource(id = R.color.main)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "신뢰를 관리하는 기준",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Text(
                    text = "명함.zip",
                    color = Color.White,
                    fontSize = 57.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 60.sp
                )

                Spacer(modifier = Modifier.height(250.dp))

                Box(
                    modifier = Modifier
                        .width(287.dp)
                        .height(60.dp)
                        .graphicsLayer {
                            scaleX = if (isPressed) 0.95f else 1f
                            scaleY = if (isPressed) 0.95f else 1f
                            alpha = if (isPressed) 0.9f else 1f
                        }
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onGoogleSignInClick()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // ✅ Google 로고
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Google로 시작하기",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
