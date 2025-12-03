package com.example.businesscardapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardapp.data.network.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.net.SocketTimeoutException
import java.net.ConnectException

data class CompanyEmailState(
    val email: String = "",
    val loading: Boolean = false,
    val toast: String? = null,
    val sent: Boolean = false,       // 201: 발송 성공 → 다음 화면 이동 트리거
    val notCompany: Boolean = false  // 200: 회사 메일 아님
)

class CompanyVerifyEmailViewModel : ViewModel() {

    // ✅ CardBoxViewModel과 동일하게, 내부에서 직접 생성
    private val repository = Repository()

    private val _ui = MutableStateFlow(CompanyEmailState())
    val ui: StateFlow<CompanyEmailState> get() = _ui

    fun onEmailChange(v: String) {
        _ui.value = _ui.value.copy(email = v, toast = null, notCompany = false, sent = false)
    }

    fun requestCode(cardId: Int) {
        val cur = _ui.value
        if (cur.loading) return

        _ui.value = cur.copy(loading = true, toast = null, notCompany = false, sent = false)

        viewModelScope.launch {
            try {
                val response = repository.verifyCompanyEmail(cardId, cur.email)
                val code = response.code()
                val msg = response.body()?.message ?: response.errorBody()?.string().orEmpty()

                Log.d("CompanyVerifyVM", "응답 코드: $code / 메시지: $msg")

                when (code) {
                    201 -> { // 일치 & 발송
                        _ui.value = _ui.value.copy(
                            loading = false,
                            toast = if (msg.isBlank()) "인증 코드를 이메일로 발송했습니다." else msg,
                            sent = true
                        )
                    }
                    200 -> { // 불일치(회사 이메일 아님)
                        _ui.value = _ui.value.copy(
                            loading = false,
                            toast = if (msg.isBlank()) "회사 이메일이 아닙니다." else msg,
                            notCompany = true
                        )
                    }
                    400 -> _ui.value = _ui.value.copy(loading = false, toast = msg.ifBlank { "요청 형식이 올바르지 않습니다." })
                    404 -> _ui.value = _ui.value.copy(loading = false, toast = msg.ifBlank { "명함이 존재하지 않습니다." })
                    424 -> _ui.value = _ui.value.copy(loading = false, toast = msg.ifBlank { "회사 홈페이지 정보를 확인할 수 없습니다." })
                    500 -> _ui.value = _ui.value.copy(loading = false, toast = msg.ifBlank { "server error!" })
                    else -> _ui.value = _ui.value.copy(loading = false, toast = "알 수 없는 응답($code)")
                }
            } catch (e: SocketTimeoutException) {
                Log.d("CompanyVerifyVM", "서버 타임아웃")
                _ui.value = _ui.value.copy(loading = false, toast = "서버 응답 지연")
            } catch (e: ConnectException) {
                Log.d("CompanyVerifyVM", "서버 연결 실패")
                _ui.value = _ui.value.copy(loading = false, toast = "서버 연결 실패")
            } catch (e: Exception) {
                Log.d("CompanyVerifyVM", "예외: ${e.message}")
                _ui.value = _ui.value.copy(loading = false, toast = (e.message ?: "네트워크 오류"))
            }
        }
    }
}
