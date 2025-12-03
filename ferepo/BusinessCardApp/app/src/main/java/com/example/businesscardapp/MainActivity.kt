package com.example.businesscardapp

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.businesscardapp.data.local.TokenProvider
import com.example.businesscardapp.ui.navigation.AppNavGraph
import com.example.businesscardapp.ui.screen.add.AlbumSelectViewModel
import com.example.businesscardapp.ui.theme.BusinessCardAppTheme
import com.example.businesscardapp.util.PrefUtil
import com.example.businesscardapp.util.ShakeForegroundService
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import android.provider.Settings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val REQ_POST_NOTI = 1001
        private const val REQ_ROLE_SCREENING = 2001
    }

    init {
        try {
            System.loadLibrary("opencv_java4")
            Log.d("OpenCV", "Library loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("OpenCV", "Failed to load native lib", e)
        }
    }

    private val albumViewModel: AlbumSelectViewModel by viewModels()

    // ✅ 위젯/알림에서 보낸 목적지 값을 보관 (Compose에서 관찰)
    private val routeState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenProvider.token = PrefUtil.getJwtToken(this)
        Log.d("MainActivity", "앱 시작 시 로드된 JWT: ${TokenProvider.token}")

        // 최초 인텐트에서 목적지 추출( route 또는 dest 둘 다 수신 지원 )
        routeState.value = normalizeRouteFromIntent(intent)

        setContent {
            BusinessCardAppTheme {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setStatusBarColor(color = Color.White, darkIcons = true)
                }

                val navController = rememberNavController()

                // Activity의 routeState를 Compose에서 구독
                val startRoute by routeState

                Box(Modifier.fillMaxSize()) {
                    AppNavGraph(
                        navController = navController,
                        albumViewModel = albumViewModel
                    )
                }

                // ✅ 목적지에 따라 네비게이션 (한 번 처리 후 routeState 초기화)
                LaunchedEffect(startRoute) {
                    when (startRoute) {
                        "mycard/share" -> {
                            Log.d("MainActivity", "Navigate: my_card_share")
                            navController.navigate("my_card_share")
                            routeState.value = null
                            intent?.removeExtra("route")
                            intent?.removeExtra("dest")
                        }
                        "mycard/shake" -> {
                            Log.d("MainActivity", "Navigate: myCardsPick")
                            navController.navigate("myCardsPick")
                            routeState.value = null
                            intent?.removeExtra("route")
                            intent?.removeExtra("dest")
                        }
                        "camera" -> {
                            Log.d("MainActivity", "Navigate: camera")
                            navController.navigate("camera")
                            routeState.value = null
                            intent?.removeExtra("route")
                            intent?.removeExtra("dest")
                        }
                        "mycard/qr" -> {
                            val cardId = intent?.getIntExtra("cardId", -1)?.takeIf { it > 0 }
                            if (cardId != null) {
                                Log.d("MainActivity", "Navigate: mycard/qr?cardId=$cardId")
                                navController.navigate("mycard/qr?cardId=$cardId")
                            } else {
                                Log.w("MainActivity", "cardId 없음 → mycard/qr로만 이동")
                                navController.navigate("mycard/qr")
                            }
                            routeState.value = null
                            intent?.removeExtra("route")
                            intent?.removeExtra("cardId")   // ✅ 사용 후 제거
                            intent?.removeExtra("dest")
                        }
                        null -> Unit
                    }
                }
            }
        }

        // 1) 알림 권한 확보 후 흔들기 서비스 시작
        ensureNotiPermissionThenStartShakeService()

        // 2) 전화 스크리닝 앱 역할 등록 요청
        requestCallScreeningRoleIfNeeded()

        // 3) 👉 오버레이 권한 요청 (여기에 추가)
        ensureOverlayPermission()
    }

    /** "다른 앱 위에 표시" 권한 없으면 설정 화면으로 이동 */
    private fun ensureOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }

    // 위젯/알림에서 새 인텐트가 들어오면 목적지 갱신 → Compose가 자동 반응
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeState.value = normalizeRouteFromIntent(intent)
    }

    // ----------------- 흔들기 포그라운드 서비스 -----------------
    private fun startShakeService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, ShakeForegroundService::class.java)
        )
    }

    private fun ensureNotiPermissionThenStartShakeService() {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                startShakeService()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQ_POST_NOTI
                )
            }
        } else {
            startShakeService()
        }
    }

    // ----------------- Call Screening 역할 요청 -----------------
    private fun requestCallScreeningRoleIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29+
            val rm = getSystemService(RoleManager::class.java)
            Log.d(
                "Role",
                "CALL_SCREENING available=${rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)}, " +
                        "held=${rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)}"
            )
            if (rm != null && !rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                Log.w("MainActivity", "ROLE_CALL_SCREENING not available on this device")
                return
            }
            if (rm != null && !rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                startActivityForResult(intent, REQ_ROLE_SCREENING)
            } else {
                Log.d("MainActivity", "ROLE_CALL_SCREENING already granted")
            }
        } else {
            Log.d("MainActivity", "API < 29: CallScreeningService 미지원")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_POST_NOTI -> startShakeService()
        }
    }

    @Deprecated("startActivityForResult is fine here for simplicity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ROLE_SCREENING) {
            Log.d("MainActivity", "ROLE_CALL_SCREENING result=$resultCode")
        }
    }

    // ----------------- 인텐트 → 내부 라우트 정규화 -----------------
    /**
     * 외부에서 전달된 인텐트에서 목적지를 추출해 내부 라우트로 정규화한다.
     * - route: 기존 위젯/내부 규격 (예: "mycard/shake")
     * - dest : 서비스/알림에서 보내는 목적지 (예: "myCardsPick")
     */
    private fun normalizeRouteFromIntent(i: Intent?): String? {
        if (i == null) return null

        // 1) 기존 route 우선 사용
        val route = i.getStringExtra("route")
        if (!route.isNullOrBlank()) return route

        // 2) 새 dest 매핑 지원
        return when (i.getStringExtra("dest")) {
            "myCardsPick"   -> "mycard/shake"   // 명함 목록(선택 전용) 화면
            "my_card_share" -> "mycard/share"   // 공유 화면으로 맵핑
            "camera"        -> "camera"
            else -> null
        }
    }
}
