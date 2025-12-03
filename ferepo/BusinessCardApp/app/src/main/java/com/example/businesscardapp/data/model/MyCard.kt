package com.example.businesscardapp.data.model

import com.google.gson.annotations.SerializedName

// ========================== (STEP 6) UI 전용 모델 ==========================
data class DigitalCardUi(
    val id: Int,
    val imageUrlVertical: String?, // 세로 이미지
    val qrImageUrl: String?        // QR 이미지 URL
)

// ========================== 등록 ==========================

// ========================== 등록 ==========================
data class MyCardRegisterRequest(
    val name: String,
    val phone: String,
    val company: String,
    val position: String? = null,
    val email: String? = null,
    val backgroundImageNum: Int,                 // 1..12 or 101..108
    @SerializedName("fontColor")                 // 서버가 fontColor/fontcolor 섞여오면 alternate 추가 가능
    val fontColor: Boolean,                      // (true: 밝은 글자)
    val fields: List<Field>? = null
) {
    data class Field(
        val fieldName: String,
        val fieldValue: String,
        val order: Int? = null
    )
}

data class MyCardRegisterResponse(
    val status: Int,
    val message: String,
    val result: Result?
) {
    data class Result(val cardId: Int)
}




// ========================== 목록 ==========================
// data/model/MyCardListResponse.kt

data class MyCardListResponse(
    val status: Int,
    val message: String,
    val result: List<MyCardListItem>?
)

data class MyCardListItem(
    @SerializedName("cardId", alternate = ["cardid"])
    val cardId: Int,

    // 서버에서 confirmed/confirm/isConfirm 등 섞여 올 수 있으니 모두 허용
    @SerializedName(value = "confirmed", alternate = ["confirm", "isConfirm", "isConfirmed"])
    val confirmed: Boolean,

    // 가로 썸네일(있어도 안쓸 예정)
    @SerializedName("imageUrlHorizontal", alternate = ["imageUrlHorizantal","image_horizontal"])
    val imageUrlHorizontal: String?,

    // ✅ 세로 이미지(목록에서 '반드시' 이것만 쓸 것)
    @SerializedName(
        value = "imageUrlVertical",
        alternate = ["image_vertical","verticalImageUrl","imageUrlV"]
    )
    val imageUrlVertical: String?
)



// ========================== 상세 ==========================
data class MyCardDetailResponse(
    val status: Int,
    val message: String,
    val result: MyCardDetail?
)

data class MyCardDetail(
    @SerializedName("cardId", alternate = ["id", "card_id"])
    val cardId: Int?,

    val name: String,
    val phone: String,
    val company: String,
    val position: String?,
    @SerializedName("imageUrlHorizontal", alternate = ["imageUrlHorizantal"])
    val imageUrlHorizontal: String?,
    @SerializedName(
        value = "imageUrlVertical",
        alternate = ["image_vertical","verticalImageUrl","imageUrlV","image_url_vertical"]
    )
    val imageUrlVertical: String?,
    @SerializedName(
        value = "customImageUrl",
        alternate = ["custom_image_url", "customImage", "custom_image", "profileImageUrl", "profile_image_url"]
    )
    val customImageUrl: String?,                 // ✅ 상세에서만 사용 (서버가 저장한 프로필 이미지 URL)
    val backgroundImageNum: Int?,               // ✅ 숫자 인덱스 (패턴/색상)
    val fontColor: Boolean,                     // (true: 밝은 글자)
    @SerializedName("confirm", alternate = ["isConfirm"])
    val confirm: Boolean,
    val shareUrl: String?,
    val qrCodeUrl: String?,
    val createdAt: String,
    val updateAt: String,
    val fields: List<MyCardField>,
    val companyHistories: List<CompanyHistory>? = null
) {

    data class CompanyHistory(
        val id: Int,
        val company: String,
        @SerializedName("isConfirm", alternate = ["confirm"])
        val isConfirm: Boolean,
        val changedAt: String
    )
}

data class MyCardField(
    val fieldName: String,
    val fieldValue: String,
    @SerializedName("fieldOrder", alternate = ["order"])
    val fieldOrder: Int?
)

// ========================== 수정 ==========================

data class MyCardEditRequest(
    val name: String,
    val phone: String,
    val company: String,
    val position: String? = null,
    val email: String? = null,
    @SerializedName("backgroundImageNum") val backgroundImageNum: Int,
    val fontColor: Boolean,
    @SerializedName("fields") val fields: List<EditField>? = null
)

data class EditField(
    @SerializedName("fieldId") val fieldId: Int? = null,
    @SerializedName("fieldName") val fieldName: String,
    @SerializedName("fieldValue") val fieldValue: String,
    @SerializedName("fieldOrder") val fieldOrder: Int? = null
)

// ========================== 히스토리 ==========================

data class MyCardHistoryResponse(
    val status: Int,
    val message: String,
    val result: List<MyCardHistoryItem>?
)

data class MyCardHistoryItem(
    val company: String,
    val isConfirm: Boolean,
    val changedAt: String
)
