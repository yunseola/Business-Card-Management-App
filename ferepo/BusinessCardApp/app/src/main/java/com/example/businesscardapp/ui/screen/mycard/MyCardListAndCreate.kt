package com.example.businesscardapp.ui.screen.mycard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.FoundationPagerIndicator
import com.example.businesscardapp.ui.component.DigitalPreviewCard



@Composable
fun MyCardListAndCreateScreen(
    navController: NavController,
    cards: List<MyCard>,
    onCreateClick: () -> Unit,
    onDetailClick: (Int) -> Unit


) {
    var showPicker by remember { mutableStateOf(false) }
    val pageCount = cards.size + 1
    val pagerState = rememberPagerState(pageCount = { pageCount }, initialPage = 0)

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { page ->
            if (page < cards.size) {
                val card = cards[page]
                val canOpenDetail = card.serverId != null

                Box(
                    Modifier
                        .fillMaxSize()
                        .let { mod ->
                            if (canOpenDetail) mod.clickable { onDetailClick(card.serverId!!) }
                            else mod // 서버 ID 없으면 상세 진입 막기(원하면 토스트 처리)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp)
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .aspectRatio(5f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = card.imageUrlV,
                            contentDescription = "내 명함 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().clickable { showPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mycard_create),
                        contentDescription = "새 명함 만들기",
                        tint = Color(0xFF4C3924),
                        modifier = Modifier.size(96.dp)
                    )
                }
            }
        }

        FoundationPagerIndicator(
            totalDots = pageCount,
            selectedIndex = pagerState.currentPage,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
        )

        Spacer(Modifier.height(12.dp))
    }

    // ⬇ 팝업(카메라/앨범/수기) – CreateScreen과 동일 동작
    if (showPicker) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPicker = false }) {
            Box(Modifier.fillMaxSize().padding(bottom = 65.dp), contentAlignment = Alignment.Center) {
                MyCardAddDialog(
                    onDismiss = { showPicker = false },
                    onCamera  = {
                        showPicker = false
                        navController.navigate("camera?from=mycard")
                    },
                    onAlbum   = {
                        showPicker = false
                        navController.navigate("album_image_picker?max=2&from=ocr_mycard")
                    },
                    onManual  = {
                        showPicker = false
                        navController.navigate("my_card_create/edit")
                    }
                )
            }
        }
    }
}