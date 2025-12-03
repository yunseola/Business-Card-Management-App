//AlbumSelectScreen.kt
package com.example.businesscardapp.ui.screen.add

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.ConfirmButton
import com.example.businesscardapp.ui.theme.BackgroundColor
import com.example.businesscardapp.ui.theme.GrayColor
import com.example.businesscardapp.ui.theme.MainColor
import com.example.businesscardapp.ui.viewmodel.PaperCardViewModel
import com.example.businesscardapp.util.BusinessCardInfo
import com.example.businesscardapp.util.runTextRecognition
import com.example.businesscardapp.data.model.PaperCardField
import android.util.Log
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AlbumSelectScreen(navController: NavController) {
    val viewModel: AlbumSelectViewModel = viewModel()
    val paperCardViewModel: PaperCardViewModel = viewModel()
    val selectedUris by viewModel.selectedUris.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ✅ 종이명함 등록 상태
    val isSuccess by paperCardViewModel.isSuccess.collectAsState()
    val isLoading by paperCardViewModel.isLoading.collectAsState()
    val error by paperCardViewModel.error.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.size > 2) {
                Toast.makeText(context, "최대 2장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.setUris(uris)
            }
        }
    )

    LaunchedEffect(Unit) {
        launcher.launch("image/*")
    }

    // ✅ 종이명함 등록 성공 처리
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "종이명함이 등록되었습니다!", Toast.LENGTH_SHORT).show()
            
            // ✅ 등록된 cardId로 상세 화면 이동
            val cardId = paperCardViewModel.registeredCardId.value
            if (cardId != null) {
                navController.navigate("card_detail/$cardId") {
                    popUpTo("main") { inclusive = false }
                }
            } else {
                navController.navigate("cardBox") {
                    popUpTo("main") { inclusive = false }
                }
            }
        }
    }

    // ✅ 에러 처리
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            Toast.makeText(context, "오류: $errorMessage", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 🔙 뒤로가기 아이콘 좌측 상단 정렬
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp), // 여백 조정
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = null,
                        tint = MainColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "이미지를 선택해주세요",
                style = MaterialTheme.typography.titleLarge.copy(color = MainColor)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "(최대 2장까지 선택 가능합니다)",
                style = MaterialTheme.typography.bodyMedium.copy(color = GrayColor),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(selectedUris) { uri ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(200.dp)
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            ConfirmButton(
                text = if (isLoading) "처리 중..." else "종이명함 등록",
                onClick = {
                    if (selectedUris.isNotEmpty()) {
                        coroutineScope.launch {
                            try {
                                // ✅ 1. OCR 처리
                                Log.d("AlbumSelectScreen", "OCR 처리 시작")
                                val ocrResults = mutableListOf<BusinessCardInfo>()
                                
                                for (uri in selectedUris) {
                                    val businessCardInfo = runTextRecognition(uri, context)
                                    ocrResults.add(businessCardInfo)
                                    Log.d("AlbumSelectScreen", "OCR 결과: $businessCardInfo")
                                }

                                // ✅ 2. 종이명함 등록 API 호출
                                if (ocrResults.isNotEmpty()) {
                                    // ✅ 첫 번째 이미지의 OCR 결과로 등록
                                    val firstResult = ocrResults.first()
                                    
                                    // ✅ 이미지 파일 생성
                                    val frontImageFile = File(selectedUris.firstOrNull()?.path ?: "")
                                    val backImageFile = if (selectedUris.size > 1) File(selectedUris[1].path ?: "") else null
                                    
                                    // ✅ PaperCardField 리스트 생성 (이메일은 상위 필드로 전달되므로 제외)
                                    val fields = listOf(
                                        "직책" to firstResult.position.ifEmpty { "" },
                                        "부서" to firstResult.department.ifEmpty { "" },
                                        "주소" to firstResult.address.ifEmpty { "" }
                                    ).filter { it.second.isNotEmpty() }
                                    .map { (fieldName, fieldValue) ->
                                        PaperCardField(fieldName = fieldName, fieldValue = fieldValue)
                                    }
                                    
                                    paperCardViewModel.registerPaperCard(
                                        name = firstResult.name.ifEmpty { "이름 없음" },
                                        phone = firstResult.phone.ifEmpty { "전화번호 없음" },
                                        company = firstResult.company.ifEmpty { "회사 없음" },
                                        position = firstResult.position.ifEmpty { "직책 없음" },
                                        email = firstResult.email.ifEmpty { null },
                                        fields = fields,
                                        image1File = frontImageFile,
                                        image2File = backImageFile
                                    )
                                } else {
                                    Toast.makeText(context, "OCR 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("AlbumSelectScreen", "종이명함 등록 실패", e)
                                Toast.makeText(context, "종이명함 등록에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = selectedUris.isNotEmpty() && !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
