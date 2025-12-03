package com.example.businesscardapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardapp.data.model.MemoRequest
import com.example.businesscardapp.data.model.EditGroupRequest
import com.example.businesscardapp.data.model.GroupName
import com.example.businesscardapp.data.model.BasicResponse
import com.example.businesscardapp.data.model.DigitalCardDetailResponse
import com.example.businesscardapp.data.network.DigitalCardRepository
import com.example.businesscardapp.BuildConfig
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DigitalCardViewModel : ViewModel() {
    
    private val digitalCardRepository = DigitalCardRepository()

    // 디지털 명함 상세 정보 상태
    private val _digitalCardDetail = MutableStateFlow<DigitalCardDetailResponse?>(null)
    val digitalCardDetail: StateFlow<DigitalCardDetailResponse?> = _digitalCardDetail

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 에러 상태
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 즐겨찾기 토글 상태
    private val _isTogglingFavorite = MutableStateFlow(false)
    val isTogglingFavorite: StateFlow<Boolean> = _isTogglingFavorite

    // 삭제 상태
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    // 그룹 수정 상태
    private val _isEditingGroup = MutableStateFlow(false)
    val isEditingGroup: StateFlow<Boolean> = _isEditingGroup

    // 메모 수정 상태
    private val _isUpdatingMemo = MutableStateFlow(false)
    val isUpdatingMemo: StateFlow<Boolean> = _isUpdatingMemo

    // 디지털 명함 상세 조회
    fun getDigitalCardDetail(cardId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = digitalCardRepository.getDigitalCardDetail(cardId, BuildConfig.GMS_KEY)
                if (response != null) {
                    _digitalCardDetail.value = response
                } else {
                    _error.value = "디지털 명함 정보를 가져올 수 없습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "알 수 없는 오류"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 디지털 명함 메모 수정
    fun updateMemo(cardId: String, memo: MemoRequest) {
        viewModelScope.launch {
            _isUpdatingMemo.value = true
            try {
                val response = digitalCardRepository.updateMemo(cardId, memo, BuildConfig.GMS_KEY)
                if (response != null) {
                    Log.d("DigitalCardViewModel", "메모 수정 성공")
                    // 메모 수정 후 상세 정보 다시 가져오기
                    getDigitalCardDetail(cardId)
                } else {
                    Log.e("DigitalCardViewModel", "메모 수정 실패")
                    _error.value = "메모 수정에 실패했습니다."
                }
            } catch (e: Exception) {
                Log.e("DigitalCardViewModel", "메모 수정 예외 발생", e)
                _error.value = "메모 수정 중 오류가 발생했습니다."
            } finally {
                _isUpdatingMemo.value = false
            }
        }
    }

    // 디지털 명함 그룹 수정
    fun editDigitalCardGroup(cardId: String, groupNames: List<String>) {
        viewModelScope.launch {
            _isEditingGroup.value = true
            try {
                val response = digitalCardRepository.editDigitalCardGroup(cardId, groupNames)
                if (response != null) {
                    Log.d("DigitalCardViewModel", "그룹 수정 성공")
                    // 그룹 수정 후 상세 정보 다시 가져오기
                    getDigitalCardDetail(cardId)
                } else {
                    Log.e("DigitalCardViewModel", "그룹 수정 실패")
                    _error.value = "그룹 수정에 실패했습니다."
                }
            } catch (e: Exception) {
                Log.e("DigitalCardViewModel", "그룹 수정 예외 발생", e)
                _error.value = "그룹 수정 중 오류가 발생했습니다."
            } finally {
                _isEditingGroup.value = false
            }
        }
    }

    // 디지털 명함 삭제
    fun deleteDigitalCard(cardId: String) {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                val response = digitalCardRepository.deleteDigitalCard(cardId, BuildConfig.GMS_KEY)
                if (response != null) {
                    Log.d("DigitalCardViewModel", "디지털 명함 삭제 성공")
                } else {
                    Log.e("DigitalCardViewModel", "디지털 명함 삭제 실패")
                    _error.value = "디지털 명함 삭제에 실패했습니다."
                }
            } catch (e: Exception) {
                Log.e("DigitalCardViewModel", "디지털 명함 삭제 예외 발생", e)
                _error.value = "디지털 명함 삭제 중 오류가 발생했습니다."
            } finally {
                _isDeleting.value = false
            }
        }
    }

    // 디지털 명함 즐겨찾기 토글
    fun toggleFavoriteDigitalCard(cardId: String) {
        viewModelScope.launch {
            _isTogglingFavorite.value = true
            try {
                val response = digitalCardRepository.toggleFavoriteDigitalCard(cardId, BuildConfig.GMS_KEY)
                if (response != null) {
                    Log.d("DigitalCardViewModel", "즐겨찾기 토글 성공")
                    // ✅ 즐겨찾기 토글 후 상세 데이터를 다시 가져오지 않음 (UI에서 처리)
                    // getDigitalCardDetail(cardId)
                } else {
                    Log.e("DigitalCardViewModel", "즐겨찾기 토글 실패")
                    _error.value = "즐겨찾기 토글에 실패했습니다."
                }
            } catch (e: Exception) {
                Log.e("DigitalCardViewModel", "즐겨찾기 토글 예외 발생", e)
                _error.value = "즐겨찾기 토글 중 오류가 발생했습니다."
            } finally {
                _isTogglingFavorite.value = false
            }
        }
    }

    // 에러 초기화
    fun clearError() {
        _error.value = null
    }
}
