package com.example.businesscardapp.ui.screen.mycard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardapp.data.model.MyCardDetailResponse
import com.example.businesscardapp.data.network.ApiService
import com.example.businesscardapp.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MyCardQrUiState(
    val loading: Boolean = true,
    val qrCodeUrl: String? = null,
    val errorMessage: String? = null
)

class MyCardQrViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // DI 없이 직접 참조
    private val api: ApiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow(MyCardQrUiState())
    val uiState: StateFlow<MyCardQrUiState> = _uiState

    // NavGraph에서 "mycard/qr?cardId={cardId}" 로 전달받음
    private val cardId: Int? =
        savedStateHandle.get<String?>("cardId")?.toIntOrNull()

    init {
        load()
    }

    private fun load() = viewModelScope.launch {
        if (cardId == null) {
            _uiState.value = MyCardQrUiState(
                loading = false,
                errorMessage = "cardId가 없어 QR을 표시할 수 없습니다."
            )
            return@launch
        }

        _uiState.value = MyCardQrUiState(loading = true)

        try {
            val resp = api.getMyCardDetail(cardId) // Response<MyCardDetailResponse>
            if (!resp.isSuccessful) {
                _uiState.value = MyCardQrUiState(
                    loading = false,
                    errorMessage = "서버 오류: ${resp.code()}"
                )
                return@launch
            }

            val body: MyCardDetailResponse? = resp.body()
            val qr = body?.result?.qrCodeUrl

            if (qr.isNullOrBlank()) {
                _uiState.value = MyCardQrUiState(
                    loading = false,
                    errorMessage = "qrCodeUrl이 없습니다."
                )
            } else {
                _uiState.value = MyCardQrUiState(
                    loading = false,
                    qrCodeUrl = qr
                )
            }
        } catch (e: Exception) {
            _uiState.value = MyCardQrUiState(
                loading = false,
                errorMessage = "네트워크 오류가 발생했습니다."
            )
        }
    }
}
