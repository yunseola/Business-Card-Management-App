package com.example.businesscardapp.ui.screen.camera

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.businesscardapp.R
import com.example.businesscardapp.data.model.*
import com.example.businesscardapp.ui.theme.*
import com.example.businesscardapp.ui.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.coroutines.delay

// 로딩 화면 컴포넌트
@Composable
fun RegisterLoadingScreen(
    onProcessingComplete: () -> Unit
) {
    var rotation by remember { mutableStateOf(0f) }
    var hasCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 2초 후에 처리 완료
        delay(2000)
        if (!hasCompleted) {
            Log.d("RegisterLoadingScreen", "로딩 완료, 콜백 호출")
            hasCompleted = true
            onProcessingComplete()
        }
    }

    LaunchedEffect(Unit) {
        // 로딩 애니메이션
        while (true) {
            delay(50)
            rotation += 5f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F3ED))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 로딩 아이콘
            Icon(
                painter = painterResource(id = R.drawable.ic_loading),
                contentDescription = "로딩",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(
                        rotationZ = rotation
                    ),
                tint = Color(0xFF4C3924)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 로딩 메시지
            Text(
                text = "명함 정보를 등록 중입니다",
                fontFamily = pretendardRegular,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color(0xFF4C3924)
            )

            Text(
                text = "잠시만 기다려 주세요",
                fontFamily = pretendardRegular,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF4C3924)
            )
        }
    }
}

// 전화번호 포맷팅 함수
fun formatPhoneNumber(phone: String): String {
    return when {
        phone.length == 11 -> "${phone.substring(0, 3)}-${phone.substring(3, 7)}-${phone.substring(7)}"
        phone.length == 10 -> "${phone.substring(0, 3)}-${phone.substring(3, 6)}-${phone.substring(6)}"
        else -> phone
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraResultScreen(
    frontImageUri: String,
    backImageUri: String = "",
    textResult: String,
    navController: NavController,
    from: String = "paper",
    cardId: Int? = null
) {
    val context = LocalContext.current

    // 🔶 종이명함 등록/업데이트 VM
    val paperCardViewModel: PaperCardViewModel = viewModel()

    // 🔶 OCR/입력 상태
    var recognizedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var frontFileUri by remember { mutableStateOf<Uri?>(null) }
    var backFileUri by remember { mutableStateOf<Uri?>(null) }
    var ocrCompleted by remember { mutableStateOf(false) }
    var isComponentActive by remember { mutableStateOf(true) }
    var isRegistering by remember { mutableStateOf(false) }  // 등록 중 로딩 상태

    // 🔶 명함 정보 상태 (화면 입력 값)
    var name by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var companyPhone by remember { mutableStateOf("") }
    var fax by remember { mutableStateOf("") }
    var dynamicFields by remember { mutableStateOf(mutableListOf<Pair<String, String>>()) }

    // 🔶 편집 모드
    val isEditMode = from == "edit" && cardId != null
    Log.d("CameraResultScreen", "컴포넌트 초기화 - frontImageUri: $frontImageUri, backImageUri: $backImageUri")
    Log.d("CameraResultScreen", "편집 모드 감지: $isEditMode, cardId: $cardId")
    
    // 🔶 URL 파라미터 제거하여 실제 파일 경로 추출
    val cleanFrontImageUri = if (frontImageUri.contains("?")) {
        frontImageUri.substringBefore("?")
    } else {
        frontImageUri
    }
    val cleanBackImageUri = if (backImageUri.contains("?")) {
        backImageUri.substringBefore("?")
    } else {
        backImageUri
    }
    Log.d("CameraResultScreen", "정리된 경로 - cleanFrontImageUri: $cleanFrontImageUri, cleanBackImageUri: $cleanBackImageUri")

    // 🔶 필요한 변수들 정의
    val outputDir = context.getExternalFilesDir(null)
    val allFields = mutableListOf<PaperCardField>()
    
    // allFields에 값 추가
    if (position.isNotEmpty()) allFields.add(PaperCardField(null, "직책", position))
    if (email.isNotEmpty()) allFields.add(PaperCardField(null, "이메일", email))
    if (address.isNotEmpty()) allFields.add(PaperCardField(null, "회사 주소", address))
    if (companyPhone.isNotEmpty()) allFields.add(PaperCardField(null, "회사 전화번호", companyPhone))
    if (fax.isNotEmpty()) allFields.add(PaperCardField(null, "팩스", fax))
    
    dynamicFields.forEach { (label, value) ->
        if (value.isNotEmpty()) allFields.add(PaperCardField(null, label, value))
    }
    
    // allFields를 List로 변환
    val allFieldsList: List<PaperCardField> = allFields.toList()

         // =========================
     // OCR 처리 (원본 로직 유지)
     // =========================
    LaunchedEffect(cleanFrontImageUri, cleanBackImageUri) {
        try {
            if (!hasError && isProcessing && !ocrCompleted && isComponentActive) {
                val outputDir = context.getExternalFilesDir(null)
                if (outputDir != null) {
                    val frontFile = File(outputDir, cleanFrontImageUri)
                    val backFile = if (cleanBackImageUri.isNotEmpty()) File(outputDir, cleanBackImageUri) else null

                    if (frontFile.exists()) {
                        val currentFrontFileUri = Uri.fromFile(frontFile)
                        val currentBackFileUri = if (backFile != null && backFile.exists()) Uri.fromFile(backFile) else null

                        val frontText = withContext(Dispatchers.IO) {
                            com.example.businesscardapp.util.runMultiOCR(currentFrontFileUri, context)
                        }
                        val backText = if (currentBackFileUri != null) {
                            withContext(Dispatchers.IO) {
                                com.example.businesscardapp.util.runMultiOCR(currentBackFileUri, context)
                            }
                        } else {
                            com.example.businesscardapp.util.BusinessCardInfo()
                        }

                        if (isProcessing && !ocrCompleted && isComponentActive) {
                            val combinedRawText = if (backText.rawText.isNotEmpty()) {
                                "${frontText.rawText}\n\n${backText.rawText}"
                            } else {
                                frontText.rawText
                            }
                            recognizedText = combinedRawText
                            frontFileUri = currentFrontFileUri
                            backFileUri = currentBackFileUri

                            // 값 주입
                            name       = frontText.name.ifEmpty { backText.name }
                            position   = frontText.position.ifEmpty { backText.position }
                            department = frontText.department.ifEmpty { backText.department }
                            phone = when {
                                frontText.mobile.isNotEmpty() -> frontText.mobile
                                backText.mobile.isNotEmpty()  -> backText.mobile
                                frontText.phone.isNotEmpty()  -> frontText.phone
                                else                          -> backText.phone
                            }
                            email   = frontText.email.ifEmpty { backText.email }
                            company = frontText.company.ifEmpty { backText.company }
                            address = frontText.address.ifEmpty { backText.address }

                            val newDynamicFields = mutableListOf<Pair<String, String>>()
                            if (department.isNotEmpty()) newDynamicFields.add("부서" to department)
                            if (address.isNotEmpty())    newDynamicFields.add("회사 주소" to address)
                            if (frontText.website.isNotEmpty() || backText.website.isNotEmpty()) {
                                newDynamicFields.add("웹사이트" to frontText.website.ifEmpty { backText.website })
                            }
                            if (frontText.phone.isNotEmpty() || backText.phone.isNotEmpty()) {
                                newDynamicFields.add("회사 번호" to frontText.phone.ifEmpty { backText.phone })
                            }
                            if (frontText.fax.isNotEmpty() || backText.fax.isNotEmpty()) {
                                newDynamicFields.add("팩스 번호" to frontText.fax.ifEmpty { backText.fax })
                            }
                            if (frontText.mobile.isNotEmpty() || backText.mobile.isNotEmpty()) {
                                newDynamicFields.add("전화번호" to frontText.mobile.ifEmpty { backText.mobile })
                            }
                            dynamicFields = newDynamicFields

                            isProcessing = false
                            ocrCompleted = true
                        }
                    } else {
                        hasError = true
                        isProcessing = false
                        ocrCompleted = true
                        Toast.makeText(context, "촬영된 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    hasError = true
                    isProcessing = false
                    ocrCompleted = true
                    Toast.makeText(context, "저장 디렉토리를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("CameraResultScreen", "OCR 처리 오류", e)
            if (isProcessing && !ocrCompleted && isComponentActive) {
                hasError = true
                isProcessing = false
                ocrCompleted = true
                Toast.makeText(context, "텍스트 인식 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isComponentActive = false
            isProcessing = false
            ocrCompleted = true
        }
    }

    // ✅ 등록 성공/실패 처리
    val isRegisterSuccess by paperCardViewModel.isSuccess.collectAsState()
    val registerError by paperCardViewModel.error.collectAsState()
    LaunchedEffect(isRegisterSuccess) {
        if (isRegisterSuccess) {
            Toast.makeText(context, "종이명함이 등록되었습니다!", Toast.LENGTH_SHORT).show()
            val id = paperCardViewModel.registeredCardId.value
            if (id != null) {
                navController.navigate("card_detail/$id") {
                    popUpTo("main") { inclusive = false }
                }
            } else {
                navController.navigate("cardBox") {
                    popUpTo("main") { inclusive = false }
                }
            }
        }
    }
    LaunchedEffect(registerError) {
        registerError?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
    // 에러 시 처리 (별도의 LaunchedEffect로 분리)
    LaunchedEffect(paperCardViewModel.error.collectAsState().value) {
        paperCardViewModel.error.value?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // =========================
    // UI
    // =========================
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { navController.popBackStack() }
                                .padding(end = 8.dp),
                            tint = Color.Black
                        )
                        Text(
                            text = "명함 정보 확인",
                            fontSize = 20.sp,
                            fontFamily = pretendardMedium,
                            color = Color.Black
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 18.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // 🔶 필수값 체크
                                if (name.isBlank() || phone.isBlank() || company.isBlank()) {
                                    Toast.makeText(context, "필수 정보(이름, 전화번호, 회사)를 입력해 주세요", Toast.LENGTH_SHORT).show()
                                    return@clickable
                                }
                                // 편집 모드에 따라 다른 API 호출
                                if (isEditMode && cardId != null) {
                                    // 편집 모드: 기존 카드 업데이트
                                    val updateRequest = UpdatePaperCardRequest(
                                        name = name,
                                        phone = phone,
                                        company = company,
                                        position = if (position.isNotEmpty()) position else null,
                                        email = if (email.isNotEmpty()) email else null,
                                        fields = if (allFields.isNotEmpty()) allFields.map { UpdateField(fieldId = null, fieldName = it.fieldName, fieldValue = it.fieldValue) } else null,
                                        groups = emptyList() // 빈 리스트로 설정하여 null 방지
                                    )
                                    
                                    // 이미지 파일 준비
                                    val image1File = if (outputDir != null) File(outputDir, cleanFrontImageUri) else null
                                    val image2File = if (cleanBackImageUri.isNotEmpty() && outputDir != null) File(outputDir, cleanBackImageUri) else null
                                    
                                    paperCardViewModel.updatePaperCard(cardId, updateRequest, image1File, image2File)
                                    Toast.makeText(context, "명함 정보 수정 중...", Toast.LENGTH_SHORT).show()

                                    // 편집 완료 후 CardDetailScreen으로 돌아가기 (즉시 갱신을 위해)
                                    navController.navigate("card_detail/$cardId?refresh=true") {
                                        popUpTo("camera") { inclusive = true }
                                    }
                                } else {
                                    // 새 명함 등록 모드 - 로딩 화면 표시
                                    isRegistering = true
                                    paperCardViewModel.registerPaperCard(
                                        name = name,
                                        phone = phone,
                                        company = company,
                                        position = if (position.isNotEmpty()) position else null,
                                        email = if (email.isNotEmpty()) email else null,
                                        fields = if (allFields.isNotEmpty()) allFields else null,
                                        image1File = File(outputDir, cleanFrontImageUri),
                                        image2File = if (cleanBackImageUri.isNotEmpty()) File(outputDir, cleanBackImageUri) else null
                                    )
                                }
                            }
                    ) {
                        Text(
                            text = if (isEditMode) "수정" else "등록",
                            fontFamily = pretendardMedium,
                            fontSize = 20.sp,
                            color = Color.Black,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (name.isBlank() || phone.isBlank() || company.isBlank()) {
                                    Toast.makeText(context, "필수 정보(이름, 전화번호, 회사)를 입력해 주세요", Toast.LENGTH_SHORT).show()
                                    return@clickable
                                }
                                if (isEditMode && cardId != null) {
                                    val updateRequest = UpdatePaperCardRequest(
                                        name = name,
                                        phone = phone,
                                        company = company,
                                        position = if (position.isNotEmpty()) position else null,
                                        email = if (email.isNotEmpty()) email else null,
                                        fields = if (allFields.isNotEmpty()) allFields.map { UpdateField(fieldId = null, fieldName = it.fieldName, fieldValue = it.fieldValue) } else null,
                                        groups = emptyList() // 빈 리스트로 설정하여 null 방지
                                    )
                                    
                                    // 이미지 파일 준비
                                    val image1File = if (outputDir != null) File(outputDir, cleanFrontImageUri) else null
                                    val image2File = if (cleanBackImageUri.isNotEmpty() && outputDir != null) File(outputDir, cleanBackImageUri) else null
                                    
                                    paperCardViewModel.updatePaperCard(cardId, updateRequest, image1File, image2File)
                                    Toast.makeText(context, "명함 정보 수정 중...", Toast.LENGTH_SHORT).show()
                                    navController.navigate("card_detail/$cardId?refresh=true") {
                                        popUpTo("camera") { inclusive = true }
                                    }
                                } else {
                                    // ✅ 신규: 중복 검사 없이 즉시 등록
                                    registerNow(
                                        name, phone, company, position, email, address, companyPhone, fax,
                                        dynamicFields, cleanFrontImageUri, cleanBackImageUri, paperCardViewModel, context, outputDir, allFieldsList
                                    )
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== 이미지 영역 =====
            Column {
                val currentFrontFileUri = frontFileUri
                val currentBackFileUri = backFileUri
                if (currentFrontFileUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentFrontFileUri)
                            .crossfade(true)
                            .allowHardware(false)
                            .build(),
                        contentDescription = "촬영된 명함 이미지",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f)
                            .background(Color(0xFFF0F0F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "명함 이미지",
                            fontFamily = pretendardRegular,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
                if (backImageUri.isNotEmpty() && currentBackFileUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentBackFileUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "촬영된 명함 뒷면 이미지",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f)
                            .padding(top = 16.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // ===== 안내/로딩/오류 UI =====
            if (isProcessing) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF8B4513)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "정보를 인식하는 중...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else if (hasError) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "오류가 발생했습니다", style = MaterialTheme.typography.titleMedium, color = Color.Red)
                    Text(text = "다시 시도해주세요", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                if (recognizedText.isEmpty() && !isProcessing && !hasError) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "텍스트 인식에 실패했습니다",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "아래 필드에 직접 정보를 입력해주세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                } else if (recognizedText.isNotEmpty() && !isProcessing && !hasError) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "텍스트 인식 완료",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "인식된 정보를 확인하고 필요 시 수정해주세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                // ===== 입력 폼 =====
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val requiredFields = mutableListOf<Pair<String, String>>()
                    requiredFields.add("이름" to name)
                    requiredFields.add("전화번호" to phone)
                    requiredFields.add("회사" to company)
                    requiredFields.add("직책" to position)
                    requiredFields.add("이메일" to email)

                    requiredFields.forEach { (label, value) ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = label,
                                fontFamily = pretendardRegular,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = value,
                                onValueChange = {
                                    when (label) {
                                        "이름" -> name = it
                                        "전화번호" -> phone = it.filter { ch -> ch.isDigit() }
                                        "직책" -> position = it
                                        "이메일" -> email = it
                                        "회사" -> company = it
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                singleLine = true,
                                keyboardOptions = when (label) {
                                    "전화번호" -> KeyboardOptions(keyboardType = KeyboardType.Phone)
                                    "이메일" -> KeyboardOptions(keyboardType = KeyboardType.Email)
                                    else -> KeyboardOptions.Default
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4C3924),
                                    unfocusedBorderColor = Color(0xFFBDBDBD),
                                    focusedContainerColor = Color(0xFFFFFFFF),
                                    unfocusedContainerColor = Color(0xFFFFFFFF)
                                ),
                                shape = RoundedCornerShape(7.dp),
                                textStyle = TextStyle(
                                    fontFamily = pretendardMedium,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            )
                        }
                    }

                    dynamicFields.forEach { (label, value) ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = label,
                                fontFamily = pretendardRegular,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = value,
                                onValueChange = { newValue ->
                                    val updated = dynamicFields.toMutableList()
                                    val index = updated.indexOfFirst { it.first == label }
                                    if (index != -1) {
                                        updated[index] = label to newValue
                                        dynamicFields = updated
                                    }
                                    when (label) {
                                        "부서" -> department = newValue
                                        "이메일" -> email = newValue
                                        "회사 주소" -> address = newValue
                                        "회사 번호" -> companyPhone = newValue.filter { ch -> ch.isDigit() }
                                        "팩스 번호" -> fax = newValue.filter { ch -> ch.isDigit() }
                                        "전화번호" -> phone = newValue.filter { ch -> ch.isDigit() }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                singleLine = true,
                                keyboardOptions = when (label) {
                                    "전화번호", "회사 번호", "팩스 번호" -> KeyboardOptions(keyboardType = KeyboardType.Phone)
                                    "이메일" -> KeyboardOptions(keyboardType = KeyboardType.Email)
                                    "웹사이트" -> KeyboardOptions(keyboardType = KeyboardType.Uri)
                                    else -> KeyboardOptions.Default
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4C3924),
                                    unfocusedBorderColor = Color(0xFFBDBDBD),
                                    focusedContainerColor = Color(0xFFFFFFFF),
                                    unfocusedContainerColor = Color(0xFFFFFFFF)
                                ),
                                shape = RoundedCornerShape(7.dp),
                                textStyle = TextStyle(
                                    fontFamily = pretendardMedium,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }

                // 등록 중 로딩 화면 표시
                if (isRegistering) {
                    RegisterLoadingScreen(
                        onProcessingComplete = {
                            isRegistering = false
                            // 등록 완료 후 처리
                            Toast.makeText(context, "종이명함이 등록되었습니다!", Toast.LENGTH_SHORT).show()
                            // 등록된 cardId로 상세 화면 이동
                            val cardId2 = paperCardViewModel.registeredCardId.value
                            if (cardId2 != null) {
                                navController.navigate("card_detail/$cardId2") {
                                    popUpTo("camera") { inclusive = true }
                                }
                            } else {
                                navController.navigate("cardBox") {
                                    popUpTo("camera") { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/** 실제 등록 API 호출 */
private fun registerNow(
    name: String,
    phone: String,
    company: String,
    position: String,
    email: String,
    address: String,
    companyPhone: String,
    fax: String,
    dynamicFields: List<Pair<String, String>>,
    cleanFrontImageUri: String,
    cleanBackImageUri: String,
    paperCardViewModel: PaperCardViewModel,
    context: android.content.Context,
    outputDir: File?,
    allFields: List<PaperCardField>
) {

    // outputDir가 null이면 등록할 수 없음
    if (outputDir == null) {
        Toast.makeText(context, "파일 저장 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        return
    }
    
    val image1File = File(outputDir, cleanFrontImageUri)
    val image2File = if (cleanBackImageUri.isNotEmpty()) File(outputDir, cleanBackImageUri) else null
    
    paperCardViewModel.registerPaperCard(
        name = name,
        phone = phone,
        company = company,
        position = if (position.isNotEmpty()) position else null,
        email = if (email.isNotEmpty()) email else null,
        fields = if (allFields.isNotEmpty()) allFields else null,
        image1File = image1File,
        image2File = image2File
    )

    Toast.makeText(context, "종이명함 등록 중...", Toast.LENGTH_SHORT).show()
}
