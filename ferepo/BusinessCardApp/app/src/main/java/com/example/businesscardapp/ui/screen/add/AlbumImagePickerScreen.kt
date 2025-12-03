//AlbumImagePickerScreen.kt
package com.example.businesscardapp.ui.screen.add

import android.net.Uri
import android.util.Log
import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background // ✅ 추가
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape // ✅ 추가
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // TopAppBar, etc.
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight // ✅ 추가
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // ✅ 추가
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.ConfirmButton
import com.example.businesscardapp.ui.screen.mycard.MyCardViewModel
import com.example.businesscardapp.ui.theme.BackgroundColor
import com.example.businesscardapp.ui.theme.MainColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumImagePickerScreen(
    navController: NavController,
    albumViewModel: AlbumSelectViewModel,   // ✅ viewModel() 지우고 주입받기
    maxSelectable: Int,
    from: String,
    cardId: String? = null,  // 편집 모드용 cardId 추가
    myCardViewModel: MyCardViewModel = viewModel()
) {
    val allUris by albumViewModel.allUris.collectAsState()
    val selectedUris by albumViewModel.selectedUris.collectAsState()
    val context = LocalContext.current
    var showLimitPopup by remember { mutableStateOf(false) }

    val safeMaxSelectable = maxOf(maxSelectable, 1)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uris = loadAllImagesFromMediaStore(context)
            albumViewModel.setUris(uris)      // ✅ 여기서도 albumViewModel
        } else {
            Toast.makeText(context, "사진 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 상단바 + 그리드
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                TopAppBar(
                    title = { Text("앨범에서 선택", color = MainColor) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = null,
                                tint = MainColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(allUris) { uri ->
                        val isSelected = selectedUris.contains(uri)
                        val selectionFull = selectedUris.size >= safeMaxSelectable && !isSelected
                        val order = selectedUris.indexOf(uri).takeIf { it >= 0 }?.plus(1)

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MainColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (!selectionFull || isSelected) {
                                        albumViewModel.toggleUri(uri) // ✅
                                    } else {
                                        showLimitPopup = true
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )

                            // 선택 순서 배지
                            if (order != null) {
                                Box(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(24.dp)
                                        .background(MainColor, CircleShape)
                                        .align(Alignment.TopStart),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = order.toString(),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } // ← LazyVerticalGrid 닫음
            } // ← Column 닫음

            // 하단 고정 버튼
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ConfirmButton(
                    text = "선택",
                    onClick = {
                        if (selectedUris.isEmpty()) {
                            Toast.makeText(context, "이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
                        } else {
                            val first = selectedUris.first()

                            // ✅ 여기! 이전 화면으로 Uri를 전달
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("picked_profile_image", first)

                            when (from) {
                                "ocr_mycard" -> navController.navigate("ocr_preview/0?from=ocr_mycard") {
                                    popUpTo("album_image_picker") { inclusive = true }
                                }
                                "my_card_edit" -> {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("edit_profile_image", first.toString())
                                    navController.popBackStack()

                                }
                                "edit" -> {
                                    // 편집 모드: OCR 미리보기로 이동하면서 cardId 전달
                                    val cardIdParam = if (cardId != null) "&cardId=$cardId" else ""
                                    navController.navigate("ocr_preview/0?from=edit$cardIdParam") {
                                        popUpTo("album_image_picker") { inclusive = true }
                                    }
                                }
                                else -> navController.navigate("ocr_preview/0?from=ocr") {
                                    popUpTo("album_image_picker") { inclusive = true }
                                }
                            }

                        }
                    },
                    enabled = selectedUris.isNotEmpty(),
                    modifier = Modifier.padding(16.dp)
                )

            }

            // 한도 초과 팝업
            if (showLimitPopup) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Black.copy(alpha = 0.5f),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {}

                    Surface(
                        modifier = Modifier.size(width = 272.dp, height = 128.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        color = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "이미지는 최대 ${safeMaxSelectable}개까지\n선택 가능합니다.",
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                        }
                    }

                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1500)
                        showLimitPopup = false
                    }
                }
            }
        }
    }
}

// Composable 밖
fun loadAllImagesFromMediaStore(context: Context): List<Uri> {
    val uris = mutableListOf<Uri>()
    val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    context.contentResolver.query(
        collection, projection, null, null, sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(collection, id)
            uris.add(contentUri)
        }
    }
    return uris
}
