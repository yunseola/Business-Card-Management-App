package com.example.businesscardapp.ui.model

data class DigitalCardUi(
    val id: Int,
    val imageUrlVertical: String?,   // 세로 이미지
    val qrImageUrl: String?          // 서버에서 준 QR 코드 이미지 URL
)
