// PaperCardModels.kt
package com.example.businesscardapp.data.model

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type


// ===================== 🟦 종이 명함 등록 =====================

// 요청 시 사용하는 개별 추가 필드 구조
data class PaperCardField(
    val fieldId: Int? = null, // null이면 새 필드
    val fieldName: String,
    val fieldValue: String
)

// 종이 명함 등록 요청 모델
data class PaperCard(
    val name: String,
    val phone: String,
    val company: String,
    val position: String? = null,
    val email: String? = null,
    val fields: List<PaperCardField>? = null
)

// 종이 명함 등록 응답 모델
data class PaperCardResponse(
    val cardId: Int
)

// ===================== 🟦 종이 명함 상세조회 =====================

data class PaperCardDetailResponse(
    val name: String,
    val phone: String,
    val company: String,
    val position: String? = null,
    val email: String? = null,
    val image1Url: String,
    val image2Url: String?,
    val isFavorite: Boolean,
    val createdAt: String,
    val fields: List<PaperCardField>?,
    val groups: List<PaperCardGroup>?,
    val memo: PaperCardMemo?,
    val imageHistories: List<ImageHistoryDto> = emptyList()
)

@JsonAdapter(ImageHistoryDto.Adapter::class) // ★ 이 줄 추가
data class ImageHistoryDto(
    val images: List<String> = emptyList(),
    val uploadedAt: String = ""
) {
    // 서버가 image1Url, image2Url, ... 가변 키로 내려주는 것을 List<String>으로 모아줌
    object Adapter : JsonDeserializer<ImageHistoryDto> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): ImageHistoryDto {
            val obj = json.asJsonObject

            val urls = buildList {
                for ((key, value) in obj.entrySet()) {
                    if (key.startsWith("image") && key.endsWith("Url") && !value.isJsonNull) {
                        val u = value.asString
                        if (u.isNotBlank()) add(u)
                    }
                }
            }

            val uploadedAt = obj["uploadedAt"]?.asString ?: ""
            return ImageHistoryDto(images = urls, uploadedAt = uploadedAt)
        }
    }
}

// 그룹 정보
data class PaperCardGroup(
    val groupId: Int,
    val groupName: String
)

// 메모 정보
data class PaperCardMemo(
    val relationship: String,
    val personality: String,
    val workStyle: String,
    val meetingNotes: String,
    val etc: String
)

// 종이 명함 수정 요청 모델
data class UpdatePaperCardRequest(
    val name: String? = null,
    val phone: String? = null,
    val company: String? = null,
    val position: String? = null,  // ✅ 직책을 최상위 필드로 추가
    val email: String? = null,     // ✅ 이메일을 최상위 필드로 추가
    val fields: List<UpdateField>? = null,
    val groups: List<UpdateGroup>? = null
)

data class UpdateField(
    val fieldId: Int?, // null이면 새 필드
    val fieldName: String,
    val fieldValue: String
)

data class UpdateGroup(
    val groupId: Int
)

// 종이 명함 메모 수정
data class MemoRequest(
    val relationship: String,
    val personality: String,
    val workStyle: String,
    val meetingNotes: String,
    val etc: String,
    val summary: String? = null   // ✅ 전화용 캐시 목적. UI에는 미표시
)