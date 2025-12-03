package com.example.businesscardapp.ui.screen.group

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.data.model.GroupItem
import com.example.businesscardapp.ui.theme.pretendardRegular
import com.example.businesscardapp.ui.viewmodel.GroupViewModel
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupEditScreen(
    navController: NavController,
    viewModel: GroupViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        Log.d("GroupEditScreen", "🟢 composed") // ✅ 화면이 실제로 그려졌는지 확인
    }
    val groupList by viewModel.groupList.collectAsState()
    val scope = rememberCoroutineScope()

    // ✅ groupId -> 수정된 이름 버퍼
    val edited = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(Unit) { viewModel.fetchGroups() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("그룹", fontFamily = pretendardRegular, fontSize = 20.sp, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painterResource(id = R.drawable.ic_back), null, tint = Color.Black)
                    }
                },
                actions = {
                    // GroupEditScreen.kt 의 TopAppBar actions 내부
                    // TopAppBar actions 안
                    val context = LocalContext.current

                    TextButton(onClick = {
                        Toast.makeText(context, "완료 클릭!", Toast.LENGTH_SHORT).show() // ✅ 눈으로 보이는 피드백
                        scope.launch {
                            Log.d("GroupEditScreen", "✅ 완료 버튼 클릭됨, edited=$edited")
                            if (edited.isEmpty()) {
                                Log.w("GroupEditScreen", "⚠️ 변경된 항목 없음")
                                navController.popBackStack()
                                return@launch
                            }
                            viewModel.commitGroupEdits(originals = groupList, edits = edited)
                            Log.d("GroupEditScreen", "✅ commitGroupEdits 완료 → fetchGroups")
                            viewModel.fetchGroups()
                            Log.d("GroupEditScreen", "✅ fetchGroups 완료 → popBackStack")
                            navController.popBackStack()
                        }
                    }) {
                        Text("완료", fontFamily = pretendardRegular, fontSize = 20.sp, color = Color.Black)
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize().background(Color.White),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(groupList, key = { it.groupId }) { group ->
                val shown = edited[group.groupId] ?: group.name // ✅ 입력값이 있으면 그것을, 없으면 원래 이름
                GroupEditItem(
                    group = group,
                    name = shown,                                 // ✅ 표시값 전달
                    onNameChange = { new -> edited[group.groupId] = new }, // ✅ 타이핑 시 버퍼에만 저장
                    onDeleteClick = { viewModel.deleteGroupById(group.groupId) }
                )
            }
        }
    }
}

@Composable
fun DeleteGroupDialog(
    groupName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFF2EFE9), shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 34.dp)
        ) {
            // 제목 (가운데 정렬) — 디자인 동일
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "그룹을 삭제하시겠어요?",
                    fontFamily = pretendardRegular,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 취소/삭제 버튼 (오른쪽 정렬) — 디자인 동일
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "취소",
                        fontFamily = pretendardRegular,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = onConfirm) {
                    Text(
                        text = "삭제",
                        fontFamily = pretendardRegular,
                        fontSize = 16.sp,
                        color = Color.Black// 강조만 다르게(원문과 동일 레이아웃)
                    )
                }
            }
        }
    }
}

@Composable
fun GroupEditItem(
    group: GroupItem,
    name: String,                      // ✅ 표시할 이름
    onNameChange: (String) -> Unit,    // ✅ 이름 변경 시 호출
    onDeleteClick: () -> Unit
) {
    var editedName by remember { mutableStateOf(group.name) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteGroupDialog(
            groupName = group.name,
            onDismiss = { showDeleteDialog = false },
            onConfirm  = {
                showDeleteDialog = false
                onDeleteClick()
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ 회색 박스(항상 수정 가능)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .background(Color(0xFFF2F2F2), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = name,
                onValueChange = onNameChange, // ✅ 버퍼만 업데이트
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = pretendardRegular,
                    fontSize = 20.sp,
                    color = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.width(12.dp))

        // ✅ 삭제 버튼 (박스 밖)
        Icon(
            painter = painterResource(id = R.drawable.ic_group_delete),
            contentDescription = "삭제",
            modifier = Modifier
                .size(22.dp)
                .clickable { showDeleteDialog = true },
            tint = Color.Red
        )
    }
}