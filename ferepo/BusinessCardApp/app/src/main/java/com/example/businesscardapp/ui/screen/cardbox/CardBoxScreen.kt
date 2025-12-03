package com.example.businesscardapp.ui.screen.cardbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.businesscardapp.data.model.Card
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.layout.ContentScale
import com.example.businesscardapp.ui.viewmodel.CardBoxViewModel
import coil.compose.AsyncImage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CardBoxScreen(
    navController: NavHostController,
    viewModel: CardBoxViewModel = viewModel()
) {
    val cardList by viewModel.cardList.collectAsState()

    var selectedFilter by remember { mutableStateOf("전체") }
    var showDropdown by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val filterOptions = listOf("전체", "이름", "회사", "직책")

    var selectedSort by remember { mutableStateOf("이름순") }
    var showSortDropdown by remember { mutableStateOf(false) }

    fun String.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
    }

    val filteredSortedCards = remember(cardList, searchText, selectedFilter, selectedSort) {
        // 1) 필터/검색
        val filtered = cardList.filter { card ->
            when (selectedFilter) {
                "이름" -> card.name.contains(searchText, ignoreCase = true)
                "회사" -> card.company.contains(searchText, ignoreCase = true)
                "직책" -> card.position?.contains(searchText, ignoreCase = true) ?: false
                else -> listOf(card.name, card.company, card.position ?: "")
                    .any { it.contains(searchText, ignoreCase = true) }
            }
        }

        // 2) 기존 정렬 로직 유지
        val baseSorted = when (selectedSort) {
            "이름순" -> filtered.sortedBy { it.name }                 // 필요하면 it.name.lowercase()
            "회사명순" -> filtered.sortedBy { it.company }             // 필요하면 it.company.lowercase()
            "등록일 최신순" -> filtered.sortedByDescending { it.createdAt.toLocalDateTime() }
            "등록일 오래된순" -> filtered.sortedBy { it.createdAt.toLocalDateTime() }
            "명함 업데이트순" -> filtered.sortedByDescending { it.updatedAt.toLocalDateTime() }
            else -> filtered
        }

        // 3) 즐겨찾기만 상단 배치 (각 그룹 내 기존 정렬 유지)
        val (favorites, normals) = baseSorted.partition { it.isFavorite }
        favorites + normals
    }

    Log.d("CardBoxScreen", "LaunchedEffect 실행됨")
    LaunchedEffect(Unit) {
        viewModel.loadCards()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "명함.zip",
                        fontSize = 22.sp,
                        fontFamily = pretendardMedium
                    )
                },
                backgroundColor = Color.White,
                elevation = 0.dp,
                modifier = Modifier.statusBarsPadding(),
                actions = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notification),
                        contentDescription = "알림",
                        tint = Color.Black,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(24.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                // 알림 버튼 클릭 시 동작
                            }
                    )
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White)
            ) {
                // 검색창
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
                        // 필터 선택
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    showDropdown = true
                                }
                            ) {
                                Text(
                                    text = selectedFilter,
                                    fontSize = 14.sp,
                                    fontFamily = pretendardMedium,
                                    color = Color(0xFF4C3924)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_cardbox_list),
                                    contentDescription = "검색 필터",
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showDropdown,
                                onDismissRequest = { showDropdown = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                filterOptions.forEach { option ->
                                    DropdownMenuItem(
                                        onClick = {
                                            selectedFilter = option
                                            showDropdown = false
                                        }
                                    ) {
                                        Text(
                                            text = option,
                                            fontFamily = pretendardMedium,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // 검색창
                        BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
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

                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "검색",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                // 클릭 동작
                            }
                        )
                    }
                }

                // 정렬 선택
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            showSortDropdown = true
                        }
                    ) {
                        Text(
                            text = selectedSort,
                            fontSize = 14.sp,
                            fontFamily = pretendardMedium,
                            color = Color(0xFF4C3924)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cardbox_list),
                            contentDescription = "정렬 기준 선택",
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortDropdown,
                        onDismissRequest = { showSortDropdown = false },
                        modifier = Modifier
                            .wrapContentWidth()
                            .background(Color.White),
                        offset = DpOffset(x = 200.dp, y = 0.dp)
                    ) {
                        listOf("이름순", "회사명순", "등록일 최신순", "등록일 오래된순", "명함 업데이트순").forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedSort = option
                                    showSortDropdown = false
                                }
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 14.sp,
                                    fontFamily = pretendardMedium,
                                    color = Color(0xFF4C3924)
                                )
                            }
                        }
                    }
                }

                // 명함 목록 or 비어있는 메시지
                if (cardList.isEmpty()) {
                    Text(
                        text = "저장된 명함이 없습니다",
                        fontSize = 16.sp,
                        fontFamily = pretendardMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(filteredSortedCards) { card ->
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
                                        if (card.isDigital) {
                                            // 디지털 명함 상세 - 즐겨찾기 상태 전달
                                            navController.navigate("digital_card_detail/${card.cardId}?isFavorite=${card.isFavorite}")
                                        } else {
                                            // 종이 명함 상세 - 즐겨찾기 상태 전달
                                            navController.navigate("card_detail/${card.cardId}?isFavorite=${card.isFavorite}")
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = card.name,
                                            fontFamily = pretendardMedium,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = card.position ?: "",   // ← ✅ 이름 옆엔 직책
                                            fontFamily = pretendardLight,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = card.company,
                                        fontFamily = pretendardLight,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(9.dp))

                                    Text(
                                        text = card.phone,
                                        fontFamily = pretendardLight,
                                        fontSize = 12.sp,
                                        color = Color.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

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
                                            Log.d("CardBoxScreen", "이미지 로딩 성공: ${card.imageUrl}")
                                        },
                                        onError = { error ->
                                            Log.e("CardBoxScreen", "이미지 로딩 실패", error.result.throwable)
                                        }
                                    )

                                    // ⭐ 즐겨찾기 표시
                                    if (card.isFavorite) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "즐겨찾기",
                                            tint = Color.Yellow,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}