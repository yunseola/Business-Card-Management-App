package com.example.businesscardapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardapp.data.model.GroupCreateRequest
import com.example.businesscardapp.data.model.GroupEditRequest
import com.example.businesscardapp.data.model.GroupItem
import com.example.businesscardapp.data.network.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {

    private val repository = GroupRepository() // 직접 생성

    private val _groupList = MutableStateFlow<List<GroupItem>>(emptyList())
    val groupList: StateFlow<List<GroupItem>> = _groupList

    fun fetchGroups() {
        viewModelScope.launch {
            try {
                Log.d("GroupViewModel", "📡 그룹 목록 요청 시작")

                val response = repository.getGroups()

                if (response.isSuccessful) {
                    val body = response.body()
                    val groups = body?.result?.groups.orEmpty()

                    Log.d("GroupViewModel", "✅ 그룹 목록 요청 성공: ${groups.size}개 수신됨")
                    groups.forEachIndexed { index, group ->
                        Log.d("GroupViewModel", "📦 그룹 $index: id=${group.groupId}, name=${group.name}, headcount=${group.headcount}")
                    }
                    _groupList.value = groups
                } else {
                    Log.e("GroupViewModel", "❌ 그룹 목록 요청 실패 - code: ${response.code()}, message: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "💥 예외 발생 - 그룹 목록 요청 중 오류", e)
            }
        }
    }

    fun createGroup(name: String) {
        viewModelScope.launch {
            try {
                Log.d("GroupViewModel", "📡 그룹 생성 요청 시작: name = $name")

                val request = GroupCreateRequest(name)
                val response = repository.createGroup(request)

                if (response.isSuccessful) {
                    Log.d("GroupViewModel", "✅ 그룹 생성 성공")
                    fetchGroups() // 생성 후 목록 새로고침
                } else {
                    Log.e("GroupViewModel", "❌ 그룹 생성 실패 - code: ${response.code()}, message: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "💥 예외 발생 - 그룹 생성 중 오류", e)
            }
        }
    }

    fun deleteGroupById(groupId: Int) {
        viewModelScope.launch {
            try {
                Log.d("GroupViewModel", "📡 그룹 삭제 요청 시작: id=$groupId")
                val response = repository.deleteGroup(groupId)
                if (response.isSuccessful) {
                    Log.d("GroupViewModel", "✅ 그룹 삭제 성공")
                    fetchGroups() // 삭제 후 목록 갱신
                } else {
                    Log.e("GroupViewModel", "❌ 그룹 삭제 실패 - ${response.code()}, ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "💥 그룹 삭제 중 예외 발생", e)
            }
        }
    }

    fun editGroupById(groupId: Int, name: String) {
        viewModelScope.launch {
            try {
                Log.d("GroupViewModel", "📡 그룹 수정 요청 시작: id=$groupId, name=$name")
                val response = repository.editGroup(
                    groupId = groupId,
                    request = GroupEditRequest(name)
                )
                if (response.isSuccessful) {
                    Log.d("GroupViewModel", "✅ 그룹 수정 성공")
                    fetchGroups() // 수정 후 목록 갱신
                } else {
                    Log.e("GroupViewModel", "❌ 그룹 수정 실패 - ${response.code()}, ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "💥 그룹 수정 중 예외 발생", e)
            }
        }
    }

    // GroupViewModel.kt 내부 (class GroupViewModel : ViewModel() { ... } 안)
    suspend fun commitGroupEdits(
        originals: List<GroupItem>,
        edits: Map<Int, String>
    ) {
        for (g in originals) {
            val newName = edits[g.groupId]?.trim()
            if (!newName.isNullOrBlank() && newName != g.name) {
                try {
                    Log.d("GroupVM", "📡 PUT /api/groups/${g.groupId} body={name:$newName}")
                    val resp = repository.editGroup(
                        groupId = g.groupId,
                        request = GroupEditRequest(newName)
                    )
                    if (!resp.isSuccessful) {
                        Log.e("GroupVM", "❌ editGroup fail id=${g.groupId} code=${resp.code()} msg=${resp.message()}")
                    } else {
                        Log.d("GroupVM", "✅ editGroup ok id=${g.groupId}")
                    }
                } catch (e: Exception) {
                    Log.e("GroupVM", "💥 editGroup exception id=${g.groupId}", e)
                }
            }
        }
    }


}
