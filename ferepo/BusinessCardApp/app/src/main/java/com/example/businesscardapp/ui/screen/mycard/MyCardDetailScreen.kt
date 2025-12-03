@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.businesscardapp.ui.screen.mycard

import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.CardOrientation
import com.example.businesscardapp.ui.component.ShareAction
import com.example.businesscardapp.ui.component.ShareBottomSheet
import com.example.businesscardapp.ui.component.ShareItem
import com.example.businesscardapp.ui.theme.pretendardRegular
import com.example.businesscardapp.util.shareCardImage
import kotlinx.coroutines.launch

private fun toAbsoluteUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    return if (path.startsWith("http://") || path.startsWith("https://")) path
    else "https://i13e201.p.ssafy.io$path"
}

@Composable
fun MyCardDetailScreen(
    navController: NavController,
    cardId: Int,
    viewModel: MyCardViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // 상세 원본
    val result = (detailState as? MyCardViewModel.DetailState.Success)?.data?.result
    val profileUrl = toAbsoluteUrl(result?.customImageUrl)

    LaunchedEffect(cardId) { viewModel.fetchMyCardDetail(cardId) }

    LaunchedEffect(detailState) {
        val st = viewModel.detailState.value
        if (st is MyCardViewModel.DetailState.Fail && st.code in listOf(404, 410)) {
            // 목록 갱신 신호 + 빠져나오기
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_my_cards", true)
            Toast.makeText(context, "이미 삭제된 명함입니다.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    LaunchedEffect(result?.customImageUrl) {
        android.util.Log.d("Detail", "customImageUrl(raw)=${result?.customImageUrl}")
    }

    // UI용 맵핑 (Info 탭에서 사용)
    val ui: MyCard? = result?.let { r ->
        MyCard(
            name = r.name,
            phone = r.phone,
            company = r.company,
            imageUrlH = toAbsoluteUrl(r.imageUrlHorizontal),
            imageUrlV = toAbsoluteUrl(r.imageUrlVertical ?: r.imageUrlHorizontal),
            profileUrl = profileUrl,
            backgroundImageUrl = "#FFFFFF",
            fontColor = !r.fontColor,
            isConfirm = r.confirm,
            createAt = r.createdAt,
            fields = r.fields.map { UiMyCardField(it.fieldName, it.fieldValue, it.fieldOrder) }
        )
    }

    // 이미지 URL (세로 우선 폴백)
    val imageV = remember(result) { toAbsoluteUrl(result?.imageUrlVertical) }
    val imageH = remember(result) { toAbsoluteUrl(result?.imageUrlHorizontal) }
    val previewUrl = imageH ?: imageV                   // ✅ 가로 우선
    if (imageH != null)               // ✅ 가로/세로 자동 설정
        CardOrientation.Landscape else CardOrientation.Portrait

    // 공유 바텀시트 상태
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showShare by remember { mutableStateOf(false) }
    val shareLink = remember(cardId) { "https://i13e201.p.ssafy.io/cards/$cardId" }

    val sharePreviewUrl = imageV ?: imageH      // 공유/다운로드는 세로 우선 유지
    imageH ?: imageV      // 비율 측정은 가로 우선!

    sharePreviewUrl

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    // MyCardDetailScreen.kt - 뒤로가기 아이콘 onClick
                    IconButton(onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("force_fresh_create_token", System.currentTimeMillis())
                        navController.popBackStack()
                    }) {
                        Icon(painterResource(id = R.drawable.ic_back), contentDescription = "뒤로가기")
                    }

                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.deleteMyCard(cardId) { ok, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (ok) {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("refresh_my_cards", true)
                                    navController.popBackStack()
                                }
                            }
                        }


                    ) {
                        Icon(painterResource(id = R.drawable.ic_delete), contentDescription = "삭제")
                    }

                    IconButton(onClick = {
                        val url = previewUrl
                        if (url.isNullOrBlank()) {
                            Toast.makeText(context, "다운로드할 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        scope.launch {
                            // TODO: 실제 저장 구현
                            Toast.makeText(context, "다운로드 준비 중...", Toast.LENGTH_SHORT).show()
                        }
                    }) { Icon(painterResource(id = R.drawable.ic_download), null) }

                    IconButton(onClick = {
                        val url = previewUrl
                        if (url.isNullOrBlank()) {
                            Toast.makeText(context, "공유할 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        scope.launch {
                            try {
                                val loader = ImageLoader(context)
                                val req = ImageRequest.Builder(context)
                                    .data(url)
                                    .allowHardware(false)
                                    .build()
                                val res = loader.execute(req)
                                val bmp = (res.drawable as? BitmapDrawable)?.bitmap
                                if (bmp != null) shareCardImage(context, bmp)
                                else Toast.makeText(context, "이미지 로드 실패", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "이미지 공유 중 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) { Icon(painterResource(id = R.drawable.ic_share), null) }

                    IconButton(onClick = {
                        viewModel.detailState.value.let { st ->
                            val d = (st as? MyCardViewModel.DetailState.Success)?.data?.result ?: return@let
                            viewModel.hydrateFromDetail(d) // 편집 버튼 눌렀을 때만
                            navController.navigate(
                                "my_card_edit?mode=edit&cardId=$cardId&nonce=${System.currentTimeMillis()}"
                            ) {
                                launchSingleTop = false
                                restoreState = false
                            }
                        }
                    }) {
                        Icon(painterResource(id = R.drawable.ic_edit), contentDescription = "편집")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,          // 평소 배경
                    scrolledContainerColor = Color.White,  // 스크롤 시 배경
                    navigationIconContentColor = Color(0xFF000000),
                    actionIconContentColor = Color(0xFF000000),
                    titleContentColor = Color(0xFF000000)
                )
            )
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Color.White)
        ) {
            // 미리보기
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    result != null -> {
                        val urlForPreview = previewUrl // = imageH (가로 전용)

                        // createdAt에서 날짜만 추출 (예: "2025-08-18T12:34:56" → "2025-08-18")
                        val dateOnly = remember(result?.createdAt) {
                            (result?.createdAt ?: "").let { raw ->
                                raw.substringBefore('T').substringBefore(' ')
                            }
                        }

                        Column(Modifier.fillMaxWidth()) {
                            if (!urlForPreview.isNullOrBlank()) {
                                AsyncImage(
                                    model = urlForPreview,
                                    contentDescription = "명함 가로 이미지",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(9f / 5f)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            } else {
                                Text(
                                    "가로 이미지가 없습니다.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    color = Color.Gray
                                )
                            }

                            // ↓ 사진 '아래'에 날짜를 오른쪽 정렬로 표시
                            if (dateOnly.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                                ) {
                                    Text(
                                        text = dateOnly,
                                        fontSize = 12.sp,
                                        color = Color(0xFF999999)
                                    )
                                }
                            }
                        }
                    }

                    detailState is MyCardViewModel.DetailState.Loading ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))

                    else ->
                        Text("표시할 데이터가 없습니다.", Modifier.align(Alignment.Center))
                }
            }

            // 탭
            var tabIndex by remember { mutableStateOf(0) }
            val tabs = listOf("정보", "히스토리")
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF4C3924),
                divider = {
                    // 전체 밑줄
                    Divider(
                        color = Color(0xFFD6CFC7), // 연한 베이지색 등 원하는 색
                        thickness = 1.dp
                    )
                },
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[tabIndex]),
                        color = Color(0xFF4C3924), // 원하는 색으로 변경
                        height = 3.dp               // 굵기도 조절 가능
                    )
                }
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = tabIndex == i,
                        onClick = { tabIndex = i },
                        text = {
                            Text(
                                text = title,
                                fontFamily = pretendardRegular,
                                fontSize = 16.sp,
                                fontWeight = if (tabIndex == i) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (tabIndex) {
                0 -> {
                    if (ui != null) InfoContent(ui)
                    else Text("정보가 없습니다.", modifier = Modifier.padding(16.dp))
                }
                1 -> {
                    val raw = result?.companyHistories
                    when {
                        raw == null -> {
                            Column(Modifier.padding(16.dp)) {
                                Text("히스토리 API가 아직 연결되지 않았습니다.", color = Color.Gray, fontSize = 14.sp)
                                Spacer(Modifier.height(6.dp))
                                Text("백엔드 명세가 준비되는 대로 표시됩니다.", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        raw.isEmpty() -> {
                            Column(Modifier.padding(16.dp)) {
                                Text("히스토리가 없습니다.", color = Color.Gray)
                            }
                        }
                        else -> {
                            // 최신순으로 내려왔다고 가정하고, 기간 문자열을 계산
                            val uiItems = remember(raw) { buildUiHistories(raw) }

                            Column(Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                                uiItems.forEach { item ->
                                    HistoryRow(
                                        period = item.period,         // 예: 2025.07~  / 2023~2025.06 / 2022
                                        company = item.company,
                                        confirmed = item.confirmed
                                    )
                                    Spacer(Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            // (선택) 바텀시트 공유 UI는 토글 시 사용
            if (showShare) {
                val options = buildList<ShareItem> {
                    add(ShareItem(R.drawable.ic_link, "링크 복사", ShareAction.CopyLink(shareLink)))
                    add(
                        ShareItem(
                            R.drawable.ic_nfc,
                            "NFC",
                            ShareAction.Callback {
                                val url = previewUrl
                                if (url.isNullOrBlank()) {
                                    Toast.makeText(context, "공유할 명함 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                                    return@Callback
                                }
                                scope.launch {
                                    try {
                                        val loader = ImageLoader(context)
                                        val req = ImageRequest.Builder(context).data(url).allowHardware(false).build()
                                        val res = loader.execute(req)
                                        val bmp = (res.drawable as? BitmapDrawable)?.bitmap
                                        if (bmp != null) shareCardImage(context, bmp)
                                        else Toast.makeText(context, "이미지 로드 실패", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "이미지 공유 중 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    )
                    add(
                        ShareItem(
                            R.drawable.ic_qr,
                            "QR 코드",
                            ShareAction.Callback {
                                if (cardId > 0) navController.navigate("myCardsShake/qr/$cardId")
                                else Toast.makeText(context, "저장된 카드가 있어야 QR 생성이 가능해요.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    )
                    add(
                        ShareItem(
                            R.drawable.ic_kakaotalk,
                            "카카오톡",
                            ShareAction.ShareText(shareLink.ifBlank { "내 명함입니다." }, "com.kakao.talk")
                        )
                    )
                    add(
                        ShareItem(
                            R.drawable.ic_mattermost,
                            "Mattermost",
                            ShareAction.ShareText(shareLink.ifBlank { "내 명함입니다." }, "com.mattermost.rn")
                        )
                    )
                    add(
                        ShareItem(
                            R.drawable.ic_instagram,
                            "인스타그램",
                            if (!previewUrl.isNullOrBlank())
                                ShareAction.ShareImage(previewUrl, "com.instagram.android")
                            else
                                ShareAction.ShareText(shareLink.ifBlank { "내 명함입니다." }, "com.instagram.android")
                        )
                    )
                }

                ShareBottomSheet(
                    sheetState = sheetState,
                    scope = scope,
                    onDismiss = { showShare = false },
                    shareLink = shareLink,
                    imageUrl = previewUrl.orEmpty(),
                    options = options,
                    footerText = "NFC 사용 시 휴대폰을 가까이 대세요"
                )
            }
        }
    }
}

/* ─────────── 파일 최상위 보조 컴포저블들 (private OK) ─────────── */

@Composable
private fun InfoContent(card: MyCard) {
    Column(modifier = Modifier.padding(16.dp)) {
        InfoRow("이름", card.name)
        InfoRow("회사", card.company)
        InfoRow("휴대전화", card.phone)
        card.fields.forEach { InfoRow(it.fieldName, it.fieldValue) }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = pretendardRegular,
            color = Color(0xFF4C3924),
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontFamily = pretendardRegular,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

//@Composable
//private fun HistoryContent(history: List<String>) {
//    Column(modifier = Modifier.padding(16.dp)) {
//        if (history.isEmpty()) Text("히스토리가 없습니다.", color = Color.Gray)
//        else history.forEach { Text("• $it", modifier = Modifier.padding(vertical = 4.dp)) }
//    }
//}
@Composable
private fun HistoryRow(period: String, company: String, confirmed: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // 기간
        Text(
            text = period,
            fontFamily = pretendardRegular,
            fontSize = 16.sp,
            color = Color(0xFF4C3924),
            modifier = Modifier.weight(1.2f)
        )
        // 회사명
        Text(
            text = company,
            fontFamily = pretendardRegular,
            fontSize = 16.sp,
            color = Color(0xFF4C3924),
            modifier = Modifier.weight(1f)
        )
        // 인증 뱃지
        if (confirmed) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF2F80FF)) // 파란색 원형 배경
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

// ---- 기간 문자열 만들기 ----
// 백엔드 모델: companyHistories: List<{ company:String, isConfirm:Boolean, changedAt:String }>
private data class UiHistory(val period: String, val company: String, val confirmed: Boolean)

/**
 * changedAt(시작 시점)을 기준으로 내림차순이라 가정.
 * i번째 항목의 종료 시점은 (i-1)번째 항목의 month로 계산.
 * 마지막(가장 최근) 항목은 "~" 오픈 형태.
 * changedAt가 없는 경우는 회사명만 표시.
 */
private fun buildUiHistories(raw: List<Any>): List<UiHistory> {
    // 안전하게 리플렉션 없이 map 접근
    fun getCompany(o: Any) = runCatching {
        o.javaClass.getMethod("getCompany").invoke(o) as? String
    }.getOrNull().orEmpty()

    fun getConfirm(o: Any) = runCatching {
        o.javaClass.getMethod("isConfirm").invoke(o) as? Boolean
    }.getOrNull() ?: false

    fun getChangedAt(o: Any) = runCatching {
        o.javaClass.getMethod("getChangedAt").invoke(o) as? String
    }.getOrNull().orEmpty()

    // 최신순(내림차순) 정렬 가정. 아니라면 여기서 정렬 추가 가능.
    val items = raw.map { Triple(getCompany(it), getConfirm(it), getChangedAt(it)) }

    // yyyy.MM 포맷터
    fun ym(s: String): String {
        if (s.isBlank()) return ""
        val datePart = s.substringBefore('T').substringBefore(' ')
        // 기대 포맷 yyyy-MM-dd
        return if (datePart.length >= 7 && datePart[4] == '-' && datePart[7] == '-') {
            "${datePart.substring(0,4)}.${datePart.substring(5,7)}"
        } else if (datePart.length >= 7 && datePart[4] == '-' ) {
            "${datePart.substring(0,4)}.${datePart.substring(5,7)}"
        } else datePart // 안전 fallback
    }

    val result = mutableListOf<UiHistory>()
    for (i in items.indices.reversed()) {
        // 역순으로 훑어 start/end 만들고, 마지막에 다시 뒤집어 원래 최신→과거 순으로 반환
        val (company, confirm, changedAt) = items[i]
        val start = ym(changedAt)

        // end는 바로 앞(더 최근) 항목의 month
        val end = if (i < items.lastIndex) ym(items[i + 1].third) else ""

        val period = when {
            start.isBlank() && end.isBlank() -> company.takeIf { false } ?: "" // 둘 다 없으면 공란
            start.isNotBlank() && end.isBlank() -> "$start~"
            start.isNotBlank() && end.isNotBlank() -> "$start~$end"
            else -> end // 비정상 데이터 fallback
        }

        result += UiHistory(period = period.ifBlank { " " }, company = company, confirmed = confirm)
    }
    return result.asReversed()
}