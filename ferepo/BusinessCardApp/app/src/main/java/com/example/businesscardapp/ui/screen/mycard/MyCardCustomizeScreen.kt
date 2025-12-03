package com.example.businesscardapp.ui.screen.mycard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.component.CardOrientation
import com.example.businesscardapp.ui.component.DigitalPreviewCard
import com.example.businesscardapp.ui.component.mapBgIndexToArgs
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MyCardCustommizeScreen(
    navController: NavController,
    viewModel: MyCardViewModel
) {
    val patterns = listOf(
        "pattern1" to R.drawable.pattern1,
        "pattern2" to R.drawable.pattern2,
        "pattern3" to R.drawable.pattern3,
        "pattern4" to R.drawable.pattern4,
        "pattern5" to R.drawable.pattern5,
        "pattern6" to R.drawable.pattern6,
        "pattern7" to R.drawable.pattern7,
        "pattern8" to R.drawable.pattern8,
        "pattern9" to R.drawable.pattern9,
        "pattern10" to R.drawable.pattern10,
        "pattern11" to R.drawable.pattern11,
        "pattern12" to R.drawable.pattern12
    )
    val colorHex = mapOf(
        "color1" to "#FFC107",
        "color2" to "#00CED1",
        "color3" to "#0D9488",
        "color4" to "#1E3A8A",
        "color5" to "#FF5722",
        "color6" to "#D6C7B0",
        "color7" to "#333333",
        "color8" to "#F9F9F6"
    )
    val colors = listOf(
        "color1" to R.drawable.color1,
        "color2" to R.drawable.color2,
        "color3" to R.drawable.color3,
        "color4" to R.drawable.color4,
        "color5" to R.drawable.color5,
        "color6" to R.drawable.color6,
        "color7" to R.drawable.color7,
        "color8" to R.drawable.color8
    )

    // VM 상태
    val fields by viewModel.fields.collectAsState()
    val profileImage by viewModel.profileImageUri.collectAsState()
    val currentBg by viewModel.background.collectAsState()
    val currentPattern by viewModel.pattern.collectAsState()
    val currentTextDark by viewModel.textDark.collectAsState()

    // 화면 내 임시 선택
    var selectedKey by remember { mutableStateOf<String?>(null) } // colorN or patternN
    var selectedTextDark by remember { mutableStateOf(currentTextDark) }

    // 프리뷰 계산
    val previewBgHex = if (selectedKey?.startsWith("color") == true)
        colorHex[selectedKey!!] ?: currentBg else currentBg
    val previewPattern = if (selectedKey?.startsWith("pattern") == true)
        selectedKey else currentPattern
    val previewCardBgHex = if (previewPattern != null) "#00000000" else previewBgHex

    val gridCols = 4
    val patternCell = 66.dp
    val colorCell = 48.dp
    fun gridHeight(count: Int, cell: Dp): Dp {
        val rows = kotlin.math.ceil(count / gridCols.toFloat()).toInt()
        return (cell + 12.dp) * rows
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = { Text("배경 변경", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            when {
                                selectedKey == null -> Unit

                                selectedKey!!.startsWith("color") -> {
                                    val hex = colorHex[selectedKey!!] ?: currentBg
                                    viewModel.setBackground(hex)              // 배경 hex 저장
                                    viewModel.setPattern(null)                // 패턴 해제
                                    val idx = selectedKey!!.removePrefix("color").toIntOrNull() ?: 1
                                    viewModel.setBackgroundImageNum(100 + idx) // 100~108
                                }

                                selectedKey!!.startsWith("pattern") -> {
                                    viewModel.setPattern(selectedKey!!)      // 패턴 저장
                                    val p = selectedKey!!.removePrefix("pattern").toIntOrNull() ?: 1
                                    viewModel.setBackgroundImageNum(p)       // 1~12
                                }
                            }
                            viewModel.setTextDark(selectedTextDark)
                            navController.popBackStack()
                        }
                    ) { Text("적용") }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
        ) {
            // 미리보기
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(160.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    DigitalPreviewCard(
                        profileUri = profileImage?.toString(),
                        bgHex = previewCardBgHex,
                        patternCode = previewPattern,
                        useDarkText = selectedTextDark,
                        name = fields.firstOrNull { it.label == "이름" }?.value.orEmpty(),
                        company = fields.firstOrNull { it.label == "회사" }?.value.orEmpty(),
                        phone = fields.firstOrNull { it.label == "연락처" }?.value.orEmpty(),
                        position = fields.firstOrNull { it.label == "직책" }?.value.orEmpty(),
                        email = fields.firstOrNull { it.label == "이메일" }?.value.orEmpty(),
                        extras = emptyList(),
                        orientation = CardOrientation.Landscape
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 패턴
            SectionTitle("배경 디자인")
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridCols),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .height(gridHeight(patterns.size, patternCell)),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(patterns) { (name, res) ->
                    val sel = selectedKey == name
                    Card(
                        modifier = Modifier
                            .size(patternCell)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedKey = name },
                        shape = RoundedCornerShape(12.dp),
                        border = if (sel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Image(
                            painter = painterResource(res),
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 색상
            SectionTitle("배경 색상")
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridCols),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .height(gridHeight(colors.size, colorCell)),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(colors) { (name, res) ->
                    val sel = selectedKey == name
                    Box(
                        modifier = Modifier
                            .size(colorCell)
                            .clip(CircleShape)
                            .clickable { selectedKey = name }
                            .border(
                                width = if (sel) 3.dp else 1.dp,
                                color = if (sel) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(res),
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 글씨색
            SectionTitle("글씨 색상")
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(
                            if (selectedTextDark) 3.dp else 1.dp,
                            if (selectedTextDark) MaterialTheme.colorScheme.primary else Color.Gray,
                            CircleShape
                        )
                        .clickable { selectedTextDark = true }
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(
                            if (!selectedTextDark) 3.dp else 1.dp,
                            if (!selectedTextDark) MaterialTheme.colorScheme.primary else Color.Gray,
                            CircleShape
                        )
                        .clickable { selectedTextDark = false }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}



@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}
