package com.example.businesscardapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardapp.data.model.Card
import com.example.businesscardapp.data.network.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.net.SocketTimeoutException
import java.net.ConnectException

class CardBoxViewModel : ViewModel() {

    private val repository = Repository()

    private val _cardList = MutableStateFlow<List<Card>>(emptyList())
    val cardList: StateFlow<List<Card>> get() = _cardList

    fun loadCards() {
        viewModelScope.launch {
            try {
                // Repository.getCardList(): Response<ApiResponse<List<Card>>>
                val response = repository.getCardList()

                Log.d("CardBoxViewModel", "응답 코드: ${response.code()}")
                Log.d("CardBoxViewModel", "응답 메시지: ${response.message()}")

                if (response.isSuccessful) {
                    val cards: List<Card> = response.body()?.result ?: emptyList() // ✅ 여기만 변경
                    Log.d("CardBoxViewModel", "카드 개수: ${cards.size}")
                    _cardList.value = cards
                } else {
                    val err = response.errorBody()?.string()
                    Log.d("CardBoxViewModel", "API 응답 실패: $err")
                    _cardList.value = emptyList()
                }
            } catch (e: SocketTimeoutException) {
                Log.d("CardBoxViewModel", "백엔드 서버 연결 타임아웃")
                _cardList.value = emptyList()
            } catch (e: ConnectException) {
                Log.d("CardBoxViewModel", "백엔드 서버 연결 실패")
                _cardList.value = emptyList()
            } catch (e: Exception) {
                Log.d("CardBoxViewModel", "백엔드 API 호출 중 예외 발생: ${e.message}")
                _cardList.value = emptyList()
            }
        }
    }
}