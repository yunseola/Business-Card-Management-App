package com.example.businesscardapp.data.model

// ===================== 디지털 명함 기본 응답 =====================
data class BasicResponse(
    val status: Int,
    val message: String,
    val result: Any? = null
)

// ===================== 디지털 명함 상세 조회 =====================
data class DigitalCardDetailResponse(
    val result: DigitalCardDetail
)

data class DigitalCardDetail(
    val name: String,
    val phone: String,
    val company: String,
    val position: String? = null,
    val email: String? = null,
    val imageUrlHorizontal: String? = null,
    val imageUrlVertical: String? = null,
    val digital: Boolean = true,
    val confirm: Boolean,
    val favorite: Boolean,
    val createdAt: String,
    val fields: List<Field>,
    val groups: List<GroupItem>,
    val memo: Memo?,
    val companyHistories: List<CompanyHistory>
)

data class Field(
    val fieldName: String,
    val fieldValue: String,
    val fieldOrder: Int
)

data class Memo(
    val relationship: String,
    val personality: String,
    val workStyle: String,
    val meetingNotes: String,
    val etc: String
)

data class CompanyHistory(
    val company: String,
    val confirm: Boolean,
    val changedAt: String
)

// ===================== 디지털 명함 그룹 관리 =====================
data class Group(
    val groupId: Int,
    val groupName: String
)

data class EditGroupRequest(
    val groups: List<GroupName>
)

data class GroupName(
    val groupName: String
)

// ===================== 디지털 명함 관계 등록 =====================
data class ConnectDigitalCardRequest(
    val targetCardId: Int,
    val relationship: String  // 관계 유형 (예: "동료", "상사", "부하직원" 등)
)


