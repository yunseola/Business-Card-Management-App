package com.example.businesscardapp.ui.screen.share

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardapp.BuildConfig
import com.example.businesscardapp.data.local.TokenProvider
import com.example.businesscardapp.data.model.BasicResponse
import com.example.businesscardapp.data.network.RetrofitClient
import com.example.businesscardapp.data.remote.ApiEnvelope
import com.example.businesscardapp.data.remote.PublicCardResult
import com.example.businesscardapp.util.PrefUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

data class RegisterUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val successCardId: String? = null,
    val previewName: String = "",
    val previewImage: String? = null
)

@HiltViewModel
class RegisterFromShareViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val api = RetrofitClient.apiService
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    /** 공개 미리보기 로딩 (이름/이미지) */
    fun loadPreview(cardId: Int) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.cardApi.getPublicCard(cardId)
                if (resp.isSuccessful) {
                    val body: ApiEnvelope<PublicCardResult>? = resp.body()
                    _uiState.value = _uiState.value.copy(
                        previewName = body?.result?.ownerName.orEmpty(),
                        previewImage = body?.result?.imageUrl
                    )
                } else {
                    val err = resp.errorBody()?.string()
                    Log.w(
                        "RegisterFromShareVM",
                        "preview fail code=${resp.code()} msg=${resp.message()} body=$err"
                    )
                }
            } catch (e: Exception) {
                Log.e("RegisterFromShareVM", "preview exception", e)
            }
        }
    }

    /** 디지털 명함 팔로우(등록) — 모든 에러를 동일 멘트로 처리, 로딩 최소 2초 유지 */
    fun register(cardId: String) {
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            setLoading(true)

            val errorMent = "이미 연결된 디지털 명함입니다"

            try {
                // Interceptor가 Authorization 헤더 붙이도록 TokenProvider 채우기
                if (TokenProvider.token.isNullOrBlank()) {
                    PrefUtil.getJwtToken(context)?.let { TokenProvider.token = it }
                }

                val resp: Response<BasicResponse> = api.registerDigitalCard(
                    cardId = cardId,
                    accessToken = BuildConfig.GMS_KEY
                )

                // 최소 2초 보장
                ensureMinLoading(start, 2000)

                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        successCardId = cardId,
                        errorMessage = null
                    )
                } else {
                    val errBody = resp.errorBody()?.string()
                    Log.e(
                        "RegisterFromShareVM",
                        "register fail code=${resp.code()} msg=${resp.message()} " +
                                "headers=${resp.headers()} body=$errBody"
                    )
                    // ❗모든 에러 멘트 통일 + 미리보기 값 보존
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        successCardId = null,
                        errorMessage = errorMent
                    )
                }
            } catch (e: Exception) {
                Log.e("RegisterFromShareVM", "register exception", e)
                ensureMinLoading(start, 2000)
                // ❗네트워크/예외도 동일 멘트 + 미리보기 값 보존
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    successCardId = null,
                    errorMessage = errorMent
                )
            }
        }
    }

    // --- helpers ---

    private fun setLoading(value: Boolean) {
        _uiState.value = _uiState.value.copy(loading = value)
    }

    /** 최소 minMillis 만큼 로딩 유지 */
    private suspend fun ensureMinLoading(startMillis: Long, minMillis: Long) {
        val elapsed = System.currentTimeMillis() - startMillis
        val remain = minMillis - elapsed
        if (remain > 0) delay(remain)
    }
}
