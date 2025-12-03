package com.example.businesscardapp.ui.screen.group

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.data.model.GroupItem
import com.example.businesscardapp.ui.theme.*
import com.example.businesscardapp.ui.viewmodel.GroupViewModel
import androidx.compose.ui.window.Dialog
import android.net.Uri


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    navController: NavController,
    viewModel: GroupViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var showEditMode by remember { mutableStateOf(false) }

    val groupState by viewModel.groupList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchGroups()
    }

    val filteredGroups = groupState.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("그룹", fontFamily = pretendardMedium, fontSize = 20.sp, color = Color.Black)
                },
                actions = {
                    IconButton(onClick = {
                        Log.d("GroupScreen", "➕ 그룹 추가 버튼 클릭됨")
                        showAddGroupDialog = true }) {
                        Icon(painter = painterResource(id = R.drawable.ic_group_add), contentDescription = "그룹 추가", tint = Color.Black)
                    }
                    IconButton(onClick = {
                        Log.d("GroupScreen", "Navigating to group_edit")
                        navController.navigate("group_edit")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "편집",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Color.White)) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(56.dp),
                placeholder = {
                    Text("검색어를 입력하세요", fontFamily = pretendardRegular, fontSize = 14.sp, color = Color(0xFFC6B9A4))
                },
                trailingIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_search), contentDescription = "검색", tint = Color(0xFF4C3624))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF6F3ED),
                    unfocusedContainerColor = Color(0xFFF6F3ED)
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = pretendardRegular, fontSize = 14.sp, color = Color(0xFF4C3924))
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(filteredGroups) { index, group ->
                    GroupItem(
                        group = group,
                        showEditMode = showEditMode,
                        onGroupClick = { navController.navigate("GroupDetail/${group.groupId}/${Uri.encode(group.name)}") },
                        onDeleteClick = { /* 삭제 로직 */ }
                    )

                    // 그룹이 여러 개일 경우 항목 사이에 Divider 추가
                    if (index < filteredGroups.lastIndex) {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 4.dp),
                            color = Color(0xFFC6B9A4),
                            thickness = 1.dp
                        )
                    }
                }
            }

            if (showAddGroupDialog) {
                AddGroupDialog(
                    onDismiss = { showAddGroupDialog = false },
                    onConfirm = { groupName ->
                        viewModel.createGroup(groupName)  // 💡 그룹 생성 요청
                        showAddGroupDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun GroupItem(
    group: GroupItem,
    showEditMode: Boolean,
    onGroupClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = group.name,
            fontSize = 20.sp,
            fontFamily = pretendardRegular,
            color = Color.Black,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            modifier = Modifier
                .padding(top = 20.dp, bottom = 20.dp)
                .clickable (
                    indication = null, // 👈 Ripple 제거
                    interactionSource = remember { MutableInteractionSource() }
                ) { onGroupClick() } // ← 여기서 콜백 호출
        )
        Spacer(Modifier.width(20.dp))
        Text(
            text = "(${group.headcount})",
            fontSize = 18.sp,
            fontFamily = pretendardRegular,
            color = Color.Gray,
            letterSpacing = 0.25.sp,
            maxLines = 1,
            modifier = Modifier
                .padding(top = 20.dp, bottom = 20.dp)
                .clickable (
                    indication = null, // 👈 Ripple 제거
                    interactionSource = remember { MutableInteractionSource() }
                ) { onGroupClick() } // ← 여기서 콜백 호출
        )
    }

    if (showEditMode) {
        IconButton(onClick = onDeleteClick) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_delete),
                contentDescription = "삭제",
                tint = Color.Red
            )
        }
    }
}

@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFF2EFE9), shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 34.dp)
        ) {
            // ⬇ 제목 텍스트 (가운데 정렬)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "생성할 그룹의 이름을 작성해주세요",
                    fontFamily = pretendardRegular,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ⬇ 그룹명 입력 필드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = pretendardRegular,
                        fontSize = 14.sp,
                        color = Color.Black
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ⬇ 취소 및 생성 버튼 (오른쪽 정렬 + 여백)
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

                TextButton(
                    onClick = { onConfirm(groupName) },
                    enabled = groupName.isNotBlank()
                ) {
                    Text(
                        text = "생성",
                        fontFamily = pretendardRegular,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}


