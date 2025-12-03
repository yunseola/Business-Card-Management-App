package com.example.businesscardapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardapp.data.model.GroupMemberItem
import com.example.businesscardapp.data.network.GroupRepository // 프로젝트 구조에 맞게
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupDetailViewModel : ViewModel() {

    private val repository = GroupRepository() // 프로젝트 구조에 맞게

    private val _members = MutableStateFlow<List<GroupMemberItem>>(emptyList())
    val members: StateFlow<List<GroupMemberItem>> = _members

    private val _allMembers = MutableStateFlow<List<GroupMemberItem>>(emptyList())
    val allMembers: StateFlow<List<GroupMemberItem>> = _allMembers

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadGroupMembers(groupId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val res = repository.getGroupMembers(groupId)
                if (res.isSuccessful) {
                    val list = res.body()?.result.orEmpty()
                    _allMembers.value = list              // 원본 저장
                    _members.value = list                 // 화면에 표시
                } else {
                    _error.value = "코드 ${res.code()} 오류"
                    _allMembers.value = emptyList()
                    _members.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "loadGroupMembers error", e)
                _error.value = "네트워크 오류"
                _allMembers.value = emptyList()
                _members.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /** 검색 실행 */
    fun searchMembers(query: String) {
        val q = query.trim()
        if (q.isEmpty()) {
            _members.value = _allMembers.value
            return
        }
        _members.value = _allMembers.value.filter { m ->
            m.name.contains(q, ignoreCase = true) ||
                    m.company.contains(q, ignoreCase = true) ||
                    m.phone.contains(q, ignoreCase = true)
        }
    }

    /** 검색 초기화(전체 보기) */
    fun clearSearch() {
        _members.value = _allMembers.value
    }
}
