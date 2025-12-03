//AppNavGraph.kt
package com.example.businesscardapp.ui.navigation

import LauncherScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import android.net.Uri
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.businesscardapp.ui.theme.*
import com.example.businesscardapp.ui.screen.intro.IntroRoute
import com.example.businesscardapp.ui.screen.MainLayout
import com.example.businesscardapp.ui.screen.add.*
import com.example.businesscardapp.ui.screen.camera.*
import com.example.businesscardapp.ui.screen.detail.CardDetailScreen
import com.example.businesscardapp.ui.screen.detail.DigitalCardDetailScreen
import com.example.businesscardapp.ui.screen.group.*
import androidx.navigation.navArgument
import com.example.businesscardapp.ui.screen.mycard.*
import androidx.navigation.NavType
import com.example.businesscardapp.data.network.MyCardRepository
import com.example.businesscardapp.data.network.RetrofitClient
import com.example.businesscardapp.ui.screen.MyCardsShakeScreen
import com.example.businesscardapp.ui.viewmodel.MyCardsShakeViewModel
import com.example.businesscardapp.ui.screen.mycard.MyCardCustommizeScreen
import com.example.businesscardapp.ui.screen.mycard.MyCardFieldSelectScreen
import com.example.businesscardapp.ui.screen.mycard.MyCardCustomizedScreen
import com.example.businesscardapp.ui.viewmodel.CompanyVerifyEmailViewModel
import com.example.businesscardapp.ui.screen.myinfo.CompanyCodeScreen
import com.example.businesscardapp.ui.screen.myinfo.CompanyEmailScreen
import com.example.businesscardapp.ui.screen.myinfo.CompanyVerifyDoneScreen
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun AppNavGraph(navController: NavHostController, albumViewModel: AlbumSelectViewModel) {
    val context = LocalContext.current
    val userId = remember { com.example.businesscardapp.util.PrefUtil.getUserId(context) }

    val myCardViewModel: MyCardViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "launcher"
    ) {
        composable("launcher") {
            LauncherScreen(navController = navController)
        }
        composable("intro") {
            IntroRoute(navController = navController)
        }

        composable("main") {
            MainLayout(appNavController = navController)
        }


        composable("camera") { // ✅ 과거 "camera" 호출 호환
            CameraScreen(
                navController = navController,
                from = "paper",
                myCardViewModel = myCardViewModel
            )
        }
        // 기존
        // composable("camera") { CameraScreen(navController = navController) }
        // 변경
        composable(
            route = "camera?from={from}&cardId={cardId}",
            arguments = listOf(
                navArgument("from"){ type = NavType.StringType; defaultValue = "paper" },
                navArgument("cardId"){ type = NavType.StringType; defaultValue = null; nullable = true }
            )
        ) { be ->
            val from = be.arguments?.getString("from") ?: "paper"
            val cardId = be.arguments?.getString("cardId")
            CameraScreen(
                navController = navController,
                from = from,
                cardId = cardId,
                myCardViewModel = myCardViewModel
            )
        }

        // ── 결과(종이명함): 기존 유지
        composable(
            route = "camera_result/{front}?back={back}&from={from}&cardId={cardId}",
            arguments = listOf(
                navArgument("front"){ type = NavType.StringType },
                navArgument("back"){ type = NavType.StringType; defaultValue = ""; nullable = true },
                navArgument("from"){ type = NavType.StringType; defaultValue = "paper" },
                navArgument("cardId"){ type = NavType.StringType; defaultValue = null; nullable = true }
            )
        ) { be ->
            CameraResultScreen(
                frontImageUri = be.arguments?.getString("front") ?: "",
                backImageUri  = be.arguments?.getString("back") ?: "",
                textResult = "",
                navController = navController,
                from = be.arguments?.getString("from") ?: "paper",
                cardId = be.arguments?.getString("cardId")?.toIntOrNull()
            )
        }

        // ── 결과(내명함 전용): 새 라우트 추가만 (다른 화면엔 영향 X)
        composable(
            route = "camera_result_mycard/{front}?back={back}",
            arguments = listOf(
                navArgument("front"){ type = NavType.StringType },
                navArgument("back"){ type = NavType.StringType; defaultValue = ""; nullable = true }
            )
        ) { be ->
            CameraResultMyCardScreen(
                frontImageUri = be.arguments?.getString("front") ?: "",
                backImageUri  = be.arguments?.getString("back") ?: "",
                navController = navController,
                myCardViewModel = myCardViewModel
            )
        }



        // 앨범 가이드/선택
        composable(
            route = "album_guide?from={from}&max={max}&cardId={cardId}",
            arguments = listOf(
                navArgument("from") { type = NavType.StringType; defaultValue = "ocr" },
                navArgument("max") { type = NavType.IntType; defaultValue = 2 },
                navArgument("cardId") { type = NavType.StringType; defaultValue = null; nullable = true }
            )
        ) { backStackEntry ->
            val from = backStackEntry.arguments?.getString("from") ?: "ocr"
            val max = backStackEntry.arguments?.getInt("max") ?: 2
            val cardId = backStackEntry.arguments?.getString("cardId")

            AlbumGuideScreen(
                navController = navController,
                from = from,
                max = max,
                cardId = cardId
            )
        }


        /// 종이명함 상세 (기존)
        composable(
            route = "card_detail/{cardId}?newImage={newImage}&refresh={refresh}&isFavorite={isFavorite}",
            arguments = listOf(
                navArgument("cardId") { type = NavType.IntType },
                navArgument("newImage") {
                    type = NavType.StringType
                    defaultValue = null
                    nullable = true
                },
                navArgument("refresh") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("isFavorite") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val cardId = backStackEntry.arguments?.getInt("cardId")
            val newImageArg = backStackEntry.arguments?.getString("newImage")
            val refresh = backStackEntry.arguments?.getBoolean("refresh") ?: false
            val isFavorite = backStackEntry.arguments?.getBoolean("isFavorite") ?: false

            // String(경로/URI) → File? 변환
            fun uriToTempFile(uri: Uri): File? = runCatching {
                context.contentResolver.openInputStream(uri)?.use { inStream ->
                    val tmp = File.createTempFile("new_image_", ".jpg", context.cacheDir)
                    tmp.outputStream().use { out -> inStream.copyTo(out) }
                    tmp
                }
            }.getOrNull()

            val newImageFile: File? = when {
                newImageArg.isNullOrBlank() -> null
                newImageArg.startsWith("content://") || newImageArg.startsWith("file://") ->
                    uriToTempFile(Uri.parse(newImageArg))        // 콘텐츠/파일 URI → 임시파일
                else -> File(newImageArg)                        // 순수 경로 문자열
            }

            CardDetailScreen(
                navController = navController,
                cardId = cardId,
                newImage = newImageFile,          // ★ 여기서 File?로 전달
                refresh = refresh,
                isFavorite = isFavorite
            )
        }

        /// 디지털 명함 상세
        composable(
            route = "digital_card_detail/{cardId}?isFavorite={isFavorite}",
            arguments = listOf(
                navArgument("cardId") { type = NavType.StringType },
                navArgument("isFavorite") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")
            val isFavorite = backStackEntry.arguments?.getBoolean("isFavorite") ?: false
            DigitalCardDetailScreen(
                navController = navController,
                cardId = cardId,
                isFavoriteParam = isFavorite
            )
        }

        composable(
            route = "card_detail_name/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedName = backStackEntry.arguments?.getString("name") ?: ""
            val name = Uri.decode(encodedName)
            CardDetailScreen(navController = navController, name = name)
        }

        composable(
            route = "GroupDetail/{groupId}/{name}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: return@composable
            val name = backStackEntry.arguments?.getString("name") ?: ""
            GroupDetailScreen(navController = navController, groupId = groupId, groupName = name)
        }


        composable("group_edit") {
            GroupEditScreen(navController)
        }

        composable(
            route = "group_member_edit/{groupId}/{groupName}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gid = backStackEntry.arguments?.getInt("groupId") ?: return@composable
            val gname = backStackEntry.arguments?.getString("groupName") ?: ""
            GroupMemberEditScreen(
                navController = navController,
                groupId = gid,
                groupName = gname
            )
        }


        // ✅ 앨범 선택 — from 필수 버전만 유지
        composable(
            route = "album_image_picker?max={max}&from={from}&cardId={cardId}",
            arguments = listOf(
                navArgument("max"){ type = NavType.IntType; defaultValue = 2 },
                navArgument("from"){ type = NavType.StringType; defaultValue = "ocr" },
                navArgument("cardId"){ type = NavType.StringType; defaultValue = null; nullable = true }
            )
        ) { be ->
            val from = be.arguments?.getString("from") ?: "ocr"
            val passedMax = be.arguments?.getInt("max") ?: 2
            val finalMax = if (from == "ocr" || from == "ocr_mycard") 2 else passedMax.coerceIn(1, 2)
            val cardId = be.arguments?.getString("cardId")

            AlbumImagePickerScreen(
                navController = navController,
                albumViewModel = albumViewModel,
                maxSelectable = finalMax,
                from = from,                       // "ocr_mycard" 또는 "my_card_edit"
                cardId = cardId,
                myCardViewModel = myCardViewModel
            )
        }



        composable("cardBox") {
            // 명함함 화면으로 이동 (MainLayout의 하단 네비게이션에서 처리)
            navController.navigate("main") {
                popUpTo("main") { inclusive = true }
            }
        }

        composable(
            route = "ocr_preview/{index}?next={next}&from={from}&cardId={cardId}",
            arguments = listOf(
                navArgument("index"){ type = NavType.IntType },
                navArgument("next"){ type = NavType.StringType; defaultValue = ""; nullable = true },
                navArgument("from"){ type = NavType.StringType; defaultValue = "ocr" },
                navArgument("cardId"){ type = NavType.StringType; defaultValue = null; nullable = true }
            )
        ) { be ->
            OcrPreviewScreen(
                navController = navController,
                index = be.arguments?.getInt("index") ?: 0,
                viewModel = viewModel(),
                albumViewModel = albumViewModel,
                from = be.arguments?.getString("from") ?: "ocr",
                cardId = be.arguments?.getString("cardId")
            )
        }


        // ✅ 최상단에서 만든 myCardViewModel 그대로 전달
        composable("my_card_create") {
            MyCardCreateScreen(
                navController = navController,
                onSave = {},
                pickedUri = null,
                myCardViewModel = myCardViewModel
            )
        }


        // (구) 생성 후 편집 화면
        composable("my_card_create/edit") {
            LaunchedEffect(Unit) {
                myCardViewModel.clearForCreateOnce()   // ✅ 이름 맞추기
            }
            MyCardEditScreen(
                navController = navController,
                viewModel = myCardViewModel,
                editCardId = null,
                onSave = { id ->
                    navController.navigate("my_card_detail/$id") {
                        popUpTo("my_card_create") { inclusive = false }
                        launchSingleTop = true
                    } }
            )
        }

        // 예시: AppNavGraph.kt
        composable(
            route = "my_card_edit?mode={mode}&cardId={cardId}&nonce={nonce}",
            arguments = listOf(
                navArgument("mode")   { defaultValue = "create" },
                navArgument("cardId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("nonce")  { defaultValue = 0L } // 새 인스턴스 보장용
            )
        ) { entry ->
            val mode = entry.arguments?.getString("mode") ?: "create"
            val argId = entry.arguments?.getInt("cardId") ?: -1
            val editCardId: Int? = if (mode == "edit" && argId > 0) argId else null

            MyCardEditScreen(
                navController = navController,
                viewModel = myCardViewModel,
                editCardId = editCardId
            )
        }



        composable(
            route = "my_card_edit/{cardId}",
            arguments = listOf(navArgument("cardId"){ type = NavType.IntType })
        ) { be ->
            val id = be.arguments?.getInt("cardId") ?: return@composable
            MyCardEditScreen(
                navController = navController,
                viewModel = myCardViewModel,
                editCardId = id
            )
        }



        composable(
            route = "my_card_detail/{cardId}",
            arguments = listOf(navArgument("cardId"){ type = NavType.IntType })
        ) { be ->
            val id = be.arguments?.getInt("cardId") ?: return@composable
            MyCardDetailScreen(
                navController = navController,
                cardId = id,
                viewModel = myCardViewModel    // ✅ 같은 VM 주입
            )
        }


        // 5) 필드 선택
        composable("my_card_field") {
            MyCardFieldSelectScreen(
                navController = navController,
                viewModel = myCardViewModel
            )
        }

        // 6) 배경 선택
        composable("my_card_customize") {
            MyCardCustommizeScreen(
                navController = navController,
                viewModel = myCardViewModel
            )
        }


        // 일반: 목록 ↔ QR 토글 가능한 화면
        composable("myCardsShake") {
            val vm = remember { MyCardsShakeViewModel(MyCardRepository(RetrofitClient.apiService)) }
            MyCardsShakeScreen(
                viewModel = vm,
                selectOnly = false,
                onPick = { cardId -> navController.navigate("myCardsShake/qr/$cardId") }
            )
        }

        // 위젯 진입: 항상 목록만 (카드 탭 시 QR 화면으로 내비)
        composable("myCardsPick") {
            val vm = remember { MyCardsShakeViewModel(MyCardRepository(RetrofitClient.apiService)) }
            MyCardsShakeScreen(
                viewModel = vm,
                selectOnly = true,
                onPick = { cardId -> navController.navigate("myCardsShake/qr/$cardId") }
            )
        }

        // QR 전용 라우트: 같은 컴포저블을 QR 모드로
        composable(
            route = "myCardsShake/qr/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.IntType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getInt("cardId") ?: return@composable
            val vm = remember { MyCardsShakeViewModel(MyCardRepository(RetrofitClient.apiService)) }
            MyCardsShakeScreen(
                viewModel = vm,
                selectOnly = false,
                startCardId = cardId,   // ★ 시작 카드 지정
                forceQr = true          // ★ 진입 즉시 QR 열기
            )
        }

        composable(
            route = "company/verify/email/{cardId}",
            arguments = listOf(navArgument("cardId"){ type = NavType.IntType })
        ) { be ->
            val id = be.arguments!!.getInt("cardId")

            // ✅ 기본 viewModel()만 사용 (기본 팩토리로 생성)
            val vm: CompanyVerifyEmailViewModel = viewModel()

            CompanyEmailScreen(
                navController = navController,
                cardId = id,
                vm = vm,
                toCodeInputRoute = { cid -> "company/verify/code/$cid" }
            )
        }

        // myCardsSelectForVerify 라우트 추가
        composable("myCardsSelectForVerify") {
            val vm = remember {
                MyCardsShakeViewModel(
                    repo = MyCardRepository(RetrofitClient.apiService)
                )
            }
            MyCardsShakeScreen(
                viewModel = vm,
                selectOnly = true,                               // ★ 선택 모드
                onPick = { cardId ->                             // ★ 이미지 탭 시
                    navController.navigate("company/verify/email/$cardId")
                }
            )
        }

        composable(
            route = "company/verify/code/{cardId}?email={email}",
            arguments = listOf(
                navArgument("cardId"){ type = NavType.IntType },
                navArgument("email"){ type = NavType.StringType; defaultValue = ""; nullable = true }
            )
        ) { be ->
            val id = be.arguments!!.getInt("cardId")
            val email = be.arguments?.getString("email")?.let(Uri::decode).orEmpty()
            CompanyCodeScreen(navController = navController, cardId = id, email = email)
        }

        composable("company/verify/done") {
            CompanyVerifyDoneScreen(navController = navController)
        }

        composable(
            route = "main?tab={tab}",
            arguments = listOf(navArgument("tab") {
                type = NavType.StringType
                defaultValue = "card_box"
            })
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getString("tab") ?: "card_box"
            MainLayout(appNavController = navController, initialTab = tab)
        }

        composable(
            route = "mycard/qr?cardId={cardId}",
            arguments = listOf(
                navArgument("cardId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            MyCardQrScreen()
        }
    }
}

@Composable
fun LauncedEffect(unit: Unit, content: () -> Unit) {

}
