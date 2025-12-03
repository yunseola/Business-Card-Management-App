package com.example.businesscardapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.viewmodel.MyCardsShakeViewModel

// ✅ 변경: 선택 모드용 파라미터 추가
@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun MyCardsShakeScreen(
    viewModel: MyCardsShakeViewModel,
    selectOnly: Boolean = false,                 // ★ 추가: true면 QR 뷰 제거 + 탭 시 선택
    onPick: ((Int) -> Unit)? = null,
    startCardId: Int? = null,
    forceQr: Boolean = false
) {
    val cards by viewModel.cards.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    // 기존 상태
    var showQr by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { viewModel.load() }

    if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
    }

    // ✅ 선택 모드면 항상 카드뷰만 보여주고, QR 분기 자체를 없앰
    val onlyCards = selectOnly || cards.isEmpty()

    if (onlyCards || !showQr) {
        // ====== 카드 뷰 (풀스크린) ======
        if (cards.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("표시할 디지털 명함이 없습니다.")
            }
        } else {
            val pagerState = rememberPagerState(initialPage = selectedIndex) { cards.size }
            LaunchedEffect(pagerState.currentPage) { selectedIndex = pagerState.currentPage }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                ) { page ->
                    val card = cards[page]
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = card.imageUrlVertical,
                            contentDescription = "디지털 명함",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(9f / 16f)
                                .clip(MaterialTheme.shapes.large)
                                .clickable {
                                    if (selectOnly) {
                                        onPick?.invoke(card.id)
                                    } else {
                                        // 기존 동작: QR 보기
                                        showQr = true
                                    }
                                }
                        )
                        Spacer(Modifier.height(12.dp))
                        if (!selectOnly) {
                            Text(
                                "탭하면 QR 보기",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "이 명함을 선택하려면 탭하세요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                PagerDots(total = cards.size, current = selectedIndex, dotSize = 6.dp, gap = 8.dp)
                Spacer(Modifier.height(16.dp))
            }
        }
        // ✅ selectOnly일 땐 여기서 return해도 되지만, 가독성 위해 else QR블록이 동작 안 함
        if (selectOnly) return
    }

    // ====== (선택 모드가 아닐 때만) QR 뷰 ======
    if (!selectOnly) {
        val count = cards.size
        val pagerState = rememberPagerState(initialPage = selectedIndex) { count }
        LaunchedEffect(pagerState.currentPage) { selectedIndex = pagerState.currentPage }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("QR Code", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = { showQr = false }) {
                            Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "뒤로가기")
                        }
                    }
                )
            }
        ) { inner ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                ) { page ->
                    val qr = cards[page].qrImageUrl
                    if (qr.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("QR 이미지가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        AsyncImage(
                            model = qr,
                            contentDescription = "QR",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                PagerDots(total = count, current = selectedIndex)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}


/** 하단 도트 인디케이터 */
@Composable
private fun PagerDots(
    total: Int,
    current: Int,
    dotSize: Dp = 6.dp,
    gap: Dp = 8.dp
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(total) { i ->
            val active = i == current
            Box(
                modifier = Modifier
                    .size(if (active) dotSize + 2.dp else dotSize)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
            )
            if (i != total - 1) Spacer(Modifier.width(gap))
        }
    }
}
