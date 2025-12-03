package com.example.businesscardapp.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.businesscardapp.R

// 각각 별도로 선언 (개별 사용 가능)
val pretendardLight = FontFamily(Font(R.font.pretendard_light))
val pretendardRegular = FontFamily(Font(R.font.pretendard_regular))
val pretendardMedium = FontFamily(Font(R.font.pretendard_medium))
val pretendardSemiBold = FontFamily(Font(R.font.pretendard_semibold))

// weight 지정 포함한 통합 선언 (선택적으로 사용 가능)
val pretendard = FontFamily(
    Font(R.font.pretendard_light, FontWeight.Light),
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold)
)
