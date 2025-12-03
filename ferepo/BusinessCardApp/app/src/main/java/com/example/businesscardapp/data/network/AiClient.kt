package com.example.businesscardapp.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AiClient {
    val api: AiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://gms.ssafy.io/") // GMS API 서버 주소
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiService::class.java)
    }
}
