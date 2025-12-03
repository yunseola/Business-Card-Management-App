package com.example.businesscardapp.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.*
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.businesscardapp.ui.component.ShareBottomSheet
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.businesscardapp.ui.viewmodel.PaperCardViewModel
import com.example.businesscardapp.data.model.MemoRequest
import com.example.businesscardapp.data.model.UpdatePaperCardRequest
import com.example.businesscardapp.data.model.UpdateField
import com.example.businesscardapp.data.model.GroupItem
import android.net.Uri

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
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = pretendardRegular,
            fontSize = 16.sp,
            color = Color(0xFF4C3924),
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = when (label) {
                "전화번호", "회사 전화번호", "팩스" -> formatPhoneNumber(value)
                else -> value
            },
            fontFamily = pretendardMedium,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableInfoField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontFamily = pretendardRegular,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                when (label) {
                    "전화번호", "회사 전화번호", "팩스" -> {
                        // 전화번호 관련 필드는 숫자만 허용
                        onValueChange(newValue.filter { it.isDigit() })
                    }
                    else -> onValueChange(newValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
            placeholder = {
                if (label != "그룹") {
                    Text(
                        text = "내용을 입력하세요",
                        fontFamily = pretendardMedium,
                        fontSize = 16.sp,
                        color = Color(0xFF878787)
                    )
                }
            },
            keyboardOptions = when (label) {
                "전화번호", "회사 전화번호", "팩스" -> KeyboardOptions(keyboardType = KeyboardType.Phone)
                "이메일" -> KeyboardOptions(keyboardType = KeyboardType.Email)
                "웹사이트" -> KeyboardOptions(keyboardType = KeyboardType.Uri)
                else -> KeyboardOptions.Default
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4C3924),
                unfocusedBorderColor = Color(0xFFBDBDBD),
                focusedContainerColor = Color(0xFFFFFFFF),
                unfocusedContainerColor = Color(0xFFFFFFFF)
            ),
            shape = RoundedCornerShape(7.dp),
            textStyle = TextStyle(
                fontFamily = pretendardMedium,
                fontSize = 16.sp,
                color = Color.Black
            )
        )
    }
}

@Composable
fun EditableGroupField(
    label: String,
    selectedGroups: Set<String>,
    onGroupClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontFamily = pretendardRegular,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color(0xFFF6F3ED), RoundedCornerShape(7.dp))
                .padding(horizontal = 16.dp)
                .clickable { onGroupClick() }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedGroups.isEmpty()) "그룹을 선택하세요" else selectedGroups.joinToString(", "),
                    fontFamily = pretendardMedium,
                    fontSize = 16.sp,
                    color = if (selectedGroups.isEmpty()) Color(0xFF878787) else Color.Black,
                    modifier = Modifier.weight(1f)
                )
                
                // 플러스 버튼 (그룹이 선택되지 않았을 때만 표시)
                if (selectedGroups.isEmpty()) {
                    IconButton(
                        onClick = onGroupClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "그룹 추가",
                            tint = Color(0xFF4C3924),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelectionDialog(
    selectedGroups: Set<Int>,
    onGroupToggle: (Int) -> Unit,
    onDismiss: () -> Unit,
    availableGroups: List<GroupItem>
) {
    AlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 제목
                Text(
                    text = "그룹 목록",
                    fontFamily = pretendardMedium,
                    fontSize = 22.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (availableGroups.isEmpty()) {
                    // 등록된 그룹이 없는 경우
                    Text(
                        text = "현재 등록된 그룹이 없습니다",
                        fontFamily = pretendardRegular,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                } else {
                    // 그룹 목록 - GroupItem 전체 사용
                    availableGroups.forEach { groupItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGroupToggle(groupItem.groupId) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = groupItem.name,
                                fontFamily = pretendardRegular,
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                            Icon(
                                painter = painterResource(
                                    id = if (selectedGroups.contains(groupItem.groupId)) {
                                        R.drawable.ic_check_box
                                    } else {
                                        R.drawable.ic_check_box_outline_blank
                                    }
                                ),
                                contentDescription = if (selectedGroups.contains(groupItem.groupId)) "선택됨" else "선택되지 않음",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 확인 버튼
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4C3924)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "확인",
                        fontFamily = pretendardMedium,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EditableMemoField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = label,
            fontFamily = pretendardRegular,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 53.dp)
                .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
                .padding(12.dp),
            textStyle = TextStyle(
                color = if (value.isNotBlank()) Color.Black else Color(0xFF878787),
                fontSize = 16.sp,
                fontFamily = pretendardMedium
            ),
            singleLine = false,
            maxLines = Int.MAX_VALUE,
            keyboardOptions = KeyboardOptions.Default,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "내용을 입력하세요",
                            color = Color(0xFF878787),
                            fontSize = 16.sp,
                            fontFamily = pretendardMedium
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}



// 삭제 확인 다이얼로그
@Composable
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "명함 삭제",
                fontFamily = pretendardMedium,
                fontSize = 18.sp,
                color = Color.Black
            )
        },
        text = {
            Text(
                text = "정말로 이 명함을 삭제하시겠습니까?\n삭제된 명함은 복구할 수 없습니다.",
                fontFamily = pretendardRegular,
                fontSize = 16.sp,
                color = Color.Black
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text(
                    text = "삭제",
                    fontFamily = pretendardMedium,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "취소",
                    fontFamily = pretendardMedium,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    )
}

@Composable
fun EditableTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = label,
            fontFamily = pretendardRegular,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                if (label == "전화번호") {
                    // 전화번호는 숫자만 허용
                    onValueChange(newValue.filter { it.isDigit() })
                } else {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                .padding(12.dp),
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = pretendardMedium
            ),
            singleLine = true
        )
    }
}

// 공유 기능 관련 컴포넌트
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheetWrapper(
    showShareSheet: Boolean,
    onDismiss: () -> Unit,
    imageUrl: String
) {
    if (showShareSheet) {
        val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()
        
        ShareBottomSheet(
            sheetState = shareSheetState,
            scope = coroutineScope,
            onDismiss = onDismiss,
            shareLink = "https://test.cardlink.com/preview/12345",
            imageUrl = imageUrl
        )
    }
}

// 전화/문자/이메일 버튼 컴포넌트
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactButtons(
    cardData: Map<String, String>,
    onPhoneClick: () -> Unit,
    onMessageClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(
            Triple(R.drawable.ic_call, "전화", onPhoneClick),
            Triple(R.drawable.ic_message, "메시지", onMessageClick),
            Triple(R.drawable.ic_email, "이메일", onEmailClick)
        ).forEach { (iconRes, desc, onClick) ->
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp)
                    .clickable { onClick() },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F3ED))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = desc,
                        tint = Color(0xFF4C3924),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
