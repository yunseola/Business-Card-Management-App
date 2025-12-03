package com.example.businesscardapp.ui.screen.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businesscardapp.ui.theme.pretendardMedium
import android.widget.Toast

// 전화번호 포맷팅 함수
fun formatPhoneNumber(phone: String): String {
    return when {
        phone.length == 11 -> {
            // 01012345678 -> 010-1234-5678
            "${phone.substring(0, 3)}-${phone.substring(3, 7)}-${phone.substring(7)}"
        }
        phone.length == 10 -> {
            // 0101234567 -> 010-123-4567
            "${phone.substring(0, 3)}-${phone.substring(3, 6)}-${phone.substring(6)}"
        }
        else -> phone
    }
}

@Composable
fun AddCardScreen() {
    val context = LocalContext.current
    
    // 상태 변수
    var name by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "명함 수기 등록",
                        fontFamily = pretendardMedium,
                        fontSize = 20.sp
                    )
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 4.dp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = position,
                onValueChange = { position = it },
                label = { Text("직책") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formatPhoneNumber(phone),
                onValueChange = { newValue ->
                    // 숫자만 허용
                    val filteredValue = newValue.filter { it.isDigit() }
                    phone = filteredValue
                },
                label = { Text("전화번호") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text("회사명") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { 
                                // 필수 필드 검증
            if (name.isBlank() || company.isBlank()) {
                Toast.makeText(context, "필수 정보(이름, 회사)를 입력해 주세요", Toast.LENGTH_SHORT).show()
                return@Button
            }
                    /* 저장 처리 로직 */ 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("저장하기")
            }
        }
    }
}
