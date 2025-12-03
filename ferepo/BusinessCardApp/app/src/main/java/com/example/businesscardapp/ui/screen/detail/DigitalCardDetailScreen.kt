package com.example.businesscardapp.ui.screen.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.businesscardapp.R
import com.example.businesscardapp.data.model.MemoRequest
import com.example.businesscardapp.ui.component.ShareBottomSheet
import com.example.businesscardapp.ui.theme.pretendardMedium
import com.example.businesscardapp.ui.theme.pretendardRegular
import com.example.businesscardapp.ui.viewmodel.DigitalCardViewModel
import com.example.businesscardapp.ui.viewmodel.GroupViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalCardDetailScreen(
    navController: NavController,
    cardId: String? = null,
    isFavoriteParam: Boolean = false  // 명함 목록에서 전달받은 즐겨찾기 상태
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val viewModel: DigitalCardViewModel = viewModel()
    val groupViewModel: GroupViewModel = viewModel()

    val digitalCardDetailResponse by viewModel.digitalCardDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isTogglingFavorite by viewModel.isTogglingFavorite.collectAsState()

    val apiGroupList by groupViewModel.groupList.collectAsState()

    var isFavorite by remember { mutableStateOf(isFavoriteParam) }  // 명함 목록에서 받아온 값으로 초기화
    var isToggling by remember { mutableStateOf(false) }
    var lastCardId by remember { mutableStateOf<String?>(null) }

    var isMemoEditMode by remember { mutableStateOf(false) }
    var editMemoRelation by remember { mutableStateOf("") }
    var editMemoTendency by remember { mutableStateOf("") }
    var editMemoWorkStyle by remember { mutableStateOf("") }
    var editMemoMeeting by remember { mutableStateOf("") }
    var editMemoEtc by remember { mutableStateOf("") }

    var selectedGroups by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    val tabs = if (isMemoEditMode) listOf("정보", "메모") else listOf("정보", "메모", "히스토리")

    var cardDataState by remember { mutableStateOf(mutableMapOf<String, String>()) }
    val cardData = cardDataState

    // 토글 완료 후 상태 리셋
    LaunchedEffect(isTogglingFavorite) {
        if (!isTogglingFavorite && isToggling) {
            isToggling = false
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, "오류: $it", Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // 즐겨찾기 토글 함수
    fun toggleFavorite() {
        if (isToggling) return
        isToggling = true
        isFavorite = !isFavorite
        cardId?.let { viewModel.toggleFavoriteDigitalCard(it) }
    }

    LaunchedEffect(cardId) { 
        cardId?.let { 
            viewModel.getDigitalCardDetail(it)
            lastCardId = it
        } 
    }
    LaunchedEffect(Unit) { groupViewModel.fetchGroups() }

    // 서버 응답으로 카드 데이터 업데이트
    LaunchedEffect(digitalCardDetailResponse) {
        digitalCardDetailResponse?.result?.let { detail ->
            val newCardData = mutableMapOf<String, String>()
            newCardData["name"] = detail.name
            newCardData["phone"] = detail.phone
            newCardData["company"] = detail.company
            newCardData["position"] = detail.position ?: ""
            newCardData["email"] = detail.email ?: ""
            newCardData["imageUrlHorizontal"] = detail.imageUrlHorizontal ?: ""
            newCardData["imageUrlVertical"] = detail.imageUrlVertical ?: ""
            newCardData["createdAt"] = detail.createdAt
            
            detail.fields.forEach { f -> newCardData[f.fieldName] = f.fieldValue }
            
            // API 목록으로 받아온 그룹 이름 캐시 (이미 화면에서 가지고 있는 apiGroupList 활용)
            val groupNameMap = apiGroupList.associate { it.groupId to it.name }

            // 상세 응답에 name이 없을 수도 있으니 보강해서 표시
            newCardData["group"] = detail.groups.joinToString(", ") { gi ->
                gi.name ?: groupNameMap[gi.groupId] ?: "그룹#${gi.groupId}"
            }

            detail.memo?.let { memo ->
                newCardData["memo_relation"] = memo.relationship
                newCardData["memo_tendency"] = memo.personality
                newCardData["memo_workStyle"] = memo.workStyle
                newCardData["memo_meeting"] = memo.meetingNotes
                newCardData["memo_etc"] = memo.etc
            }
            
            cardDataState = newCardData
        }
    }

    // 새로운 카드 로드 시 즐겨찾기 상태 동기화 (토글 중이 아닐 때만)
    LaunchedEffect(digitalCardDetailResponse, lastCardId, cardId) {
        if (!isToggling && lastCardId != null && lastCardId != cardId) {
            digitalCardDetailResponse?.result?.let { detail ->
                val serverFavorite = detail.favorite
                // 새로운 카드로 변경 시에만 서버 값으로 동기화
                // 토글 중이 아닐 때만 서버 상태로 업데이트
            }
        }
    }



    if (isLoading && !isTogglingFavorite) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (error != null && !isTogglingFavorite) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = error!!, color = Color.Red, fontFamily = pretendardMedium)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isMemoEditMode) "메모 편집" else (cardData["name"] ?: "디지털 명함"),
                        fontFamily = pretendardMedium,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "뒤로가기", tint = Color.Black)
                    }
                },
                actions = {
                    if (isMemoEditMode) {
                        TextButton(onClick = {
                            val memoRequest = MemoRequest(
                                relationship = editMemoRelation,
                                personality = editMemoTendency,
                                workStyle = editMemoWorkStyle,
                                meetingNotes = editMemoMeeting,
                                etc = editMemoEtc
                            )
                            cardId?.let { viewModel.updateMemo(it, memoRequest) }
                            Toast.makeText(context, "메모가 수정되었습니다", Toast.LENGTH_SHORT).show()
                            isMemoEditMode = false
                        }) {
                            Text("완료", fontFamily = pretendardMedium, fontSize = 16.sp, color = Color.Black)
                        }
                    } else {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(painter = painterResource(id = R.drawable.ic_delete), contentDescription = "삭제", tint = Color.Black)
                        }
                        IconButton(onClick = { showShareSheet = true }) {
                            Icon(painter = painterResource(id = R.drawable.ic_share), contentDescription = "공유", tint = Color.Black)
                        }
                        IconButton(onClick = {
                            when (selectedTab) {
                                0 -> Toast.makeText(context, "디지털 명함은 메모만 편집할 수 있습니다", Toast.LENGTH_SHORT).show()
                                1 -> { isMemoEditMode = true; selectedTab = 1 }
                                2 -> Toast.makeText(context, "히스토리는 수정할 수 없습니다", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(painter = painterResource(id = R.drawable.ic_edit), contentDescription = "편집", tint = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            digitalCardDetailResponse?.result?.let { detail ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(9f/5f)
                        ) {
                            val imageUrl = detail.imageUrlHorizontal ?: detail.imageUrlVertical
                            if (!imageUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(imageUrl).crossfade(true).build(),
                                    contentDescription = "디지털 명함 이미지",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("디지털 명함 이미지를 찾을 수 없습니다", fontFamily = pretendardRegular, fontSize = 14.sp, color = Color.Gray)
                                }
                            }

                            if (!isMemoEditMode) {
                                IconButton(
                                    onClick = { toggleFavorite() },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                ) {
                                    when {
                                        isTogglingFavorite -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFFFFE100)
                                            )
                                        }
                                        isFavorite == null -> {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_star_off),
                                                contentDescription = "즐겨찾기 로딩 중",
                                                tint = Color(0xFFFFE100),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                painter = painterResource(
                                                    id = if (isFavorite) R.drawable.ic_star_on else R.drawable.ic_star_off
                                                ),
                                                contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                                                tint = Color(0xFFFFE100),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = detail.createdAt.take(10),
                                fontFamily = pretendardRegular,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isMemoEditMode) {
                ContactButtons(
                    cardData = cardData,
                    onPhoneClick = {
                        val phone = cardData["phone"]?.replace("-", "")
                        if (!phone.isNullOrEmpty()) {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                        }
                    },
                    onMessageClick = {
                        val phone = cardData["phone"]?.replace("-", "")
                        if (!phone.isNullOrEmpty()) {
                            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")))
                        }
                    },
                    onEmailClick = {
                        val email = cardData["email"]
                        if (!email.isNullOrEmpty()) {
                            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
                        }
                    }
                )
            }

            // 탭
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val tabsList = if (isMemoEditMode) listOf("정보", "메모") else listOf("정보", "메모", "히스토리")
                    tabsList.forEachIndexed { index, tab ->
                        Text(
                            text = tab,
                            fontFamily = pretendardMedium,
                            fontSize = 16.sp,
                            color = Color(0xFF4C3924),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .clickable { if (!isMemoEditMode) selectedTab = index }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(Color(0xFFC6B9A4))
                )
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    val tabsList = if (isMemoEditMode) listOf("정보", "메모") else listOf("정보", "메모", "히스토리")
                    tabsList.forEachIndexed { index, _ ->
                        Box(modifier = Modifier.weight(1f)) {
                            if (selectedTab == index) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFF4C3924))
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        val ordered = mutableListOf<Pair<String, String>>()
                        cardData["name"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("이름" to it) }
                        cardData["phone"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("전화번호" to it) }
                        cardData["company"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("회사" to it) }
                        ordered.add("직책" to (cardData["position"] ?: ""))
                        ordered.add("이메일" to (cardData["email"] ?: ""))

                        cardData.forEach { (k, v) ->
                            if (v.isNotEmpty() && k !in listOf(
                                    "name","phone","company","position","email",
                                    "imageUrlHorizontal","imageUrlVertical","createdAt",
                                    "memo_relation","memo_tendency","memo_workStyle","memo_meeting","memo_etc", "group"
                                )
                            ) ordered.add(k to v)
                        }
                        ordered.add("그룹" to (cardData["group"] ?: ""))

                        ordered.forEach { (label, value) -> InfoRow(label, value) }

                        if (cardData.isEmpty() || cardData.values.all { it.isEmpty() }) {
                            Text("명함 정보가 없습니다.", fontFamily = pretendardRegular, fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                }
                1 -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        if (isMemoEditMode) {
                            EditableMemoField("관계", editMemoRelation) { editMemoRelation = it }
                            EditableMemoField("성향", editMemoTendency) { editMemoTendency = it }
                            EditableMemoField("업무 스타일", editMemoWorkStyle) { editMemoWorkStyle = it }
                            EditableMemoField("미팅 기록", editMemoMeeting) { editMemoMeeting = it }
                            EditableMemoField("기타", editMemoEtc) { editMemoEtc = it }
                        } else {
                            listOf(
                                "관계" to (cardData["memo_relation"] ?: ""),
                                "성향" to (cardData["memo_tendency"] ?: ""),
                                "업무 스타일" to (cardData["memo_workStyle"] ?: ""),
                                "미팅 기록" to (cardData["memo_meeting"] ?: ""),
                                "기타" to (cardData["memo_etc"] ?: "")
                            ).forEach { (label, content) ->
                                Text(label, fontFamily = pretendardRegular, fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(vertical = 4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 53.dp)
                                        .background(color = Color(0xFFF6F3ED), shape = RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = content,
                                        fontFamily = pretendardMedium,
                                        fontSize = 16.sp,
                                        color = if (content.isNotBlank()) Color.Black else Color.Gray.copy(alpha = 0.4f)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        val histories = digitalCardDetailResponse?.result?.companyHistories.orEmpty()
                        if (histories.isEmpty()) {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("이전 명함 기록이 없습니다", fontFamily = pretendardRegular, fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        } else {
                            val sorted = histories.sortedBy { it.changedAt }
                            val uiItems = sorted.mapIndexed { index, item ->
                                val start = item.changedAt
                                val nextStart = sorted.getOrNull(index + 1)?.changedAt
                                HistoryUiRow(
                                    period = buildPeriod(start, nextStart),
                                    company = item.company,
                                    // ⚠️ 모델 프로퍼티: confirm (JSON isConfirm)
                                    isConfirm = (item.confirm == true)
                                )
                            }.reversed()

                            uiItems.forEachIndexed { idx, row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 왼쪽: 기간
                                    Text(
                                        text = row.period,
                                        fontFamily = pretendardRegular,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // 오른쪽: 회사명 + 배지 고정 슬롯
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = row.company,
                                            fontFamily = pretendardMedium,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        )
                                        // 항상 오른쪽에 8dp + 24dp 공간 확보 → 회사명이 조금 왼쪽에 고정
                                        Spacer(Modifier.width(8.dp))
                                        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                            if (row.isConfirm) ConfirmBadge()
                                        }
                                    }
                                }
                                if (idx < uiItems.lastIndex) Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (showDeleteDialog) {
                DeleteConfirmDialog(
                    onConfirm = {
                        cardId?.let { viewModel.deleteDigitalCard(it) }
                        Toast.makeText(context, "디지털 명함이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }

            if (showGroupDialog) {
                GroupSelectionDialog(
                    selectedGroups = selectedGroups,
                    onGroupToggle = { gid ->
                        selectedGroups = if (selectedGroups.contains(gid)) selectedGroups - gid else selectedGroups + gid
                    },
                    onDismiss = { showGroupDialog = false },
                    availableGroups = apiGroupList
                )
            }

            if (showShareSheet) {
                ShareBottomSheetWrapper(
                    showShareSheet = showShareSheet,
                    onDismiss = { showShareSheet = false },
                    imageUrl = cardData["imageUrlHorizontal"] ?: ""
                )
            }
        }
    }
}

/* ===== 히스토리 표기 & 배지 (동일 파일, 최상위) ===== */

private data class HistoryUiRow(
    val period: String,
    val company: String,
    val isConfirm: Boolean
)

private fun buildPeriod(startIso: String, nextStartIso: String?): String {
    val start = parseDateFlexible(startIso) ?: return ""
    val endExclusive = nextStartIso?.let { parseDateFlexible(it) }
    val endInclusive = endExclusive?.let { cal ->
        Calendar.getInstance().apply { time = cal; add(Calendar.DATE, -1) }.time
    }
    fun ym(d: Date) = SimpleDateFormat("yyyy.MM", Locale.getDefault()).format(d)
    fun y(d: Date) = SimpleDateFormat("yyyy", Locale.getDefault()).format(d)

    return if (endInclusive == null) {
        "${ym(start)}~"
    } else {
        val cs = Calendar.getInstance().apply { time = start }
        val ce = Calendar.getInstance().apply { time = endInclusive }
        val sameYear = cs.get(Calendar.YEAR) == ce.get(Calendar.YEAR)
        val startIsJan = cs.get(Calendar.MONTH) == Calendar.JANUARY
        val endIsDec = ce.get(Calendar.MONTH) == Calendar.DECEMBER
        if (sameYear && startIsJan && endIsDec) y(start) else "${ym(start)}~${ym(endInclusive)}"
    }
}

private fun parseDateFlexible(s: String): Date? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd"
    )
    for (p in patterns) {
        try {
            val fmt = SimpleDateFormat(p, Locale.getDefault())
            fmt.isLenient = false
            return fmt.parse(s)
        } catch (_: Exception) {}
    }
    return null
}

@Composable
private fun ConfirmBadge() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(Color(0xFF1E88E5), shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("✓", color = Color.White, fontSize = 14.sp, fontFamily = pretendardMedium)
    }
}
