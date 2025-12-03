package com.example.businesscardapp.data.model

// 알림 목록 항목
data class NotificationItem(
    val cardId: Int,
    val message: String,
    val isRead: Boolean,
    val createdAt: String
)
