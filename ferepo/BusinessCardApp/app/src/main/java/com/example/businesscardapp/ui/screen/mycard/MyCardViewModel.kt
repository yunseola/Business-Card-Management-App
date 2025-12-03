package com.example.businesscardapp.ui.screen.mycard

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.businesscardapp.data.model.MyCardDetailResponse
import com.example.businesscardapp.data.model.MyCardRegisterResponse
import com.example.businesscardapp.data.model.MemoRequest
import com.example.businesscardapp.data.model.EditGroupRequest
import com.example.businesscardapp.data.model.GroupName
import com.example.businesscardapp.data.model.BasicResponse
import com.example.businesscardapp.data.network.ApiService
import com.example.businesscardapp.data.network.Repository
import com.example.businesscardapp.data.network.RetrofitClient
import com.example.businesscardapp.data.network.DigitalCardRepository
import com.example.businesscardapp.BuildConfig
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.businesscardapp.data.model.MyCardEditRequest
import com.example.businesscardapp.data.model.EditField
import com.example.businesscardapp.data.model.MyCardDetail
import com.example.businesscardapp.data.model.MyCardRegisterRequest
import com.example.businesscardapp.util.uriToPart
import java.util.Collections.emptyList





// ===== UI에서 쓰는 간단 데이터 =====
data class FieldState(val label: String, val value: String)

data class UiMyCardField(
    val fieldName: String,
    val fieldValue: String,
    val order: Int? = null
)

data class MyCard(
    val name: String,
    val phone: String,
    val company: String,
    val imageUrlH: String? = null,
    val imageUrlV: String?= null,
    val backgroundImageUrl: String, // HEX
    val profileUrl: String? = null,
    val fontColor: Boolean,         // true = 검정(미리보기용)
    val isConfirm: Boolean,
    val createAt: String,
    val fields: List<UiMyCardField>,
    val serverId: Int? = null       // ★ 추가
) {

}

data class MyCardListItem(
    @SerializedName("cardId", alternate = ["cardid"])
    val cardId: Int,
    @SerializedName(value = "confirmed", alternate = ["confirm","isConfirm","isConfirmed"])
    val confirmed: Boolean,
    @SerializedName("imageUrlHorizontal", alternate = ["imageUrlHorizantal","image_horizontal"])
    val imageUrlHorizontal: String?,
    @SerializedName(value = "imageUrlVertical",
        alternate = ["image_vertical","verticalImageUrl","imageUrlV"])
    val imageUrlVertical: String?
)


class MyCardViewModel : ViewModel() {

    private var currentDetailId: Int? = null   // ★ 추가

    private val myCardRepo = com.example.businesscardapp.data.network.MyCardRepository(
        RetrofitClient.apiService
    )


    private fun toAbsoluteUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http://") || path.startsWith("https://")) path
        else "https://i13e201.p.ssafy.io$path" // BASE_URL에 맞게
    }


    fun refreshMyCards() = viewModelScope.launch {
        try {
            val resp = myCardRepo.getMyCardList() // 반환형: MyCardListResponse
            val items = resp?.result.orEmpty()

            // 디버그
            android.util.Log.d("MyCardVM", "mine size=${items.size}")
            items.take(3).forEach {
                android.util.Log.d(
                    "MyCardVM",
                    "id=${it.cardId}, v=${it.imageUrlVertical}, h=${it.imageUrlHorizontal}"
                )
            }

            _cards.value = items.map { item ->
                val v = toAbsoluteUrl(item.imageUrlVertical)
                val h = toAbsoluteUrl(item.imageUrlHorizontal)
                MyCard(
                    name = "",
                    phone = "",
                    company = "",
                    imageUrlH = h,                 // 가로/세로 둘 다 보관
                    imageUrlV = v,
                    backgroundImageUrl = "#FFFFFF",
                    profileUrl = null,             // 목록 단계에선 없음
                    fontColor = true,
                    isConfirm = item.confirmed,
                    createAt = "",
                    fields = emptyList(),
                    serverId = item.cardId
                )
            }

            // (선택) 텍스트/프로필 보강: 목록 아이템별 상세를 비동기로 채우기
            _cards.value.forEach { c ->
                c.serverId?.let { id ->
                    viewModelScope.launch {
                        val detail = myCardRepo.getMyCardDetail(id)
                        detail?.result?.let { d ->
                            _cards.update { list ->
                                list.map {
                                    if (it.serverId == id) it.copy(
                                        name = d.name.orEmpty(),
                                        company = d.company.orEmpty(),
                                        phone = d.phone.orEmpty(),
                                        profileUrl = toAbsoluteUrl(d.customImageUrl) // ✅ 프로필
                                    ) else it
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MyCardVM", "refreshMyCards error", e)
        }
    }







    fun tryRefreshFromServerIfEmpty() {
        if (_cards.value.isNotEmpty()) return
        refreshMyCards()   // ✅ 이미 아래에 만든 함수 재사용
    }


    // 필드 라벨 정규화는 이미 있는 normalizeLabel(...)를 그대로 사용합니다.
    fun updateOrAddField(label: String, value: String) {
        val norm = normalizeLabel(label)
        viewModelScope.launch {
            _fields.update { list ->
                val idx = list.indexOfFirst { normalizeLabel(it.label) == norm }
                val base = list.toMutableList()
                if (idx >= 0) {
                    // 기존 항목 값만 업데이트
                    base[idx] = base[idx].copy(value = value)
                    base.pinRequiredOnTop()
                } else {
                    // 추가 필드는 최대 5개 제한 유지
                    val extrasCnt = base.count { normalizeLabel(it.label) !in REQUIRED }
                    if (extrasCnt >= 5) return@update base
                    (base + FieldState(norm, value)).pinRequiredOnTop()
                }
            }
        }
    }

    private val fetched = mutableSetOf<Int>()

    fun prefetchCardDetail(id: Int) {
        if (!fetched.add(id)) return // 이미 가져온 건 생략
        viewModelScope.launch {
            val d = myCardRepo.getMyCardDetail(id)?.result ?: return@launch
            _cards.update { list ->
                list.map {
                    if (it.serverId == id) it.copy(
                        name = d.name.orEmpty(),
                        company = d.company.orEmpty(),
                        phone = d.phone.orEmpty(),
                        profileUrl = toAbsoluteUrl(d.customImageUrl)
                    ) else it
                }
            }
        }
    }


    // OCR에서 사진 URI를 저장할 때 호출
    fun setPhotoUri(uri: Uri?) {
        _photoUri.value = uri
    }

    // ===== Repository 준비 (RetrofitClient 그대로 사용) =====
    private val api: ApiService = RetrofitClient.apiService
    private val repository = Repository() // 이미 RetrofitClient 쓰는 구현


    // ===== 화면 상태 =====
    private val REQUIRED = listOf("이름", "연락처", "회사")

    private val _fields = MutableStateFlow(
        listOf(FieldState("이름",""), FieldState("연락처",""), FieldState("회사",""))
    )
    val fields: StateFlow<List<FieldState>> = _fields

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri

    // 패턴 선택 (예: "pattern3" 또는 null)
    private val _pattern = MutableStateFlow<String?>(null)
    val pattern: StateFlow<String?> = _pattern
    fun setPattern(code: String?) { _pattern.value = code }

    private val _background = MutableStateFlow("#FFFFFF")
    val background: StateFlow<String> = _background
    fun setBackground(hex: String) { _background.value = hex }

    // MyCardViewModel.kt (상태들 근처)
    private val _bgNum = MutableStateFlow<Int?>(null)
    val bgNum: StateFlow<Int?> = _bgNum


    fun setBackgroundImageNum(n: Int?) { _bgNum.value = n }

    // 색상 매핑 (단색 100~108)
    private val bgColorMap = mapOf(
        100 to "#FFC107", 101 to "#00CED1", 102 to "#0D9488", 103 to "#1E3A8A",
        104 to "#FF5722", 105 to "#D6C7B0", 106 to "#333333", 107 to "#F9F9F6", 108 to "#F9F9F6"
    )


    private val _textDark = MutableStateFlow(true) // true=검정(미리보기)
    val textDark: StateFlow<Boolean> = _textDark
    fun setTextDark(dark: Boolean) { _textDark.value = dark }

    private val _cards = MutableStateFlow<List<MyCard>>(emptyList())
    val cards: StateFlow<List<MyCard>> = _cards

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    private fun normalizeLabel(raw: String): String = when (raw.trim()) {
        "휴대전화","전화번호","핸드폰","Mobile","mobile","Phone","phone" -> "연락처"
        "성명","Name","name" -> "이름"
        "회사명","Company","company" -> "회사"
        else -> raw.trim()
    }

    fun updateField(idx: Int, new: FieldState) = viewModelScope.launch {
        _fields.update { it.toMutableList().apply { set(idx, new) }.pinRequiredOnTop() }
    }
    fun addField() = viewModelScope.launch { _fields.update { it + FieldState("새 필드","") } }
    fun removeField(index: Int) = viewModelScope.launch {
        _fields.update { list ->
            if (index !in list.indices) return@update list
            val t = list[index]; if (normalizeLabel(t.label) in REQUIRED) return@update list
            list.toMutableList().apply { removeAt(index) }.pinRequiredOnTop()
        }
    }

    private fun getValue(label: String): String {
        val norm = normalizeLabel(label)
        return _fields.value.firstOrNull { normalizeLabel(it.label)==norm }?.value.orEmpty()
    }

    private fun List<FieldState>.pinRequiredOnTop(): List<FieldState> {
        val map = this.groupBy { normalizeLabel(it.label) }.mapValues { it.value.last() }
        val fixed = REQUIRED.map { k -> map[k]?.copy(label=k) ?: FieldState(k,"") }
        val extras = this.filter { normalizeLabel(it.label) !in REQUIRED }
        return fixed + extras
    }

    //===============================================================================
    // ★ 개발 중 로컬 동작용 플래그 (원하면 false로)






    // 앨범에서 고른 ‘프로필 이미지’(명함 오른쪽 하단에 들어갈 사진)
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri
    fun setProfileImageUri(uri: Uri?) { _profileImageUri.value = uri }

    // 추가필드 중 ‘미리보기에 보이도록’ 체크된 라벨의 순서 보관
    private val _visibleExtraLabels = MutableStateFlow<List<String>>(emptyList())
    val visibleExtraLabels: StateFlow<List<String>> = _visibleExtraLabels

    // 필수 라벨 제외한 전체 추가필드 라벨
    private val FIXED_ORDER = listOf("이름","연락처","회사","직책","이메일")
    fun applyVisibleFields(visibleLabelsInOrder: List<String>) {
        // 필수는 제외하고, 존재하는 추가필드만 + 순서 그대로 반영
        val normalized = visibleLabelsInOrder
            .map { it.trim() }
            .filter { it.isNotBlank() && it !in FIXED_ORDER }
        _visibleExtraLabels.value = normalized.distinct()
    }



    // 미리보기용 extras (라벨, 값) 리스트 — 체크 순서대로, 공백 제외, 최대 5개
    val previewExtras: List<Pair<String, String>>
        get() {
            val map = _fields.value.associate { it.label to it.value }
            return _visibleExtraLabels.value
                .mapNotNull { lbl ->
                    val v = map[lbl].orEmpty().trim()
                    if (v.isNotBlank()) lbl to v else null
                }
                .take(5)
        }






    // 필드선택 방식 유지 + 미리보기 반영 순서
    private val _selectedLabels = MutableStateFlow<List<String>>(emptyList())
    val selectedLabels: StateFlow<List<String>> = _selectedLabels

    fun setSelectedLabels(labelsInOrder: List<String>) {
        _selectedLabels.value = labelsInOrder
        applyVisibleFields(labelsInOrder) // 이미 구현돼 있는 함수 재사용
    }



    fun saveMyCardToListAndReturnIndex(): Int {
        val card = buildCardOrNull() ?: return -1
        val newList = _cards.value + card
        _cards.value = newList
        return newList.lastIndex
    }

    // ===== 생성 상태 =====
    sealed class CreateState {
        data object Idle : CreateState()
        data object Loading : CreateState()
        data class Success(val cardId: Int) : CreateState()
        data class Fail(val code: Int?, val msg: String) : CreateState()
    }
    private val _createState = MutableStateFlow<CreateState>(CreateState.Idle)
    val createState: StateFlow<CreateState> = _createState

    // ===== 상세 상태 =====
    sealed class DetailState {
        data object Idle : DetailState()
        data object Loading : DetailState()
        data class Success(val data: MyCardDetailResponse) : DetailState()
        data class Fail(val code: Int?, val msg: String) : DetailState()
    }
    private val _detailState = MutableStateFlow<DetailState>(DetailState.Idle)
    val detailState: StateFlow<DetailState> = _detailState




    // ★ 임시 cardId는 음수로 발급 (-1, -2, ...)
    private val useLocalFallback: Boolean = true
    private var nextTempId = -1
    private fun allocateTempId(): Int = nextTempId--
    // ★ 로컬 저장 후 Success로 전환
    private fun createLocalAndSuccess(): Int {
        val card = buildCardOrNull() ?: run {
            _createState.value = CreateState.Fail(null, "이름/연락처/회사는 필수입니다.")
            return -1
        }
        val tempId = allocateTempId()
        _cards.value = _cards.value + card.copy(serverId = tempId)
        _createState.value = CreateState.Success(tempId)
        return tempId
    }


    private fun buildEditFieldsForApi(): List<EditField>? {
        val map = _fields.value.associate { it.label to it.value }
        val list = _visibleExtraLabels.value.mapIndexed { idx, label ->
            val value = map[label].orEmpty().trim()
            if (value.isBlank()) null
            else EditField(
                fieldId = null,                 // 기존 필드 id 있으면 채워도 됨
                fieldName = label.take(100),
                fieldValue = value.take(100),
                fieldOrder = (idx + 1)          // 체크 순서대로 1~5
            )
        }.filterNotNull()

        return if (list.isEmpty()) null else list
    }

    fun updateMyCardMultipart(
        context: Context,
        cardId: Int,
        backgroundImageNum: Int,
        imageH: Uri? = null,
        imageV: Uri? = null
    ) {

        // updateMyCardMultipart(...)
        val hPart = imageH?.let { context.uriToPartCompressed("imageUrlHorizontal", it) }
        val vPart = imageV?.let { context.uriToPartCompressed("imageUrlVertical", it) }

        // 1) 화면 상태 -> DTO
        val name  = getValue("이름").trim().take(50)
        val phone = getValue("연락처").filter(Char::isDigit).take(20)
        val company = getValue("회사").trim().take(100)
        if (name.isBlank() || phone.isBlank() || company.isBlank()) {
            _createState.value = CreateState.Fail(null, "이름/연락처/회사는 필수입니다.")
            return
        }
        val position = getValue("직책").trim().take(50).ifBlank { null }
        val email = getValue("이메일").trim().take(50).ifBlank { null }
        val apiFontColor = !textDark.value

        val extras: List<EditField>? = buildEditExtrasForApi()


        val payload = MyCardEditRequest(
            name = name,
            phone = phone,
            company = company,
            position = position,
            email = email,
            backgroundImageNum = backgroundImageNum,
            fontColor = apiFontColor,
            fields = extras
        )
        val requestBody = Gson()
            .toJson(payload)
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        viewModelScope.launch {
            _createState.value = CreateState.Loading
            _saving.value = true
            try {
                val hPart = imageH?.let { context.uriToPart("imageUrlHorizontal", it) }
                val vPart = imageV?.let { context.uriToPart("imageUrlVertical", it) }


                // ✅ named args로 호출 (시그니처 헷갈림 방지)
                val msg = myCardRepo.editMyCard(
                    cardId      = cardId,
                    requestJson = requestBody,
                    imageH      = hPart,
                    imageV      = vPart
                )

                if (msg.startsWith("실패") || msg.startsWith("에러")) {
                    _createState.value = CreateState.Fail(null, msg)
                } else {
                    _createState.value = CreateState.Success(cardId)
                }
            } catch (e: Exception) {
                _createState.value = CreateState.Fail(null, e.message ?: "네트워크 오류")
            } finally { _saving.value = false }
        }
    }



    // VM 내부에 추가/수정
    private data class ApiField(
        val fieldName: String,
        val fieldValue: String,
        val order: Int
    )
    private data class ApiRequest(
        val name: String,
        val phone: String,
        val company: String,
        val position: String?,
        val email: String?,
        val backgroundImageNum: Int,
        val fontColor: Boolean,      // 명세: true=밝은 글씨
        val fields: List<ApiField>?
    )


    private val FIXED = setOf("이름","연락처","회사","직책","이메일")

    // 선택된 라벨의 순서를 1..5로 매핑 (중복 제거 + 최대 5개)
    private fun buildVisibleOrderMap(): Map<String, Int> =
        _visibleExtraLabels.value
            .map { it.trim() }
            .filter { it.isNotBlank() && it !in FIXED }
            .distinct()
            .take(5)
            .withIndex()
            .associate { (idx, label) -> label to (idx + 1) }

    // 고정 5개 제외한 모든 추가필드 상태
    private fun allExtraFields(): List<FieldState> =
        _fields.value.filter { normalizeLabel(it.label) !in FIXED }


    /**
     * 내 명함 등록 (multipart)
     * @param jwt "Bearer " 없이 순수 토큰
     */
    // 등록
    fun submitMyCardMultipart(
        context: Context,
        backgroundImageNum: Int,
        imageH: Uri? = null,
        imageV: Uri? = null,
        includeCustomImage: Boolean = true
    ) {
        val json = buildRequestJson(backgroundImageNum) ?: return
        viewModelScope.launch {
            _createState.value = CreateState.Loading
            _saving.value = true
            try {
                android.util.Log.d("MyCardVM", "submitMyCardMultipart(): profileImageUri=${profileImageUri.value}, includeCustomImage=$includeCustomImage")
                val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                // submitMyCardMultipart(...) 내부
                val hPart = imageH?.let { context.uriToPartCompressed("imageUrlHorizontal", it) }
                val vPart = imageV?.let { context.uriToPartCompressed("imageUrlVertical", it) }
                val customPart = if (includeCustomImage)
                    profileImageUri.value?.let { context.uriToPartCompressed("custom_image", it) }
                else null

                android.util.Log.d("MyCardVM", "customPart is ${if (customPart==null) "NULL" else "NOT NULL"}")

                val resp = myCardRepo.registerMyCard(
                    requestJson = requestBody,
                    customImage = customPart,
                    imageH = hPart,
                    imageV = vPart
                )

                if (resp.isSuccessful) {
                    val id = resp.body()?.result?.cardId
                    if (id != null) _createState.value = CreateState.Success(id)
                    else _createState.value = CreateState.Fail(resp.code(), "응답 형식 오류")
                } else {
                    _createState.value = CreateState.Fail(resp.code(), resp.errorBody()?.string().orEmpty())
                }
            } catch (e: Exception) {
                _createState.value = CreateState.Fail(null, e.message ?: "네트워크 오류")
            } finally { _saving.value = false }
        }
    }



    private fun buildRequestJson(backgroundImageNum: Int): String? {
        val name = getValue("이름").trim().take(50)
        val phone = getValue("연락처").filter(Char::isDigit).take(20)
        val company = getValue("회사").trim().take(100)
        if (name.isBlank() || phone.isBlank() || company.isBlank()) {
            _error.value = "이름/연락처/회사는 필수입니다."; return null
        }
        val position = getValue("직책").trim().take(50).ifBlank { null }
        val email    = getValue("이메일").trim().take(50).ifBlank { null }
        val apiFontColor = !textDark.value  // VM(true=검정) -> API(true=밝음)

        val orderMap = buildVisibleOrderMap()

        val extras: List<ApiField>? =
            allExtraFields()
                .mapNotNull { f ->
                    val v = f.value.trim()
                    if (v.isBlank()) null else ApiField(
                        fieldName = f.label.take(100),
                        fieldValue = v.take(100),
                        order = orderMap[f.label] ?: 0      // 선택 안 됨 → 0
                    )
                }
                .let { if (it.isEmpty()) null else it }

        val req = ApiRequest(
            name = name,
            phone = phone,
            company = company,
            position = position,
            email = email,
            backgroundImageNum = backgroundImageNum,
            fontColor = apiFontColor,
            fields = extras
        )
        return Gson().toJson(req)
    }


    private fun buildEditExtrasForApi(): List<EditField>? {
        val orderMap = buildVisibleOrderMap()
        val list = allExtraFields()
            .mapNotNull { f ->
                val v = f.value.trim()
                if (v.isBlank()) null else EditField(
                    fieldId = null,                          // 기존 id 알면 채우고, 모르겠으면 null
                    fieldName = f.label.take(100),
                    fieldValue = v.take(100),
                    fieldOrder = orderMap[f.label] ?: 0      // 비선택 → 0
                )
            }
        return if (list.isEmpty()) null else list
    }





    // MyCardViewModel.kt (same file, 아래에 추가)
    private fun Context.uriToPartCompressed(
        key: String,
        uri: Uri,
        maxSide: Int = 1600,       // 긴 변 최대 px
        maxBytes: Int = 800_000,   // <= ~800KB 목표
    ): MultipartBody.Part? {
        return try {
            // 1) Bitmap bounds로 inSampleSize 계산
            val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, opts) }
            val (w, h) = opts.outWidth to opts.outHeight
            if (w <= 0 || h <= 0) return uriToPart(key, uri) // 실패 시 원본 fallback

            var sample = 1
            var tw = w; var th = h
            while (tw > maxSide || th > maxSide) {
                sample *= 2
                tw = w / sample; th = h / sample
            }

            // 2) 실제 디코드
            val real = android.graphics.BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            val bmp = contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, real) }
                ?: return uriToPart(key, uri)

            // 3) JPEG/WEBP로 용량 맞추기
            val cache = java.io.File.createTempFile("upload_", ".jpg", cacheDir)
            var q = 88
            do {
                java.io.FileOutputStream(cache).use { out ->
                    // WEBP_LOSSY(안드 30+) 쓰고 싶으면 JPEG 대신 WEBP_LOSSY로 바꿔도 됩니다.
                    bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, q, out)
                }
                q -= 8
            } while (cache.length() > maxBytes && q >= 56)
            bmp.recycle()

            val body = cache.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(key, cache.name, body)
        } catch (_: Exception) {
            // 실패하면 기존 방식으로라도 보냄
            uriToPart(key, uri)
        }
    }



    private fun queryDisplayName(cr: ContentResolver, uri: Uri): String? {
        cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) { return c.getString(idx) }
        }
        return null
    }


    /** 서버 상세 조회 */
    fun fetchMyCardDetail(cardId: Int) {
        viewModelScope.launch {
            currentDetailId = cardId
            _detailState.value = DetailState.Loading
            try {
                val body = myCardRepo
                    .getMyCardDetail(cardId)
                if (body != null && body.status in 200..299 && body.result != null) {
                    _detailState.value = DetailState.Success(body)
                } else {
                    _detailState.value = DetailState.Fail(body?.status, body?.message ?: "결과가 비어있습니다.")
                }
            } catch (e: Exception) {
                _detailState.value = DetailState.Fail(null, e.message ?: "네트워크 오류")
            }
        }
    }


    fun onCardDeleted(cardId: Int) {
        // 목록 제거
        _cards.update { list -> list.filterNot { it.serverId == cardId } }

        // 상세 상태가 같은 카드면 초기화
        val ds = _detailState.value
        if (ds is DetailState.Success && ds.data.result?.cardId == cardId) {
            _detailState.value = DetailState.Idle
            currentDetailId = null
        }

        // 프리페치 캐시 제거(있다면)
        fetched.remove(cardId)

        // 생성/편집 상태 등도 안전하게 초기화 (선택)
        _error.value = null
    }

    fun deleteMyCard(cardId: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val (ok, msg) = myCardRepo.deleteMyCard(cardId)
            if (ok) onCardDeleted(cardId)            // ★ 공통 정리
            onResult(ok, msg)
        }
    }




    fun resetCreateState() {
        _createState.value = CreateState.Idle
    }

    // ===== 미리보기용 로컬 카드 =====
    private fun buildCardOrNull(): MyCard? {
        val name = getValue("이름").trim()
        val phone = getValue("연락처").trim()
        val company = getValue("회사").trim()
        if (name.isEmpty() || phone.isEmpty() || company.isEmpty()) {
            _error.value = "이름/연락처/회사는 필수입니다."; return null
        }
        val custom = _fields.value
            .filter { it.label.isNotBlank() && it.value.isNotBlank() && normalizeLabel(it.label) !in REQUIRED }
            .mapIndexed { idx, f -> UiMyCardField(f.label, f.value, idx) }
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return MyCard(
            name, phone, company,
            imageUrlH = photoUri.value?.toString(), // ← 변경: 예전 imageUrl 대신
            imageUrlV = photoUri.value?.toString(),
            backgroundImageUrl = background.value,
            fontColor = textDark.value,
            isConfirm = false,
            createAt = date,
            fields = custom
        )
    }


    private val _profileRemoteUrl = MutableStateFlow<String?>(null)
    val profileRemoteUrl: StateFlow<String?> = _profileRemoteUrl

    fun backgroundIndexForApi(): Int = resolveBackgroundIndex()

    fun hydrateFromDetail(d: MyCardDetail) {
        // 1) 필드 적재
        val base = mutableListOf(
            FieldState("이름",   d.name.orEmpty()),
            FieldState("연락처", d.phone.orEmpty()),
            FieldState("회사",   d.company.orEmpty())
        )
        if (!d.position.isNullOrBlank()) base += FieldState("직책", d.position!!)

        d.fields.orEmpty()
            .sortedBy { it.fieldOrder ?: 0 }
            .forEach { f ->
                val label = f.fieldName.trim()
                val value = f.fieldValue.trim()
                if (label.isNotBlank() && value.isNotBlank()) base += FieldState(label, value)
            }
        _fields.value = base


        // 선택(표시)된 추가 필드 라벨 복원 (1..5만)
        val visible = d.fields.orEmpty()
            .filter { (it.fieldOrder ?: 0) in 1..5 }
            .sortedBy { it.fieldOrder ?: 0 }
            .mapNotNull { it.fieldName?.trim() }
            .filter { it.isNotBlank() }

        // 고정 5개는 선택목록에서 제외
        val fixed = setOf("이름","연락처","회사","직책","이메일")
        _visibleExtraLabels.value = visible.filter { it !in fixed }.distinct()


        val n = d.backgroundImageNum
        _bgNum.value = n
        when {
            n == null -> {_pattern.value = null; _background.value = "#FFFFFF" }
            n in 1..12 -> {
                _pattern.value = "pattern$n"
                _background.value = "#00000000"
            }
            n in 100..108 -> {
                val hex = when (n) {
                    101 -> "#FFC107"
                    102 -> "#00CED1"
                    103 -> "#0D9488"
                    104 -> "#1E3A8A"
                    105 -> "#FF5722"
                    106 -> "#D6C7B0"
                    107 -> "#333333"
                    108 -> "#F9F9F6"
                    else -> "#FFFFFF"
                }
                _pattern.value = null
                _background.value = hex
            }
            else -> { _pattern.value = null; _background.value = "#FFFFFF" }
        }

        _profileRemoteUrl.value = toAbsoluteUrl(d.customImageUrl)

        // 3) 글씨색 (API: fontColor=true=밝은글씨 → VM: textDark=false)
        _textDark.value = !d.fontColor


    }

    // MyCardViewModel.kt 예시
    data class MyCardDraft(
        val name: String = "",
        val phone: String = "",
        val company: String = "",
        val position: String = "",
        val email: String = "",
        val fields: List<Pair<String,String>> = emptyList(),
        val profileUrl: String? = null,
        val backgroundCode: String? = null,
        val imageUrl: String? = null,
    )



    private fun goToEmptyEdit(nav: NavController, vm: MyCardViewModel) {
        vm.clearForCreate()
        nav.navigate("my_card_edit?mode=create&cardId=-1&nonce=${System.currentTimeMillis()}") {
            popUpTo("my_card_edit") { inclusive = true } // ★ 동일하게 추가
            launchSingleTop = false
            restoreState = false
        }
    }




    fun clearForCreate() {
        _fields.value = listOf(
            FieldState("이름",""),
            FieldState("연락처",""),
            FieldState("회사","")
        )
        _profileImageUri.value = null
        _photoUri.value = null
        _pattern.value = null
        _background.value = "#FFFFFF"
        _textDark.value = true
        _bgNum.value = null

        // ★ 추가 리셋 (누락분)
        _visibleExtraLabels.value = emptyList()  // 선택 표시 필드 초기화
        _selectedLabels.value = emptyList()      // 선택 순서 초기화
        _profileRemoteUrl.value = null           // 상세의 프로필 URL 잔존 제거
        _createState.value = CreateState.Idle    // 생성 상태도 초기화
    }


    private var hasInit = false

    fun clearForCreateOnce() {
        if (hasInit) return
        clearForCreate()  // ← 이미 구현하신 함수
        hasInit = true
    }

    private val HEX_TO_INDEX = mapOf(
        "#FFC107" to 101, "#00CED1" to 102, "#0D9488" to 103,
        "#1E3A8A" to 104, "#FF5722" to 105, "#D6C7B0" to 106,
        "#333333" to 107, "#F9F9F6" to 108,  "#FFFFFF" to 108 // 👈 추가
    )

    private fun resolveBackgroundIndex(): Int {
        // 1) 명시적으로 지정된 인덱스가 있으면 그것을 사용
        _bgNum.value?.let { n ->
            if ((n in 1..12) || (n in 101..108)) return n
        }

        // 2) 패턴 상태가 있으면 1~12로 파싱
        _pattern.value?.let { code ->
            code.removePrefix("pattern").toIntOrNull()?.let { p ->
                if (p in 1..12) return p
            }
        }

        // 3) 배경 hex를 인덱스로 역매핑
        HEX_TO_INDEX[_background.value.uppercase()]?.let { return it }

        // 4) 아무 것도 없으면 기본(예: 노랑=101)
        return 101
    }

    private fun buildExtrasForApi(): List<ApiField>? {
        // 고정 5개 제외한 모든 추가필드
        val allExtras = _fields.value.filter {
            normalizeLabel(it.label) !in listOf("이름","연락처","회사","직책","이메일")
        }

        // 선택된(표시) 필드의 순서를 1..5로 매핑
        val orderMap = _visibleExtraLabels.value
            .withIndex()
            .associate { it.value to (it.index + 1) }   // 선택 안 된 건 매핑 없음

        val list = allExtras.mapNotNull { f ->
            val v = f.value.trim()
            if (v.isBlank()) null else ApiField(
                fieldName = f.label.take(100),
                fieldValue = v.take(100),
                order     = orderMap[f.label] ?: 0       // ← 비선택은 0
            )
        }

        return if (list.isEmpty()) null else list
    }

}








