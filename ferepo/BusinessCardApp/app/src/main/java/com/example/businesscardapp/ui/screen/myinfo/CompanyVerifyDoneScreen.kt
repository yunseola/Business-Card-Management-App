package com.example.businesscardapp.ui.screen.myinfo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CompanyVerifyDoneScreen(navController: NavController) {
    Scaffold { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("인증이 완료되었습니다 ✅", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                navController.navigate("main?tab=my_info") {
                    popUpTo("main") { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                Text("확인")
            }
        }
    }
}
