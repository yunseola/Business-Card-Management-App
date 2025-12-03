package com.example.businesscardapp.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 백엔드 표준 응답 형태:
 * { status, message, result: T? }
 */
data class ApiEnvelope<T>(
    val status: Int,
    val message: String,
    val result: T?
)

/** 공개 미리보기 정보 */
data class PublicCardResult(
    val ownerName: String,
    val imageUrl: String?
)

interface CardApi {

    /** 미설치 웹 폴백과 동일 데이터를 JSON으로 제공 (앱 미리보기용) */
    @GET("/api/public/cards/{cardId}")
    suspend fun getPublicCard(
        @Path("cardId") cardId: Int
    ): Response<ApiEnvelope<PublicCardResult>>

}
