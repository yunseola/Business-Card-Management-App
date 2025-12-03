package com.example.businesscardapp.ui.screen.mycard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MyCardPreview(vm: MyCardViewModel) {
    val fields = vm.fields.collectAsState().value
    val bg = vm.background.collectAsState().value ?: "#FFFFFF"

    val name = fields.firstOrNull { it.label == "이름" }?.value.orEmpty()
    val company = fields.firstOrNull { it.label == "회사" }?.value.orEmpty()
    val phone = fields.firstOrNull { it.label == "연락처" }?.value.orEmpty()
    val position = fields.firstOrNull { it.label == "직책" }?.value.orEmpty()
    val department = fields.firstOrNull { it.label == "부서" }?.value.orEmpty()

    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(parseBgColor(bg))  // ← Color 로 변경
                .padding(16.dp)
        ) {
            Column {
                Text(company.ifBlank { "회사명" }, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                Text(name.ifBlank { "이름" }, style = MaterialTheme.typography.headlineSmall, color = Color.Black)
                Text(
                    listOf(position, department).filter { it.isNotBlank() }.joinToString(" "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                Spacer(Modifier.weight(1f))
                Text(phone.ifBlank { "연락처" }, style = MaterialTheme.typography.bodySmall, color = Color.Black)
            }
        }
    }
}

@Composable
private fun parseBgColor(code: String): Color {
    val hex = code.removePrefix("color:")
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color(0xFFF5F5F5)
    }
}