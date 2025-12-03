// ui/screen/mycard/CameraResultMyCardScreen.kt

package com.example.businesscardapp.ui.screen.mycard

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.businesscardapp.util.runMultiOCR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.businesscardapp.ui.screen.mycard.MyCardViewModel


@Composable
fun CameraResultMyCardScreen(
    frontImageUri: String,
    backImageUri: String = "",
    navController: NavController,
    myCardViewModel: MyCardViewModel
) {
    // ✅ 내부에서 VM 생성 (공유 안 해도 됨)
//    val myCardViewModel: MyCardViewModel = viewModel()

    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(frontImageUri, backImageUri) {
        try {
            val outputDir = context.getExternalFilesDir(null) ?: return@LaunchedEffect
            val frontFile = File(outputDir, frontImageUri)
            val backFile = if (backImageUri.isNotEmpty()) File(outputDir, backImageUri) else null

            if (!frontFile.exists()) {
                hasError = true
                Toast.makeText(context, "앞면 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@LaunchedEffect
            }

            val frontUri = Uri.fromFile(frontFile)
            val backUri  = if (backFile?.exists() == true) Uri.fromFile(backFile) else null

            // OCR
            val frontResult = withContext(Dispatchers.IO) { runMultiOCR(frontUri, context) }
            val backResult  = backUri?.let { withContext(Dispatchers.IO) { runMultiOCR(it, context) } }

            // 필드 주입 (이름/연락처/회사/직책/이메일 + 부서(추가필드))
            myCardViewModel.updateOrAddField("이름",   frontResult.name ?: backResult?.name.orEmpty())
            myCardViewModel.updateOrAddField("회사",   frontResult.company ?: backResult?.company.orEmpty())
            myCardViewModel.updateOrAddField("연락처", frontResult.mobile.ifEmpty {
                frontResult.phone.ifEmpty { backResult?.mobile ?: backResult?.phone.orEmpty() }
            })
            myCardViewModel.updateOrAddField("직책",  frontResult.position ?: backResult?.position.orEmpty())
            myCardViewModel.updateOrAddField("이메일", frontResult.email ?: backResult?.email.orEmpty())
            // 선택 추가필드
            myCardViewModel.updateOrAddField("부서",  frontResult.department ?: backResult?.department.orEmpty())

            // 미리보기용 이미지
//            myCardViewModel.setPhotoUri(frontUri)

            // 편집화면 이동
            navController.navigate("my_card_create/edit") { launchSingleTop = true }
        } catch (e: Exception) {
            hasError = true
            Toast.makeText(context, "OCR 처리 중 오류 발생", Toast.LENGTH_SHORT).show()
        } finally {
            isProcessing = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        when {
            isProcessing -> CircularProgressIndicator()
            hasError     -> Text("OCR 실패", color = Color.Red)
            else         -> Text("편집 화면으로 이동 중...")
        }
    }
}