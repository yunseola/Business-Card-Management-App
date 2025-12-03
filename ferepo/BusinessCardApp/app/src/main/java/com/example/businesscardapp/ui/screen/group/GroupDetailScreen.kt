package com.example.businesscardapp.ui.screen.group

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.pretendardLight
import com.example.businesscardapp.ui.theme.pretendardMedium
import com.example.businesscardapp.ui.viewmodel.GroupDetailViewModel
import kotlinx.coroutines.delay

@Composable
fun GroupDetailScreen(
    navController: NavController,
    groupId: Int,
    groupName: String,
    viewModel: GroupDetailViewModel = viewModel()
) {
    val members by viewModel.members.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(groupId) {
        viewModel.loadGroupMembers(groupId)
    }

    // ✅ 입력할 때마다 자동 검색 (디바운스 250ms)
    LaunchedEffect(searchText) {
        delay(250)
        viewModel.searchMembers(searchText)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.White,
                elevation = 0.dp,
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "뒤로",
                                tint = Color.Black
                            )
                        }
                        Text(
                            text = groupName,
                            fontSize = 22.sp,
                            fontFamily = pretendardMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("group_member_edit/$groupId/$groupName") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "수정",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // 🔎 검색창 (UI 동일)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                backgroundColor = Color(0xFFF6F3ED),
                elevation = 0.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 검색 입력
                    BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it }, // ← 입력 시 상태만 갱신 (검색은 위 LaunchedEffect가 수행)
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            fontFamily = pretendardMedium,
                            color = Color(0xFF4C3924),
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .padding(horizontal = 4.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (searchText.isEmpty()) {
                                    Text(
                                        text = "검색어를 입력하세요",
                                        fontSize = 14.sp,
                                        fontFamily = pretendardMedium,
                                        color = Color(0xFFC6B9A4),
                                        lineHeight = 25.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    // 기존 돋보기 아이콘은 그대로 두되, 클릭해도 동일하게 동작
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "검색",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                viewModel.searchMembers(searchText)
                            }
                    )
                }
            }

            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                !error.isNullOrEmpty() -> {
                    // 요구사항: 에러만 표시, 평소엔 아무 문구 X
                    Text(
                        text = error ?: "에러",
                        fontSize = 16.sp,
                        fontFamily = pretendardMedium,
                        color = Color.Red,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                members.isEmpty() -> {
                    // 아무 글자도 표시하지 않음
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(members, key = { it.id }) { card ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFC6B9A4), RoundedCornerShape(8.dp))
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        navController.navigate("card_detail/${card.id}")
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = card.name,
                                            fontFamily = pretendardMedium,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = card.position ?: "",
                                            fontFamily = pretendardLight,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Text(
                                        text = card.company,
                                        fontFamily = pretendardLight,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(Modifier.height(9.dp))

                                    Text(
                                        text = card.phone,
                                        fontFamily = pretendardLight,
                                        fontSize = 12.sp,
                                        color = Color.Black
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Box(
                                    modifier = Modifier
                                        .size(width = 180.dp, height = 100.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE0E0E0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = card.imageUrl,
                                        contentDescription = "명함 이미지",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop,
                                        onSuccess = {
                                            Log.d("GroupDetail", "이미지 로딩 성공: ${card.imageUrl}")
                                        },
                                        onError = { err ->
                                            Log.e("GroupDetail", "이미지 로딩 실패", err.result.throwable)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
