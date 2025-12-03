package com.example.businesscardapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardapp.data.network.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class CodeUi(
    val code: String = "",
    val loading: Boolean = false,
    val success: Boolean = false,
    val toast: String? = null
)

class CompanyVerifyCodeViewModel : ViewModel() {
    private val repo = Repository()

    private val _ui = MutableStateFlow(CodeUi())
    val ui: StateFlow<CodeUi> = _ui

    fun onCodeChange(v: String) {
        _ui.value = _ui.value.copy(code = v.filter { it.isDigit() }.take(6))
    }

    fun submit(cardId: Int, email: String) {
        val code = _ui.value.code.trim()
        if (code.length != 6) { /* 에러 처리 */ return }
        _ui.value = _ui.value.copy(loading = true, toast = null)
        viewModelScope.launch {
            val resp = repo.verifyCompanyCode(cardId, email, code)
            if (resp.isSuccessful && resp.body()?.verify == 1) {
                _ui.value = _ui.value.copy(loading = false, success = true, toast = resp.body()?.message)
            } else {
                val msg = resp.body()?.message ?: resp.errorBody()?.string().orEmpty()
                _ui.value = _ui.value.copy(loading = false, toast = msg.ifBlank { "인증 실패" })
            }
        }
    }

    fun consumeToast() { _ui.value = _ui.value.copy(toast = null) }
}
