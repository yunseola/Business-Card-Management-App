package com.example.businesscardapp.ui.component

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.businesscardapp.R

enum class CardOrientation { Landscape, Portrait }


private val BG_COLOR_MAP = mapOf(
    101 to "#FFC107",  // 노랑
    102 to "#00CED1",  // 청록
    103 to "#0D9488",  // 청록 진한 버전
    104 to "#1E3A8A",  // 남색
    105 to "#FF5722",  // 주황
    106 to "#D6C7B0",  // 베이지
    107 to "#333333",  // 다크
    108 to "#F9F9F6"   // 화이트(아이보리)
)

// 서버가 패턴도 정수 인덱스로 줄 경우(예: 1~12) → pattern 문자열로 변환
private val PATTERN_CODE_MAP = mapOf(
    1 to "pattern1",
    2 to "pattern2",
    3 to "pattern3",
    4 to "pattern4",
    5 to "pattern5",
    6 to "pattern6",
    7 to "pattern7",
    8 to "pattern8",
    9 to "pattern9",
    10 to "pattern10",
    11 to "pattern11",
    12 to "pattern12",
)

/**
 * 서버의 backgroundImageNum(Int?) → 카드 컴포넌트에서 쓰는 (bgHex, patternCode)로 변환
 * - 색상 인덱스(101~108): bgHex 세팅, patternCode = null
 * - 패턴 인덱스(201~212 등): patternCode 세팅, bgHex는 투명("#00000000") 권장
 */
fun mapBgIndexToArgs(index: Int?): Pair<String?, String?> {
    if (index == null) return null to null
    BG_COLOR_MAP[index]?.let { hex ->
        return hex to null
    }
    PATTERN_CODE_MAP[index]?.let { code ->
        // 패턴은 카드 내부에서 패턴 이미지가 깔리므로, 배경색은 투명으로 처리
        return "#00000000" to code
    }
    // 매핑에 없으면 기본(white)
    return "#FFFFFF" to null
}

private fun formatPhoneDisplay(raw: String): String {
    val d = raw.filter(Char::isDigit).take(11)
    return when {
        d.length <= 3 -> d
        d.length <= 7 -> "${d.substring(0,3)}-${d.substring(3)}"
        else          -> "${d.substring(0,3)}-${d.substring(3,7)}-${d.substring(7)}"
    }
}

private fun patternRes(code: String?): Int? = when (code) {
    "pattern1" -> R.drawable.pattern1
    "pattern2" -> R.drawable.pattern2
    "pattern3" -> R.drawable.pattern3
    "pattern4" -> R.drawable.pattern4
    "pattern5" -> R.drawable.pattern5
    "pattern6" -> R.drawable.pattern6
    "pattern7" -> R.drawable.pattern7
    "pattern8" -> R.drawable.pattern8
    "pattern9" -> R.drawable.pattern9
    "pattern10" -> R.drawable.pattern10
    "pattern11" -> R.drawable.pattern11
    "pattern12" -> R.drawable.pattern12
    else -> null
}

/**
 * 공용 미리보기 카드
 * - 이미지가 로드되면 가로/세로 비를 자동 감지해서 레이아웃을 강제 분기
 *   (세로 이미지 → Portrait, 가로 이미지 → Landscape)
 * - 이미지 정보가 아직 없을 땐 전달받은 orientation 으로 1차 렌더링
 */
@Composable
fun DigitalPreviewCard(
    // 👇 방향 결정을 위한 ‘미리보기(카드) 이미지’ (없으면 자동측정 안 함)
    orientationImageUri: String? = null,

    // 👇 카드 안에 보여줄 프로필(인물) 사진 — 방향에는 영향 없음
    profileUri: String? = null,

    bgHex: String?,
    patternCode: String? = null,
    useDarkText: Boolean = true,
    name: String,
    company: String,
    phone: String,
    position: String = "",
    email: String = "",
    extras: List<Pair<String, String>> = emptyList(),

    // 기본 표시 방향 (자동측정 실패/미사용 시 이 값 사용)
    orientation: CardOrientation = CardOrientation.Landscape,
) {
    val context = LocalContext.current

    // 1) 배경색/패턴 준비
    val parsedBg = remember(bgHex) {
        runCatching { Color(android.graphics.Color.parseColor(bgHex ?: "#FFFFFF")) }
            .getOrElse { Color.White }
    }
    val pattern = patternRes(patternCode)

    // 2) 텍스트 색상
    val textPrimary = if (useDarkText) Color.Black else Color.White
    val textSecondary = if (useDarkText) Color(0xFF666666) else Color(0xFFEFEFEF)

    // 3) 전화번호 포맷
    val phoneDisplay = remember(phone) { formatPhoneDisplay(phone) }

    // 4) 이미지 비율 감지용 상태: null이면 아직 모름
    var measuredOrientation by remember(orientationImageUri) { mutableStateOf<CardOrientation?>(null) }

    // 5) 하나의 ImageRequest에 listener를 달아 성공 시 intrinsic size로 방향 결정
    val imageRequestForMeasure = remember(orientationImageUri) {
        if (orientationImageUri.isNullOrBlank()) null
        else ImageRequest.Builder(context)
            .data(orientationImageUri)
            .crossfade(true)
            .listener(
                onSuccess = { _, result ->
                    val d = (result as? SuccessResult)?.drawable
                    val w = d?.intrinsicWidth ?: 0
                    val h = d?.intrinsicHeight ?: 0
                    if (w > 0 && h > 0) {
                        measuredOrientation = if (h >= w) CardOrientation.Portrait else CardOrientation.Landscape
                    }
                }
            )
            .build()
    }
    val effectiveOrientation = measuredOrientation ?: orientation
    Log.d("DigitalPreviewCard", "prop=$orientation, effective=$effectiveOrientation")

    val photoRequest = remember(profileUri) {
        if (profileUri.isNullOrBlank()) null
        else ImageRequest.Builder(context)
            .data(profileUri)
            .crossfade(true)
            .build()
    }

    // 7) 카드의 외곽 비율 선택
    val cardModifier =
        if (effectiveOrientation == CardOrientation.Landscape)
            Modifier.fillMaxWidth().aspectRatio(9f / 5f)
        else
            Modifier.fillMaxWidth().aspectRatio(5f / 9f)

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = pattern?.let { Color.Transparent } ?: parsedBg
        ),
        border = CardDefaults.outlinedCardBorder(true)
    ) {
        // 패턴이면 먼저 패턴 배경을 깔아줌
        if (pattern != null) {
            Image(
                painter = painterResource(pattern),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (imageRequestForMeasure != null) {
            AsyncImage(
                model = imageRequestForMeasure,
                contentDescription = null,
                modifier = Modifier
                    .size(1.dp)      // 화면에 거의 영향 없음
                    .padding(0.dp),  // 불필요 레이아웃 영향 최소화
                contentScale = ContentScale.FillBounds
            )
        }

        // 본문 레이아웃
        if (effectiveOrientation == CardOrientation.Landscape) {
            LandscapeContent(
                photoRequest = photoRequest,
                name = name,
                company = company,
                phoneDisplay = phoneDisplay,
                position = position,
                email = email,
                extras = extras,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
        } else {
            PortraitContent(
                photoRequest = photoRequest,
                name = name,
                company = company,
                phoneDisplay = phoneDisplay,
                position = position,
                email = email,
                extras = extras,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
        }
    }
}

/* -------------------- Landscape 레이아웃 -------------------- */
@Composable
private fun LandscapeContent(
    photoRequest: ImageRequest?,
    name: String,
    company: String,
    phoneDisplay: String,
    position: String,
    email: String,
    extras: List<Pair<String, String>>,
    textPrimary: Color,
    textSecondary: Color
) {
    Box(Modifier.fillMaxSize()) {
        if (company.isNotBlank()) {
            Text(
                text = company,
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 16.dp)
            )
        }

        if (photoRequest != null) {
            AsyncImage(
                model = photoRequest,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .size(76.dp)
                    .widthIn(max = 120.dp)
                    .heightIn(max = 96.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, end = 120.dp)
        ) {
            if (name.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    if (position.isNotBlank()) {
                        Spacer(Modifier.width(6.dp))
                        Text(text = position, color = textSecondary, fontSize = 7.sp)
                    }
                }
            }
            if (phoneDisplay.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(text = phoneDisplay, color = textPrimary, fontSize = 8.sp)
            }
            if (email.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(text = email, color = textPrimary, fontSize = 8.sp)
            }
            if (extras.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                extras.take(5).forEach { (label, value) ->
                    if (value.isNotBlank()) {
                        Text("$label: $value", color = textSecondary, fontSize = 7.sp)
                    }
                }
            }
        }
    }
}

/* -------------------- Portrait 레이아웃 -------------------- */
/* -------------------- Portrait 레이아웃 (세로 전용, 요구사항 반영) -------------------- */
/* -------------------- Portrait 레이아웃 (세로 전용, 요구사항 반영) -------------------- */
@Composable
private fun PortraitContent(
    photoRequest: ImageRequest?,
    name: String,
    company: String,
    phoneDisplay: String,
    position: String,
    email: String,
    extras: List<Pair<String, String>>,
    textPrimary: Color,
    textSecondary: Color
) {
    // 스케일 파라미터
    val FONT = 4f          // 모든 글자 4배
    val SPACE = 4f         // 모든 Spacer 4배
    val PHOTO_SCALE = 2.5f // 사진 가로폭 배율 (기준 0.62f)

    // ▶ 바깥 여백: 24.dp * 4
    Spacer(Modifier.height(24.dp * SPACE))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ▶ 내부 프레임
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = textSecondary.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 16.dp * 4
                Spacer(Modifier.height(40.dp * SPACE))

                // ▶ 회사명: 26.sp * 4, 무조건 가운데
                if (company.isNotBlank()) {
                    Text(
                        text = company,
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (80.sp * FONT),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // 20.dp * 4
                Spacer(Modifier.height(40.dp * SPACE))

                // ▶ 프로필 사진: 7:9, 가로폭 0.62f * 2.5 = 1.55 → 최대 1f로 캡
                Box(
                    modifier = Modifier
                        .fillMaxWidth(minOf(1f, 0.62f * PHOTO_SCALE))
                        .aspectRatio(7f / 9f)  // 절대 정사각형 X
                ) {
                    if (photoRequest != null) {
                        AsyncImage(
                            model = photoRequest,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }

                // 8.dp * 4
                Spacer(Modifier.height(40.dp * SPACE))

                // ▶ 이름 / 직책: (18.sp, 13.sp) * 4, 중앙 정렬 강제
                if (name.isNotBlank()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = name,
                                color = textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = (100.sp * FONT)
                            )
                            if (position.isNotBlank()) {
                                Spacer(Modifier.width(8.dp)) // 가로 간격은 시각적 균형 유지
                                Text(
                                    text = position,
                                    color = textSecondary,
                                    fontSize = (13.sp * FONT)
                                )
                            }
                        }
                    }
                }

                // ▶ 전화번호: 14.sp * 4, 위 여백 6.dp * 4
                if (phoneDisplay.isNotBlank()) {
                    Spacer(Modifier.height(6.dp * SPACE))
                    Text(
                        text = phoneDisplay,
                        color = textPrimary,
                        fontSize = (80.sp * FONT),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // ▶ 이메일: 14.sp * 4, 위 여백 2.dp * 4
                if (email.isNotBlank()) {
                    Spacer(Modifier.height(2.dp * SPACE))
                    Text(
                        text = email,
                        color = textPrimary,
                        fontSize = (80.sp * FONT),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // ▶ 추가 필드: 위 여백 12.dp * 4, 줄별 Box로 "강제" 가운데
                Spacer(Modifier.height(12.dp * SPACE))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    extras.take(5).forEachIndexed { idx, (label, value) ->
                        if (value.isNotBlank()) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$label: $value",
                                    color = textSecondary,
                                    fontSize = (60.sp * FONT),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            if (idx < extras.take(5).lastIndex) {
                                // 항목 간 간격 (기본 6.dp 가정) * 4
                                Spacer(Modifier.height(20.dp * SPACE))
                            }
                        }
                    }
                }
            }
        }
    }
}
