// MyCardCreateScreen.kt
package com.example.businesscardapp.ui.screen.mycard

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.CardOrientation
import com.example.businesscardapp.ui.component.DigitalPreviewCard
import com.example.businesscardapp.ui.component.FoundationPagerIndicator
import com.example.businesscardapp.ui.theme.pretendardRegular
import com.example.businesscardapp.ui.theme.pretendardSemiBold



private fun goToEmptyEdit(navController: NavController, vm: MyCardViewModel) {
    vm.clearForCreate()

    navController.navigate("my_card_edit?mode=create&cardId=-1&nonce=${System.currentTimeMillis()}") {
        // ★ 과거 편집 화면(있다면)과 그 ViewModel을 백스택에서 제거
        popUpTo("my_card_edit") { inclusive = true }
        launchSingleTop = false
        restoreState = false
    }
}







@Composable
fun MyCardCreateScreen(
    navController: NavController,
    onSave: () -> Unit,
    pickedUri: Uri?,
    myCardViewModel: MyCardViewModel
) {


    val vm = myCardViewModel

    // ✅ 첫 진입 시 서버 새로고침
    LaunchedEffect(Unit) { vm.refreshMyCards() }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(navController, lifecycleOwner) {
        val entry = navController.currentBackStackEntry ?: return@DisposableEffect onDispose { }
        val handle = entry.savedStateHandle

        // 1) 목록 새로고침 신호 (기존 그대로)
        val refreshLive = handle.getLiveData<Boolean>("refresh_my_cards")
        val refreshObs = androidx.lifecycle.Observer<Boolean> { need ->
            if (need == true) {
                myCardViewModel.refreshMyCards()      // 서버 동기화 (목록 유지)
                handle["refresh_my_cards"] = false
            }
        }
        refreshLive.observe(lifecycleOwner, refreshObs)

        // 2) ★ 드래프트 초기화 신호: 상세→뒤로가기 시 세팅된 토큰 감지
        val freshLive = handle.getLiveData<Long>("force_fresh_create_token")
        val freshObs = androidx.lifecycle.Observer<Long> {
            myCardViewModel.clearForCreate()          // 드래프트만 리셋, cards는 건드리지 않음
            // 필요하면 재사용 방지용으로 무시(토큰을 굳이 지울 필요는 없음)
            // handle["force_fresh_create_token"] = 0L
        }
        freshLive.observe(lifecycleOwner, freshObs)

        onDispose {
            refreshLive.removeObserver(refreshObs)
            freshLive.removeObserver(freshObs)
        }
    }





//    // MyCardCreateScreen.kt 상단 쪽
//    val backEntry = remember(navController) {
//        runCatching { navController.getBackStackEntry("main?tab={tab}") }
//            .getOrElse { navController.getBackStackEntry("main") }
//    }
//
//    DisposableEffect(backEntry) {
//        val live = backEntry.savedStateHandle.getLiveData<Boolean>("refresh_my_cards")
//        val obs = androidx.lifecycle.Observer<Boolean> { need ->
//            if (need == true) {
//                myCardViewModel.refreshMyCards()
//                backEntry.savedStateHandle["refresh_my_cards"] = false
//            }
//        }
//        live.observeForever(obs)
//        onDispose { live.removeObserver(obs) }
//    }



    // 진입 시 목록 갱신
    LaunchedEffect(Unit) { vm.refreshMyCards() }




    val cards by vm.cards.collectAsState()
    var showPicker by remember { mutableStateOf(false) }




    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F3ED))
    ) {
        if (cards.isEmpty()) {
            // ===== 카드 0개: 중앙 큰 + =====
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mycard_create),
                    contentDescription = "디지털 명함 생성",
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            goToEmptyEdit(navController, vm)   // ← 이렇게 교체
                        },
                    tint = Color(0xFF4C3924)
                )


                Spacer(Modifier.height(24.dp))

                Text(
                    text = "디지털 명함 생성하기",
                    fontSize = 24.sp,
                    fontFamily = pretendardSemiBold,
                    color = Color.Black
                )
            }
        } else {
            // ===== 카드 ≥1: 스와이프 + 마지막 페이지는 큰 + =====
            val pageCount = cards.size + 1
            val pagerState = rememberPagerState(pageCount = { pageCount }, initialPage = 0)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Spacer(Modifier.height(12.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    if (page < cards.size) {
                        val c = cards[page]
                        val canOpenDetail = c.serverId != null

//                        val thumbUrl = c.imageUrlV ?: c.imageUrlH
//                        val hasImage = !thumbUrl.isNullOrBlank()

                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .let { mod ->
                                    if (canOpenDetail) mod.clickable { navController.navigate("my_card_detail/${c.serverId}") }
                                    else mod
                                }
                        ) {

                            android.util.Log.d("MyCardCreateScreen", "page=$page v=${c.imageUrlV}")
                            // 목록은 세로 미리보기 고정 (이미지 위주)
                            DigitalPreviewCard(
                                orientationImageUri = null,
                                orientation= CardOrientation.Portrait,
                                // VM이 상세 받아오면 세로로 교체됨
                                profileUri = c.profileUrl,
                                bgHex = "#00000000",                // 목록은 패턴/색상 대신 이미지 강조
                                patternCode = null,
                                name = c.name,
                                company = c.company,
                                phone = c.phone,
                                position = c.fields.firstOrNull { it.fieldName == "직책" }?.fieldValue.orEmpty(),
                                email = c.fields.firstOrNull { it.fieldName == "이메일" }?.fieldValue.orEmpty(),
                                extras = c.fields
                                    .filter { it.fieldName !in listOf("직책", "이메일") }
                                    .map { it.fieldName to it.fieldValue }  ,                // 목록에선 부가항목 생략
                                useDarkText = true
                            )
                        }
                    } else {
                        // 마지막 페이지: 큰 + 로 새 명함 만들기
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    goToEmptyEdit(navController, vm)
                                },
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
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
        }

        // ===== 공통: 추가 다이얼로그 =====
        if (showPicker) {
            Dialog(onDismissRequest = { showPicker = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 65.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MyCardAddDialog(
                        onDismiss = { showPicker = false },
                        onCamera = {
                            showPicker = false
                            navController.navigate("camera?from=mycard")
                        },
                        onAlbum = {
                            showPicker = false
                            navController.navigate("album_image_picker?max=2&from=ocr_mycard")
                        },
                        onManual = {
                            showPicker = false
                            goToEmptyEdit(navController, vm)
                        }
                    )
                }
            }
        }
    }
}




/* ===== 공용 ===== */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Composable
fun MyCardAddDialog(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onAlbum: () -> Unit,
    onManual: () -> Unit
) {
    androidx.compose.material.Card(
        modifier = Modifier
            .width(287.dp)
            .height(190.dp)
            .clickable(enabled = false) {},
        shape = RoundedCornerShape(4.dp),
        elevation = 8.dp,
        backgroundColor = Color(0xFFF6F3ED)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DialogItem("카메라로 촬영하기", onCamera)
            Spacer(Modifier.height(6.dp))
            DialogItem("앨범에서 선택하기", onAlbum)
            Spacer(Modifier.height(6.dp))
            DialogItem("수기로 작성하기", onManual)
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
fun DialogItem(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .noRippleClickable { onClick() }
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontFamily = pretendardRegular,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF4C3924),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
