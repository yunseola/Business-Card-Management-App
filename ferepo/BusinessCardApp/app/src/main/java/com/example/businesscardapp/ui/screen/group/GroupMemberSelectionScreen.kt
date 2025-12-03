package com.example.businesscardapp.ui.screen.group

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.businesscardapp.R
import com.example.businesscardapp.data.model.Card
import com.example.businesscardapp.ui.theme.pretendardLight
import com.example.businesscardapp.ui.theme.pretendardMedium
import com.example.businesscardapp.ui.viewmodel.GroupMemberEditViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GroupMemberEditScreen(
    navController: NavController,
    groupId: Int,
    groupName: String,
    viewModel: GroupMemberEditViewModel = viewModel()
) {
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val allCards by viewModel.allCards.collectAsState()
    val selected by viewModel.selected.collectAsState()

    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(groupId) { viewModel.load(groupId) }

    // ✅ 입력할 때마다 필터링 + 선택된 멤버를 상단으로 정렬
    val filtered = remember(allCards, searchText, selected) {
        val base = if (searchText.isBlank()) {
            allCards
        } else {
            allCards.filter { c ->
                c.name.contains(searchText, ignoreCase = true) ||
                        c.company.contains(searchText, ignoreCase = true) ||
                        (c.position ?: "").contains(searchText, ignoreCase = true)
            }
        }
        // 선택(checked) 여부를 기준으로 내림차순 정렬 → 선택된 카드가 위로
        base.sortedWith(
            compareByDescending<Card> { selected.contains(it.cardId) }
                .thenBy { it.name } // 동순위는 이름 오름차순(원하면 다른 기준으로 바꿔도 OK)
        )
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
                    TextButton(
                        onClick = {
                            viewModel.save(groupId) { ok ->
                                if (ok) navController.popBackStack()
                            }
                        }
                    ) {
                        Text("완료", fontSize = 16.sp, color = Color.Black, fontFamily = pretendardMedium)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Color.White)
        ) {
            // 검색창 (UI 동일)
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
                    BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            fontFamily = pretendardMedium,
                            color = Color(0xFF4C3924)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        decorationBox = { innerField ->
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                                if (searchText.isEmpty()) {
                                    Text(
                                        text = "검색어를 입력하세요",
                                        fontSize = 14.sp,
                                        fontFamily = pretendardMedium,
                                        color = Color(0xFFC6B9A4)
                                    )
                                }
                                innerField()
                            }
                        }
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "검색",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                /* 별도 트리거 없이 입력 즉시 필터링되므로 클릭 이벤트 없음 */
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
                    Text(
                        text = error ?: "에러",
                        color = Color.Red,
                        fontFamily = pretendardMedium,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(filtered, key = { it.cardId }) { card ->
                            MemberRow(
                                card = card,
                                checked = selected.contains(card.cardId),
                                onToggle = { viewModel.toggle(card.cardId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(
    card: Card,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ 박스 바깥 체크박스
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF4C3924),
                uncheckedColor = Color(0xFF4C3924)
            ),
            modifier = Modifier.padding(end = 8.dp)
        )

        // ✅ 내용 박스
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFC6B9A4), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
                    .size(width = 100.dp, height = 60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = card.imageUrl,
                    contentDescription = "명함 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
