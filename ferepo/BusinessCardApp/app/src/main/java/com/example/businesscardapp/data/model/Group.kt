package com.example.businesscardapp.data.model

import com.google.gson.annotations.SerializedName

// 명함 그룹 목록
data class GroupListResponse(
    val status: Int,
    val message: String,
    val result: GroupResult?
)

data class GroupResult(
    val groups: List<GroupItem>
)

data class GroupItem(
    val groupId: Int,
    @SerializedName(value = "name", alternate = ["groupName"])
    val name: String,
    val headcount: Int = 0
)

// 명함 그룹 생성
data class GroupCreateRequest(
    val name: String
)

// 명함 그룹 수정
data class GroupEditRequest(
    val name: String
)

data class GroupMembersRequest(
    val groupId: Int,
    val members: List<GroupMemberUpdate>
)

data class GroupMemberUpdate(
    val cardId: Int,
    @SerializedName("digital") val digital: Boolean   // ← 키 이름을 digital 로 보냄
)

// 명함 그룹원 목록
data class GroupMemberResponse(
    val status: Int,
    val message: String,
    val result: List<GroupMemberItem>?
)

data class GroupMemberItem(
    val is_digital: Boolean,
    val id: Int,
    val name: String,
    val phone: String,
    val company: String,
    val position: String? = null,
    val imageUrl: String,
    val isConfirmed: Boolean,
    val isFavorite: Boolean
)
