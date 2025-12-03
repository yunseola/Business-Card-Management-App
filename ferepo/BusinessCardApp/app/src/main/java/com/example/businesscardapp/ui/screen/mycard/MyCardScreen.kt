// MyCardScreen.kt
package com.example.businesscardapp.ui.screen.mycard

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MyCardScreen(
    navController: NavHostController,
    myCardVm: MyCardViewModel        // ✅ Main/NavGraph에서 주입된 동일 인스턴스 사용
) {
    // 목록 상태
    val cards by myCardVm.cards.collectAsState()

    // 최초 진입 시 1회 로드
    LaunchedEffect(Unit) {
        myCardVm.refreshMyCards()
    }

    // 생성/편집/삭제 화면에서 보낸 "refresh_my_cards" 신호를 구독해 목록 새로고침
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(navController, lifecycleOwner) {
        // 보통 신호는 "main" 엔트리에 넣으므로 우선 거기서 구독,
        // 혹시 없으면 현재 엔트리로 폴백
        val backEntry = runCatching { navController.getBackStackEntry("main") }
            .getOrElse { navController.currentBackStackEntry!! }

        val live = backEntry.savedStateHandle.getLiveData<Boolean>("refresh_my_cards")
        val observer = Observer<Boolean> { need ->
            if (need == true) {
                myCardVm.refreshMyCards()
                backEntry.savedStateHandle.remove<Boolean>("refresh_my_cards")
            }
        }
        live.observe(lifecycleOwner, observer)
        onDispose { live.removeObserver(observer) }
    }

    // 앨범/카메라에서 되돌아오며 저장해 둔 picked_uri 관찰
    val backStackEntry by navController.currentBackStackEntryAsState()
    val pickedUriStrFlow = remember(backStackEntry) {
        backStackEntry?.savedStateHandle?.getStateFlow("picked_uri", "")
    }
    val pickedUriStr by (pickedUriStrFlow?.collectAsState() ?: remember { mutableStateOf("") })
    val pickedUri: Uri? = pickedUriStr.takeIf { it.isNotBlank() }?.let(Uri::parse)

    // (선택) 한 번 썼으면 비워주기
    LaunchedEffect(pickedUri) {
        if (pickedUri != null) {
            backStackEntry?.savedStateHandle?.remove<String>("picked_uri")
        }
    }

    if (cards.isEmpty()) {
        // 명함이 없을 경우: 생성 화면
        MyCardCreateScreen(
            navController = navController,
            onSave = { navController.navigate("my_card_create") },
            pickedUri = pickedUri,
            myCardViewModel = myCardVm
        )
    } else {
        // 명함이 있을 경우: 리스트+생성 화면
        MyCardListAndCreateScreen(
            navController = navController,
            cards = cards,
            onCreateClick = { navController.navigate("my_card_create/edit") },
            onDetailClick = { id -> navController.navigate("my_card_detail/$id") }
        )
    }
}
