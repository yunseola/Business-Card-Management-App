package com.example.businesscardapp.ui.screen.mycard

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.CardOrientation
import com.example.businesscardapp.ui.component.DigitalPreviewCard
import com.example.businesscardapp.ui.screen.mycard.MyCardViewModel.CreateState
import com.example.businesscardapp.ui.theme.MainColor
import com.example.businesscardapp.ui.theme.pretendardRegular
import com.example.businesscardapp.util.exportDigitalCardToPng
import com.example.businesscardapp.util.uriToPart
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCardEditScreen(
    navController: NavController,
    viewModel: MyCardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    editCardId: Int? = null,          // ✅ 기본값 추가
    onSave: (Int) -> Unit = {}        // ✅ 기본값 추가
) {


    LaunchedEffect(editCardId) {
        editCardId?.let { viewModel.fetchMyCardDetail(it) }
    }

    // (중요) 상세 → 편집일 때만 hydrate 되도록 가드
    var hydrated by remember { mutableStateOf(false) }
    val detailState by viewModel.detailState.collectAsState()
    LaunchedEffect(detailState, editCardId) {
        if (editCardId != null && !hydrated) {
            val s = detailState
            if (s is MyCardViewModel.DetailState.Success) {
                s.data.result?.let {
                    viewModel.hydrateFromDetail(it)
                    hydrated = true
                }
            }
        }
        if (editCardId == null) {

        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // VM 상태
    val fields by viewModel.fields.collectAsState()
    val background by viewModel.background.collectAsState()
    val createState by viewModel.createState.collectAsState()

    // 고정 5개 + 필수 3개
    val fixedOrder = listOf("이름", "연락처", "회사", "직책", "이메일")
    val required = setOf("이름", "연락처", "회사")

    // 값
    val name = fields.firstOrNull { it.label == "이름" }?.value.orEmpty()
    val phone = fields.firstOrNull { it.label == "연락처" }?.value.orEmpty()
    val company = fields.firstOrNull { it.label == "회사" }?.value.orEmpty()
    val position = fields.firstOrNull { it.label == "직책" }?.value.orEmpty()
    val email = fields.firstOrNull { it.label == "이메일" }?.value.orEmpty()


    // MyCardEditScreen.kt Composable 내부 상태
    val profileImage by viewModel.profileImageUri.collectAsState()
    val previewExtras = viewModel.previewExtras

    // ── VM 상태 수집들 바로 아래에 추가 ──
    val textDark by viewModel.textDark.collectAsState()
    viewModel.bgNum.collectAsState().value ?: 0

    val bgCode by viewModel.background.collectAsState()
    val pattern by viewModel.pattern.collectAsState()

    val (bgHex, patternCode) = remember(bgCode, pattern) {
        if (pattern != null) "#00000000" to pattern else bgCode to null
    }

    val remoteProfile by viewModel.profileRemoteUrl.collectAsState()
    val localProfile by viewModel.profileImageUri.collectAsState()

    // 미리보기용 최종 이미지 선택 (로컬 > 서버)
    val previewProfileUri = localProfile?.toString() ?: remoteProfile


    // 미리보기용 추가 필드
    val extras: List<Pair<String, String>> = remember(fields) {
        fields
            .filter { it.label !in fixedOrder }
            .filter { it.value.isNotBlank() }
            .take(5)
            .map { (lbl, v) -> (if (lbl.isBlank()) "새 필드" else lbl) to v }
    }

    val canRegister = remember(fields) {
        required.all { r -> fields.firstOrNull { it.label == r }?.value?.isNotBlank() == true }
    }

    val isEditing = editCardId != null                         // ✅ 편집 모드 여부
    val isLoading = createState is CreateState.Loading         // ✅ 로딩 상태

    LaunchedEffect(editCardId) {                               // ✅ 상세 불러오기
        editCardId?.let { viewModel.fetchMyCardDetail(it) }
    }

    // ✅ 앱이 보관한 JWT (Bearer 없이 순수 토큰 문자열만!)
    //    실제로는 MainActivity/Datastore 등에서 주입받아 사용하세요.
    remember {
        "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhMjM3ODIwODlAZ21haWwuY29tIiwidXNlcklkIjozLCJpYXQiOjE3NTQ5NjI3OTAsImV4cCI6MTc1NTU2NzU5MH0.axJOSxEf6beITR8HGoccEgJuRPwXmIp1tQ5AJPvqJEM"
    }



    LaunchedEffect(createState) {
        when (val s = createState) {
            is CreateState.Success -> {
                val newId = s.cardId


                // 1) 목록 host 에 새로고침 신호
                runCatching {
                    navController.getBackStackEntry("main?tab={tab}")
                        .savedStateHandle["refresh_my_cards"] = true
                }.onFailure {
                    runCatching {
                        navController.getBackStackEntry("main")
                            .savedStateHandle["refresh_my_cards"] = true
                    }
                }
                // 1) 목록 화면에 새로고침 신호
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("refresh_my_cards", true)

                // 2) 성공 멘트 (원하면 스낵바/토스트)
                Toast.makeText(context, "등록 성공!", Toast.LENGTH_SHORT).show()

                // 3) 상세로 이동(선택). 아니면 단순 popBackStack 도 OK.
                navController.navigate("my_card_detail/$newId") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                }

                viewModel.resetCreateState()
            }

            is CreateState.Fail -> {
                Toast.makeText(context, s.msg, Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }


    // 로그도 같이
    LaunchedEffect(profileImage) {
        android.util.Log.d("MyCardEdit", "profileImageUri changed: $profileImage")
    }

    // 화면에서 바로 보이도록 작은 디버그 UI
    Column(Modifier.padding(12.dp)) {
        Text(text = "ProfileUri: ${profileImage ?: "null"}", color = Color.Gray, fontSize = 12.sp)

        // 실제 미리보기 (Coil)
        if (profileImage != null) {
            coil.compose.AsyncImage(
                model = profileImage,
                contentDescription = "선택된 프로필",
                modifier = Modifier.size(80.dp)
            )
        }
    }

    // 앨범에서 프로필 이미지 선택 결과 수신(누수 방지)
    DisposableEffect(Unit) {
        val live = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Uri>("picked_profile_image")
        val obs = androidx.lifecycle.Observer<Uri> { uri ->
            viewModel.setProfileImageUri(uri)
        }
        live?.observe(lifecycleOwner, obs)
        onDispose { live?.removeObserver(obs) }
    }


    // ───────────────── Scaffold 등 기존 UI ─────────────────
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors( // ★ 추가
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF4C3924),
                    navigationIconContentColor = Color(0xFF4C3924),
                    actionIconContentColor = Color(0xFF4C3924)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "뒤로가기")
                    }
                },
                actions = {
//

                    TextButton(
                        onClick = {
                            if (!canRegister) {
                                Toast.makeText(context, "이름, 연락처, 회사를 입력해주세요.", Toast.LENGTH_SHORT)
                                    .show()
                                return@TextButton
                            }

                            val bgForCard = if (pattern != null) "#00000000" else background
                            val patternForCard = pattern


                            scope.launch {
                                // 1) 현재 입력 상태로 가로 PNG 생성
                                val fileH = exportDigitalCardToPng(
                                    context = context,
                                    photoUri = profileImage?.toString(),   // 앨범에서 고른 사진이 있으면 자동 반영
                                    bgHex = bgForCard,                // 패턴 쓰면 투명 or 코드에 맞게 전달
                                    patternCode = pattern,                // 패턴 코드 사용 시 "pattern7" 등
                                    useDarkText = textDark,                // 상태에 맞게
                                    name = name,
                                    company = company,
                                    phoneDisplay = phone,
                                    position = position,
                                    email = email,
                                    extras = extras,
                                    orientation = CardOrientation.Landscape
                                )

                                // 2) 세로 PNG 생성
                                val fileV = exportDigitalCardToPng(
                                    context = context,
                                    photoUri = profileImage?.toString(),
                                    bgHex = bgForCard,
                                    patternCode = patternForCard,
                                    useDarkText = textDark,
                                    name = name,
                                    company = company,
                                    phoneDisplay = phone,
                                    position = position,
                                    email = email,
                                    extras = extras,
                                    orientation = CardOrientation.Portrait
                                )

                                // 3) File -> content:// Uri (FileProvider)
                                val uriH = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.file_provider",
                                    fileH
                                )
                                val uriV = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.file_provider",
                                    fileV
                                )


                                // 3) Uri -> Multipart (확정 키 이름 사용!)
                                context.uriToPart("imageUrlHorizontal", uriH)
                                context.uriToPart("imageUrlVertical", uriV)


                                val bgIndex = viewModel.backgroundIndexForApi() // ★ new

                                if (isEditing) {
                                    viewModel.updateMyCardMultipart(
                                        context = context,
                                        cardId = editCardId!!,
                                        backgroundImageNum = bgIndex,
                                        imageH = uriH,
                                        imageV = uriV
                                    )
                                } else {
                                    viewModel.submitMyCardMultipart(
                                        context = context,
                                        backgroundImageNum = bgIndex,
                                        imageH = uriH,
                                        imageV = uriV,
                                        includeCustomImage = (viewModel.profileImageUri.value != null)
                                    )
                                }
                            }
                        }
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                                color = MainColor
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            if (isLoading) "등록 중..." else "등록",

                            color = if (canRegister && !isLoading) MainColor else Color(0xFFBDBDBD),
                            fontFamily = pretendardRegular

                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = pretendardRegular)
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {

                // ===== 미리보기 =====
                DigitalPreviewCard(

                    profileUri = previewProfileUri,
//                    bgHex = background,
                    bgHex = bgHex,                 // 이미 계산한 (bgHex, patternCode) 쓰기!
                    patternCode = patternCode,
                    name = name,
                    company = company,
                    phone = phone,
                    position = position,
                    email = email,
                    extras = previewExtras,               // ✅ 체크된 순서대로
                    orientation = CardOrientation.Landscape
                )

                Spacer(Modifier.height(10.dp))

                // ===== 액션 행 =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { navController.navigate("album_image_picker?max=1&from=my_card_edit") }
                    ) { Text("+ 이미지 불러오기", fontSize = 14.sp, color = MainColor) }


                    Spacer(Modifier.width(5.dp))

                    ActionItem(
                        iconRes = R.drawable.ic_mycard_field,
                        text = "필드 선택",
                        onClick = {
                            val curMap = fields.associate { it.label to it.value }
                            navController.currentBackStackEntry?.savedStateHandle?.set("current_values", curMap)
                            navController.navigate("my_card_field")
                        }
                    )

                    Spacer(Modifier.width(5.dp))

                    ActionItem(
                        iconRes = R.drawable.ic_edit,
                        text = "배경 변경",
                        onClick = { navController.navigate("my_card_customize") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ===== 고정 5개 필드 =====
                fixedOrder.forEach { fixedLabel ->
                    val currentValue = fields.firstOrNull { it.label == fixedLabel }?.value.orEmpty()
                    val isPhone = fixedLabel == "연락처"
                    val isRequired = fixedLabel in required
                    val isError = isRequired && currentValue.isBlank()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fixedLabel,
                            fontSize = 13.sp,
                            color = Color.Black,
                            fontWeight = if (isRequired) FontWeight.SemiBold else FontWeight.Normal,
                        )
                        if (isRequired) {
                            Spacer(Modifier.width(4.dp))
                            Text("*", color = Color(0xFFD32F2F), fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    OutlinedTextField(
                        value = if (isPhone) currentValue.filter(Char::isDigit) else currentValue,
                        onValueChange = { newText ->
                            val v = if (isPhone) newText.filter(Char::isDigit).take(11) else newText
                            viewModel.updateOrAddField(fixedLabel, v)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp),
                        isError = isError,
                        supportingText = {
                            if (isError) Text("필수 입력입니다", color = Color(0xFFD32F2F), fontSize = 11.sp)
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = pretendardRegular,
                            fontSize = 16.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = MainColor,
                            focusedBorderColor = MainColor,
                            unfocusedBorderColor = Color(0xFFBDBDBD),
                            focusedContainerColor = Color(0xFFFFFFFF),
                            unfocusedContainerColor = Color(0xFFFFFFFF)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )

                    Spacer(Modifier.height(12.dp))
                }

                // ===== 추가 필드 =====
                val additional = fields.filter { it.label !in fixedOrder }
                additional.forEach { field ->
                    val idx = fields.indexOf(field)
                    MyCardAdditionalFieldRow(
                        label = field.label,
                        value = field.value,
                        onLabelChange = { newLabel ->
                            if (idx >= 0) viewModel.updateField(idx, field.copy(label = newLabel))
                        },
                        onValueChange = { newValue ->
                            if (idx >= 0) viewModel.updateField(idx, field.copy(value = newValue))
                        },
                        onDelete = { if (idx >= 0) viewModel.removeField(idx) }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // ===== 추가 버튼 =====
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.addField() }
                        .padding(top = 4.dp, bottom = 24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mycard_create),
                        contentDescription = "필드 추가",
                        tint = Color(0xFF4C3924),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("필드 추가", color = Color(0xFF4C3924), fontFamily = pretendardRegular)
                }
            }
        }
    }
}


/* ────────────────────── 서브 컴포저블 ────────────────────── */

@Composable
private fun ActionItem(
    iconRes: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MainColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(text = text, fontSize = 14.sp, color = MainColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyCardAdditionalFieldRow(
    label: String,
    value: String,
    onLabelChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }


    // 라벨 줄 (placeholder = "새 필드")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF1F1F1))
            .clickable { showDelete = !showDelete }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        val displayedLabel = if (label == "새 필드") "" else label

        Box(modifier = Modifier.weight(1f)) {
            if (displayedLabel.isBlank()) {
                Text(
                    text = "새 필드",
                    color = Color(0xFF9E9E9E),
                    fontSize = 13.sp
                )
            }
            BasicTextField(
                value = displayedLabel,
                onValueChange = { onLabelChange(it) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = pretendardRegular,
                    fontSize = 13.sp,
                    color = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showDelete) {
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_field_delete),
                contentDescription = "필드 삭제",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDelete() }
            )
        }
    }

    Spacer(Modifier.height(6.dp))

    // 값 입력
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("내용을 입력하세요", color = Color(0xFFBDBDBD)) },
        textStyle = LocalTextStyle.current.copy(
            fontFamily = pretendardRegular, fontSize = 14.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            cursorColor = MainColor,
            focusedBorderColor = MainColor,
            unfocusedBorderColor = Color(0xFFBDBDBD),
            focusedContainerColor = Color(0xFFFFFFFF),
            unfocusedContainerColor = Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(6.dp)
    )
}

@Composable
private fun PreviewLine(
    value: String,
    emphasis: Boolean = false
) {
    if (value.isBlank()) return
    Text(
        text = value,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontFamily = pretendardRegular,
        fontSize = if (emphasis) 18.sp else 12.sp,
        fontWeight = if (emphasis) FontWeight.SemiBold else FontWeight.Normal,
        color = Color.Black
    )
}
