package com.example.businesscardapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardapp.data.network.MyCardRepository
import com.example.businesscardapp.ui.model.DigitalCardUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyCardsShakeViewModel(
private val repo: MyCardRepository
) : ViewModel() {

    private val _cards = MutableStateFlow<List<DigitalCardUi>>(emptyList())
    val cards: StateFlow<List<DigitalCardUi>> = _cards

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { repo.getMyCardsForViewer() } // token 없이 호출
                .onSuccess { _cards.value = it }
                .onFailure { _error.value = it.message ?: "명함 로딩 실패" }
            _loading.value = false
        }
    }
}

