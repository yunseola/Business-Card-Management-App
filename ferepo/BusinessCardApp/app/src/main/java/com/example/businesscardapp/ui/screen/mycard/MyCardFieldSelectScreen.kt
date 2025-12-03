package com.example.businesscardapp.ui.screen.mycard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.DigitalPreviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCardFieldSelectScreen(
    navController: NavController,
    viewModel: MyCardViewModel,
    maxSelectable: Int = 5
) {
    val fields   by viewModel.fields.collectAsState()
    val bgHex    by viewModel.background.collectAsState()
    val pattern  by viewModel.pattern.collectAsState()
    val textDark by viewModel.textDark.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()

    val requiredUI = listOf("이름","연락처","회사","직책","이메일")
    val extras = remember(fields) { fields.filter { it.label !in requiredUI } }

    // ✅ VM에 저장된 선택 순서 불러오기
    val savedOrder by viewModel.selectedLabels.collectAsState()

    val order = remember { mutableStateListOf<String>() }
    val checked = remember { mutableStateMapOf<String, Boolean>() }

    // ✅ 최초 진입/리컴포즈 시, savedOrder 기준으로 체크/순서 복원
    LaunchedEffect(extras, savedOrder) {
        order.clear()
        checked.clear()

        val saved = savedOrder.ifEmpty { extras.map { it.label }.take(maxSelectable) }

        // savedOrder에 있는 것부터 순서대로 채우기
        saved.forEach { label ->
            if (extras.any { it.label == label } && order.size < maxSelectable) {
                order.add(label)
                checked[label] = true
            }
        }
        // 나머지는 미선택 상태로
        extras.forEach { f ->
            if (f.label !in checked) checked[f.label] = false
        }
    }

    fun selectedCount() = order.size

//    val mandatory = setOf("이름","연락처","회사","직책","이메일")




    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        // ✅ 선택 결과를 VM에 반영(필수는 VM에서 제외 처리하므로 extra들만 넘김)
                        viewModel.setSelectedLabels(order.toList())
                        navController.navigateUp() // ✅ 새로 push하지 말고 되돌아가기
                    }) { Text("적용") }
                },
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .background(Color.White)
        ) {
            val cardBg = if (pattern != null) "#00000000" else bgHex
            DigitalPreviewCard(
                orientationImageUri = photoUri?.toString(),
                bgHex = cardBg,
                patternCode = pattern,
                useDarkText = textDark,
                name     = fields.firstOrNull { it.label == "이름" }?.value.orEmpty(),
                company  = fields.firstOrNull { it.label == "회사" }?.value.orEmpty(),
                phone    = fields.firstOrNull { it.label == "연락처" }?.value.orEmpty(),
                position = fields.firstOrNull { it.label == "직책" }?.value.orEmpty(),
                email    = fields.firstOrNull { it.label == "이메일" }?.value.orEmpty(),
                // 필요하면 추가 항목 미리보기도 전달 가능:
                extras = extras.filter { checked[it.label] == true }.map { it.label to it.value }
            )

            Spacer(Modifier.height(12.dp))
            Text("추가 항목은 최대 ${maxSelectable}개까지 표시할 수 있어요.", color = Color.DarkGray)
            Spacer(Modifier.height(8.dp))

            // 항상 표시
            requiredUI.forEach { label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Spacer(Modifier.width(8.dp))
                    Text(text = label, color = Color.Black)
                    Spacer(Modifier.weight(1f))
                    val v = fields.firstOrNull { it.label == label }?.value.orEmpty()
                    Text(text = v, color = Color.DarkGray)
                }
            }

            Spacer(Modifier.height(8.dp))

            // 추가 항목 (체크박스)
            extras.forEach { f ->
                val isChecked = checked[f.label] == true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                        val newState = !isChecked
                        if (newState) {
                            if (selectedCount() >= maxSelectable) return@clickable
                            if (!order.contains(f.label)) order.add(f.label)
                        } else {
                            order.remove(f.label)
                        }
                        checked[f.label] = newState
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { newChecked ->
                            if (newChecked) {
                                if (selectedCount() < maxSelectable && !order.contains(f.label)) {
                                    order.add(f.label)
                                    checked[f.label] = true
                                }
                            } else {
                                order.remove(f.label)
                                checked[f.label] = false
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4C3924),
                            uncheckedColor = Color.Gray
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = f.label, color = Color.Black)
                    Spacer(Modifier.weight(1f))
                    Text(text = f.value, color = Color.DarkGray)
                }
            }
        }
    }
}
