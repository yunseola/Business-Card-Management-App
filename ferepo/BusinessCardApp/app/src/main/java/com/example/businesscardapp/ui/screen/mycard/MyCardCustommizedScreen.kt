package com.example.businesscardapp.ui.screen.mycard

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.MainColor
import com.example.businesscardapp.ui.theme.pretendardRegular
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource


// MyCardCustomizedScreen.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCardCustomizedScreen(
    navController: NavController,
    viewModel: MyCardViewModel,
    onDone: () -> Unit
) {
    // 1) viewModel 에서 상태 읽어오기
    val fields by viewModel.fields.collectAsState()
    val photoUri by viewModel.profileImageUri.collectAsState()
    val background by viewModel.background.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 명함 편집", color = MainColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            tint = MainColor
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val id = viewModel.saveMyCardToListAndReturnIndex()
                        if (id >= 0) {
                            // 먼저 이 화면을 닫고(옵션), 상세로 이동
                            onDone()  // AppNavGraph에서 popBackStack() 하도록 넘겼다면 유지
                            navController.navigate("my_card_detail/$id")
                        } else {
                            // 실패 안내 (필수값 미입력 등)
                            // 필요하면 Toast 사용
                        }
                    }) {
                        Text("저장", color = MainColor)
                    }
                }

            )
        },
        content = { inner ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(16.dp)
            ) {
                // ─── 2) 명함 미리보기
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .then(
                            runCatching {
                                // background: "#RRGGBB" 형식 가정
                                Modifier.background(Color(AndroidColor.parseColor(background)))
                            }.getOrElse { Modifier }
                        )
                ) {
                    // 사진이 있으면 앞에 놓기
                    photoUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "명함 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ─── 3) 필드 편집 리스트
                fields.forEachIndexed { idx, field ->
                    EditableFieldItem(
                        label = field.label,
                        value = field.value,
                        onLabelChange = { newLabel ->
                            viewModel.updateField(idx, field.copy(label = newLabel))
                        },
                        onValueChange = { newValue ->
                            viewModel.updateField(idx, field.copy(value = newValue))
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // ─── 4) 필드 추가 버튼
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.addField() }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mycard_field),
                        contentDescription = "필드 추가",
                        tint = MainColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("필드 추가 +", color = MainColor)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditableFieldItem(
    label: String,
    value: String,
    onLabelChange: (String) -> Unit,
    onValueChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        // ─ 라벨 입력란 (테두리 없는 필드)
        OutlinedTextField(
            value = label,
            onValueChange = onLabelChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent), // 배경 자체 제거 가능
            shape = RoundedCornerShape(0.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = MainColor,
                unfocusedTextColor = MainColor,
                focusedPlaceholderColor = Color.Gray,
                unfocusedPlaceholderColor = Color.Gray
            ),
            placeholder = { Text("필드 이름") }
        )

        Spacer(Modifier.height(4.dp))

        // ─ 값 입력란 (테두리 없음)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            shape = RoundedCornerShape(0.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedPlaceholderColor = Color.LightGray,
                unfocusedPlaceholderColor = Color.LightGray
            ),
            placeholder = { Text("내용을 입력하세요") }
        )
    }
}
