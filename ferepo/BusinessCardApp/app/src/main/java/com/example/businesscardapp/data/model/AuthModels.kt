//회원
package com.example.businesscardapp.data.model

//회원가입
data class SignupRequest(
    val email: String,
    val password: String,
    val name: String
)

//회사인증
data class CompanyAuthRequest(
    val email: String
)

data class CompanyCodeVerifyRequest(
    val email: String,
    val code: String
)

data class CompanyCodeVerifyResponse(
    val status: Int,
    val message: String,
    val verify: Int      // 1=성공, 2=실패
)

//로그인
data class LoginResponse(
    val jwtToken: String,
    val userId: String
)