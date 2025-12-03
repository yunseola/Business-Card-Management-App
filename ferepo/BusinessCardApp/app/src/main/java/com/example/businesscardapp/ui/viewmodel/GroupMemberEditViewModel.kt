package com.example.businesscardapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardapp.data.model.Card
import com.example.businesscardapp.data.model.GroupMemberItem
import com.example.businesscardapp.data.model.GroupMemberUpdate
import com.example.businesscardapp.data.model.GroupMembersRequest
import com.example.businesscardapp.data.network.GroupRepository
import com.example.businesscardapp.data.network.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupMemberEditViewModel(
    private val groupRepo: GroupRepository = GroupRepository(),
    private val cardRepo: Repository = Repository()
) : ViewModel() {

    private val _allCards = MutableStateFlow<List<Card>>(emptyList())
    val allCards: StateFlow<List<Card>> = _allCards

    private val _currentMembers = MutableStateFlow<Set<Int>>(emptySet())
    val currentMembers: StateFlow<Set<Int>> = _currentMembers

    private val _selected = MutableStateFlow<Set<Int>>(emptySet())
    val selected: StateFlow<Set<Int>> = _selected

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load(groupId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // 1) 내 전체 명함
                val cardsResp = cardRepo.getCardList()
                if (cardsResp.isSuccessful) {
                    _allCards.value = cardsResp.body()?.result ?: emptyList()
                } else {
                    _error.value = "내 명함 조회 실패 (${cardsResp.code()})"
                }

                // 2) 현재 그룹 멤버
                val memResp = groupRepo.getGroupMembers(groupId)
                if (memResp.isSuccessful) {
                    val ids = memResp.body()?.result.orEmpty()
                        .map(GroupMemberItem::id)
                        .toSet()
                    _currentMembers.value = ids
                    _selected.value = ids
                } else {
                    _error.value = "그룹 멤버 조회 실패 (${memResp.code()})"
                }
            } catch (e: Exception) {
                _error.value = "예외: ${e.message}"
                Log.e("GroupMemberEditVM", "load error", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggle(cardId: Int) {
        val set = _selected.value.toMutableSet()
        if (!set.add(cardId)) set.remove(cardId)
        _selected.value = set
    }

    fun isChecked(cardId: Int) = _selected.value.contains(cardId)

    /**
     * ✅ 서버 스펙 변경 반영:
     * - body: { groupId, members:[{cardId, digital}] }
     * - 종이/디지털 구분 없이 한 번에 전송
     */
    fun save(groupId: Int, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val byId = _allCards.value.associateBy { it.cardId }
                val members = _selected.value.mapNotNull { id ->
                    val card = byId[id] ?: return@mapNotNull null
                    GroupMemberUpdate(cardId = id, digital = card.isDigital)
                }

                val resp = groupRepo.putGroupMembers(
                    groupId = groupId,
                    request = GroupMembersRequest(groupId = groupId, members = members)
                )

                val ok = resp.isSuccessful
                if (!ok) _error.value = "저장 실패 (${resp.code()})"
                onDone(ok)
            } catch (e: Exception) {
                _error.value = "예외: ${e.message}"
                Log.e("GroupMemberEditVM", "save error", e)
                onDone(false)
            } finally {
                _loading.value = false
            }
        }
    }
}
