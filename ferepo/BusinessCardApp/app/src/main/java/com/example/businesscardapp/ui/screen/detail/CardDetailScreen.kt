package com.example.businesscardapp.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import com.example.businesscardapp.data.model.BusinessCardInfo
import com.google.gson.Gson
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.net.Uri
import androidx.compose.ui.text.style.TextAlign
import java.io.File
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.pager.HorizontalPagerIndicator
import androidx.compose.runtime.saveable.rememberSaveable
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.businesscardapp.ui.viewmodel.PaperCardViewModel
import com.example.businesscardapp.ui.viewmodel.GroupViewModel
import com.example.businesscardapp.data.model.MemoRequest
import com.example.businesscardapp.data.model.UpdatePaperCardRequest
import com.example.businesscardapp.data.model.UpdateField
import com.example.businesscardapp.data.model.UpdateGroup
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.FileOutputStream
import java.io.InputStream
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    navController: NavController,
    cardId: Int? = null,
    name: String? = null,
    newCardInfo: String? = null,
    newImage: File? = null,  // 편집 모드에서 새로 촬영한 이미지
    startInEditMode: Boolean = false,
    refresh: Boolean = false,
    isFavorite: Boolean = false  // 명함 목록에서 전달받은 즐겨찾기 상태
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    
    // ✅ ViewModel
    val viewModel: PaperCardViewModel = viewModel()
    val groupViewModel: GroupViewModel = viewModel()
    
    // ✅ States
    val paperCardDetail by viewModel.paperCardDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val apiGroupList by groupViewModel.groupList.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState(initial = false)

    // ✅ 최초 상세 조회
    LaunchedEffect(cardId, refresh) {
        cardId?.let { viewModel.getPaperCardDetail(it) }
    }

    // ✅ 그룹 목록
    LaunchedEffect(Unit) {
        groupViewModel.fetchGroups()
    }
    
    // ✅ 즐겨찾기 상태
    var isFavorite by remember { mutableStateOf(isFavorite) }  // 명함 목록에서 받아온 값으로 초기화
    var isToggling by remember { mutableStateOf(false) }
    var lastCardId by remember { mutableStateOf<Int?>(null) }

    // ✅ 로딩/에러 (즐찾 토글 중엔 가리지 않음)
    if (isLoading && !isToggling) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (error != null && !isToggling) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = error!!, color = Color.Red, fontFamily = pretendardMedium)
        }
        return
    }

    // 편집 모드
    var isEditMode by rememberSaveable { mutableStateOf(startInEditMode || newImage != null) }
    var isMemoEditMode by remember { mutableStateOf(false) }

    // 편집 가능한 입력 상태
    var editName by remember { mutableStateOf("") }
    var editPosition by remember { mutableStateOf("") }
    var editDepartment by remember { mutableStateOf("") }
    var editCompany by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var editCompanyPhone by remember { mutableStateOf("") }
    var editFax by remember { mutableStateOf("") }
    var editWebsite by remember { mutableStateOf("") }

    // 메모 편집 상태
    var editMemoRelation by remember { mutableStateOf("") }
    var editMemoTendency by remember { mutableStateOf("") }
    var editMemoWorkStyle by remember { mutableStateOf("") }
    var editMemoMeeting by remember { mutableStateOf("") }
    var editMemoEtc by remember { mutableStateOf("") }

    // 그룹 선택 상태
    var showGroupDialog by remember { mutableStateOf(false) }
    var selectedGroups by remember { mutableStateOf(setOf<Int>()) }

    // 삭제 다이얼로그
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 이미지 선택 다이얼로그
    var showImageSelectionDialog by remember { mutableStateOf(false) }

    // 히스토리
    data class HistoryItem(val imageUrl: String, val date: String, val cardData: Map<String, String>)
    var historyItems by remember { mutableStateOf(listOf<HistoryItem>()) }

    // ✅ 하단 히스토리 미리보기 시트 상태 + 상단과 동일 비율
    var showHistorySheet by remember { mutableStateOf(false) }
    var selectedHistoryItem by remember { mutableStateOf<HistoryItem?>(null) }
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val cardAspect = 1.6f

    // 항상 cardBox로 이동하는 헬퍼
    fun goCardBox() {
        navController.navigate("cardBox") {
            launchSingleTop = true
            popUpTo("cardBox") { inclusive = false }
        }
    }
    // 날짜 포맷
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd",
                "yyyy.MM.dd",
                "yyyy/MM/dd",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
            )
            for (fmt in inputFormats) {
                try {
                    val parser = SimpleDateFormat(fmt, Locale.getDefault())
                    val d = parser.parse(dateString)
                    if (d != null) return SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(d)
                } catch (_: Exception) {}
            }
            ""
        } catch (_: Exception) { "" }
    }

    // 하드웨어 뒤로가기 처리: 무조건 cardBox
    BackHandler {
        goCardBox()
    }

    // 즐겨찾기 토글
    fun toggleFavorite() {
        Log.d("CardDetailScreen", "toggleFavorite 호출됨 - cardId: $cardId, isToggling: $isToggling, 현재 isFavorite: $isFavorite")
        if (cardId != null && !isToggling) {
            isToggling = true
            // 즉시 UI 상태 변경
            isFavorite = !isFavorite
            Log.d("CardDetailScreen", "즐겨찾기 토글 완료: $isFavorite")
            viewModel.toggleFavorite(cardId)
        } else {
            Log.d("CardDetailScreen", "토글 조건 불만족 - cardId: $cardId, isToggling: $isToggling")
        }
    }
    
    // 에러 발생 시 롤백
    LaunchedEffect(error) {
        error?.let { _ ->
            if (isToggling) {
                isToggling = false
            }
        }
    }
    
    // 로딩 완료 시 토글 상태 해제 (즐겨찾기 토글 후에만)
    LaunchedEffect(isLoading) {
        if (!isLoading && isToggling) {
            // 서버 응답이 완전히 처리된 후 토글 상태 해제
            kotlinx.coroutines.delay(500)
            isToggling = false
            Log.d("CardDetailScreen", "토글 상태 해제됨")
        }
    }
    
    // 서버 응답으로 즐겨찾기 상태 동기화 (초기 로드 시에만)
    LaunchedEffect(paperCardDetail) {
        paperCardDetail?.let { detail ->
            if (!isToggling && lastCardId != cardId) {
                // 새로운 카드 로드 시에만 서버 상태로 초기화 (단, 명함 목록에서 받아온 값이 우선)
                val serverFavorite = detail.isFavorite ?: false
                Log.d("CardDetailScreen", "새 카드 로드 - 서버 즐겨찾기: $serverFavorite, 목록에서 받아온 값: $isFavorite")
                // 명함 목록에서 받아온 값이 있으면 그것을 우선시
                if (lastCardId == null) {
                    // 최초 로드 시에만 명함 목록 값 사용
                    Log.d("CardDetailScreen", "최초 로드 - 명함 목록 값 사용: $isFavorite")
                } else {
                    // 새로운 카드로 변경 시 서버 값 사용
                    isFavorite = serverFavorite
                    Log.d("CardDetailScreen", "새 카드 로드로 즐겨찾기 상태 초기화: $serverFavorite")
                }
            } else if (!isToggling) {
                Log.d("CardDetailScreen", "기존 카드 - 서버 상태 동기화 건너뜀")
            } else {
                Log.d("CardDetailScreen", "토글 중이므로 서버 상태 동기화 건너뜀")
            }
        }
    }
    
    // 토글 완료 후 상태 동기화 (서버 응답을 신뢰하지 않음)
    LaunchedEffect(isToggling) {
        if (!isToggling) {
            // 토글이 완료되면 서버 상태와 동기화하되, 사용자 액션을 우선시
            paperCardDetail?.let { detail ->
                // 서버 응답이 사용자 액션과 일치하는지 확인
                val serverFavorite = detail.isFavorite ?: false
                Log.d("CardDetailScreen", "토글 완료 후 상태 동기화: 서버=${serverFavorite}, 현재=${isFavorite}")
                
                // 서버 응답이 사용자 액션과 다르면 사용자 액션 유지
                if (serverFavorite != isFavorite) {
                    Log.d("CardDetailScreen", "서버 응답과 사용자 액션이 다름 - 사용자 액션 유지")
                    // 사용자 액션을 유지하고 서버 상태는 무시
                }
            }
        }
    }
    
    // 초기 즐겨찾기 상태 설정 (새로운 카드 로드 시에만)
    LaunchedEffect(cardId) {
        if (cardId != null && !isToggling && lastCardId != cardId) {
            paperCardDetail?.let { detail ->
                val serverFavorite = detail.isFavorite ?: false
                Log.d("CardDetailScreen", "초기 상태 설정 - 서버: $serverFavorite, 목록에서 받아온 값: $isFavorite")
                // 명함 목록에서 받아온 값이 우선
                if (lastCardId == null) {
                    Log.d("CardDetailScreen", "최초 로드 - 명함 목록 값 유지: $isFavorite")
                } else {
                    isFavorite = serverFavorite
                    Log.d("CardDetailScreen", "새 카드 - 서버 값 사용: $serverFavorite")
                }
            }
        }
    }

    // 클라이언트 표시용 카드 데이터
    var cardDataState by remember { mutableStateOf(mutableMapOf<String, String>()) }

    // 초기 데이터 바인딩
    LaunchedEffect(paperCardDetail, newCardInfo) {
        cardDataState = if (paperCardDetail != null) {
                val dataMap = mutableMapOf<String, String>(
                    "name" to (paperCardDetail?.name ?: ""),
                    "company" to (paperCardDetail?.company ?: ""),
                    "phone" to (paperCardDetail?.phone ?: ""),
                    "group" to (paperCardDetail?.groups?.joinToString(", ") { it.groupName } ?: ""),
                    "imageUri" to (paperCardDetail?.image1Url ?: ""),
                    "imageUri2" to (paperCardDetail?.image2Url ?: ""),
                    "capturedDate" to (paperCardDetail?.createdAt ?: "")
                )
            // top-level
            dataMap["position"] = paperCardDetail?.position ?: ""
            dataMap["email"] = paperCardDetail?.email ?: ""
            // fields
            paperCardDetail?.fields?.forEach { f -> dataMap[f.fieldName] = f.fieldValue }
                dataMap
            } else if (newCardInfo != null) {
            try {
                val info = Gson().fromJson(newCardInfo, BusinessCardInfo::class.java)
                mutableMapOf(
                    "name" to info.name,
                    "company" to info.company,
                    "department" to info.department,
                    "position" to info.position,
                    "phone" to info.phone,
                    "companyAddress" to info.address,
                        "companyPhone" to "",
                        "fax" to "",
                    "email" to info.email,
                        "group" to "",
                        "website" to "",
                    "address" to info.address,
                    "imageUri" to info.imageUri,
                    "imageUri2" to info.imageUri2,
                    "capturedDate" to info.capturedDate
                    )
                } catch (e: Exception) {
                    Log.e("CardDetailScreen", "명함 정보 파싱 실패", e)
                mutableMapOf()
            }
        } else mutableMapOf()
    }
    val cardData = cardDataState

    // 서버 응답 도착 시 로그 및 동기화 + ✅ 서버 히스토리 매핑
    LaunchedEffect(paperCardDetail) {
        paperCardDetail?.let { detail ->
            Log.d("CardDetailScreen", "=== 서버 응답 데이터 확인 (LaunchedEffect) ===")
            Log.d("CardDetailScreen", "paperCardDetail: $detail")
            Log.d("CardDetailScreen", "fields: ${detail.fields}")
            detail.fields?.forEach { f ->
                Log.d("CardDetailScreen", "Field - id: ${f.fieldId}, name: ${f.fieldName}, value: ${f.fieldValue}")
            }
            // 표시 데이터 반영
            cardDataState.clear()
            cardDataState["name"] = detail.name
            cardDataState["phone"] = detail.phone
            cardDataState["company"] = detail.company
            cardDataState["position"] = detail.position ?: ""
            cardDataState["email"] = detail.email ?: ""
            // 추가 필드 처리 (기본 필드와 중복되지 않도록)
            detail.fields?.forEach { f -> 
                if (f.fieldName !in listOf("이름", "전화번호", "회사", "직책", "이메일")) {
                    cardDataState[f.fieldName] = f.fieldValue
                }
            }
            val groupNames = detail.groups?.map { it.groupName } ?: emptyList()
            cardDataState["group"] = groupNames.joinToString(", ")
            detail.memo?.let { memo ->
                cardDataState["memo_relation"] = memo.relationship
                cardDataState["memo_tendency"] = memo.personality
                cardDataState["memo_workStyle"] = memo.workStyle
                cardDataState["memo_meeting"] = memo.meetingNotes
                cardDataState["memo_etc"] = memo.etc
            }
            // 즐겨찾기 상태는 derivedStateOf에서 자동으로 처리됨
            lastCardId = cardId

            // ✅ 서버 히스토리 목록 -> 화면용으로 변환
            historyItems = paperCardDetail?.imageHistories
                ?.flatMap { dto ->
                    val dateStr = formatDate(dto.uploadedAt)
                    dto.images.map { img ->
                        HistoryItem(
                            imageUrl = img,
                            date = dateStr,
                            cardData = emptyMap() // 필요 시 채우기
                        )
                    }
                }
                ?: emptyList()
        }
    }

    // 이미지 저장
    fun saveImageToGallery(imageUri: String) {
        try {
            val uri = when {
                imageUri.startsWith("http://") || imageUri.startsWith("https://") -> Uri.parse(imageUri)
                imageUri.startsWith("content://") || imageUri.startsWith("file://") -> Uri.parse(imageUri)
                else -> {
                    val f = context.getExternalFilesDir(null)?.let { File(it, imageUri) }
                    if (f?.exists() == true) Uri.fromFile(f) else null
                }
            }
            if (uri != null) {
                val input: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(input)
                input?.close()
                if (bitmap != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val cv = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "명함_${System.currentTimeMillis()}.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                        }
                        val outUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
                        outUri?.let { u ->
                            context.contentResolver.openOutputStream(u)?.use { os ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                            }
                        }
                    } else {
                        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val f = File(dir, "명함_${System.currentTimeMillis()}.jpg")
                        FileOutputStream(f).use { os -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os) }
                    }
                    Toast.makeText(context, "명함 이미지를 저장하였습니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "이미지 저장에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "이미지를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("CardDetailScreen", "이미지 저장 실패", e)
            Toast.makeText(context, "이미지 저장에 실패했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    val currentDate = remember(paperCardDetail, newCardInfo) {
        when {
            paperCardDetail != null -> formatDate(paperCardDetail?.createdAt)
            newCardInfo != null -> try {
                val info = Gson().fromJson(newCardInfo, BusinessCardInfo::class.java)
                formatDate(info.capturedDate)
            } catch (_: Exception) { "" }
            else -> ""
        }
    }

    val imageUrls = remember(paperCardDetail, newCardInfo, newImage) {
        if (newImage != null && newImage.exists()) {
            mutableListOf(newImage.absolutePath)
        } else if (paperCardDetail != null) {
            mutableListOf<String>().apply {
                paperCardDetail?.image1Url?.let { if (it.isNotEmpty()) add(it) }
                paperCardDetail?.image2Url?.let { if (it.isNotEmpty()) add(it) }
            }
        } else if (newCardInfo != null) {
            try {
                val info = Gson().fromJson(newCardInfo, BusinessCardInfo::class.java)
                mutableListOf<String>().apply {
                    if (!info.imageUri.isNullOrEmpty()) add(info.imageUri)
                    if (!info.imageUri2.isNullOrEmpty()) add(info.imageUri2)
                }
            } catch (_: Exception) { mutableListOf() }
        } else mutableListOf()
    }

    val pagerState = rememberPagerState()

    // 편집 모드 진입 시 초기화
    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            editName = cardData["name"] ?: ""
            editPosition = cardData["position"] ?: ""
            editDepartment = cardData["department"] ?: ""
            editCompany = cardData["company"] ?: ""
            editPhone = cardData["phone"] ?: ""
            editEmail = cardData["email"] ?: ""
            editAddress = cardData["address"] ?: ""
            editCompanyPhone = cardData["companyPhone"] ?: ""
            editFax = cardData["fax"] ?: ""
            editWebsite = cardData["website"] ?: ""
            
            val savedGroups = cardData["group"] ?: ""
            selectedGroups = if (savedGroups.isNotEmpty()) {
                val names = savedGroups.split(", ")
                apiGroupList.filter { it.name in names }.map { it.groupId }.toSet()
            } else emptySet()
        }
    }
    // 메모 편집 모드 진입 시 초기화
    LaunchedEffect(isMemoEditMode, cardData) {
        if (isMemoEditMode) {
            editMemoRelation = cardData["memo_relation"] ?: ""
            editMemoTendency = cardData["memo_tendency"] ?: ""
            editMemoWorkStyle = cardData["memo_workStyle"] ?: ""
            editMemoMeeting = cardData["memo_meeting"] ?: ""
            editMemoEtc = cardData["memo_etc"] ?: ""
        }
    }

    // 탭 라벨
    val tabs = if (isEditMode || isMemoEditMode) listOf("정보", "메모") else listOf("정보", "메모", "히스토리")

    val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showShareSheet by remember { mutableStateOf(false) }
    ShareBottomSheetWrapper(
        showShareSheet = showShareSheet,
            onDismiss = { showShareSheet = false },
            imageUrl = "https://picsum.photos/600/400"
        )

    // 그룹 선택 다이얼로그
    if (showGroupDialog) {
        val availableGroups = apiGroupList
        GroupSelectionDialog(
            selectedGroups = selectedGroups,
            onGroupToggle = { gid ->
                selectedGroups = if (selectedGroups.contains(gid)) selectedGroups - gid else selectedGroups + gid
            },
            onDismiss = { 
                showGroupDialog = false
                // 그룹 선택 후 그룹 목록 새로고침하여 인원수 갱신
                groupViewModel.fetchGroups()
            },
            availableGroups = availableGroups
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            isEditMode -> "명함 정보 편집"
                            isMemoEditMode -> "메모 편집"
                            else -> (cardData["name"] ?: "")
                        },
                        fontFamily = pretendardMedium,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    // ✅ 무조건 cardBox로
                    IconButton(onClick = { goCardBox() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    when {
                        isEditMode -> {
                            TextButton(onClick = {
                                if (cardId == null) return@TextButton
                                // 필수값 검증
                                if (editName.isBlank() || editPhone.isBlank() || editCompany.isBlank()) {
                                    Toast.makeText(context, "필수 정보(이름, 전화번호, 회사)를 입력해 주세요", Toast.LENGTH_SHORT).show()
                                    return@TextButton
                                }

                                    val existingFields = paperCardDetail?.fields ?: emptyList()
                                val existByName = existingFields.associateBy { it.fieldName }

                                // ✅ 전체 덮어쓰기: 고정 필드 5종은 항상 포함 (빈 문자열도 전송)
                                val fullFieldMap = linkedMapOf(
                                    "부서" to editDepartment,
                                    "회사 주소" to editAddress,
                                    "회사 전화번호" to editCompanyPhone,
                                    "팩스" to editFax,
                                    "웹사이트" to editWebsite
                                )

                                // 기존에 존재하던 추가(동적) 필드는 값 보존해서 같이 보냄
                                existingFields.forEach { f ->
                                    if (f.fieldName !in fullFieldMap.keys) {
                                        val keep = cardDataState[f.fieldName] ?: f.fieldValue
                                        fullFieldMap[f.fieldName] = keep
                                    }
                                }

                                // UpdateField로 변환 (fieldId 매칭)
                                val fieldsForUpdate = fullFieldMap.map { (fname, fval) ->
                                    UpdateField(
                                        fieldId = existByName[fname]?.fieldId,
                                        fieldName = fname,
                                        fieldValue = fval // "" 가능 → 서버에서 해당 값 초기화
                                    )
                                }

                                // 그룹 전체 전송(선택 없으면 빈 리스트)
                                val updateGroups = selectedGroups.map { gid -> UpdateGroup(groupId = gid) }

                                val req = UpdatePaperCardRequest(
                                    name     = editName.trim(),
                                    company  = editCompany.trim(),
                                    phone    = editPhone.trim(),
                                    position = editPosition, // "" 허용
                                    email    = editEmail,    // "" 허용
                                    fields   = fieldsForUpdate.ifEmpty { emptyList() },
                                    groups   = updateGroups.ifEmpty { emptyList() }
                                )

                                // (참고) 로컬 히스토리 추가는 제거 — 서버 히스토리만 사용

                                Log.d("CardDetailScreen", "전체 덮어쓰기 요청: $req")
                                viewModel.updatePaperCard(cardId, req, imageFile1 = newImage)
                                Toast.makeText(context, "정보가 수정되었습니다", Toast.LENGTH_SHORT).show()

                                // 화면 표시 값도 즉시 반영
                                cardDataState["name"] = editName
                                cardDataState["position"] = editPosition
                                cardDataState["department"] = editDepartment
                                cardDataState["company"] = editCompany
                                cardDataState["phone"] = editPhone
                                cardDataState["email"] = editEmail
                                cardDataState["address"] = editAddress
                                cardDataState["companyPhone"] = editCompanyPhone
                                cardDataState["fax"] = editFax
                                cardDataState["website"] = editWebsite

                                val selectedGroupNames = selectedGroups.mapNotNull { gid -> apiGroupList.find { it.groupId == gid }?.name }
                                cardDataState["group"] = selectedGroupNames.joinToString(", ")

                                // 그룹 편집 완료 후 그룹 목록 새로고침하여 인원수 갱신
                                groupViewModel.fetchGroups()

                                isEditMode = false
                            }) {
                                Text(text = "완료", fontFamily = pretendardMedium, fontSize = 16.sp, color = Color.Black)
                            }
                        }
                        isMemoEditMode -> {
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
                                Text(text = "완료", fontFamily = pretendardMedium, fontSize = 16.sp, color = Color.Black)
                            }
                        }
                        else -> {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(painter = painterResource(id = R.drawable.ic_delete), contentDescription = "삭제", tint = Color.Black)
                            }
                            IconButton(onClick = { 
                                if (imageUrls.isNotEmpty()) {
                                    val currentImageUri = imageUrls[pagerState.currentPage]
                                    saveImageToGallery(currentImageUri)
                                } else {
                                    Toast.makeText(context, "저장할 이미지가 없습니다", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(painter = painterResource(id = R.drawable.ic_download), contentDescription = "저장", tint = Color.Black)
                            }
                            IconButton(onClick = { showShareSheet = true }) {
                                Icon(painter = painterResource(id = R.drawable.ic_share), contentDescription = "공유", tint = Color.Black)
                            }
                            IconButton(onClick = {
                                when (selectedTab) {
                                    0 -> { isEditMode = true; selectedTab = 0 }
                                    1 -> { isMemoEditMode = true; selectedTab = 1 }
                                    2 -> Toast.makeText(context, "히스토리는 수정할 수 없습니다", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(painter = painterResource(id = R.drawable.ic_edit), contentDescription = "편집", tint = Color.Black)
                            }
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
            if (imageUrls.isNotEmpty()) {
                val outputDir = context.getExternalFilesDir(null)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        HorizontalPager(
                            count = imageUrls.size,
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(9f/5f)
                        ) { page ->
                            val imageUri = imageUrls[page]
                            val uri = try {
                                when {
                                    imageUri.startsWith("http://") || imageUri.startsWith("https://") -> Uri.parse(imageUri)
                                    imageUri.startsWith("content://") || imageUri.startsWith("file://") -> Uri.parse(imageUri)
                                    else -> {
                                        val f = outputDir?.let { File(it, imageUri) }
                                        if (f?.exists() == true) Uri.fromFile(f) else null
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("CardDetailScreen", "URI 파싱 실패: $imageUri", e); null
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                if (uri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(uri).crossfade(true).build(),
                                        contentDescription = "명함 이미지",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("명함 이미지를 찾을 수 없습니다", fontFamily = pretendardRegular, fontSize = 14.sp, color = Color.Gray)
                                    }
                                }

                                // 즐겨찾기
                                if (!isEditMode) {
                                    IconButton(
                                        onClick = { toggleFavorite() },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).zIndex(1f)
                                    ) {
                                        when {
                                            isToggling -> {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = Color(0xFFFFE100)
                                                )
                                            }
                                            else -> {
                                                val starIcon = if (isFavorite) R.drawable.ic_star_on else R.drawable.ic_star_off
                                                Log.d("CardDetailScreen", "즐겨찾기 아이콘 렌더링: isFavorite=$isFavorite, icon=$starIcon")
                                                Icon(
                                                    painter = painterResource(id = starIcon),
                                                    contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                                                    tint = Color(0xFFFFE100),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 편집 모드 오버레이
                                if (isEditMode) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color(0x33000000)).zIndex(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(width = 122.dp, height = 68.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .align(Alignment.Center)
                                            .clickable { showImageSelectionDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_edit_card),
                                            contentDescription = "명함 편집",
                                            modifier = Modifier.size(32.dp),
                                            tint = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            HorizontalPagerIndicator(
                                pagerState = pagerState,
                                activeColor = Color(0xFF4C3924),
                                inactiveColor = Color(0xFFE0E0E0),
                                modifier = Modifier.align(Alignment.Center)
                            )
                            Text(
                                text = currentDate,
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

            if (!isEditMode && !isMemoEditMode) {
                ContactButtons(
                    cardData = cardData,
                    onPhoneClick = {
                                            val phoneNumber = cardData["phone"]
                                            if (!phoneNumber.isNullOrEmpty()) {
                            val cleaned = phoneNumber.replace("-", "")
                            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$cleaned") }
                                                context.startActivity(intent)
                                            }
                    },
                    onMessageClick = {
                                            val phoneNumber = cardData["phone"]
                                            if (!phoneNumber.isNullOrEmpty()) {
                            val cleaned = phoneNumber.replace("-", "")
                            val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("smsto:$cleaned") }
                                                context.startActivity(intent)
                                            }
                    },
                    onEmailClick = {
                                            val email = cardData["email"]
                                            if (!email.isNullOrEmpty()) {
                            val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:$email") }
                                                context.startActivity(intent)
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
                    tabs.forEachIndexed { index, tab ->
                        Text(
                            text = tab,
                            fontFamily = pretendardMedium,
                            fontSize = 16.sp,
                            color = Color(0xFF4C3924),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .clickable {
                                    if (!isEditMode && !isMemoEditMode) selectedTab = index
                                }
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
                    tabs.forEachIndexed { index, _ ->
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
                        if (isEditMode) {
                            // 기존에 등록된 필드만 편집 가능하도록 수정
                            val editFields = mutableListOf<Pair<String, String>>().apply {
                                // 기본 필드 (항상 표시)
                                add("이름" to editName)
                                add("전화번호" to editPhone)
                                add("회사" to editCompany)
                                add("직책" to editPosition) // 항상 표시
                                add("이메일" to editEmail)   // 항상 표시
                                
                                // 기존에 등록된 추가 필드만 표시
                                if (editDepartment.isNotEmpty()) add("부서" to editDepartment)
                                if (editCompanyPhone.isNotEmpty()) add("회사 전화번호" to editCompanyPhone)
                                if (editFax.isNotEmpty()) add("팩스" to editFax)
                                if (editAddress.isNotEmpty()) add("회사 주소" to editAddress)
                                if (editWebsite.isNotEmpty()) add("웹사이트" to editWebsite)
                            }
                            editFields.forEach { (label, value) ->
                                when (label) {
                                    "이름" -> EditableInfoField(label, value) { editName = it }
                                    "전화번호" -> EditableInfoField(label, value) { editPhone = it }
                                    "회사" -> EditableInfoField(label, value) { editCompany = it }
                                    "직책" -> EditableInfoField(label, value) { editPosition = it }
                                    "이메일" -> EditableInfoField(label, value) { editEmail = it }
                                    "부서" -> EditableInfoField(label, value) { editDepartment = it }
                                    "회사 전화번호" -> EditableInfoField(label, value) { editCompanyPhone = it }
                                    "팩스" -> EditableInfoField(label, value) { editFax = it }
                                    "회사 주소" -> EditableInfoField(label, value) { editAddress = it }
                                    "웹사이트" -> EditableInfoField(label, value) { editWebsite = it }
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                                    .height(1.dp)
                                    .background(Color(0xFFC6B9A4))
                            )
                            
                            EditableGroupField(
                                label = "그룹",
                                selectedGroups = selectedGroups.mapNotNull { gid -> apiGroupList.find { it.groupId == gid }?.name }.toSet(),
                                onGroupClick = { showGroupDialog = true }
                            )
                        } else {
                            val ordered = mutableListOf<Pair<String, String>>()
                            cardData["name"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("이름" to it) }
                            cardData["phone"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("전화번호" to it) }
                            cardData["company"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("회사" to it) }
                            cardData["position"]?.let { ordered.add("직책" to it) }
                            cardData["email"]?.let { ordered.add("이메일" to it) }
                            cardData["department"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("부서" to it) }
                            cardData["companyPhone"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("회사 전화번호" to it) }
                            cardData["fax"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("팩스" to it) }
                            cardData["address"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("회사 주소" to it.replace("+", " ")) }
                            cardData["website"]?.takeIf { it.isNotEmpty() }?.let { ordered.add("웹사이트" to it) }

                            // 추가 필드(내부 키 제외)
                            cardData.forEach { (k, v) ->
                                if (v.isNotEmpty() && k !in listOf(
                                        "name","phone","company","position","email",
                                        "department","companyPhone","fax","address","website",
                                        "imageUri","imageUri2","capturedDate","group",
                                        "memo_relation","memo_tendency","memo_workStyle","memo_meeting","memo_etc"
                                    )
                                ) {
                                    ordered.add(k to v)
                                }
                            }
                            ordered.add("그룹" to (cardData["group"] ?: ""))

                            ordered.forEach { (label, value) -> InfoRow(label, value) }

                            if (cardData.isEmpty() || cardData.values.all { it.isEmpty() }) {
                                Text("명함 정보가 없습니다.", fontFamily = pretendardRegular, fontSize = 16.sp, color = Color.Gray)
                            }
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
                                Text(
                                    text = label,
                                    fontFamily = pretendardRegular,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
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
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
                2 -> {
                    if (historyItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("이전 명함 기록이 없습니다", fontFamily = pretendardRegular, fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            historyItems.forEach { h ->
                                Text(
                                    text = h.date,
                                    fontFamily = pretendardMedium,
                                    fontSize = 14.sp,
                                    color = Color(0xFF4C3924),
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .clickable {
                                            selectedHistoryItem = h
                                            showHistorySheet = true
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(cardAspect)
                                        ) {
                                            val uri = try {
                                                when {
                                                    h.imageUrl.startsWith("http://") || h.imageUrl.startsWith("https://") -> Uri.parse(h.imageUrl)
                                                    h.imageUrl.startsWith("content://") || h.imageUrl.startsWith("file://") -> Uri.parse(h.imageUrl)
                                                    else -> {
                                                        val f = context.getExternalFilesDir(null)?.let { File(it, h.imageUrl) }
                                                        if (f?.exists() == true) Uri.fromFile(f) else null
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e("CardDetailScreen", "히스토리 URI 파싱 실패: ${h.imageUrl}", e); null
                                            }
                                            if (uri != null) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(context).data(uri).crossfade(true).build(),
                                                    contentDescription = "히스토리 명함 이미지",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                                                        .background(Color(0xFFF0F0F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                                                        "이미지를 찾을 수 없습니다",
                            fontFamily = pretendardRegular,
                                                        fontSize = 14.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                            val d = h.cardData
                                            d["name"]?.takeIf { it.isNotEmpty() }?.let {
                                                Text(it, fontFamily = pretendardMedium, fontSize = 18.sp, color = Color.Black, modifier = Modifier.padding(bottom = 4.dp))
                                            }
                                            d["company"]?.takeIf { it.isNotEmpty() }?.let {
                                                Text(it, fontFamily = pretendardRegular, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                                            }
                                            d["position"]?.takeIf { it.isNotEmpty() }?.let {
                                                Text(it, fontFamily = pretendardRegular, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                                            }
                                            d["phone"]?.takeIf { it.isNotEmpty() }?.let {
                                                Text(it, fontFamily = pretendardRegular, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // ✅ 하단 BottomSheet: 히스토리 미리보기 (풀스크린 X)
    if (showHistorySheet && selectedHistoryItem != null) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = historySheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = selectedHistoryItem?.date.orEmpty(),
                    fontFamily = pretendardMedium,
                    fontSize = 14.sp,
                    color = Color(0xFF4C3924),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val uri = try {
                    val u = selectedHistoryItem!!.imageUrl
                    when {
                        u.startsWith("http://") || u.startsWith("https://") -> Uri.parse(u)
                        u.startsWith("content://") || u.startsWith("file://") -> Uri.parse(u)
                        else -> {
                            val f = context.getExternalFilesDir(null)?.let { File(it, u) }
                            if (f?.exists() == true) Uri.fromFile(f) else null
                        }
                    }
                } catch (_: Exception) { null }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(cardAspect) // ✅ 상단 이미지와 동일 비율
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                ) {
                    if (uri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(uri).crossfade(true).build(),
                            contentDescription = "히스토리 미리보기",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            "이미지를 찾을 수 없습니다",
                            fontFamily = pretendardRegular,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                cardId?.let { id ->
                    viewModel.deletePaperCard(id)
                    Toast.makeText(context, "명함이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    goCardBox()
                }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    
    // 이미지 선택 다이얼로그
    if (showImageSelectionDialog) {
        ImageSelectionDialog(
            onDismiss = { showImageSelectionDialog = false },
            onCameraClick = {
                showImageSelectionDialog = false
                navController.navigate("camera?from=edit&cardId=$cardId")
            },
            onAlbumClick = {
                showImageSelectionDialog = false
                navController.navigate("album_guide?from=edit&cardId=$cardId&max=1")
            }
        )
    }

    // 수정/삭제 성공 시 처리 → cardBox로
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            goCardBox()
        }
    }

    // 즐겨찾기 토글 완료 후 플래그 리셋
    LaunchedEffect(paperCardDetail) {
        if (isToggling && paperCardDetail != null) {
            isToggling = false
        }
    }
}

// ✅ 이미지 선택 다이얼로그 컴포넌트
@Composable
fun ImageSelectionDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onAlbumClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(287.dp)
                .height(120.dp)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F3ED))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onCameraClick() }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = "카메라로 촬영하기",
                        fontFamily = pretendardRegular,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1D1B20),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onAlbumClick() }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = "앨범에서 선택하기",
                        fontFamily = pretendardRegular,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1D1B20),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
