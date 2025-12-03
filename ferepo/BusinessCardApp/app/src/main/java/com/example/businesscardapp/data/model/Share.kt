package com.example.businesscardapp.data.model

// 디지털 명함 공유
data class ShareCardResponse(
    val status: Int,
    val message: String,
    val result: ShareResult?
)

data class ShareResult(
    val image_url_horizontal: String,
    val share_url: String
)

// 내 명함 공유
data class ShareMyCardResponse(
    val status: Int,
    val message: String,
    val result: ShareMyCardResult?
)

data class ShareMyCardResult(
    val url: String?,
    val image: String?,   // type = paper
    val qr: String?,      // type = qr
    val nfcId: String?    // type = nfc
)