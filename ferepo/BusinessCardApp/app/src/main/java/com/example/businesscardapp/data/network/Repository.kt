// Repository.kt
package com.example.businesscardapp.data.network

import com.example.businesscardapp.data.model.*
import android.util.Log
import com.example.businesscardapp.data.network.AiClient.api
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import com.example.businesscardapp.ui.model.DigitalCardUi

class Repository {
    private val apiService = RetrofitClient.apiService

    // ===================== 👤 회원 =====================

    //로그인
    suspend fun googleAuth(accessToken: String): Response<ApiResponse<LoginResponse>> {
        val headerValue = "Bearer $accessToken"
        return apiService.googleLogin(authorizationHeader = headerValue)
    }

    //회사인증
    suspend fun verifyCompanyEmail(cardId: Int, email: String): Response<ApiResponse<Nothing>> {
        val request = CompanyAuthRequest(email)
        return RetrofitClient.apiService.verifyCompanyEmail(cardId, request)
    }

    suspend fun verifyCompanyCode(cardId: Int, email: String, code: String)
            : Response<CompanyCodeVerifyResponse> {
        return RetrofitClient.apiService.verifyCompanyCode(
            cardId, CompanyCodeVerifyRequest(email.trim(), code.trim())
        )
    }

    // ===================== 🗂 명함 =====================

    //명함 목록
    suspend fun getCardList()
            = apiService.getCardList() // Response<ApiResponse<CardListWrapper>>



    // ===================== 📄 종이 명함 =====================
// 종이 명함 등록 API
    suspend fun registerPaperCard(
        request: RequestBody,
        image1: MultipartBody.Part,
        image2: MultipartBody.Part? = null
    ): Response<ApiResponse<PaperCardResponse>> {
        return RetrofitClient.apiService.registerPaperCard(request, image1, image2)
    }

    // ====================== 📄 종이 명함 상세 ==================
    suspend fun getPaperCardDetail(cardId: Int): Response<ApiResponse<PaperCardDetailResponse>> {
        return RetrofitClient.apiService.getPaperCardDetail(cardId)
    }

    // ====================== 📄 종이 명함 수정 ==================
    suspend fun updatePaperCard(
        cardId: Int, request: RequestBody,
                                image1: MultipartBody.Part? = null,
                                image2: MultipartBody.Part? = null

//                                        request: UpdatePaperCardRequest
                                            ): Response<ApiResponse<Nothing>>
                                        {
        return RetrofitClient.apiService.updatePaperCard(cardId, request, image1, image2)
    }

    // =======================📄 종이 명함 삭제 =======================
    suspend fun deletePaperCard(cardId: Int): Response<ApiResponse<Nothing>> {
        return RetrofitClient.apiService.deletePaperCard(cardId)
    }

    // ========================📄 종이 명함 즐겨찾기 =======================
    suspend fun toggleFavorite(cardId: Int): Response<ApiResponse<Nothing>> {
        return RetrofitClient.apiService.toggleFavorite(cardId)
    }

    // ========================📄 종이 명함 메모수정 =======================
    suspend fun updateMemo(cardId: Int, memo: MemoRequest): Response<Unit> =
        RetrofitClient.apiService.updateMemo(cardId, memo)


    // 전화 수신 시 명함 정보 표시
    suspend fun getCardInfoOnCall(phone: String): Response<ApiResponse<CardCallInfoResponse>> {
        return RetrofitClient.apiService.getCardInfoOnCall(phone)
    }

    // 알림 목록 조회
    suspend fun getNotificationList(): Response<ApiResponse<List<NotificationItem>>> {
        return RetrofitClient.apiService.getNotificationList()
    }


}


// 디지털명함
class DigitalCardRepository {
    //디지털 명함 생성
    suspend fun registerCard(cardId: String, accessToken: String): String {
        return try {
            val response = RetrofitClient.apiService.registerDigitalCard(
                cardId = cardId,
                accessToken = accessToken
            )
            if (response.isSuccessful) {
                response.body()?.message ?: "성공했지만 메시지가 없어요"
            } else {
                val errorMsg = response.errorBody()?.string()
                "실패: $errorMsg"
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception: ${e.message}")
            "에러 발생: ${e.localizedMessage}"
        }
    }

    // 디지털 명함 상세 조회
    suspend fun getDigitalCardDetail(
        cardId: String,
        accessToken: String
    ): DigitalCardDetailResponse? {
        return try {
            val response = RetrofitClient.apiService.getDigitalCardDetail(
                cardId = cardId,
                accessToken = accessToken
            )
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("Repository", "실패: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("Repository", "예외 발생: ${e.localizedMessage}")
            null
        }
    }

    // 디지털 명함 삭제
    suspend fun deleteDigitalCard(cardId: String, accessToken: String): BasicResponse? {
        return try {
            val response = RetrofitClient.apiService.deleteDigitalCard(
                cardId = cardId,
                accessToken = accessToken
            )
            if (response.isSuccessful) {
                response.body()
            } else {
                // 에러 응답 파싱
                val errorBody = response.errorBody()?.string()
                Log.e("Repository", "삭제 실패: ${response.code()} - $errorBody")
                
                // 에러 응답을 BasicResponse 형태로 변환
                when (response.code()) {
                    400 -> BasicResponse(
                        status = 400,
                        message = "잘못된 요청입니다."
                    )
                    403 -> BasicResponse(
                        status = 403,
                        message = "접근 권한이 없습니다."
                    )
                    404 -> BasicResponse(
                        status = 404,
                        message = "명함을 찾을 수 없습니다."
                    )
                    500 -> BasicResponse(
                        status = 500,
                        message = "server error!"
                    )
                    else -> BasicResponse(
                        status = response.code(),
                        message = "삭제에 실패했습니다."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "삭제 예외 발생: ${e.localizedMessage}")
            BasicResponse(
                status = 500,
                message = "에러 발생: ${e.localizedMessage}"
            )
        }
    }

    // 디지털 명함 그룹 수정
    suspend fun editDigitalCardGroup(
        cardId: String,
        groupNames: List<String>
    ): BasicResponse? {
        val request = EditGroupRequest(
            groups = groupNames.map { GroupName(it) }
        )

        return try {
            val response = RetrofitClient.apiService.editDigitalCardGroup(
                cardId = cardId,
                request = request
            )
            if (response.isSuccessful) {
                response.body()
            } else {
                // 에러 응답 파싱
                val errorBody = response.errorBody()?.string()
                Log.e("Repository", "그룹 수정 실패: ${response.code()} - $errorBody")
                
                // 에러 응답을 BasicResponse 형태로 변환
                when (response.code()) {
                    400 -> BasicResponse(
                        status = 400,
                        message = "잘못된 요청입니다."
                    )
                    403 -> BasicResponse(
                        status = 403,
                        message = "접근 권한이 없습니다."
                    )
                    404 -> BasicResponse(
                        status = 404,
                        message = "명함을 찾을 수 없습니다."
                    )
                    500 -> BasicResponse(
                        status = 500,
                        message = "server error!"
                    )
                    else -> BasicResponse(
                        status = response.code(),
                        message = "그룹 수정에 실패했습니다."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "그룹 수정 예외 발생: ${e.localizedMessage}")
            BasicResponse(
                status = 500,
                message = "에러 발생: ${e.localizedMessage}"
            )
        }
    }

    // 디지털 명함 즐겨찾기
    suspend fun toggleFavoriteDigitalCard(cardId: String, accessToken: String): BasicResponse? {
        return try {
            val response = RetrofitClient.apiService.toggleFavoriteDigitalCard(
                cardId = cardId,
                accessToken = accessToken
            )
            if (response.isSuccessful) {
                response.body()
            } else {
                // 에러 응답 파싱
                val errorBody = response.errorBody()?.string()
                Log.e("Repository", "즐겨찾기 실패: ${response.code()} - $errorBody")
                
                // 에러 응답을 BasicResponse 형태로 변환
                when (response.code()) {
                    400 -> BasicResponse(
                        status = 400,
                        message = "잘못된 요청입니다."
                    )
                    403 -> BasicResponse(
                        status = 403,
                        message = "접근 권한이 없습니다."
                    )
                    404 -> BasicResponse(
                        status = 404,
                        message = "명함을 찾을 수 없습니다."
                    )
                    500 -> BasicResponse(
                        status = 500,
                        message = "server error!"
                    )
                    else -> BasicResponse(
                        status = response.code(),
                        message = "즐겨찾기에 실패했습니다."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "즐겨찾기 예외 발생: ${e.localizedMessage}")
            BasicResponse(
                status = 500,
                message = "에러 발생: ${e.localizedMessage}"
            )
        }
    }

    // ===================== 🔗 디지털 명함 관계 등록 =====================
    suspend fun connectDigitalCard(
        cardId: String, 
        targetCardId: Int, 
        relationship: String, 
        accessToken: String
    ): BasicResponse? {
        return try {
            val request = ConnectDigitalCardRequest(targetCardId, relationship)
            val response = RetrofitClient.apiService.connectDigitalCard(
                cardId = cardId,
                request = request,
                accessToken = accessToken
            )
            
            if (response.isSuccessful) {
                response.body()
            } else {
                // 에러 응답 파싱
                val errorBody = response.errorBody()?.string()
                Log.e("Repository", "관계 등록 실패: ${response.code()} - $errorBody")
                
                // 에러 응답을 BasicResponse 형태로 변환
                when (response.code()) {
                    400 -> BasicResponse(
                        status = 400,
                        message = "필수 입력값이 누락되었습니다."
                    )
                    404 -> BasicResponse(
                        status = 404,
                        message = "사용자를 찾을 수 없습니다. (존재하지 않는 사용자입니다.)"
                    )
                    500 -> BasicResponse(
                        status = 500,
                        message = "server error!"
                    )
                    else -> BasicResponse(
                        status = response.code(),
                        message = "관계 등록에 실패했습니다."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "관계 등록 예외 발생: ${e.localizedMessage}")
            BasicResponse(
                status = 500,
                message = "에러 발생: ${e.localizedMessage}"
            )
        }
    }

    // ===================== 📝 디지털 명함 메모 수정 =====================
    suspend fun updateMemo(
        cardId: String,
        memo: MemoRequest,
        accessToken: String
    ): BasicResponse? {
        return try {
            val response = RetrofitClient.apiService.updateMemo(
                cardId = cardId,
                request = memo,
                accessToken = accessToken
            )
            
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("Repository", "메모 수정 실패: ${response.code()}")
                when (response.code()) {
                    400 -> BasicResponse(
                        status = 400,
                        message = "잘못된 요청입니다."
                    )
                    404 -> BasicResponse(
                        status = 404,
                        message = "명함을 찾을 수 없습니다."
                    )
                    500 -> BasicResponse(
                        status = 500,
                        message = "server error!"
                    )
                    else -> BasicResponse(
                        status = response.code(),
                        message = "메모 수정에 실패했습니다."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "메모 수정 예외 발생: ${e.localizedMessage}")
            BasicResponse(
                status = 500,
                message = "에러 발생: ${e.localizedMessage}"
            )
        }
    }
}

// 공유
class ShareRepository {
    // 디지털 명함 공유
    suspend fun shareDigitalCard(
        cardId: String,
        shareType: String,
        accessToken: String
    ): ShareCardResponse? {
        return try {
            val response = RetrofitClient.apiService.shareDigitalCard(
                cardId = cardId,
                type = shareType,
                accessToken = accessToken
            )

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("ShareRepository", "공유 실패: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ShareRepository", "공유 예외 발생: ${e.localizedMessage}")
            null
        }
    }

    // 내 명함 공유
    suspend fun shareMyCard(
        cardId: String,
        shareType: String
    ): ShareMyCardResponse? {
        return try {
            val response = RetrofitClient.apiService.shareMyCard(
                cardId = cardId,
                shareType = shareType
            )
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}



class MyCardRepository(private val apiService: ApiService) {

    // 내 명함 등록 (multipart)
    suspend fun registerMyCard(
        requestJson: RequestBody,
        customImage: MultipartBody.Part? = null,
        imageH: MultipartBody.Part? = null,
        imageV: MultipartBody.Part? = null
    ): Response<MyCardRegisterResponse> {
        return RetrofitClient.apiService.registerMyCard(
            requestJson,   // @Part("request")
            customImage,   // @Part("custom_image")
            imageH,        // @Part("imageUrlHorizontal")
            imageV         // @Part("imageUrlVertical")
        )
    }
    // 내 명함 목록 조회
    suspend fun getMyCardList(): MyCardListResponse? {
        return try {
            val response = RetrofitClient.apiService.getMyCardList(
            )
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("Repository", "명함 목록 실패: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("Repository", "명함 목록 예외 발생: ${e.localizedMessage}")
            null
        }
    }

    // 내 명함 상세 조회
    suspend fun getMyCardDetail(cardId: Int): MyCardDetailResponse? {
        return try {
            val response = RetrofitClient.apiService.getMyCardDetail(
                cardId = cardId
            )
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("CardRepository", "상세조회 실패: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("CardRepository", "상세조회 예외 발생: ${e.localizedMessage}")
            null
        }
    }


    suspend fun editMyCard(
        cardId: Int,
        requestJson: RequestBody,
        imageH: MultipartBody.Part? = null,
        imageV: MultipartBody.Part? = null
    ): String {
        return try {
            val resp = RetrofitClient.apiService.editMyCard(
                cardId = cardId,
                requestJson = requestJson,
                imageUrlHorizontal = imageH,
                imageUrlVertical = imageV
            )
            if (resp.isSuccessful) resp.body()?.message ?: "성공했지만 메시지가 없어요"
            else "실패: ${resp.errorBody()?.string()}"
        } catch (e: Exception) {
            "에러 발생: ${e.localizedMessage}"
        }
    }


    suspend fun deleteMyCard(cardId: Int): Pair<Boolean, String> {
        return try {
            val resp = apiService.deleteMyCard(cardId)
            when {
                resp.isSuccessful -> true to (resp.body()?.message ?: "삭제 완료")
                resp.code() in listOf(404, 410) -> true to "이미 삭제된 명함"
                else -> false to (resp.errorBody()?.string().orEmpty().ifBlank { "삭제 실패 (${resp.code()})" })
            }
        } catch (e: Exception) {
            false to ("네트워크 오류: ${e.localizedMessage ?: "알 수 없는 오류"}")
        }
    }



    // 내 명함 히스토리
    suspend fun fetchMyCardHistory(cardId: Int): Response<MyCardHistoryResponse> {
        return apiService.getMyCardHistory(cardId = cardId)
    }

    // 내 명함: 흔들기 뷰어용 카드 리스트 조회
    suspend fun getMyCardsForViewer(): List<DigitalCardUi> {
        val uiList = mutableListOf<DigitalCardUi>()
        val summaries = getMyCardList()?.result.orEmpty()

        for (summary in summaries) {
            val detail = getMyCardDetail(summary.cardId)?.result
            uiList += DigitalCardUi(
                id = summary.cardId,
                imageUrlVertical = detail?.imageUrlVertical,
                qrImageUrl = detail?.qrCodeUrl
            )
        }
        return uiList

    }
}

// 그룹
class GroupRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {
    // 명함 그룹 목록
    suspend fun getGroups(): Response<GroupListResponse> {
        return apiService.getGroups() // accessToken 생략
    }

    // 명함 그룹 생성
    suspend fun createGroup(request: GroupCreateRequest): Response<ApiResponse<Nothing>> {
        return apiService.createGroup(request = request) // accessToken 생략
    }

    // 명함 그룹 수정
    suspend fun editGroup(
        groupId: Int,
        request: GroupEditRequest,
    ): Response<ApiResponse<Nothing>> {
        return apiService.editGroup(
            groupId = groupId,
            request = request
        )
    }

    // 명함 그룹원 수정
    suspend fun putGroupMembers(
        groupId: Int,
        request: GroupMembersRequest
    ): Response<ApiResponse<Nothing>> {
        return apiService.putGroupMembers(groupId, request)
    }

    // 명함 그룹 삭제
    suspend fun deleteGroup(groupId: Int): Response<ApiResponse<Nothing>> {
        return apiService.deleteGroup(groupId)
    }

    // 명함 그룹원 목록
    suspend fun getGroupMembers(groupId: Int): Response<ApiResponse<List<GroupMemberItem>>> {
        return apiService.getGroupMembers(groupId)
    }
}