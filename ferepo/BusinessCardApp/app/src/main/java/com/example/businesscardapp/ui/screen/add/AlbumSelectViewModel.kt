//AlbumSelectViewModel.kt
package com.example.businesscardapp.ui.screen.add

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AlbumSelectViewModel : ViewModel() {

    // 전체 선택 가능한 이미지 리스트
    private val _allUris = MutableStateFlow<List<Uri>>(emptyList())
    val allUris: StateFlow<List<Uri>> = _allUris

    // 실제 사용자가 선택한 이미지 리스트 (최대 2장)
    private val _selectedUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedUris: StateFlow<List<Uri>> = _selectedUris

    /** 앨범에서 가져온 전체 이미지 리스트 저장 (선택은 초기화) */
    fun setUris(uris: List<Uri>) {
        _allUris.value = uris
        _selectedUris.value = emptyList() // 초기화
    }

    /** 이미지 선택 또는 선택 해제 토글. 최대 2장까지만 가능 */
    fun toggleUri(uri: Uri) {
        val current = _selectedUris.value.toMutableList()
        if (current.contains(uri)) {
            current.remove(uri)
        } else if (current.size < 2) {
            current.add(uri)
        }
        _selectedUris.value = current
    }
}
