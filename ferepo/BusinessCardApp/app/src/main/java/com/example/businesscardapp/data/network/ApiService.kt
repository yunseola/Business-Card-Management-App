package com.example.businesscardapp.data.network

import retrofit2.Response
import retrofit2.http.*
import com.example.businesscardapp.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ApiService {
    // 디지털 명함 등록
    @POST("/api/cards/digital/{cardId}")
    suspend fun registerDigitalCard(
        @Path("cardId") cardId: String,
        @Header("Type") type: String = "BEARER",
        @Header("Access-Token") accessToken: String
    ): Response<BasicResponse>

    // 디지털 명함 상세 조회
    @GET("/api/cards/digital/{cardId}")
    suspend fun getDigitalCardDetail(
        @Path("cardId") cardId: String,
        @Header("Type") type: String = "BEARER",
        @Header("Access-Token") accessToken: String
    ): Response<DigitalCardDetailResponse>

    // 디지털 명함 삭제
    @DELETE("/api/cards/digital/{cardId}")
    suspend fun deleteDigitalCard(
        @Path("cardId") cardId: String,
        @Header("Type") type: String = "BEARER",
        @Header("Access-Token") accessToken: String
    ): Response<BasicResponse>

    // 디지털 명함 그룹 수정
    @PUT("/api/cards/digital/{cardId}/group")
    suspend fun editDigitalCardGroup(
        @Path("cardId") cardId: String,
        @Body request: EditGroupRequest,
        @Header("Type") type: String = "BEARER"
    ): Response<BasicResponse>

    @PUT("/api/cards/digital/{cardId}/favorite")
    suspend fun toggleFavoriteDigitalCard(
        @Path("cardId") cardId: String,
        @Header("Type") type: String = "BEARER",
        @Header("Access-Token") accessToken: String
    ): Response<BasicResponse>

    // ===================== 🔗 디지털 명함 관계 등록 =====================
    @POST("/api/cards/digital/{cardId}/connect")
    suspend fun connectDigitalCard(
        @Path("cardId") cardId: String,
        @Body request: ConnectDigitalCardRequest,
        @Header("Type") type: String = "BEARER",
        @Header("Access-Token") accessToken: String
    ): Response<BasicResponse>

    // ===================== 📝 디지털 명함 메모 수정 =====================
    @PUT("/api/cards/digital/{cardId}/memo")
    suspend fun updateMemo(
        @Path("cardId") cardId: String,
        @Body request: MemoRequest,
        @Header("Type") type: String = "BEARER",
        @Header("Access-Token") accessToken: String
    ): Response<BasicResponse>

    // 디지털 명함 공유 (image, link, qr)
    @GET("/api/cards/digital/{cardId}/share")
    suspend fun shareDigitalCard(
        @Path("cardId") cardId: String,
        @Query("type") type: String,
        @Header("Type") typeHeader: String = "BEARER",
        @Header("Access-Token") accessToken: String
    ): Response<ShareCardResponse>

    // 내 명함 공유
    @GET("/api/cards/mine/{cardId}/share")
    suspend fun shareMyCard(
        @Path("cardId") cardId: String,
        @Query("type") shareType: String, // 예: "image", "link", "qr", "paper", "nfc"
        @Header("Type") typeHeader: String = "BEARER"
    ): Response<ShareMyCardResponse>





    // 내 명함 등록 (multipart)
    @Multipart
    @POST("/api/cards/mine")
    suspend fun registerMyCard(
        @Part("request") requestJson: RequestBody,          // ✅ 텍스트 파트: 이름 O
        @Part custom_image: MultipartBody.Part?,            // ✅ 파일 파트: 이름 X
        @Part imageUrlHorizontal: MultipartBody.Part?,      // ✅ 파일 파트: 이름 X
        @Part imageUrlVertical: MultipartBody.Part?         // ✅ 파일 파트: 이름 X
    ): Response<MyCardRegisterResponse>



    // 내 명함 목록 조회
    @GET("/api/cards/mine")
    suspend fun getMyCardList(
        @Header("Type") typeHeader: String = "BEARER"
    ): Response<MyCardListResponse>

    // 내 명함 상세 조회
    @GET("/api/cards/mine/{cardId}")
    suspend fun getMyCardDetail(
        @Path("cardId") cardId: Int,
        @Header("Type") typeHeader: String = "BEARER"
    ): Response<MyCardDetailResponse>

    // 내 명함 수정
    @Multipart
    @PUT("api/cards/mine/{cardId}")
    suspend fun editMyCard(
        @Path("cardId") cardId: Int,
        @Part("request") requestJson: RequestBody,          // ✅ 텍스트
        @Part imageUrlHorizontal: MultipartBody.Part?,      // ✅ 파일
        @Part imageUrlVertical: MultipartBody.Part?         // ✅ 파일
    ): Response<BasicResponse>

    // 내 명함 삭제
    @DELETE("/api/cards/mine/{cardId}")
    suspend fun deleteMyCard(@Path("cardId") cardId: Int)
            : Response<BasicResponse>





    // 내 명함 히스토리
    @GET("/api/cards/mine/{cardId}/history")
    suspend fun getMyCardHistory(
        @Path("cardId") cardId: Int
    ): Response<MyCardHistoryResponse>



    // 명함 그룹 목록
    @GET("/api/groups")
    suspend fun getGroups(
        @Header("Type") type: String = "BEARER"
    ): Response<GroupListResponse>

    // 명함 그룹 생성
    @POST("/api/groups")
    suspend fun createGroup(
        @Body request: GroupCreateRequest,
        @Header("Type") type: String = "BEARER"
    ): Response<ApiResponse<Nothing>>

    // 명함 그룹 수정
    @PUT("/api/groups/{groupId}")
    suspend fun editGroup(
        @Path("groupId") groupId: Int,
        @Body request: GroupEditRequest,
        @Header("Type") type: String = "BEARER"
    ): Response<ApiResponse<Nothing>>

    // 명함 그룹 삭제
    @DELETE("/api/groups/{groupId}")
    suspend fun deleteGroup(
        @Path("groupId") groupId: Int,
        @Header("Type") type: String = "BEARER"
    ): Response<ApiResponse<Nothing>>

    // 명함 그룹원 목록
    @GET("/api/groups/{groupId}/members")
    suspend fun getGroupMembers(
        @Path("groupId") groupId: Int,
        @Header("Type") type: String = "BEARER"
    ): Response<ApiResponse<List<GroupMemberItem>>>

    // 명함 그룹원 수정
    @PUT("/api/groups/{groupId}/members")
    suspend fun putGroupMembers(
        @Path("groupId") groupId: Int,
        @Body body: GroupMembersRequest,
        @Header("Type") type: String = "BEARER"
    ): Response<ApiResponse<Nothing>>



    // ===================== 👤 회원 (AUTH) =====================

    //회원가입 및 로그인
    /*@POST("/oauth2/authorization/google")
    suspend fun googleLogin(
        @Header("Type") authorization: String = "BEARER",
        @Header("Access-Token") accessToken: String
    ): Response<Unit>*/

    @POST("/api/auth/google")
    suspend fun googleLogin(
        @Header("Authorization") authorizationHeader: String
    ): Response<ApiResponse<LoginResponse>>

    // 회사인증: 인증코드 발송 (스펙 맞춤)
    @Headers("Content-Type: application/json; charset=utf8")
    @POST("/api/company/{cardId}/request-code")
    suspend fun verifyCompanyEmail(
        @Path("cardId") cardId: Int,
        @Body request: CompanyAuthRequest
    ): Response<ApiResponse<Nothing>>

    // 인증 코드 검증
    @Headers("Content-Type: application/json; charset=utf8")
    @POST("/api/company/{cardId}/verify-code")
    suspend fun verifyCompanyCode(
        @Path("cardId") cardId: Int,
        @Body request: CompanyCodeVerifyRequest
    ): Response<CompanyCodeVerifyResponse>

    // ===================== 🗂 명함 (CARD) =====================
    //명함목록
    @GET("/api/cards")
    suspend fun getCardList(): Response<ApiResponse<List<Card>>>

    // ===================== 📄 종이명함 (PAPER CARD) =====================
    @Multipart
    @POST("/api/cards/paper")
    suspend fun registerPaperCard(
        @Part("request") request: RequestBody,
        @Part image1: MultipartBody.Part,     // 이미지 파일
        @Part image2: MultipartBody.Part? = null // 선택적 이미지
    ): Response<ApiResponse<PaperCardResponse>>

    // ===================== 📄 종이 명함 상세 조회 =====================
    @GET("/api/cards/paper/{cardId}")
    suspend fun getPaperCardDetail(
        @Path("cardId") cardId: Int
    ): Response<ApiResponse<PaperCardDetailResponse>>


    // ===================== 📄 종이 명함 수정 ====================
    @Multipart
    @PUT("/api/cards/paper/{cardId}")
    suspend fun updatePaperCard(
        @Path("cardId") cardId: Int,
        @Part("request") request: RequestBody,
        @Part image1: MultipartBody.Part? = null,     // 이미지 파일
        @Part image2: MultipartBody.Part? = null
    ): Response<ApiResponse<Nothing>>

    // ====================== 📄 종이 명함 삭제 ============================
    @DELETE("/api/cards/paper/{cardId}")
    suspend fun deletePaperCard(
        @Path("cardId") cardId: Int
    ): Response<ApiResponse<Nothing>>

    // ====================== 📄 종이 명함 즐겨찾기 ============================
    @PUT("/api/cards/paper/{cardId}/favorite")
    suspend fun toggleFavorite(
        @Path("cardId") cardId: Int
    ): Response<ApiResponse<Nothing>>

    // ====================== 📄 종이 명함 메모수정 ============================
    @PUT("/api/memos/paper/{cardId}")
    suspend fun updateMemo(
        @Path("cardId") cardId: Int,
        @Body memo: MemoRequest
    ): Response<Unit>

    // 전화 수신 시 명함 정보 표시
    @GET("/api/call/{phone}")
    suspend fun getCardInfoOnCall(
        @Path("phone") phone: String
    ): Response<ApiResponse<CardCallInfoResponse>>

    // 알림 목록 조회
    @GET("/api/noti")
    suspend fun getNotificationList(): Response<ApiResponse<List<NotificationItem>>>

}


