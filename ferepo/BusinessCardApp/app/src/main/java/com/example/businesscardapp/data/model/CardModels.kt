//명함
package com.example.businesscardapp.data.model

import com.google.gson.annotations.SerializedName

data class Card(
    val cardId: Int,
    val name: String,
    val phone: String,
    val company: String,
    val imageUrl: String,
    val position: String?,
    val email: String?,
    @SerializedName("digital")
    val isDigital: Boolean,
    @SerializedName("confirmed")
    val isConfirmed: Boolean,
    @SerializedName("favorite")
    val isFavorite: Boolean,
    val createdAt: String,
    val updatedAt: String
)

// /api/cards 응답의 result 내부 구조용 래퍼
data class CardListWrapper(
    val result: List<Card>
)

// 명함 정보 데이터 클래스 (카메라 촬영 후 입력된 정보)
data class BusinessCardInfo(
    val cardId: Int? = null,
    val name: String = "",
    val position: String = "",
    val department: String = "",
    val phone: String = "",
    val email: String = "",
    val company: String = "",
    val address: String = "",
    val imageUri: String = "",
    val imageUri2: String = "",
    val capturedDate: String = ""
)

// 공통 API 응답 모델
data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val result: T
)