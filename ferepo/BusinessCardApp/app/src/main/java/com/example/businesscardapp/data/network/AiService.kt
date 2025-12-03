package com.example.businesscardapp.data.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Header

data class AiMessage(
    val role: String,
    val content: String
)

data class AiRequest(
    val model: String,
    val messages: List<AiMessage>
)

data class AiChoice(
    val message: AiMessage
)

data class AiResponse(
    val choices: List<AiChoice>
)

interface AiService {
    @Headers("Content-Type: application/json")
    @POST("https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions")
    suspend fun getSummary(
        @Header("Authorization") apiKey: String,
        @Body request: AiRequest
    ): AiResponse
}
