package com.example.businesscardapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.businesscardapp.data.network.Repository
import com.example.businesscardapp.data.model.PaperCard
import com.example.businesscardapp.data.model.PaperCardField
import com.example.businesscardapp.data.model.PaperCardDetailResponse
import com.example.businesscardapp.data.model.MemoRequest
import com.example.businesscardapp.data.model.UpdatePaperCardRequest
import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.example.businesscardapp.BuildConfig
import com.example.businesscardapp.data.network.AiClient
import com.example.businesscardapp.data.network.AiRequest
import com.example.businesscardapp.data.network.AiMessage
import android.text.TextUtils
import com.example.businesscardapp.data.model.UpdateGroup

class PaperCardViewModel : ViewModel() {
    
    // Repository 가져오기
    private val repository = Repository()
    
    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 성공 상태
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()
    
    // 에러 상태
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // ✅ 등록된 cardId 저장
    private val _registeredCardId = MutableStateFlow<Int?>(null)
    val registeredCardId: StateFlow<Int?> = _registeredCardId.asStateFlow()
    
    // ✅ 종이명함 상세 정보
    private val _paperCardDetail = MutableStateFlow<PaperCardDetailResponse?>(null)
    val paperCardDetail: StateFlow<PaperCardDetailResponse?> = _paperCardDetail.asStateFlow()

    // ✅ AI로 한줄 요약 생성
    private suspend fun generateAiSummary(
        relationship: String,
        personality: String,
        workStyle: String,
        meetingNotes: String,
        etc: String
    ): String {
        Log.d("AI", "generateAiSummary() entered")
        // 프롬프트에 넣을 원문(라벨 포함, 한국어 지시)
        val payload = """
            관계: $relationship
            성향: $personality
            업무 스타일: $workStyle
            회의 메모: $meetingNotes
            기타: $etc
        """.trimIndent()

        val req = AiRequest(
            model = "gpt-4o",
            messages = listOf(
                AiMessage(
                    role = "developer",
                    content = "당신은 전화를 받기 직전에 잠깐 볼 수 있는 짧고 친근한 요약을 만들어주는 도우미입니다. " +
                            "출력은 반드시 한 문장, 한국어, 최대 30자로 하며, 부드럽고 자연스럽게 표현하세요. " +
                            "레이블이나 불필요한 말은 빼고, 바로 도움이 될 핵심만 전하세요."
                ),
                AiMessage(
                    role = "user",
                    content = "아래 사람의 메모를 참고해 전화 직전 유용한 한 줄 요약을 만들어주세요.\n$payload"
                )
            )
        )

        return try {
            val apiKey = "Bearer ${BuildConfig.GMS_KEY}"
            Log.d("AI", "key? empty=${BuildConfig.GMS_KEY.isNullOrBlank()} len=${BuildConfig.GMS_KEY.length}")
            Log.d("AI", "req -> ${req.model}, payloadLen=${payload.length}")

            val res = AiClient.api.getSummary(apiKey, req)
            Log.d("AI", "res choices size=${res.choices?.size ?: -1}")
            val raw = res.choices.firstOrNull()?.message?.content.orEmpty()
            sanitizeOneLine(raw).ifBlank {
                // AI가 비워주거나 형식이 이상하면 로컬 Fallback
                fallbackSummary(payload)
            }
        } catch (e: Exception) {
            Log.e("AI", "요약 생성 실패: ${e.localizedMessage}", e)
            fallbackSummary(payload)
        }
    }

    // ✅ 안전장치: 한 줄/길이 제한/공백 정리
    private fun sanitizeOneLine(text: String, maxLen: Int = 30): String {
        val oneLine = text
            .replace("\n", " ")
            .replace("\r", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .removeSurrounding("\"") // 따옴표만 반환되는 경우 방지
        return if (oneLine.length <= maxLen) oneLine else oneLine.substring(0, maxLen)
    }

    // ✅ 네트워크 실패 시 Fallback(간단 규칙)
    private fun fallbackSummary(src: String, maxLen: Int = 30): String {
        // 우선순위: 회의 메모 > 성향 > 업무스타일 > 관계 > 기타
        val lines = src.lines().map { it.substringAfter(":").trim() }.filter { it.isNotBlank() }
        val first = lines.firstOrNull().orEmpty()
        return sanitizeOneLine(first, maxLen).ifBlank { "메모 없음" }
    }


    // 종이명함 등록 함수
    fun registerPaperCard(
        name: String,
        phone: String,
        company: String,
        position: String? = null,
        email: String? = null,
        fields: List<PaperCardField>? = null,
        image1File: File,
        image2File: File? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("API", "=== 종이명함 등록 시작 ===")
            Log.d("API", "이름: $name, 전화: $phone, 회사: $company")
            
            try {
                // PaperCard 객체 만들기 (API 명세에 맞게)
                val paperCard = PaperCard(
                    name = name,
                    phone = phone,
                    company = company,
                    position = position,
                    email = email,
                    fields = fields
                )
                val gson = Gson()
                val json = gson.toJson(paperCard)

                val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

                // 2. 이미지1 -> MultipartBody.Part
                val image1Body = image1File.asRequestBody("image/*".toMediaTypeOrNull())
                val image1Part = MultipartBody.Part.createFormData("image1", image1File.name, image1Body)

                // 3. 이미지2 -> MultipartBody.Part (optional)
                val image2Part = image2File?.let {
                    val image2Body = it.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image2", it.name, image2Body)
                }

                val response = repository.registerPaperCard(requestBody, image1Part, image2Part)
                
                Log.d("API", "PaperCard 객체 생성 완료: $paperCard")
                
                // API 호출
                Log.d("API", "Repository API 호출 시작...")
                // val response = repository.registerPaperCard(paperCard)
                Log.d("API", "API 응답 받음: ${response.code()}")
                
                if (response.isSuccessful) {
                    _isSuccess.value = true
                    // ✅ 등록된 cardId 저장
                    _registeredCardId.value = response.body()?.result?.cardId
                    Log.d("API", "종이명함 등록 성공! CardId: ${response.body()?.result?.cardId}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API", "등록 실패: ${response.code()}, 에러: $errorBody")
                    _error.value = "등록 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("API", "종이명함 등록 예외 발생", e)
                _error.value = e.message ?: "알 수 없는 오류"
            } finally {
                _isLoading.value = false
                Log.d("API", "=== 종이명함 등록 종료 ===")
            }
        }
    }
    
    // ✅ 종이명함 상세 조회 함수
    fun getPaperCardDetail(cardId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // 즉시 갱신을 위해 이전 데이터 초기화
            _paperCardDetail.value = null
            
            try {
                val response = repository.getPaperCardDetail(cardId)
                
                if (response.isSuccessful) {
                    _paperCardDetail.value = response.body()?.result
                    Log.d("API", "종이명함 상세 조회 성공!")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API", "상세 조회 실패: ${response.code()}, 에러: $errorBody")
                    _error.value = "상세 조회 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("API", "종이명함 상세 조회 예외 발생", e)
                _error.value = e.message ?: "알 수 없는 오류"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // ✅ 종이명함 수정 함수
    fun updatePaperCard(
        cardId: Int, request: UpdatePaperCardRequest, imageFile1: File? = null,
        imageFile2: File? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                Log.d("API", "=== 종이명함 수정 시작 ===")
                Log.d("API", "CardId: $cardId, Request: $request")

                val gson = Gson()
                val jsonString = gson.toJson(request)
                val requestBody = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())

// 이미지 파일 MultipartBody.Part 준비 (null 가능)
                val imagePart1: MultipartBody.Part? = if (imageFile1 != null) {
                    val reqFile = imageFile1.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("image1", imageFile1.name, reqFile)
                } else null

                val imagePart2: MultipartBody.Part? = if (imageFile2 != null) {
                    val reqFile = imageFile2.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("image2", imageFile2.name, reqFile)
                } else null

// API 호출
                val response = repository.updatePaperCard(cardId, requestBody, imagePart1, imagePart2)
                
                Log.d("API", "API 응답 받음: ${response.code()}")
                
                if (response.isSuccessful) {
                    // 수정 성공 시 상세 정보 다시 로드
                    getPaperCardDetail(cardId)
                    Log.d("API", "종이명함 수정 성공!")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API", "수정 실패: ${response.code()}, 에러: $errorBody")
                    _error.value = "수정 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("API", "종이명함 수정 예외 발생", e)
                _error.value = e.message ?: "알 수 없는 오류"
            } finally {
                _isLoading.value = false
                Log.d("API", "=== 종이명함 수정 종료 ===")
            }
        }
    }

    fun updatePaperCardKeepGroups(
        cardId: Int,
        base: UpdatePaperCardRequest,
        image1: java.io.File? = null,
        image2: java.io.File? = null
    ) {
        viewModelScope.launch {
            try {
                // 1) 현재 카드 상세 조회해서 기존 그룹 가져오기
                val detailRes = repository.getPaperCardDetail(cardId)
                val existingGroups: List<UpdateGroup>? = if (detailRes.isSuccessful) {
                    detailRes.body()?.result?.groups
                        ?.mapNotNull { gi -> gi.groupId }
                        ?.map { gid -> UpdateGroup(groupId = gid) }
                        // 서버가 null을 싫어하면 빈 리스트로
                        ?.let { it } ?: emptyList()
                } else {
                    // 상세 조회 실패 시, 최소한 null은 피함 (서버가 null에서 NPE)
                    emptyList()
                }

                // 2) base에 groups가 이미 채워져 있으면 건드리지 않고,
                //    없으면(existingGroups)로 채워서 그대로 보냄
                val merged = if (base.groups != null) base else base.copy(groups = existingGroups)

                // 3) 기존에 쓰던 업데이트 API 호출로 위임
                //  - 네 프로젝트에서 updatePaperCard의 시그니처가
                //    (cardId, request: UpdatePaperCardRequest) 인지,
                //    (cardId, request: RequestBody, image1?, image2?) 인지에 맞춰 호출하세요.
                updatePaperCard(cardId, merged, image1, image2)

            } catch (e: Exception) {
                _error.value = "업데이트 중 오류: ${e.message}"
            }
        }
    }

    // ✅ 메모 수정 함수
    fun updateMemo(cardId: Int, memo: MemoRequest) {
        Log.d("AI", "updateMemo() called cardId=$cardId")
        viewModelScope.launch {
            Log.d("AI", "updateMemo() launched")
            _isLoading.value = true
            _error.value = null

            try {
                Log.d("AI", "before AI call")
                // 🔽 메모 내용으로 AI 요약 생성
                val aiSummary = generateAiSummary(
                    relationship = memo.relationship,
                    personality = memo.personality,
                    workStyle = memo.workStyle,
                    meetingNotes = memo.meetingNotes,
                    etc = memo.etc
                )
                Log.d("AI", "after AI call summary=$aiSummary")

                // 🔽 summary를 채워서 서버에 보냄
                val req = memo.copy(summary = aiSummary)
                val response = repository.updateMemo(cardId, req)

                if (response.isSuccessful) {
                    getPaperCardDetail(cardId)
                    Log.d("API", "메모 수정 성공!")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API", "메모 수정 실패: ${response.code()}, 에러: $errorBody")
                    _error.value = "메모 수정 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("API", "메모 수정 예외 발생", e)
                _error.value = e.message ?: "알 수 없는 오류"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ✅ 종이명함 삭제 함수
    fun deletePaperCard(cardId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                Log.d("API", "=== 종이명함 삭제 시작 ===")
                Log.d("API", "CardId: $cardId")
                
                val response = repository.deletePaperCard(cardId)
                
                Log.d("API", "API 응답 받음: ${response.code()}")
                
                if (response.isSuccessful) {
                    Log.d("API", "종이명함 삭제 성공!")
                    // 삭제 성공 시 처리
                    _isSuccess.value = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API", "삭제 실패: ${response.code()}, 에러: $errorBody")
                    _error.value = "삭제 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("API", "종이명함 삭제 예외 발생", e)
                _error.value = e.message ?: "알 수 없는 오류"
            } finally {
                _isLoading.value = false
                Log.d("API", "=== 종이명함 삭제 종료 ===")
            }
        }
    }

    // ✅ 즐겨찾기 토글 함수
    fun toggleFavorite(cardId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                Log.d("API", "=== 즐겨찾기 토글 시작 ===")
                Log.d("API", "CardId: $cardId")
                
                val response = repository.toggleFavorite(cardId)
                
                Log.d("API", "API 응답 받음: ${response.code()}")
                
                if (response.isSuccessful) {
                    Log.d("API", "즐겨찾기 토글 성공!")
                    // 토글 성공 시 에러 상태만 클리어
                    _error.value = null
                    // ✅ 즐겨찾기 토글 후 상세 데이터를 다시 가져오지 않음 (UI에서 처리)
                    // getPaperCardDetail(cardId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API", "즐겨찾기 토글 실패: ${response.code()}, 에러: $errorBody")
                    _error.value = "즐겨찾기 토글 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("API", "즐겨찾기 토글 예외 발생", e)
                _error.value = e.message ?: "알 수 없는 오류"
            } finally {
                _isLoading.value = false
                Log.d("API", "=== 즐겨찾기 토글 종료 ===")
            }
        }
    }
}
