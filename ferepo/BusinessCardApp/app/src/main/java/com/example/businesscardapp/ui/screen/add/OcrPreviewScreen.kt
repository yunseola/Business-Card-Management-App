//OcrPreviewScreen.kt
// ui/screen/add/OcrPreviewScreen.kt
package com.example.businesscardapp.ui.screen.add

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.ConfirmButton
import com.example.businesscardapp.ui.component.EditableQuadOverlay
import com.example.businesscardapp.ui.theme.*
import com.example.businesscardapp.ui.viewmodel.OcrViewModel
import com.example.businesscardapp.util.cropBitmapWithCorners
import com.example.businesscardapp.util.saveBitmapToInternalStorage
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrPreviewScreen(
    navController: NavController,
    index: Int,
    viewModel: OcrViewModel = viewModel(),
    albumViewModel: AlbumSelectViewModel = viewModel(),
    from: String = "ocr",
    cardId: String? = null  // 편집 모드용 cardId 추가

) {
    val context = LocalContext.current
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = index)
    val coroutineScope = rememberCoroutineScope()

    val selectedUris by albumViewModel.selectedUris.collectAsState()
    val imageBitmap by viewModel.fullBitmap.collectAsState(initial = null)



    // 스와이프 시 이미지 새로 로드
    LaunchedEffect(listState.firstVisibleItemIndex, selectedUris) {
        selectedUris.getOrNull(listState.firstVisibleItemIndex)?.let { uri ->
            viewModel.resetCorners()
            viewModel.loadAndDetectCard(context, uri)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundColor) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopAppBar(
                title = {},
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

            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                itemsIndexed(selectedUris) { pageIndex, uri ->
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        val canvasW = constraints.maxWidth
                        val canvasH = constraints.maxHeight

                        imageBitmap?.let { bitmap ->
                            val scale = minOf(
                                canvasW.toFloat() / bitmap.width,
                                canvasH.toFloat() / bitmap.height
                            )
                            val dispW = (bitmap.width * scale).toInt()
                            val dispH = (bitmap.height * scale).toInt()
                            val offX = (canvasW - dispW) / 2
                            val offY = (canvasH - dispH) / 2

                            val density = LocalDensity.current
                            val dispWdp = with(density) { dispW.toDp() }
                            val dispHdp = with(density) { dispH.toDp() }

                            Box(
                                modifier = Modifier
                                    .size(dispWdp, dispHdp)
                                    .offset { IntOffset(offX, offY) },
                                contentAlignment = Alignment.TopStart
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )

                                EditableQuadOverlay(
                                    corners = viewModel.corners.value,
                                    onCornerMoved = { idx, off -> viewModel.updateCorner(idx, off) },
                                    imageWidth = bitmap.width,
                                    imageHeight = bitmap.height,
                                    canvasWidth = dispW,
                                    canvasHeight = dispH,
                                    imageOffsetX = 0,
                                    imageOffsetY = 0
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "명함의 테두리를 확인해주세요\n(${listState.firstVisibleItemIndex + 1}/${selectedUris.size})",
                style = MaterialTheme.typography.bodyMedium.copy(color = GrayColor),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ConfirmButton(
                text = "확인",
                onClick = {
                    if (selectedUris.isEmpty()) {
                        Toast.makeText(context, "이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
                        return@ConfirmButton
                    }

                    val bitmap1 = viewModel.fullBitmap.value
                    val corners1 = viewModel.corners.value
                    if (bitmap1 == null || corners1.size != 4) {
                        Toast.makeText(context, "첫 번째 이미지의 인식 영역을 지정하세요.", Toast.LENGTH_SHORT).show()
                        return@ConfirmButton
                    }

                    val file1 = saveBitmapToInternalStorage(context, cropBitmapWithCorners(bitmap1, corners1))
                        ?: run {
                            Toast.makeText(context, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            return@ConfirmButton
                        }

                    val resultBase = if (from == "ocr_mycard" || from == "mycard")
                        "camera_result_mycard" else "camera_result"
                    
                    // 편집 모드일 때 cardId 파라미터 추가
                    val cardIdParam = if (from == "edit" && cardId != null) "&from=edit&cardId=$cardId" else ""

                    if (selectedUris.size >= 2) {
                        val secondIndex = if (listState.firstVisibleItemIndex == 0) 1 else 0
                        viewModel.resetCorners()
                        viewModel.loadAndDetectCard(context, selectedUris[secondIndex])

                        coroutineScope.launch {
                            delay(500)

                            val b2 = viewModel.fullBitmap.value
                            val c2 = viewModel.corners.value

                            if (b2 != null && c2.size == 4) {
                                val file2 = saveBitmapToInternalStorage(context, cropBitmapWithCorners(b2, c2))
                                if (file2 != null) {
                                    val f = Uri.encode(file1); val b = Uri.encode(file2)
                                    navController.navigate("$resultBase/$f?back=$b$cardIdParam") { popUpTo("main") { inclusive = false } }
                                    return@launch
                                }
                            }
                            // 뒷면 실패 시 앞면만
                            val f = Uri.encode(file1)
                            navController.navigate("$resultBase/$f$cardIdParam") { popUpTo("main") { inclusive = false } }
                        }
                    } else {
                        val f = Uri.encode(file1)
                        navController.navigate("$resultBase/$f$cardIdParam") { popUpTo("main") { inclusive = false } }
                    }
                },
                enabled = selectedUris.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )

        }
    }
}
