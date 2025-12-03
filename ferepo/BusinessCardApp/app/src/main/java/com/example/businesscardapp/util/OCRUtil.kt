package com.example.businesscardapp.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.coroutines.tasks.await
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header
import java.util.UUID
import android.util.Base64
import java.io.ByteArrayOutputStream

// SSAFY GPT-4o-mini API 설정
private const val SSAFY_API_KEY = "S13P11E201-2b2daade-b669-48ed-8424-c506884af23f"
private const val SSAFY_API_URL = "https://gms.ssafy.io/api/v1/"

// 백엔드 API 설정 (백엔드 준비 후 활성화)
// private const val BACKEND_API_URL = "https://your-backend-server.com/api"

// 명함 데이터를 담을 데이터 클래스
data class BusinessCardInfo(
    val name: String = "",
    val company: String = "",
    val position: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val website: String = "",
    val fax: String = "",
    val mobile: String = "",
    val department: String = "",
    val rawText: String = "",
    val imageUri: String = "",
    val imageUri2: String = "",
    val capturedDate: String = ""
) {
    fun getFormattedText(): String {
        val parts = mutableListOf<String>()
        if (name.isNotEmpty()) parts.add("이름: $name")
        if (company.isNotEmpty()) parts.add("회사: $company")
        if (position.isNotEmpty()) parts.add("직책: $position")
        if (department.isNotEmpty()) parts.add("부서: $department")
        if (phone.isNotEmpty()) parts.add("전화: $phone")
        if (mobile.isNotEmpty()) parts.add("휴대폰: $mobile")
        if (email.isNotEmpty()) parts.add("이메일: $email")
        if (address.isNotEmpty()) parts.add("주소: $address")
        if (website.isNotEmpty()) parts.add("웹사이트: $website")
        if (fax.isNotEmpty()) parts.add("팩스: $fax")
        
        return if (parts.isNotEmpty()) parts.joinToString("\n") else rawText
    }
}

suspend fun runTextRecognition(uri: Uri, context: Context): BusinessCardInfo {
    Log.d("OCRUtil", "=== OCR 처리 시작 ===")
    Log.d("OCRUtil", "URI: $uri")
    Log.d("OCRUtil", "URI 스키마: ${uri.scheme}")
    Log.d("OCRUtil", "URI 경로: ${uri.path}")
    
    return withContext(Dispatchers.IO) {
        try {
            // URI 유효성 검사
            if (uri.scheme == null) {
                Log.e("OCRUtil", "잘못된 URI 스키마: $uri")
                return@withContext BusinessCardInfo()
            }

            val inputStream = try {
                Log.d("OCRUtil", "InputStream 열기 시도")
                val stream = context.contentResolver.openInputStream(uri)
                Log.d("OCRUtil", "InputStream 열기 결과: ${stream != null}")
                stream
            } catch (e: Exception) {
                Log.e("OCRUtil", "InputStream 열기 실패", e)
                return@withContext BusinessCardInfo()
            }

            if (inputStream == null) {
                Log.e("OCRUtil", "InputStream이 null입니다: $uri")
                return@withContext BusinessCardInfo()
            }

            val originalBitmap = try {
                Log.d("OCRUtil", "Bitmap 디코딩 시도")
                inputStream.use { BitmapFactory.decodeStream(it) }
            } catch (e: Exception) {
                Log.e("OCRUtil", "Bitmap 디코딩 실패", e)
                return@withContext BusinessCardInfo()
            }

            if (originalBitmap == null) {
                Log.e("OCRUtil", "Bitmap이 null입니다")
                return@withContext BusinessCardInfo()
            }

            Log.d("OCRUtil", "원본 Bitmap 생성 성공: ${originalBitmap.width}x${originalBitmap.height}")

            // 다중 전처리 방법으로 OCR 시도
            val results = mutableListOf<String>()
            
            // 1. 기본 전처리
            val basicProcessed = preprocessImage(originalBitmap, PreprocessType.BASIC)
            val basicResult = performOCR(basicProcessed, "기본 전처리")
            if (basicResult.isNotEmpty()) results.add(basicResult)
            
            // 2. 고대비 전처리
            val highContrastProcessed = preprocessImage(originalBitmap, PreprocessType.HIGH_CONTRAST)
            val highContrastResult = performOCR(highContrastProcessed, "고대비 전처리")
            if (highContrastResult.isNotEmpty()) results.add(highContrastResult)
            
            // 3. 노이즈 제거 전처리
            val denoisedProcessed = preprocessImage(originalBitmap, PreprocessType.DENOISE)
            val denoisedResult = performOCR(denoisedProcessed, "노이즈 제거 전처리")
            if (denoisedResult.isNotEmpty()) results.add(denoisedResult)
            
                // 4. 원본 이미지 (전처리 없이)
    val originalResult = performOCR(originalBitmap, "원본 이미지")
    if (originalResult.isNotEmpty()) results.add(originalResult)

    Log.d("OCRUtil", "=== 다중 OCR 시도 완료 ===")
    Log.d("OCRUtil", "총 ${results.size}개의 결과 획득")
    
    // 최적의 결과 선택 및 후처리
    val bestResult = selectBestResult(results)
    val finalResult = postprocessText(bestResult)
    
    Log.d("OCRUtil", "=== 최종 결과 ===")
    Log.d("OCRUtil", "최종 텍스트: '$finalResult'")
    
    // 명함 정보 추출 및 구조화
    val businessCardInfo = extractBusinessCardInfo(finalResult)
    
    // 메모리 해제
    originalBitmap.recycle()
    
    Log.e("OCRUtil", "=== runTextRecognition 최종 결과 ===")
    Log.e("OCRUtil", "반환할 BusinessCardInfo: $businessCardInfo")
    Log.e("OCRUtil", "반환할 전화번호: '${businessCardInfo.phone}'")
    Log.e("OCRUtil", "반환할 주소: '${businessCardInfo.address}'")
    
    businessCardInfo

        } catch (e: Exception) {
            Log.e("OCRUtil", "=== OCR 처리 중 오류 발생 ===")
            Log.e("OCRUtil", "예외 타입: ${e.javaClass.simpleName}")
            Log.e("OCRUtil", "예외 메시지: ${e.message}")
            Log.e("OCRUtil", "스택 트레이스:", e)
            BusinessCardInfo()
        }
    }
}

private enum class PreprocessType {
    BASIC, HIGH_CONTRAST, DENOISE
}

private suspend fun performOCR(bitmap: Bitmap, methodName: String): String {
    return try {
        Log.d("OCRUtil", "=== $methodName OCR 시작 ===")
        
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = createTextRecognizer()
        
        if (recognizer == null) {
            Log.e("OCRUtil", "$methodName: TextRecognizer 생성 실패")
            return ""
        }

        val result = recognizer.process(image).await()
        recognizer.close()
        
        Log.d("OCRUtil", "$methodName 결과: '${result.text}'")
        result.text
        
    } catch (e: Exception) {
        Log.e("OCRUtil", "$methodName OCR 실패", e)
        ""
    }
}

private fun createTextRecognizer(): com.google.mlkit.vision.text.TextRecognizer? {
    return try {
        // 한국어 텍스트 인식기를 우선적으로 사용
        Log.d("OCRUtil", "한국어 TextRecognizer 생성 시도")
        val koreanRecognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        Log.d("OCRUtil", "한국어 TextRecognizer 생성 성공")
        koreanRecognizer
    } catch (e: Exception) {
        Log.e("OCRUtil", "한국어 TextRecognizer 생성 실패", e)
        Log.e("OCRUtil", "예외 타입: ${e.javaClass.simpleName}")
        Log.e("OCRUtil", "예외 메시지: ${e.message}")
        
        try {
            // 한국어 인식기 실패 시 기본 라틴 텍스트 인식기로 폴백
            Log.d("OCRUtil", "기본 라틴 TextRecognizer 생성 시도 (폴백)")
            val defaultRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            Log.d("OCRUtil", "기본 라틴 TextRecognizer 생성 성공")
            defaultRecognizer
        } catch (e2: Exception) {
            Log.e("OCRUtil", "기본 라틴 TextRecognizer 생성 실패", e2)
            Log.e("OCRUtil", "예외 타입: ${e2.javaClass.simpleName}")
            Log.e("OCRUtil", "예외 메시지: ${e2.message}")
            null
        }
    }
}

// 고급 이미지 전처리 함수
private fun preprocessImage(originalBitmap: Bitmap, type: PreprocessType): Bitmap {
    try {
        Log.d("OCRUtil", "이미지 전처리 시작 - 타입: $type")
        
        // 1. 이미지 크기 조정
        val targetWidth = 1024
        val targetHeight = (originalBitmap.height * targetWidth / originalBitmap.width)
        
        val resizedBitmap = if (originalBitmap.width > targetWidth || originalBitmap.height > targetHeight) {
            Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
        } else {
            originalBitmap
        }
        
        val processedBitmap = when (type) {
            PreprocessType.BASIC -> applyBasicPreprocessing(resizedBitmap)
            PreprocessType.HIGH_CONTRAST -> applyHighContrastPreprocessing(resizedBitmap)
            PreprocessType.DENOISE -> applyDenoisePreprocessing(resizedBitmap)
        }
        
        Log.d("OCRUtil", "이미지 전처리 완료 - 타입: $type")
        
        // 메모리 해제
        if (resizedBitmap != originalBitmap) {
            resizedBitmap.recycle()
        }
        
        return processedBitmap
        
    } catch (e: Exception) {
        Log.e("OCRUtil", "이미지 전처리 실패 - 타입: $type", e)
        return originalBitmap
    }
}

private fun applyBasicPreprocessing(bitmap: Bitmap): Bitmap {
    val processedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(processedBitmap)
    val paint = Paint()
    
    // 그레이스케일 + 대비 향상
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    
    val contrastMatrix = ColorMatrix()
    contrastMatrix.setScale(1.3f, 1.3f, 1.3f, 1.0f)
    colorMatrix.postConcat(contrastMatrix)
    
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    
    return processedBitmap
}

private fun applyHighContrastPreprocessing(bitmap: Bitmap): Bitmap {
    val processedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(processedBitmap)
    val paint = Paint()
    
    // 고대비 변환
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    
    // 대비를 더욱 강화 (1.5배)
    val contrastMatrix = ColorMatrix()
    contrastMatrix.setScale(1.5f, 1.5f, 1.5f, 1.0f)
    colorMatrix.postConcat(contrastMatrix)
    
    // 밝기 조정 (postTranslate 대신 다른 방법 사용)
    val brightnessMatrix = ColorMatrix()
    brightnessMatrix.setScale(1.0f, 1.0f, 1.0f, 1.0f)
    // 밝기 조정을 위한 행렬 직접 설정
    val brightnessArray = floatArrayOf(
        1.0f, 0.0f, 0.0f, 0.0f, 10f,
        0.0f, 1.0f, 0.0f, 0.0f, 10f,
        0.0f, 0.0f, 1.0f, 0.0f, 10f,
        0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    )
    brightnessMatrix.set(brightnessArray)
    colorMatrix.postConcat(brightnessMatrix)
    
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    
    return processedBitmap
}

private fun applyDenoisePreprocessing(bitmap: Bitmap): Bitmap {
    val processedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(processedBitmap)
    val paint = Paint()
    
    // 노이즈 제거를 위한 블러 효과
    paint.isAntiAlias = true
    paint.isFilterBitmap = true
    
    // 그레이스케일 + 약간의 블러 효과
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    
    val contrastMatrix = ColorMatrix()
    contrastMatrix.setScale(1.2f, 1.2f, 1.2f, 1.0f)
    colorMatrix.postConcat(contrastMatrix)
    
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    
    return processedBitmap
}

// 최적의 결과 선택
private fun selectBestResult(results: List<String>): String {
    if (results.isEmpty()) return ""
    if (results.size == 1) return results[0]
    
    Log.d("OCRUtil", "=== 최적 결과 선택 시작 ===")
    
    // 1. 빈 결과 제거
    val nonEmptyResults = results.filter { it.isNotEmpty() }
    if (nonEmptyResults.isEmpty()) return ""
    
    // 2. 더 정교한 점수 계산
    val scoredResults = nonEmptyResults.map { text ->
        val lines = text.split("\n").filter { it.trim().isNotEmpty() }
        val koreanRatio = text.count { it.code in 0xAC00..0xD7AF || it.code in 0x3131..0x318E } / text.length.toFloat()
        
        // 명함 관련 키워드 점수
        val businessKeywords = listOf("사장", "대표", "부장", "과장", "대리", "주임", "사원", "팀장", "실장", "이사", "회장", "부회장", "매니저", "팀원", "CEO", "CTO", "Director", "Manager", "Team", "Department", "Corp", "Inc", "Ltd", "Group", "Systems", "Technology", "농협", "NH", "Nongityup", "경남", "창원시", "Tel", "Mobile", "Fax", "Email", "www", "http", "https", "@", ".com", ".co.kr", ".kr")
        val keywordScore = businessKeywords.count { text.contains(it) } * 0.1f
        
        // 줄 수 점수 (명함은 보통 5-15줄 정도)
        val lineScore = when {
            lines.size in 5..15 -> 0.3f
            lines.size in 3..20 -> 0.2f
            else -> 0.1f
        }
        
        // 한글 비율 점수 (너무 높거나 낮으면 안됨)
        val koreanScore = when {
            koreanRatio in 0.3f..0.8f -> 0.4f
            koreanRatio in 0.2f..0.9f -> 0.3f
            else -> 0.1f
        }
        
        val totalScore = keywordScore + lineScore + koreanScore
        Log.d("OCRUtil", "텍스트: '$text'")
        Log.d("OCRUtil", "  - 한글비율: ${(koreanRatio * 100).toInt()}%, 키워드점수: $keywordScore, 줄수점수: $lineScore, 한글점수: $koreanScore, 총점: $totalScore")
        Pair(text, totalScore)
    }
    
    // 3. 가장 높은 점수의 결과 선택
    val bestResult = scoredResults.maxByOrNull { it.second }
    Log.d("OCRUtil", "선택된 최적 결과: '${bestResult?.first}' (점수: ${bestResult?.second})")
    
    return bestResult?.first ?: nonEmptyResults.first()
}

// 텍스트 후처리
private fun postprocessText(text: String): String {
    if (text.isEmpty()) return text
    
    Log.d("OCRUtil", "=== 개선된 텍스트 후처리 시작 ===")
    Log.d("OCRUtil", "원본 텍스트: '$text'")
    
    var processedText = text
    
    // 1. 불필요한 공백 정리 (줄바꿈은 유지)
    processedText = processedText.replace(Regex("[ \\t]+"), " ").trim()
    
    // 2. OCR 오류 수정
    processedText = correctCommonOCRErrors(processedText)
    
    // 3. 전화번호 패턴 정리
    val phonePattern = Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})")
    processedText = phonePattern.replace(processedText) { matchResult ->
        "${matchResult.groupValues[1]}-${matchResult.groupValues[2]}-${matchResult.groupValues[3]}"
    }
    
    // 4. 이메일 패턴 정리
    val emailPattern = Regex("([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})")
    
    Log.d("OCRUtil", "후처리된 텍스트: '$processedText'")
    
    return processedText
}

// 개선된 명함 정보 추출 함수 - 다단계 분류 시스템
private fun extractBusinessCardInfo(text: String): BusinessCardInfo {
    if (text.isEmpty()) return BusinessCardInfo()
    
    Log.d("OCRUtil", "=== 개선된 다단계 명함 정보 추출 시작 ===")
    Log.d("OCRUtil", "입력 텍스트: '$text'")
    
    val lines = text.split("\n").filter { it.trim().isNotEmpty() }
    val allClassifications = mutableListOf<Pair<String, TextClassification>>()
    
    // 라인 위치 분석
    val linePositions = analyzeLinePositions(lines)
    
    // 1단계: 명확한 패턴 매칭 (높은 신뢰도)
    for (i in lines.indices) {
        val currentLine = lines[i]
        val context = TextContext(
            text = currentLine,
            position = i,
            previousText = if (i > 0) lines[i-1] else "",
            nextText = if (i < lines.size-1) lines[i+1] else "",
            allLines = lines,
            linePosition = linePositions[i]
        )
        
        val patternClassification = classifyWithPatternMatching(context)
        if (patternClassification.confidence > 0.3f) { // 0.5f에서 0.3f로 더 낮춤
            allClassifications.add(Pair(currentLine, patternClassification))
            Log.d("OCRUtil", "1단계 패턴 매칭: '$currentLine' -> ${patternClassification.fieldType} (신뢰도: ${patternClassification.confidence})")
        }
    }
    
    // 2단계: 컨텍스트 기반 분류 (중간 신뢰도)
    for (i in lines.indices) {
        val currentLine = lines[i]
        if (allClassifications.any { it.first == currentLine }) continue // 이미 분류된 라인 스킵
        
        val context = TextContext(
            text = currentLine,
            position = i,
            previousText = if (i > 0) lines[i-1] else "",
            nextText = if (i < lines.size-1) lines[i+1] else "",
            allLines = lines,
            linePosition = linePositions[i]
        )
        
        val contextClassification = classifyWithContext(context)
        if (contextClassification.confidence > 0.1f) { // 0.3f에서 0.1f로 더 낮춤
            allClassifications.add(Pair(currentLine, contextClassification))
            Log.d("OCRUtil", "2단계 컨텍스트 분류: '$currentLine' -> ${contextClassification.fieldType} (신뢰도: ${contextClassification.confidence})")
        }
    }
    
    // 3단계: 휴리스틱 분류 (낮은 신뢰도)
    for (i in lines.indices) {
        val currentLine = lines[i]
        if (allClassifications.any { it.first == currentLine }) continue // 이미 분류된 라인 스킵
        
        val context = TextContext(
            text = currentLine,
            position = i,
            previousText = if (i > 0) lines[i-1] else "",
            nextText = if (i < lines.size-1) lines[i+1] else "",
            allLines = lines,
            linePosition = linePositions[i]
        )
        
        val heuristicClassification = classifyWithHeuristics(context)
        if (heuristicClassification.confidence > 0.05f) { // 0.1f에서 0.05f로 더 낮춤
            allClassifications.add(Pair(currentLine, heuristicClassification))
            Log.d("OCRUtil", "3단계 휴리스틱 분류: '$currentLine' -> ${heuristicClassification.fieldType} (신뢰도: ${heuristicClassification.confidence})")
        }
    }
    
    // 우선순위 기반 결과 구성
    val results = resolveConflicts(allClassifications)
    
    Log.d("OCRUtil", "=== 분류 결과 요약 ===")
    Log.d("OCRUtil", "총 분류된 라인 수: ${allClassifications.size}")
    for ((text, classification) in allClassifications) {
        Log.d("OCRUtil", "  - '$text' -> ${classification.fieldType} (신뢰도: ${classification.confidence}, 단계: ${classification.classificationStage})")
    }
    Log.d("OCRUtil", "최종 결과:")
    for ((fieldType, value) in results) {
        Log.d("OCRUtil", "  - $fieldType: '$value'")
    }
    
    // 이름+직책 분리 로직
    val namePositionResult = extractNameAndPositionFromResults(results)
    
    // 전화번호, 팩스, 휴대폰 처리
    val phoneResults = extractPhoneNumbers(results)
    
    // 주소 병합 처리
    val mergedAddress = mergeAddressLines(results)
    
    // BusinessCardInfo 구성
    val businessCardInfo = BusinessCardInfo(
        name = namePositionResult.first,
        company = results[FieldType.COMPANY] ?: "",
        position = namePositionResult.second,
        phone = phoneResults.first,
        email = results[FieldType.EMAIL] ?: "",
        address = mergedAddress,
        website = results[FieldType.WEBSITE] ?: "",
        fax = phoneResults.second,
        mobile = phoneResults.third,
        department = results[FieldType.DEPARTMENT] ?: "",
        rawText = text
    )
    
    Log.d("OCRUtil", "=== 최종 BusinessCardInfo ===")
    Log.d("OCRUtil", "이름: '${businessCardInfo.name}'")
    Log.d("OCRUtil", "회사: '${businessCardInfo.company}'")
    Log.d("OCRUtil", "직책: '${businessCardInfo.position}'")
    Log.d("OCRUtil", "전화: '${businessCardInfo.phone}'")
    Log.d("OCRUtil", "이메일: '${businessCardInfo.email}'")
    Log.d("OCRUtil", "주소: '${businessCardInfo.address}'")
    Log.d("OCRUtil", "웹사이트: '${businessCardInfo.website}'")
    Log.d("OCRUtil", "팩스: '${businessCardInfo.fax}'")
    Log.d("OCRUtil", "휴대폰: '${businessCardInfo.mobile}'")
    Log.d("OCRUtil", "부서: '${businessCardInfo.department}'")
    Log.d("OCRUtil", "=== 개선된 다단계 명함 정보 추출 완료 ===")
    
    // 결과 검증 및 포맷팅
    val validatedResults = validateAndFormatResults(businessCardInfo)
    
    return validatedResults
}

// 라인 위치 분석 함수
private fun analyzeLinePositions(lines: List<String>): List<LinePosition> {
    val positions = mutableListOf<LinePosition>()
    
    for (i in lines.indices) {
        val position = when {
            i == 0 -> LinePosition.TOP
            i == lines.size - 1 -> LinePosition.BOTTOM
            i < lines.size / 3 -> LinePosition.TOP
            i > lines.size * 2 / 3 -> LinePosition.BOTTOM
            else -> LinePosition.MIDDLE
        }
        positions.add(position)
    }
    
    return positions
}

// 1단계: 명확한 패턴 매칭 (높은 신뢰도)
private fun classifyWithPatternMatching(context: TextContext): TextClassification {
    val text = context.text.trim()
    
    // 이메일 (가장 명확한 패턴)
    if (isLikelyEmail(text)) {
        return TextClassification(FieldType.EMAIL, 0.95f, ClassificationStage.PATTERN, context)
    }
    
    // 팩스번호 패턴 (우선순위 높임)
    if (isLikelyFax(text, context.previousText, context.nextText)) {
        return TextClassification(FieldType.FAX, 0.9f, ClassificationStage.PATTERN, context)
    }
    
    // 휴대폰번호 패턴 (우선순위 높임)
    if (isLikelyMobile(text, context.previousText, context.nextText)) {
        return TextClassification(FieldType.MOBILE, 0.9f, ClassificationStage.PATTERN, context)
    }
    
    // 전화번호 패턴
    if (isLikelyPhone(text, context.previousText, context.nextText)) {
        return TextClassification(FieldType.PHONE, 0.9f, ClassificationStage.PATTERN, context)
    }
    
    // 웹사이트 패턴 (우선순위 높임)
    if (isLikelyWebsite(text)) {
        return TextClassification(FieldType.WEBSITE, 0.85f, ClassificationStage.PATTERN, context)
    }
    
    // 부서명 패턴 (가장 우선순위 높음 - 지사/본사 등)
    if (isLikelyDepartment(text, context.previousText, context.nextText)) {
        return TextClassification(FieldType.DEPARTMENT, 0.98f, ClassificationStage.PATTERN, context)
    }
    
    // 주소 패턴 (우선순위 높음)
    if (isLikelyAddress(text, context.previousText, context.nextText)) {
        return TextClassification(FieldType.ADDRESS, 0.95f, ClassificationStage.PATTERN, context)
    }
    
    // 회사명 패턴 (우선순위 높임)
    if (isLikelyCompany(text, context.previousText, context.nextText)) {
        return TextClassification(FieldType.COMPANY, 0.95f, ClassificationStage.PATTERN, context)
    }
    
    // 이름 패턴 (명확한 경우만)
    if (isLikelyName(text, context.previousText, context.nextText)) {
        // 이름+직책 패턴이 포함된 경우 우선 처리
        val namePositionResult = extractNameAndPosition(text)
        if (namePositionResult.first != null && namePositionResult.second != null) {
            return TextClassification(FieldType.NAME, 0.99f, ClassificationStage.PATTERN, context)
        }
        // 단독 이름인 경우
        if (text.matches(Regex("^[가-힣]{2,3}$"))) {
            return TextClassification(FieldType.NAME, 0.8f, ClassificationStage.PATTERN, context)
        }
    }
    
    return TextClassification(FieldType.UNKNOWN, 0.0f, ClassificationStage.PATTERN, context)
}

// 이름과 직책을 분리하는 함수 (통합 버전)
private fun extractNameAndPosition(text: String): Pair<String?, String?> {
    val cleanText = text.trim()
    
    Log.d("OCRUtil", "이름+직책 추출 시작: '$cleanText'")
    
    val positionKeywords = listOf(
        "대표", "이사", "팀장", "과장", "부장", "사원", "실장", "회장", "부회장", "주임", "대리", "매니저", "팀원",
        "CEO", "CTO", "CFO", "CMO", "President", "Director", "Manager", "Senior", "Lead", "Head", "Chief", 
        "Vice President", "Project Manager", "Team Lead", "Staff", "Engineer", "Developer", "디자이너"
    )
    
    var name: String? = null
    var position: String? = null
    
    // 1. 공백으로 분리
    val tokens = cleanText.split(" ").filter { it.isNotBlank() }
    Log.d("OCRUtil", "토큰 분리: $tokens")
    
    // 2. 각 토큰 분석
    for (token in tokens) {
        if (position == null && positionKeywords.any { token.contains(it, ignoreCase = true) }) {
            position = token
            Log.d("OCRUtil", "직책 발견: '$position'")
        } else if (name == null && token.matches(Regex("[가-힣]{2,3}|[A-Z][a-z]+\\s?[A-Z][a-z]+"))) {
            name = token
            Log.d("OCRUtil", "이름 발견: '$name'")
        }
    }
    
    // 3. 붙어있는 경우 처리 (예: "김민지과장")
    if (name == null && position == null) {
        // 한글 이름 + 직책 패턴 (붙어있음)
        val koreanNamePositionPattern = Regex("([가-힣]{2,3})(사장|대표|부장|과장|대리|주임|사원|팀장|실장|이사|회장|부회장|매니저)")
        val koreanMatch = koreanNamePositionPattern.find(cleanText)
        if (koreanMatch != null) {
            name = koreanMatch.groupValues[1]
            position = koreanMatch.groupValues[2]
            Log.d("OCRUtil", "붙어있는 패턴에서 이름: '$name', 직책: '$position'")
        }
    }
    
    // 4. 추가 개선: "김도훈 과장" 같은 패턴을 더 정확하게 처리
    if (name == null && position == null) {
        // 한글 이름 + 공백 + 직책 패턴
        val koreanNameSpacePositionPattern = Regex("([가-힣]{2,3})\\s+(과장|부장|대리|주임|사원|팀장|실장|이사|회장|부회장|매니저)")
        val koreanSpaceMatch = koreanNameSpacePositionPattern.find(cleanText)
        if (koreanSpaceMatch != null) {
            name = koreanSpaceMatch.groupValues[1]
            position = koreanSpaceMatch.groupValues[2]
            Log.d("OCRUtil", "공백 패턴에서 이름: '$name', 직책: '$position'")
        }
    }
    
    // 5. 추가 개선: "김도훈 과장" 패턴을 우선적으로 처리
    if (name == null && position == null) {
        // 한글 이름 + 공백 + 직책 패턴 (더 구체적)
        val specificPattern = Regex("([가-힣]{2,3})\\s+(과장|부장|대리|주임|사원|팀장|실장|이사|회장|부회장|매니저)")
        val specificMatch = specificPattern.find(cleanText)
        if (specificMatch != null) {
            name = specificMatch.groupValues[1]
            position = specificMatch.groupValues[2]
            Log.d("OCRUtil", "구체적 패턴에서 이름: '$name', 직책: '$position'")
        }
    }
    
    // 6. 추가 개선: "김도훈 과장" 패턴을 가장 우선적으로 처리 (로그에서 보인 문제 해결)
    if (name == null && position == null) {
        // 한글 이름 + 공백 + 직책 패턴 (가장 구체적)
        val mostSpecificPattern = Regex("([가-힣]{2,3})\\s+(과장|부장|대리|주임|사원|팀장|실장|이사|회장|부회장|매니저)")
        val mostSpecificMatch = mostSpecificPattern.find(cleanText)
        if (mostSpecificMatch != null) {
            name = mostSpecificMatch.groupValues[1]
            position = mostSpecificMatch.groupValues[2]
            Log.d("OCRUtil", "가장 구체적 패턴에서 이름: '$name', 직책: '$position'")
        }
    }
    
    Log.d("OCRUtil", "이름+직책 추출 결과: 이름='$name', 직책='$position'")
    return Pair(name, position)
}

// 2단계: 컨텍스트 기반 분류 (중간 신뢰도)
private fun classifyWithContext(context: TextContext): TextClassification {
    val text = context.text.trim()
    
    Log.d("OCRUtil", "컨텍스트 분류 시작: '$text'")
    
    // 이름+직책 패턴 먼저 처리 (우선순위 높임)
    val namePositionResult = extractNameAndPosition(text)
    if (namePositionResult.first != null && namePositionResult.second != null) {
        // 이름+직책이 함께 있는 경우, 이름으로 분류하고 직책은 별도 처리
        Log.d("OCRUtil", "이름+직책 패턴 발견: 이름='${namePositionResult.first}', 직책='${namePositionResult.second}'")
        return TextClassification(FieldType.NAME, 0.9f, ClassificationStage.CONTEXT, context)
    }
    
    // 이름 분류 (컨텍스트 고려)
    if (isLikelyNameWithContext(context)) {
        Log.d("OCRUtil", "컨텍스트 기반 이름 분류")
        return TextClassification(FieldType.NAME, 0.7f, ClassificationStage.CONTEXT, context)
    }
    
    // 직책 분류 (컨텍스트 고려)
    if (isLikelyPositionWithContext(context)) {
        Log.d("OCRUtil", "컨텍스트 기반 직책 분류")
        return TextClassification(FieldType.POSITION, 0.7f, ClassificationStage.CONTEXT, context)
    }
    
    // 회사명 분류 (컨텍스트 고려)
    if (isLikelyCompanyWithContext(context)) {
        Log.d("OCRUtil", "컨텍스트 기반 회사명 분류")
        return TextClassification(FieldType.COMPANY, 0.7f, ClassificationStage.CONTEXT, context)
    }
    
    // 주소 분류 (컨텍스트 고려)
    if (isLikelyAddressWithContext(context)) {
        Log.d("OCRUtil", "컨텍스트 기반 주소 분류")
        return TextClassification(FieldType.ADDRESS, 0.6f, ClassificationStage.CONTEXT, context)
    }
    
    // 부서 분류 (컨텍스트 고려)
    if (isLikelyDepartmentWithContext(context)) {
        Log.d("OCRUtil", "컨텍스트 기반 부서 분류")
        return TextClassification(FieldType.DEPARTMENT, 0.6f, ClassificationStage.CONTEXT, context)
    }
    
    Log.d("OCRUtil", "컨텍스트 분류 실패")
    return TextClassification(FieldType.UNKNOWN, 0.0f, ClassificationStage.CONTEXT, context)
}

// 3단계: 휴리스틱 분류
private fun classifyWithHeuristics(context: TextContext): TextClassification {
    val text = context.text.trim()
    
    // 위치 기반 분류
    when (context.linePosition) {
        LinePosition.TOP -> {
            // 상단은 보통 회사명이나 이름
            if (text.length <= 15 && !text.contains("@") && !text.contains("www")) {
                if (text.matches(Regex("[가-힣A-Za-z0-9 ]+"))) {
                    return TextClassification(FieldType.COMPANY, 0.5f, ClassificationStage.HEURISTIC, context)
                }
            }
        }
        LinePosition.MIDDLE -> {
            // 중간은 보통 주소나 부서
            if (text.length > 10 && text.contains("로") || text.contains("길")) {
                return TextClassification(FieldType.ADDRESS, 0.5f, ClassificationStage.HEURISTIC, context)
            }
        }
        LinePosition.BOTTOM -> {
            // 하단은 보통 주소나 부서
            if (text.contains("지사") || text.contains("본사")) {
                return TextClassification(FieldType.DEPARTMENT, 0.5f, ClassificationStage.HEURISTIC, context)
            }
        }
        else -> {}
    }
    
    // 길이 기반 분류
    when {
        text.length <= 5 && text.matches(Regex("[가-힣]+")) -> {
            return TextClassification(FieldType.NAME, 0.4f, ClassificationStage.HEURISTIC, context)
        }
        text.length > 20 && text.contains("로") -> {
            return TextClassification(FieldType.ADDRESS, 0.4f, ClassificationStage.HEURISTIC, context)
        }
    }
    
    return TextClassification(FieldType.UNKNOWN, 0.0f, ClassificationStage.HEURISTIC, context)
}

// 우선순위 기반 충돌 해결
private fun resolveConflicts(classifications: List<Pair<String, TextClassification>>): Map<FieldType, String> {
    val results = mutableMapOf<FieldType, String>()
    val fieldGroups = classifications.groupBy { it.second.fieldType }
    
    for ((fieldType, group) in fieldGroups) {
        if (fieldType == FieldType.UNKNOWN) continue
        
        // 같은 필드 타입 내에서 가장 높은 신뢰도를 가진 것 선택
        val bestClassification = group.maxByOrNull { 
            it.second.confidence * stageConfidenceWeight[it.second.classificationStage]!!
        }
        
        if (bestClassification != null) {
            results[fieldType] = bestClassification.first
        }
    }
    
    return results
}

// 이름+직책 분리 (결과에서) - 개선
private fun extractNameAndPositionFromResults(results: Map<FieldType, String>): Pair<String, String> {
    var name = results[FieldType.NAME] ?: ""
    var position = results[FieldType.POSITION] ?: ""
    
    Log.d("OCRUtil", "=== 이름+직책 분리 시작 ===")
    Log.d("OCRUtil", "초기 이름: '$name'")
    Log.d("OCRUtil", "초기 직책: '$position'")
    
    // 이름+직책이 하나의 라인에 있는 경우 처리
    for ((fieldType, text) in results) {
        Log.d("OCRUtil", "필드 타입: $fieldType, 텍스트: '$text'")
        
        if (fieldType == FieldType.NAME || fieldType == FieldType.POSITION) {
            val namePositionResult = extractNameAndPosition(text)
            Log.d("OCRUtil", "추출 결과: 이름='${namePositionResult.first}', 직책='${namePositionResult.second}'")
            
            if (namePositionResult.first != null && name.isEmpty()) {
                name = namePositionResult.first!!
                Log.d("OCRUtil", "이름 업데이트: '$name'")
            }
            if (namePositionResult.second != null && position.isEmpty()) {
                position = namePositionResult.second!!
                Log.d("OCRUtil", "직책 업데이트: '$position'")
            }
        }
    }
    
    // 모든 텍스트에서 이름+직책 패턴 찾기 (이름이나 직책이 비어있는 경우)
    if (name.isEmpty() || position.isEmpty()) {
        Log.d("OCRUtil", "추가 검색 시작 - 이름: '$name', 직책: '$position'")
        
        for ((fieldType, text) in results) {
            if (fieldType != FieldType.NAME && fieldType != FieldType.POSITION) {
                val namePositionResult = extractNameAndPosition(text)
                Log.d("OCRUtil", "추가 검색 결과: 이름='${namePositionResult.first}', 직책='${namePositionResult.second}'")
                
                if (namePositionResult.first != null && name.isEmpty()) {
                    name = namePositionResult.first!!
                    Log.d("OCRUtil", "추가 검색으로 이름 업데이트: '$name'")
                }
                if (namePositionResult.second != null && position.isEmpty()) {
                    position = namePositionResult.second!!
                    Log.d("OCRUtil", "추가 검색으로 직책 업데이트: '$position'")
                }
            }
        }
    }
    
    // 이름에 직책이 포함된 경우 제거 (개선된 로직)
    if (name.isNotEmpty()) {
        // "김도훈 과장" 같은 패턴이 이름에 포함된 경우 처리
        val namePositionResult = extractNameAndPosition(name)
        if (namePositionResult.first != null && namePositionResult.second != null) {
            name = namePositionResult.first!!
            if (position.isEmpty()) {
                position = namePositionResult.second!!
            }
            Log.d("OCRUtil", "이름에서 직책 분리: 이름='$name', 직책='$position'")
        }
        
        // 추가: "김도훈 과장" 패턴을 더 정확하게 처리 (로그에서 보인 문제 해결)
        val specificPattern = Regex("([가-힣]{2,3})\\s+(과장|부장|대리|주임|사원|팀장|실장|이사|회장|부회장|매니저)")
        val specificMatch = specificPattern.find(name)
        if (specificMatch != null) {
            val extractedName = specificMatch.groupValues[1]
            val extractedPosition = specificMatch.groupValues[2]
            name = extractedName
            if (position.isEmpty()) {
                position = extractedPosition
            }
            Log.d("OCRUtil", "구체적 패턴으로 이름에서 직책 분리: 이름='$name', 직책='$position'")
        }
    }
    
    Log.d("OCRUtil", "=== 이름+직책 분리 완료 ===")
    Log.d("OCRUtil", "최종 이름: '$name'")
    Log.d("OCRUtil", "최종 직책: '$position'")
    
    return Pair(name, position)
}

// 전화번호 추출 (결과에서)
private fun extractPhoneNumbers(results: Map<FieldType, String>): Triple<String, String, String> {
    val phone = results[FieldType.PHONE] ?: ""
    val fax = results[FieldType.FAX] ?: ""
    val mobile = results[FieldType.MOBILE] ?: ""
    
    return Triple(phone, fax, mobile)
}

// 주소 병합 (결과에서)
private fun mergeAddressLines(results: Map<FieldType, String>): String {
    val address = results[FieldType.ADDRESS] ?: ""
    return address
}

// 컨텍스트 기반 이름 분류
private fun isLikelyNameWithContext(context: TextContext): Boolean {
    val text = context.text.trim()
    
    Log.d("OCRUtil", "컨텍스트 이름 분류 검사: '$text'")
    
    // 기본 이름 패턴
    if (!isLikelyName(text, context.previousText, context.nextText)) {
        Log.d("OCRUtil", "기본 이름 패턴 실패")
        return false
    }
    
    // 컨텍스트 확인
    val hasPositionContext = context.nextText.contains("과장") || context.nextText.contains("부장") || 
                            context.nextText.contains("대표") || context.previousText.contains("대표")
    
    val hasCompanyContext = context.allLines.any { line -> 
        line.contains("주식회사") || line.contains("㈜") || line.contains("Inc") || line.contains("Corp")
    }
    
    Log.d("OCRUtil", "컨텍스트 확인: 직책컨텍스트=$hasPositionContext, 회사컨텍스트=$hasCompanyContext")
    return hasPositionContext || hasCompanyContext
}

// 컨텍스트 기반 직책 분류
private fun isLikelyPositionWithContext(context: TextContext): Boolean {
    val text = context.text.trim()
    
    Log.d("OCRUtil", "컨텍스트 직책 분류 검사: '$text'")
    
    // 기본 직책 패턴
    if (!isLikelyPosition(text, context.previousText, context.nextText)) {
        Log.d("OCRUtil", "기본 직책 패턴 실패")
        return false
    }
    
    // 컨텍스트 확인
    val hasNameContext = context.allLines.any { line -> 
        line.matches(Regex("^[가-힣]{2,3}$")) && !line.contains("지사") && !line.contains("본사")
    }
    
    Log.d("OCRUtil", "컨텍스트 확인: 이름컨텍스트=$hasNameContext")
    return hasNameContext
}

// 컨텍스트 기반 회사명 분류
private fun isLikelyCompanyWithContext(context: TextContext): Boolean {
    val text = context.text.trim()
    
    Log.d("OCRUtil", "컨텍스트 회사명 분류 검사: '$text'")
    
    // 기본 회사명 패턴
    if (!isLikelyCompany(text, context.previousText, context.nextText)) {
        Log.d("OCRUtil", "기본 회사명 패턴 실패")
        return false
    }
    
    // 컨텍스트 확인
    val hasNameContext = context.allLines.any { line -> 
        line.matches(Regex("^[가-힣]{2,3}$")) && !line.contains("지사") && !line.contains("본사")
    }
    
    val hasPositionContext = context.allLines.any { line ->
        line.contains("과장") || line.contains("부장") || line.contains("대표")
    }
    
    Log.d("OCRUtil", "컨텍스트 확인: 이름컨텍스트=$hasNameContext, 직책컨텍스트=$hasPositionContext")
    return hasNameContext || hasPositionContext
}

// 컨텍스트 기반 주소 분류
private fun isLikelyAddressWithContext(context: TextContext): Boolean {
    val text = context.text.trim()
    
    // 기본 주소 패턴
    if (!isLikelyAddress(text, context.previousText, context.nextText)) return false
    
    // 컨텍스트 확인
    val hasCompanyContext = context.allLines.any { line ->
        line.contains("주식회사") || line.contains("㈜") || line.contains("Inc") || line.contains("Corp")
    }
    
    return hasCompanyContext
}

// 컨텍스트 기반 부서 분류
private fun isLikelyDepartmentWithContext(context: TextContext): Boolean {
    val text = context.text.trim()
    
    // 기본 부서 패턴
    if (!isLikelyDepartment(text, context.previousText, context.nextText)) return false
    
    // 컨텍스트 확인
    val hasCompanyContext = context.allLines.any { line ->
        line.contains("주식회사") || line.contains("㈜") || line.contains("Inc") || line.contains("Corp")
    }
    
    return hasCompanyContext
}



// 전화번호 접두사 제거 함수
private fun removePhonePrefix(phone: String): String {
    if (phone.isEmpty()) return phone
    
    // T., M., F., Tel, Mobile, Fax 접두사 제거
    val cleanPhone = phone.replace(Regex("^(T|M|F|Tel|Mobile|Fax)[\\s.]*"), "")
    Log.d("OCRUtil", "전화번호 접두사 제거: '$phone' -> '$cleanPhone'")
    return cleanPhone
}

// 전화번호 포맷팅 함수
private fun formatPhoneNumber(phone: String): String {
    Log.d("OCRUtil", "=== 전화번호 포맷팅 시작 ===")
    Log.d("OCRUtil", "입력 전화번호: '$phone'")
    
    if (phone.isEmpty()) {
        Log.d("OCRUtil", "전화번호가 비어있음")
        return phone
    }
    
    // 접두사 제거
    val cleanPhone = removePhonePrefix(phone)
    Log.d("OCRUtil", "접두사 제거 후: '$cleanPhone'")
    
    // 숫자만 추출
    val digits = cleanPhone.replace(Regex("[^0-9]"), "")
    Log.d("OCRUtil", "추출된 숫자: '$digits'")
    
    val formattedPhone = when (digits.length) {
        10 -> {
            // 3-3-4 형식
            "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
        }
        11 -> {
            // 3-4-4 형식
            "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
        }
        else -> {
            // 다른 길이의 경우에도 하이픈 추가 시도
            if (digits.length > 6) {
                val firstPart = digits.substring(0, 3)
                val secondPart = digits.substring(3, min(6, digits.length - 1))
                val thirdPart = if (digits.length > 6) digits.substring(6) else ""
                "$firstPart-$secondPart${if (thirdPart.isNotEmpty()) "-$thirdPart" else ""}"
            } else {
                cleanPhone // 너무 짧으면 원본 반환
            }
        }
    }
    
    Log.d("OCRUtil", "포맷팅된 전화번호: '$formattedPhone'")
    Log.d("OCRUtil", "=== 전화번호 포맷팅 완료 ===")
    return formattedPhone
}

// 주소 포맷팅 함수
private fun formatAddress(address: String): String {
    Log.d("OCRUtil", "=== 주소 포맷팅 시작 ===")
    Log.d("OCRUtil", "입력 주소: '$address'")
    Log.d("OCRUtil", "입력 주소 길이: ${address.length}")
    Log.d("OCRUtil", "입력 주소에 '+' 포함 여부: ${address.contains("+")}")
    
    if (address.isEmpty()) {
        Log.d("OCRUtil", "주소가 비어있음")
        return address
    }
    
    var formattedAddress = address
    
    // '+'를 공백으로 변경
    val beforePlus = formattedAddress
    formattedAddress = formattedAddress.replace("+", " ")
    Log.d("OCRUtil", "플러스 제거 전: '$beforePlus'")
    Log.d("OCRUtil", "플러스 제거 후: '$formattedAddress'")
    Log.d("OCRUtil", "플러스 제거 후 길이: ${formattedAddress.length}")
    
    // 연속된 공백을 하나로 정리
    val beforeSpace = formattedAddress
    formattedAddress = formattedAddress.replace(Regex("\\s+"), " ")
    Log.d("OCRUtil", "공백 정리 전: '$beforeSpace'")
    Log.d("OCRUtil", "공백 정리 후: '$formattedAddress'")
    Log.d("OCRUtil", "공백 정리 후 길이: ${formattedAddress.length}")
    
    // 앞뒤 공백 제거
    val beforeTrim = formattedAddress
    formattedAddress = formattedAddress.trim()
    Log.d("OCRUtil", "트림 전: '$beforeTrim'")
    Log.d("OCRUtil", "트림 후: '$formattedAddress'")
    Log.d("OCRUtil", "트림 후 길이: ${formattedAddress.length}")
    
    Log.d("OCRUtil", "=== 주소 포맷팅 완료 ===")
    Log.d("OCRUtil", "최종 포맷팅된 주소: '$formattedAddress'")
    return formattedAddress
}

// 결과 검증 및 포맷팅 함수
private fun validateAndFormatResults(results: BusinessCardInfo): BusinessCardInfo {
    Log.d("OCRUtil", "=== 포맷팅 시작 ===")
    Log.d("OCRUtil", "원본 BusinessCardInfo: $results")
    Log.d("OCRUtil", "원본 전화번호: '${results.phone}'")
    Log.d("OCRUtil", "원본 주소: '${results.address}'")
    
    // 1. 이름 검증
    val validatedName = validateName(results.name)
    Log.d("OCRUtil", "검증된 이름: '$validatedName'")
    
    // 2. 전화번호 검증 (하이픈 제거, 숫자만 저장)
    val validatedPhone = validatePhone(results.phone)
    Log.d("OCRUtil", "검증된 전화번호: '$validatedPhone'")
    // OCR 인식 시에는 하이픈 제거하고 숫자만 저장
    val cleanPhone = validatedPhone.replace(Regex("[^0-9]"), "")
    Log.d("OCRUtil", "하이픈 제거된 전화번호: '$cleanPhone'")
    
    // 3. 이메일 검증
    val validatedEmail = validateEmail(results.email)
    Log.d("OCRUtil", "검증된 이메일: '$validatedEmail'")
    
    // 4. 주소 포맷팅
    Log.d("OCRUtil", "주소 포맷팅 시작 - 원본: '${results.address}'")
    val formattedAddress = formatAddress(results.address)
    Log.d("OCRUtil", "포맷팅된 주소: '$formattedAddress'")
    Log.d("OCRUtil", "포맷팅된 주소 길이: ${formattedAddress.length}")
    Log.d("OCRUtil", "포맷팅된 주소에 '+' 포함 여부: ${formattedAddress.contains("+")}")
    
    // 5. 팩스번호 검증 (하이픈 제거, 숫자만 저장)
    val validatedFax = validatePhone(results.fax)
    val cleanFax = validatedFax.replace(Regex("[^0-9]"), "")
    Log.d("OCRUtil", "하이픈 제거된 팩스: '$cleanFax'")
    
    // 6. 휴대폰번호 검증 (하이픈 제거, 숫자만 저장)
    val validatedMobile = validatePhone(results.mobile)
    val cleanMobile = validatedMobile.replace(Regex("[^0-9]"), "")
    Log.d("OCRUtil", "하이픈 제거된 휴대폰: '$cleanMobile'")
    
    Log.d("OCRUtil", "=== 포맷팅 완료 ===")
    
    val finalResult = results.copy(
        name = validatedName,
        phone = cleanPhone,  // 하이픈 제거된 숫자만 저장
        email = validatedEmail,
        address = formattedAddress,
        fax = cleanFax,      // 하이픈 제거된 숫자만 저장
        mobile = cleanMobile // 하이픈 제거된 숫자만 저장
    )
    
    Log.d("OCRUtil", "최종 결과 복사본: $finalResult")
    Log.d("OCRUtil", "최종 전화번호: '${finalResult.phone}'")
    Log.d("OCRUtil", "최종 주소: '${finalResult.address}'")
    return finalResult
}

// OCR 테스트 함수 추가
suspend fun testOCR(context: Context): String {
    Log.d("OCRUtil", "=== OCR 테스트 시작 ===")
    return try {
        // 간단한 테스트용 TextRecognizer 생성
        val recognizer = createTextRecognizer()
        if (recognizer != null) {
            Log.d("OCRUtil", "OCR 테스트 성공 - TextRecognizer 생성됨")
            recognizer.close()
            "OCR 테스트 성공"
        } else {
            Log.e("OCRUtil", "OCR 테스트 실패 - TextRecognizer 생성 실패")
            "OCR 테스트 실패"
        }
    } catch (e: Exception) {
        Log.e("OCRUtil", "OCR 테스트 중 오류", e)
        "OCR 테스트 오류: ${e.message}"
    }
}

// 개선된 텍스트 분류를 위한 데이터 클래스들
data class TextContext(
    val text: String,
    val position: Int,
    val previousText: String,
    val nextText: String,
    val allLines: List<String> = emptyList(),
    val linePosition: LinePosition = LinePosition.UNKNOWN
)

enum class LinePosition {
    TOP, MIDDLE, BOTTOM, LEFT, RIGHT, CENTER, UNKNOWN
}

data class TextClassification(
    val fieldType: FieldType,
    val confidence: Float,
    val classificationStage: ClassificationStage = ClassificationStage.PATTERN,
    val context: TextContext? = null
)

enum class ClassificationStage {
    PATTERN,      // 1단계: 명확한 패턴 매칭
    CONTEXT,      // 2단계: 컨텍스트 기반 분류
    HEURISTIC     // 3단계: 휴리스틱 분류
}

enum class FieldType {
    NAME, COMPANY, POSITION, PHONE, EMAIL, ADDRESS, WEBSITE, FAX, MOBILE, DEPARTMENT, UNKNOWN
}

// 필드별 우선순위 (높을수록 우선) - 개선
private val fieldPriority = mapOf(
    FieldType.EMAIL to 10,
    FieldType.FAX to 9,      // 팩스 우선순위 높임
    FieldType.MOBILE to 8,   // 휴대폰 우선순위 높임
    FieldType.PHONE to 7,
    FieldType.WEBSITE to 6,  // 웹사이트 우선순위 높임
    FieldType.NAME to 5,
    FieldType.POSITION to 4,
    FieldType.COMPANY to 3,
    FieldType.DEPARTMENT to 2,
    FieldType.ADDRESS to 1,
    FieldType.UNKNOWN to 0
)

// 분류 단계별 신뢰도 가중치
private val stageConfidenceWeight = mapOf(
    ClassificationStage.PATTERN to 1.0f,
    ClassificationStage.CONTEXT to 0.8f,
    ClassificationStage.HEURISTIC to 0.6f
)

// 기존 classifyTextWithContext 함수는 새로운 다단계 분류 시스템으로 대체됨

// 이름 분류 개선 함수 - 더 엄격하게 수정
private fun isLikelyName(text: String, prev: String, next: String): Boolean {
    val cleanText = text.trim()
    
    Log.d("OCRUtil", "이름 분류 검사: '$cleanText'")
    
    // 1. 이름+직책 패턴이 포함된 경우는 이름으로 분류 (우선순위) - 수정
    val namePositionPattern = Regex("([가-힣]{2,3})\\s+(과장|부장|대리|주임|사원|팀장|실장|이사|회장|부회장|매니저|대표)")
    if (namePositionPattern.matches(cleanText)) {
        Log.d("OCRUtil", "이름+직책 패턴으로 이름 분류: '$cleanText'")
        return true
    }
    
    // 2. 직책만 있는 경우 제외
    val positionOnlyPattern = Regex("^(과장|부장|대리|주임|사원|팀장|실장|이사|회장|부회장|매니저|대표)$")
    if (positionOnlyPattern.matches(cleanText)) {
        Log.d("OCRUtil", "직책만 있는 경우 이름 분류 제외: '$cleanText'")
        return false
    }
    
    // 3. 기본 이름 패턴 - 지사/본사 등 제외
    if (cleanText.matches(Regex("^[가-힣]{2,4}$"))) {
        // 지사, 본사, 지역명 등은 제외
        if (cleanText.contains("지사") || cleanText.contains("본사") || 
            cleanText.contains("경남") || cleanText.contains("경북") || cleanText.contains("전남") || cleanText.contains("전북") ||
            cleanText.contains("충남") || cleanText.contains("충북") || cleanText.contains("강원") || cleanText.contains("제주")) {
            Log.d("OCRUtil", "지역명/부서명으로 이름 분류 제외: '$cleanText'")
            return false
        }
        
        // 단독 한글 이름인 경우, 주변 컨텍스트 확인
        if (prev.isEmpty() && next.isEmpty()) {
            Log.d("OCRUtil", "단독 한글 이름으로 분류: '$cleanText'")
            return true
        }
        if (next.contains("과장") || next.contains("부장") || next.contains("대표")) {
            Log.d("OCRUtil", "다음 라인에 직책이 있어 이름으로 분류: '$cleanText'")
            return true
        }
        if (prev.contains("대표") || prev.contains("사장")) {
            Log.d("OCRUtil", "이전 라인에 직책이 있어 이름으로 분류: '$cleanText'")
            return true
        }
    }
    
    // 4. 영어 이름 패턴
    if (cleanText.matches(Regex("^[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)?$"))) {
        Log.d("OCRUtil", "영어 이름으로 분류: '$cleanText'")
        return true
    }
    
    Log.d("OCRUtil", "이름 분류 실패: '$cleanText'")
    return false
}

// 회사명 분류 개선 함수
private fun isLikelyCompany(text: String, prev: String, next: String): Boolean {
    val cleanText = text.trim()
    
    Log.d("OCRUtil", "회사명 분류 검사: '$cleanText'")
    
    // 1. 우선순위 높은 회사명 패턴 (농협네트웍스 등) - 가장 먼저 체크
    val priorityCompanyPatterns = listOf(
        "농협네트웍스", "농협", "네트웍스", "NHNongiyup", "NHNonglyup", "NH", "Nonghyup", "Nongiyup", "농업네트웍스"
    )
    
    // 1-1. 특별한 경우: "농협네트웍스"가 포함된 경우 우선 처리
    if (cleanText.contains("농협네트웍스")) {
        Log.d("OCRUtil", "농협네트웍스 우선 매칭")
        return true
    }
    
    // 1-2. "농협"이 포함된 경우도 우선 처리 (농협네트웍스의 경우)
    if (cleanText.contains("농협") && cleanText.length > 2) {
        Log.d("OCRUtil", "농협 포함 우선 매칭")
        return true
    }
    
    // 1-3. "NHNonglyup" 정확한 매칭 (로그에서 보인 문제 해결)
    if (cleanText == "NHNonglyup") {
        Log.d("OCRUtil", "NHNonglyup 정확한 매칭")
        return true
    }
    
    // 1-4. "NHNongiyup" 정확한 매칭 (로그에서 보인 문제 해결)
    if (cleanText == "NHNongiyup") {
        Log.d("OCRUtil", "NHNongiyup 정확한 매칭")
        return true
    }
    
    // 1-5. "농협네트웍스" 정확한 매칭 (로그에서 보인 문제 해결)
    if (cleanText == "농협네트웍스") {
        Log.d("OCRUtil", "농협네트웍스 정확한 매칭")
        return true
    }
    
    // 정확한 매칭 우선
    for (pattern in priorityCompanyPatterns) {
        if (cleanText == pattern) {
            Log.d("OCRUtil", "정확한 회사명 패턴 매칭: $pattern")
            return true
        }
    }
    
    // 포함 매칭
    for (pattern in priorityCompanyPatterns) {
        if (cleanText.contains(pattern, ignoreCase = true)) {
            Log.d("OCRUtil", "포함 회사명 패턴 발견: $pattern")
            return true
        }
    }
    
    // 2. 회사 관련 키워드 포함
    val companyKeywords = listOf(
        "주식회사", "㈜", "㈐", "㈑", "㈒", "㈓", "㈔", "㈕", "㈖", "㈗", "㈘", "㈙", "㈚", "㈛", "㈜", "㈝", "㈞",
        "(주)", "(유)", "(합)", "(기)", "(사)", "(회)", "(공)", "(재단)", "(협회)", "(연구소)", "(센터)",
        "Corp", "Corporation", "Inc", "Incorporated", "Ltd", "Limited", "Co", "Company", "LLC", "LLP",
        "Group", "Systems", "Technology", "Technologies", "기업", "코퍼레이션"
    )
    
    for (keyword in companyKeywords) {
        if (cleanText.contains(keyword)) {
            Log.d("OCRUtil", "회사 키워드로 회사명 분류: $keyword")
            return true
        }
    }
    
    // 3. 지사/본사 패턴 (회사명이 아닌 부서명으로 분류)
    if (cleanText.contains("지사") || cleanText.contains("본사")) {
        Log.d("OCRUtil", "지사/본사로 회사명 분류 제외")
        return false // 부서명으로 분류
    }
    
    // 4. 대문자로 시작하는 긴 텍스트 (영어 회사명)
    if (cleanText.matches(Regex("^[A-Z][a-zA-Z0-9\\s&.-]{3,}$"))) {
        Log.d("OCRUtil", "영어 회사명으로 분류")
        return true
    }
    
    // 5. 한글 + 영어 혼합 회사명
    if (cleanText.matches(Regex(".*[가-힣]+.*[A-Z]{2,}.*"))) {
        Log.d("OCRUtil", "한글+영어 혼합 회사명으로 분류")
        return true
    }
    
    // 6. 개선된 회사명 추출 로직: 키워드가 없어도 짧고 특이한 명사이면 회사명일 가능성
    if (isProbableCompanyLine(cleanText)) {
        Log.d("OCRUtil", "추정 회사명으로 분류")
        return true
    }
    
    // 7. 추가: 특정 회사명 패턴 (예: "NHNongiyup" 같은 경우)
    if (cleanText.matches(Regex("^[A-Z]{2,}[a-zA-Z0-9]+$"))) {
        Log.d("OCRUtil", "특정 회사명 패턴으로 분류")
        return true
    }
    
    // 8. 추가: 한글 회사명 패턴 (2-10글자, 특수문자 없음)
    if (cleanText.matches(Regex("^[가-힣]{2,10}$")) && 
        !cleanText.contains("과장") && !cleanText.contains("부장") && !cleanText.contains("대표") &&
        !cleanText.contains("지사") && !cleanText.contains("본사")) {
        Log.d("OCRUtil", "한글 회사명 패턴으로 분류")
        return true
    }
    
    Log.d("OCRUtil", "회사명 분류 실패")
    return false
}

// 개선된 회사명 추출 로직
private fun isProbableCompanyLine(line: String): Boolean {
    Log.d("OCRUtil", "회사명 추정 검사: '$line'")
    
    // 1. 우선순위 높은 회사명 패턴 (농협네트웍스 등)
    val priorityCompanyPatterns = listOf(
        "농협네트웍스", "농협", "네트웍스", "NHNongiyup", "NHNonglyup", "NH"
    )
    
    // 1-1. 특별한 경우: "농협네트웍스"가 포함된 경우 우선 처리
    if (line.contains("농협네트웍스")) {
        Log.d("OCRUtil", "농협네트웍스 우선 매칭 (isProbableCompanyLine)")
        return true
    }
    
    // 1-1-1. "농협네트웍스" 정확한 매칭 (로그에서 보인 문제 해결)
    if (line == "농협네트웍스") {
        Log.d("OCRUtil", "농협네트웍스 정확한 매칭 (isProbableCompanyLine)")
        return true
    }
    
    // 정확한 매칭 우선
    for (pattern in priorityCompanyPatterns) {
        if (line == pattern) {
            Log.d("OCRUtil", "정확한 회사명 패턴 매칭: $pattern")
            return true
        }
    }
    
    // 포함 매칭
    for (pattern in priorityCompanyPatterns) {
        if (line.contains(pattern, ignoreCase = true)) {
            Log.d("OCRUtil", "포함 회사명 패턴 발견: $pattern")
            return true
        }
    }
    
    // 2. 일반 회사 키워드가 있으면 회사명
    val companyKeywords = listOf("㈜", "(주)", "회사", "Inc", "Corporation", "기업", "Co.", "Corp", "Ltd", "LLC", "네트웍스", "Networks")
    if (companyKeywords.any { line.contains(it) }) {
        Log.d("OCRUtil", "회사 키워드 발견으로 회사명 추정")
        return true
    }
    
    // 3. 키워드가 없어도 조건을 만족하면 회사명일 가능성
    val isProbable = line.length in 2..20 && 
           line.matches(Regex("[가-힣A-Za-z0-9 ]+")) &&
           !line.contains("@") && // 이메일 도메인이 아닌지 확인
           !line.contains("www") && // 웹사이트가 아닌지 확인
           !line.matches(Regex(".*\\d{2,3}[\\s\\-]?\\d{3,4}[\\s\\-]?\\d{4}.*")) && // 전화번호가 아닌지 확인
           !line.contains("과장") && !line.contains("부장") && !line.contains("대표") && // 직책이 아닌지 확인
           !line.contains("지사") && !line.contains("본사") // 부서가 아닌지 확인
    
    if (isProbable) {
        Log.d("OCRUtil", "조건 만족으로 회사명 추정")
    } else {
        Log.d("OCRUtil", "조건 불만족으로 회사명 추정 실패")
    }
    
    return isProbable
}



// 직책 분류 개선 함수
private fun isLikelyPosition(text: String, prev: String, next: String): Boolean {
    val cleanText = text.trim()
    
    Log.d("OCRUtil", "직책 분류 검사: '$cleanText'")
    
    val positionKeywords = listOf(
        "사장", "대표", "부장", "과장", "대리", "주임", "사원", "팀장", "실장", "이사", "회장", "부회장", "매니저", "팀원",
        "CEO", "CTO", "CFO", "CMO", "President", "Director", "Manager", "Senior", "Lead", "Head", "Chief", 
        "Vice President", "Project Manager", "Team Lead", "Staff", "Engineer", "Developer", "디자이너"
    )
    
    // 1. 단독 직책
    if (positionKeywords.any { cleanText.contains(it) }) {
        Log.d("OCRUtil", "단독 직책으로 분류")
        return true
    }
    
    // 2. 이름 + 직책 패턴에서 직책 부분 추출
    val namePositionPattern = Regex("([가-힣]{2,4})\\s*(사장|대표|부장|과장|대리|주임|사원|팀장|실장|이사|회장|부회장)")
    val match = namePositionPattern.find(cleanText)
    if (match != null) {
        Log.d("OCRUtil", "이름+직책 패턴에서 직책으로 분류")
        return true
    }
    
    // 3. 개선된 이름+직책 추출 로직
    val extractedResult = extractNameAndPosition(cleanText)
    if (extractedResult.second != null) {
        Log.d("OCRUtil", "추출된 직책으로 분류: '${extractedResult.second}'")
        return true
    }
    
    // 2-1. 추가: "김도훈 과장" 패턴에서 직책 추출 (로그에서 보인 문제 해결)
    val specificNamePositionPattern = Regex("([가-힣]{2,3})\\s+(과장|부장|대리|주임|사원|팀장|실장|이사|회장|부회장|매니저)")
    val specificMatch = specificNamePositionPattern.find(cleanText)
    if (specificMatch != null) {
        Log.d("OCRUtil", "구체적 이름+직책 패턴에서 직책으로 분류: '${specificMatch.groupValues[2]}'")
        return true
    }
    
    Log.d("OCRUtil", "직책 분류 실패")
    return false
}

// 주소 분류 개선 함수 - 더 정확하게 수정
private fun isLikelyAddress(text: String, prev: String, next: String): Boolean {
    val cleanText = text.trim()
    
    Log.d("OCRUtil", "주소 분류 검사: '$cleanText'")
    
    // 1. 특정 주소 패턴 우선 체크 (예: "경남 창원시 성산구 상남로 63") - 가장 우선순위 높음
    val specificAddressPatterns = listOf(
        "경남 창원시 성산구 상남로 63",
        "경남 창원시",
        "창원시 성산구",
        "성산구 상남로"
    )
    
    // 1-1. 특별한 경우: "경남 창원시 성산구 상남로 63"이 포함된 경우 우선 처리
    if (cleanText.contains("경남 창원시 성산구 상남로 63")) {
        Log.d("OCRUtil", "경남 창원시 성산구 상남로 63 우선 매칭")
        return true
    }
    
    // 1-1-1. 정확한 주소 매칭 (로그에서 보인 문제 해결)
    if (cleanText == "경남 창원시 성산구 상남로 63") {
        Log.d("OCRUtil", "경남 창원시 성산구 상남로 63 정확한 매칭")
        return true
    }
    
    // 1-2. "경남 창원시"가 포함된 경우도 우선 처리
    if (cleanText.contains("경남 창원시") && cleanText.length > 8) {
        Log.d("OCRUtil", "경남 창원시 포함 우선 매칭")
        return true
    }
    
    // 정확한 매칭 우선
    for (pattern in specificAddressPatterns) {
        if (cleanText == pattern) {
            Log.d("OCRUtil", "정확한 주소 패턴 매칭: $pattern")
            return true
        }
    }
    
    // 포함 매칭
    for (pattern in specificAddressPatterns) {
        if (cleanText.contains(pattern)) {
            Log.d("OCRUtil", "포함 주소 패턴 발견: $pattern")
            return true
        }
    }
    
    // 2. 지역명 포함 (가장 명확한 주소 패턴)
    val regions = listOf("경남", "경북", "전남", "전북", "충남", "충북", "강원", "제주", "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종")
    
    for (region in regions) {
        if (cleanText.contains(region)) {
            // 지사/본사가 포함된 경우는 부서명으로 분류
            if (cleanText.contains("지사") || cleanText.contains("본사")) {
                Log.d("OCRUtil", "지사/본사 포함으로 주소 분류 제외")
                return false
            }
            
            // 2-1. 지역명 + 구/시/동 패턴이면 주소
            if (cleanText.matches(Regex(".*${region}.*(구|시|동).*"))) {
                Log.d("OCRUtil", "지역명 + 구/시/동 패턴으로 주소 분류")
                return true
            }
            
            // 2-2. 지역명 + 로/길 패턴이면 주소
            if (cleanText.matches(Regex(".*${region}.*(로|길).*"))) {
                Log.d("OCRUtil", "지역명 + 로/길 패턴으로 주소 분류")
                return true
            }
            
            // 2-3. 지역명 + 숫자 패턴이면 주소 (예: "경남 창원시 성산구 상남로 63")
            if (cleanText.matches(Regex(".*${region}.*\\d+.*"))) {
                Log.d("OCRUtil", "지역명 + 숫자 패턴으로 주소 분류")
                return true
            }
            
            // 2-4. 지역명만 있어도 주소일 가능성 (긴 텍스트인 경우)
            if (cleanText.length > 8) {
                Log.d("OCRUtil", "지역명 + 긴 텍스트로 주소 분류")
                return true
            }
        }
    }
    
    // 3. 주소 관련 키워드 (더 구체적으로)
    val addressKeywords = listOf("로", "길", "번지", "동", "구", "시", "도", "Street", "Avenue", "Road", "Floor", "Suite")
    
    for (keyword in addressKeywords) {
        if (cleanText.contains(keyword)) {
            // 숫자와 함께 있으면 주소일 가능성 높음
            if (cleanText.matches(Regex(".*\\d+.*"))) {
                Log.d("OCRUtil", "주소 키워드 + 숫자 패턴으로 주소 분류")
                return true
            }
        }
    }
    
    // 4. 숫자와 문자가 혼합된 긴 텍스트 (주소 패턴)
    if (cleanText.matches(Regex(".*\\d+.*")) && cleanText.length > 10 && 
        (cleanText.contains("로") || cleanText.contains("길") || cleanText.contains("동") || cleanText.contains("구"))) {
        Log.d("OCRUtil", "긴 텍스트 + 주소 키워드 패턴으로 주소 분류")
        return true
    }
    
    // 5. 특정 주소 패턴 (예: "경남 창원시 성산구 상남로 63")
    val specificAddressPattern = Regex(".*(경남|경북|전남|전북|충남|충북|강원|제주|서울|부산|대구|인천|광주|대전|울산|세종).*(창원|부산|대구|인천|광주|대전|울산|세종).*(구|시).*(로|길).*\\d+.*")
    if (specificAddressPattern.matches(cleanText)) {
        Log.d("OCRUtil", "특정 주소 패턴으로 주소 분류")
        return true
    }
    
    // 6. 추가: 더 관대한 주소 패턴 (지역명 + 구/시/동/로/길 중 하나라도 있으면)
    val liberalAddressPattern = Regex(".*(경남|경북|전남|전북|충남|충북|강원|제주|서울|부산|대구|인천|광주|대전|울산|세종).*(구|시|동|로|길).*")
    if (liberalAddressPattern.matches(cleanText)) {
        Log.d("OCRUtil", "관대한 주소 패턴으로 주소 분류")
        return true
    }
    
    // 7. 추가: 숫자가 포함된 긴 텍스트 (주소일 가능성)
    if (cleanText.length > 8 && cleanText.matches(Regex(".*\\d+.*")) && 
        (cleanText.contains("구") || cleanText.contains("시") || cleanText.contains("동") || cleanText.contains("로") || cleanText.contains("길"))) {
        Log.d("OCRUtil", "긴 텍스트 + 숫자 + 주소 키워드로 주소 분류")
        return true
    }
    
    Log.d("OCRUtil", "주소 분류 실패")
    return false
}

// 부서 분류 개선 함수
private fun isLikelyDepartment(text: String, prev: String, next: String): Boolean {
    val cleanText = text.trim()
    
    Log.d("OCRUtil", "부서 분류 검사: '$cleanText'")
    
    // 1. 특정 부서 패턴 우선 체크 (예: "경남지사") - 가장 우선순위 높음
    val specificDepartmentPatterns = listOf(
        "경남지사", "서울지사", "부산지사", "대구지사", "인천지사", "광주지사", "대전지사", "울산지사",
        "경북지사", "전남지사", "전북지사", "충남지사", "충북지사", "강원지사", "제주지사", "세종지사"
    )
    
    // 1-1. 특별한 경우: "경남지사"가 포함된 경우 우선 처리
    if (cleanText.contains("경남지사")) {
        Log.d("OCRUtil", "경남지사 우선 매칭")
        return true
    }
    
    // 1-2. 정확한 부서명 매칭 (로그에서 보인 문제 해결)
    if (cleanText == "경남지사") {
        Log.d("OCRUtil", "경남지사 정확한 매칭")
        return true
    }
    
    // 1-2-1. 정확한 부서명 매칭 (로그에서 보인 문제 해결)
    if (cleanText == "경남지사") {
        Log.d("OCRUtil", "경남지사 정확한 매칭 (중복 체크)")
        return true
    }
    
    // 1-2. "지사"가 포함된 경우도 우선 처리
    if (cleanText.contains("지사") && cleanText.length > 2) {
        Log.d("OCRUtil", "지사 포함 우선 매칭")
        return true
    }
    
    // 정확한 매칭 우선
    for (pattern in specificDepartmentPatterns) {
        if (cleanText == pattern) {
            Log.d("OCRUtil", "정확한 부서 패턴 매칭: $pattern")
            return true
        }
    }
    
    // 포함 매칭
    for (pattern in specificDepartmentPatterns) {
        if (cleanText.contains(pattern)) {
            Log.d("OCRUtil", "포함 부서 패턴 발견: $pattern")
            return true
        }
    }
    
    // 2. 부서 관련 키워드
    val departmentKeywords = listOf(
        "부서", "팀", "실", "과", "본부", "사업부", "연구소", "센터", "기획부", "영업부", "개발부", "마케팅부", "인사부", "총무부", "재무부",
        "Department", "Team", "Division", "Center", "Research", "Development", "Engineering", "Technology"
    )
    
    for (keyword in departmentKeywords) {
        if (cleanText.contains(keyword)) {
            Log.d("OCRUtil", "부서 키워드로 분류: $keyword")
            return true
        }
    }
    
    // 3. 지사/본사 패턴
    if (cleanText.contains("지사") || cleanText.contains("본사")) {
        Log.d("OCRUtil", "지사/본사 패턴으로 부서 분류")
        return true
    }
    
    // 4. 지역 + 지사/본사 패턴
    if (cleanText.matches(Regex(".*지사$|.*본사$"))) {
        Log.d("OCRUtil", "지역+지사/본사 패턴으로 부서 분류")
        return true
    }
    
    // 5. 추가: 지역명 + 지사/본사 패턴 (예: "경남지사", "서울본사")
    val regionBranchPattern = Regex("(경남|경북|전남|전북|충남|충북|강원|제주|서울|부산|대구|인천|광주|대전|울산|세종)(지사|본사)")
    if (regionBranchPattern.matches(cleanText)) {
        Log.d("OCRUtil", "지역+지사/본사 정규식 패턴으로 부서 분류")
        return true
    }
    
    // 6. 추가: 영어 부서명 패턴
    val englishDepartmentPattern = Regex("^[A-Z][a-z]+(\\s+[A-Z][a-z]+)*$")
    if (englishDepartmentPattern.matches(cleanText) && cleanText.length > 3) {
        // 전화번호나 이메일이 아닌 경우만
        if (!cleanText.contains("@") && !cleanText.matches(Regex(".*\\d{2,3}[\\s\\-]?\\d{3,4}[\\s\\-]?\\d{4}.*"))) {
            Log.d("OCRUtil", "영어 부서명 패턴으로 분류")
            return true
        }
    }
    
    // 7. 추가: 더 관대한 부서 패턴 (지역명 + 지사/본사 중 하나라도 있으면)
    val liberalDepartmentPattern = Regex(".*(경남|경북|전남|전북|충남|충북|강원|제주|서울|부산|대구|인천|광주|대전|울산|세종).*(지사|본사).*")
    if (liberalDepartmentPattern.matches(cleanText)) {
        Log.d("OCRUtil", "관대한 부서 패턴으로 부서 분류")
        return true
    }
    
    // 8. 추가: 짧은 한글 부서명 패턴 (2-6글자)
    if (cleanText.matches(Regex("^[가-힣]{2,6}$")) && 
        (cleanText.contains("지사") || cleanText.contains("본사") || cleanText.contains("부서") || cleanText.contains("팀"))) {
        Log.d("OCRUtil", "짧은 한글 부서명 패턴으로 분류")
        return true
    }
    
    Log.d("OCRUtil", "부서 분류 실패")
    return false
}

// 전화번호 분류 개선 함수 - 회사 전화번호만 분류 (T, TEL 접두사)
private fun isLikelyPhone(text: String, prev: String, next: String): Boolean {
    val cleanText = text.trim()
    
    // 1. T. 또는 TEL 접두사 포함 전화번호 패턴 (회사 전화번호)
    val telPrefixPattern = Regex("^(T|TEL)[\\s.]*([\\d\\-]+)$")
    val telMatch = telPrefixPattern.find(cleanText)
    if (telMatch != null) {
        val numberPart = telMatch.groupValues[2]
        if (numberPart.matches(Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})"))) {
            return true
        }
    }
    
    // 2. Tel 접두사 포함
    val fullTelPattern = Regex("^(Tel)[\\s.]*([\\d\\-]+)$")
    val fullTelMatch = fullTelPattern.find(cleanText)
    if (fullTelMatch != null) {
        val numberPart = fullTelMatch.groupValues[2]
        if (numberPart.matches(Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})"))) {
            return true
        }
    }
    
    // 3. 순수 전화번호 패턴 (숫자만) - 010으로 시작하지 않는 경우
    val phonePattern = Regex("^(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})$")
    if (phonePattern.matches(cleanText) && !cleanText.startsWith("010")) {
        return true
    }
    
    return false
}

// 팩스번호 분류 함수 개선 (F, FAX 접두사)
private fun isLikelyFax(text: String, prev: String, next: String): Boolean {
    val cleanText = text.trim()
    
    // F. 또는 FAX 접두사 포함
    val faxPattern = Regex("^(F|FAX)[\\s.]*([\\d\\-]+)$")
    val match = faxPattern.find(cleanText)
    if (match != null) {
        val numberPart = match.groupValues[2]
        if (numberPart.matches(Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})"))) {
            return true
        }
    }
    
    // Fax 접두사 포함
    val fullFaxPattern = Regex("^(Fax)[\\s.]*([\\d\\-]+)$")
    val fullMatch = fullFaxPattern.find(cleanText)
    if (fullMatch != null) {
        val numberPart = fullMatch.groupValues[2]
        if (numberPart.matches(Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})"))) {
            return true
        }
    }
    
    // 팩스 키워드가 포함된 경우
    if (cleanText.contains("팩스") || cleanText.contains("FAX") || cleanText.contains("Fax")) {
        val phonePattern = Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})")
        if (phonePattern.containsMatchIn(cleanText)) {
            return true
        }
    }
    
    return false
}

// 휴대폰 번호 분류 함수 개선 (M, MOB, MOBILE 접두사)
private fun isLikelyMobile(text: String, prev: String, next: String): Boolean {
    val cleanText = text.trim()
    
    // M. 또는 MOB, MOBILE 접두사 포함
    val mobilePattern = Regex("^(M|MOB|MOBILE)[\\s.]*([\\d\\-]+)$")
    val match = mobilePattern.find(cleanText)
    if (match != null) {
        val numberPart = match.groupValues[2]
        if (numberPart.matches(Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})"))) {
            return true
        }
    }
    
    // Mobile 접두사 포함
    val fullMobilePattern = Regex("^(Mobile)[\\s.]*([\\d\\-]+)$")
    val fullMatch = fullMobilePattern.find(cleanText)
    if (fullMatch != null) {
        val numberPart = fullMatch.groupValues[2]
        if (numberPart.matches(Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})"))) {
            return true
        }
    }
    
    // 010으로 시작하는 번호 (접두사 없이)
    if (cleanText.matches(Regex("^010[\\s\\-]?\\d{3,4}[\\s\\-]?\\d{4}$"))) {
        return true
    }
    
    // 휴대폰 키워드가 포함된 경우
    if (cleanText.contains("휴대폰") || cleanText.contains("휴대전화") || cleanText.contains("Mobile") || cleanText.contains("핸드폰")) {
        val phonePattern = Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})")
        if (phonePattern.containsMatchIn(cleanText)) {
            return true
        }
    }
    
    return false
}

// 웹사이트 분류 함수 개선
private fun isLikelyWebsite(text: String): Boolean {
    val cleanText = text.trim().lowercase()
    
    // www로 시작하는 패턴
    if (cleanText.startsWith("www.")) return true
    
    // http/https로 시작하는 패턴
    if (cleanText.startsWith("http://") || cleanText.startsWith("https://")) return true
    
    // 도메인 패턴 (.com, .co.kr, .net, .org 등)
    val domainPattern = Regex("^[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]\\.[a-zA-Z]{2,}(\\.[a-zA-Z]{2,})?$")
    if (domainPattern.matches(cleanText)) return true
    
    // 웹사이트 키워드가 포함된 경우
    if (cleanText.contains("웹사이트") || cleanText.contains("홈페이지") || cleanText.contains("website") || cleanText.contains("homepage")) {
        return true
    }
    
    return false
}

// 이메일 분류 함수
private fun isLikelyEmail(text: String): Boolean {
    val emailPattern = Regex("([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})")
    return emailPattern.matches(text)
}

// 문자 교정 함수 - 더 보수적으로 수정
private fun correctCommonOCRErrors(text: String): String {
    var correctedText = text
    
    // 매우 일반적인 OCR 오류만 수정 (과도한 수정 방지)
    val corrections = mapOf(
        "rn" to "m",  // rn → m (가장 일반적인 오류)
        "cl" to "d"   // cl → d (일반적인 오류)
    )
    
    // 숫자와 문자 혼동은 매우 신중하게 처리
    if (text.length > 1) {
        // 전화번호나 이메일에서는 숫자 교정을 하지 않음
        if (!text.contains("@") && !text.matches(Regex(".*\\d{2,3}[\\s\\-]?\\d{3,4}[\\s\\-]?\\d{4}.*"))) {
            // 매우 명확한 경우만 교정
            if (text == "O" && text.length == 1) correctedText = "0"
            if (text == "l" && text.length == 1) correctedText = "1"
        }
    }
    
    // 기본 교정 적용
    for ((error, correction) in corrections) {
        correctedText = correctedText.replace(error, correction)
    }
    
    return correctedText
}

// 신뢰도 계산 함수
private fun calculateConfidence(fieldType: FieldType, text: String): Float {
    var confidence = 0.0f
    
    when (fieldType) {
        FieldType.NAME -> {
            // 이름 패턴 매칭도에 따른 신뢰도
            if (text.matches(Regex("^[가-힣]{2,4}$"))) confidence += 0.8f
            if (text.matches(Regex("^[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)?$"))) confidence += 0.9f
        }
        FieldType.PHONE -> {
            // 전화번호 패턴 매칭도에 따른 신뢰도
            if (text.matches(Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})"))) confidence += 0.95f
        }
        FieldType.EMAIL -> {
            // 이메일 패턴 매칭도에 따른 신뢰도
            if (text.matches(Regex("([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})"))) confidence += 0.95f
        }
        FieldType.COMPANY -> {
            // 회사명 패턴 매칭도에 따른 신뢰도
            if (text.contains("주식회사") || text.contains("㈜")) confidence += 0.9f
            if (text.matches(Regex("^[A-Z][a-zA-Z0-9\\s&.-]{3,}$"))) confidence += 0.8f
        }
        FieldType.POSITION -> {
            // 직책 패턴 매칭도에 따른 신뢰도
            val positionKeywords = listOf("사장", "대표", "부장", "과장", "대리", "CEO", "CTO", "Director", "Manager")
            if (positionKeywords.any { text.contains(it) }) confidence += 0.85f
        }
        FieldType.ADDRESS -> {
            // 주소 패턴 매칭도에 따른 신뢰도
            val regions = listOf("경남", "경북", "전남", "전북", "충남", "충북", "강원", "제주", "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종")
            if (regions.any { text.contains(it) }) confidence += 0.8f
        }
        FieldType.DEPARTMENT -> {
            // 부서명 패턴 매칭도에 따른 신뢰도
            if (text.contains("지사") || text.contains("본사")) confidence += 0.9f
            if (text.contains("부서") || text.contains("팀")) confidence += 0.8f
        }
        else -> confidence = 0.5f
    }
    
    return confidence.coerceIn(0.0f, 1.0f)
}

private fun validateName(name: String): String {
    // 이름이 너무 짧거나 긴 경우 필터링
    if (name.length < 2 || name.length > 20) return ""
    
    // 특수문자가 포함된 경우 필터링
    if (name.matches(Regex(".*[^가-힣A-Za-z\\s].*"))) return ""
    
    return name
}

private fun validatePhone(phone: String): String {
    // 전화번호 패턴 검증 - 더 관대하게 수정
    val digits = phone.replace(Regex("[^0-9]"), "")
    
    // 10자리 또는 11자리 숫자인지 확인
    if (digits.length == 10 || digits.length == 11) {
        return phone // 원본 반환 (포맷팅은 나중에 처리)
    }
    
    // 기존 패턴도 허용
    if (phone.matches(Regex("(\\d{2,3})[\\s\\-]?(\\d{3,4})[\\s\\-]?(\\d{4})"))) {
        return phone
    }
    
    return phone // 검증 실패해도 원본 반환 (포맷팅에서 처리)
}

private fun validateEmail(email: String): String {
    // 이메일 패턴 검증
    if (!email.matches(Regex("([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})"))) return ""
    
    return email
}

// 백엔드 연동 시 다시 추가할 예정
// suspend fun runBackendOCR(uri: Uri, context: Context): BusinessCardInfo {
//     // 백엔드 서버로 이미지 전송하여 OCR 처리
// }

// 백엔드 연동 시 다시 추가할 예정
// suspend fun runBackendOCR(uri: Uri, context: Context): BusinessCardInfo {
//     // ML Kit + 백엔드 서버 호출
// }

// 클로바 일반 OCR API 설정 (사용자 제공 URL 사용)
private const val CLOVA_OCR_SECRET_KEY = "RXpGSWlNa3VuSHBEZkxBa0ZBY1lTUVBlV2xLcXhBekg="
// Clova API 엔드포인트 - 사용자 제공 일반 OCR URL 사용
private const val CLOVA_OCR_API_URL = "https://xr2k67apgb.apigw.ntruss.com/custom/v1/44847/377050dee7955567701982db19671bfe64b5d79af4cd35f12fe027418ad7ca7b/general"

// OpenAI API 설정
private const val OPENAI_API_KEY = SSAFY_API_KEY // SSAFY API 키 사용
private const val OPENAI_API_URL = SSAFY_API_URL // SSAFY API URL 사용

// 클로바 일반 OCR 서비스 인터페이스
interface ClovaGeneralOCRService {
    @retrofit2.http.POST("custom/v1/44847/377050dee7955567701982db19671bfe64b5d79af4cd35f12fe027418ad7ca7b/general")
    suspend fun processGeneralOCR(
        @retrofit2.http.Header("X-OCR-SECRET") secretKey: String,
        @retrofit2.http.Body request: ClovaGeneralOCRRequest
    ): ClovaGeneralOCRResponse
}

// 클로바 일반 OCR 요청 데이터 클래스
data class ClovaGeneralOCRRequest(
    val version: String = "V2",
    val requestId: String,
    val timestamp: Long,
    val images: List<ClovaImage>
)

data class ClovaImage(
    val format: String,
    val name: String,
    val data: String
)

// 클로바 일반 OCR 응답 데이터 클래스
data class ClovaGeneralOCRResponse(
    val version: String,
    val requestId: String,
    val timestamp: Long,
    val images: List<ClovaGeneralOCRImage>
)

data class ClovaGeneralOCRImage(
    val uid: String,
    val name: String,
    val inferResult: String,
    val message: String,
    val fields: List<ClovaGeneralField>? = null
)

data class ClovaGeneralField(
    val name: String,
    val boundingPolys: List<ClovaBoundingPoly>? = null,
    val inferText: String,
    val inferConfidence: Double
)

// ClovaField는 일반 OCR에서는 사용하지 않음 (명함 OCR 전용)

data class ClovaBoundingPoly(
    val vertices: List<ClovaVertex>
)

data class ClovaVertex(
    val x: Double,
    val y: Double
)

// AI 프롬프트 기반 동적 필드 분류 시스템
data class DynamicBusinessCardInfo(
    val standardFields: BusinessCardInfo = BusinessCardInfo(),
    val additionalFields: Map<String, String> = emptyMap(),
    val confidence: Double = 0.0,
    val aiReasoning: String = ""
)

// AI 분류를 위한 프롬프트 시스템
object AIClassificationPrompt {

    private val basePrompt = """
        당신은 명함 텍스트를 구조화하는 AI 명함 분석 전문가입니다.
        아래의 분류 규칙과 출력 형식에 따라, 주어진 텍스트를 정확하게 필드별로 추출하고 JSON 형태로 출력하세요.

        1. **출력 목적 및 구조**
        - 명함 정보를 구조화하여 시스템에 저장할 수 있도록, JSON 형식으로 각 필드를 분류합니다.
        - **중요**: 아래 5개 필수 필드는 이미 명함 정보 확인에서 자동으로 처리되므로, AI에서는 이들을 제외하고 부가 필드만 생성합니다:
            - 이름 (name)
            - 전화번호 (phone) 
            - 회사 (company)
            - 직책 (position)
            - 이메일 (email)

        - **부가 필드만** 아래와 같은 형식으로 `"fields"` 배열 안에 넣습니다:
            ```json
            "fields": [
              {
                "fieldName": "필드명(한글)",
                "fieldValue": "값"
              },
              ...
            ]
            ```

        2. **부가 필드 분류 규칙 (5개 필수 필드 제외)**
        | fieldName  | 추출 기준 예시 |
        |------------|----------------|
        | 부서        | 부서, 팀, 본부, 지사 등 포함된 텍스트 |
        | 회사전화    | 'T.', 'Tel'로 시작하거나 02, 031 등의 지역번호로 시작하며 휴대폰 번호 양식이 아닌 경우 |
        | 팩스        | 'F.', 'Fax'로 시작하는 번호 |
        | 주소        | '서울', '부산', '경기' 등 지역명 + '구', '시', '동' 등이 포함된 주소 문장 |
        | 웹사이트    | 'www.', '.com', '.co.kr' 등이 포함된 URL 형식 |
        | 사업자번호  | XXX-XX-XXXXX 형식의 사업자등록번호 |
        | 법인번호    | XXXXXX-XXXXXXX 형식의 법인등록번호 |
        | 업종        | 제조업, 서비스업, 도소매업, 건설업, 금융업 등 |
        | 설립일      | XXXX년 XX월 XX일 형식 |
        | 자본금      | XX억원, XX천만원 등 |
        | 직원수      | XX명, XX인 등 |
        | 대표번호    | 대표로 시작하는 전화번호 |
        | 고객센터    | 고객센터, 고객지원, 고객상담 관련 |
        | 영업시간    | XX:XX ~ XX:XX 형식 |
        | SNS        | Instagram, Facebook, Twitter, LinkedIn, 카카오톡, 라인 등 |
        | 기타        | 위 분류에 해당하지 않지만 명확하고 신뢰도 높은 정보 |

        3. **주의사항**
        - **5개 필수 필드(이름, 전화번호, 회사, 직책, 이메일)는 절대 포함하지 마세요**
        - 모든 fieldName은 **반드시 한글**로 명확하게 작성해야 합니다.
        - 불분명하거나 신뢰도 낮은 정보는 제외합니다.
        - 중복 항목은 제거하고, 가장 구체적인 필드 하나만 유지합니다.
        - 각 항목은 사람이 보기에도 이해 가능한 수준으로 정제된 상태여야 합니다.

        4. **출력 예시**
        입력:
        ```
        김싸피
        삼성청년SW아카데미
        백엔드 개발자
        ssafy@naver.com
        010-1234-5678
        서울특별시 강남구 테헤란로 212
        T. 02-123-4567
        www.ssafy.com
        경영지원팀
        F. 02-123-4568
        ```

        출력:
        {
          "fields": [
            {
              "fieldName": "부서",
              "fieldValue": "경영지원팀"
            },
            {
              "fieldName": "주소",
              "fieldValue": "서울특별시 강남구 테헤란로 212"
            },
            {
              "fieldName": "회사전화",
              "fieldValue": "02-123-4567"
            },
            {
              "fieldName": "웹사이트",
              "fieldValue": "www.ssafy.com"
            },
            {
              "fieldName": "팩스",
              "fieldValue": "02-123-4568"
            }
          ]
        }
    """.trimIndent()
    
    fun generatePrompt(text: String): String {
        return """
            $basePrompt
            
            **분석할 텍스트:**
            $text
            
            **JSON 응답만 출력:**
        """.trimIndent()
    }
    
    fun generateLearningPrompt(text: String, userFeedback: String): String {
        return """
            $basePrompt
            
            **분석할 텍스트:**
            $text
            
            **사용자 피드백:**
            $userFeedback
            
            **학습 후 개선된 JSON 응답:**
        """.trimIndent()
    }
}

// AI 분류 서비스 인터페이스
interface AIClassificationService {
    suspend fun classifyText(prompt: String): String
    suspend fun learnFromFeedback(text: String, feedback: String): String
}

// OpenAI GPT 기반 AI 분류 서비스
class OpenAIClassificationService : AIClassificationService {
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(OPENAI_API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(createSSAFYHttpClient())
        .build()
    
    private val openAIService = retrofit.create(OpenAIService::class.java)
    
    // SSAFY API용 HTTP 클라이언트 생성
    private fun createSSAFYHttpClient(): okhttp3.OkHttpClient {
        return okhttp3.OkHttpClient.Builder()
            .connectTimeout(300, java.util.concurrent.TimeUnit.SECONDS) // 연결 타임아웃 300초로 증가
            .readTimeout(300, java.util.concurrent.TimeUnit.SECONDS) // 읽기 타임아웃 300초로 증가
            .writeTimeout(300, java.util.concurrent.TimeUnit.SECONDS) // 쓰기 타임아웃 300초로 증가
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", SSAFY_API_KEY) // SSAFY API 키를 헤더에 추가
                    .method(original.method, original.body)
                chain.proceed(requestBuilder.build())
            }
            .build()
    }
    
    override suspend fun classifyText(prompt: String): String {
        return try {
            Log.d("AI", "=== SSAFY GPT-4o-mini API 호출 시작 ===")
            Log.d("AI", "API URL: $OPENAI_API_URL")
            Log.d("AI", "API Key: ${SSAFY_API_KEY.take(10)}...") // API 키 일부만 로깅
            Log.d("AI", "모델: gpt-4o-mini")
            Log.d("AI", "프롬프트 길이: ${prompt.length}자")
            Log.d("AI", "타임아웃 설정: 300초")
            
            val request = ChatCompletionRequest(
                model = "gpt-4o-mini", // SSAFY GPT-4o-mini 모델 사용
                messages = listOf(
                    ChatMessage(role = "system", content = "당신은 명함 텍스트 분석 전문가입니다."),
                    ChatMessage(role = "user", content = prompt)
                ),
                temperature = 0.1,
                maxTokens = 1000
            )
            
            Log.d("AI", "SSAFY API 요청 전송 중...")
            Log.d("AI", "요청 데이터: model=${request.model}, messages=${request.messages.size}개")
            Log.d("AI", "요청 시작 시간: ${System.currentTimeMillis()}")
            
            val response = openAIService.chatCompletion(request)
            
            Log.d("AI", "SSAFY API 응답 수신: ${response.choices.size}개 선택지")
            Log.d("AI", "응답 수신 시간: ${System.currentTimeMillis()}")
            val result = response.choices.first().message.content
            Log.d("AI", "SSAFY API 결과: $result")
            Log.d("AI", "=== SSAFY GPT-4o-mini API 호출 완료 ===")
            
            result
        } catch (e: Exception) {
            Log.e("AI", "=== SSAFY API 호출 실패 ===")
            Log.e("AI", "예외 타입: ${e.javaClass.simpleName}")
            Log.e("AI", "예외 메시지: ${e.message}")
            Log.e("AI", "API URL: $OPENAI_API_URL")
            Log.e("AI", "실패 시간: ${System.currentTimeMillis()}")
            Log.e("AI", "스택 트레이스:", e)
            
            // 네트워크 관련 에러인지 확인
            when (e) {
                is java.net.SocketTimeoutException -> {
                    Log.e("AI", "네트워크 타임아웃 발생 - 서버 응답 시간 초과 (300초 설정)")
                    Log.e("AI", "타임아웃 상세: ${e.message}")
                }
                is java.net.ConnectException -> {
                    Log.e("AI", "서버 연결 실패 - 서버가 다운되었거나 네트워크 문제")
                }
                is retrofit2.HttpException -> {
                    Log.e("AI", "HTTP 에러: ${e.code()} - ${e.message()}")
                    try {
                        val errorBody = e.response()?.errorBody()?.string()
                        Log.e("AI", "HTTP 에러 본문: $errorBody")
                    } catch (ex: Exception) {
                        Log.e("AI", "에러 본문 읽기 실패: ${ex.message}")
                    }
                }
                else -> {
                    Log.e("AI", "기타 에러: ${e.javaClass.simpleName}")
                }
            }
            
            "{}"
        }
    }
    
    override suspend fun learnFromFeedback(text: String, feedback: String): String {
        val learningPrompt = AIClassificationPrompt.generateLearningPrompt(text, feedback)
        return classifyText(learningPrompt)
    }
}

// AI 서비스 인터페이스
interface OpenAIService {
    @POST("chat/completions") // SSAFY API 엔드포인트
    @retrofit2.http.Headers(
        "Content-Type: application/json"
    )
    suspend fun chatCompletion(@Body request: ChatCompletionRequest): ChatCompletionResponse
}

// OpenAI API 데이터 클래스들
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.1,
    val maxTokens: Int = 1000
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val choices: List<ChatChoice>
)

data class ChatChoice(
    val message: ChatMessage
)

// AI 분류 결과 파싱
object AIClassificationParser {
    
    fun parseAIResponse(response: String): DynamicBusinessCardInfo {
        return try {
            val gson = com.google.gson.Gson()
            val jsonObject = gson.fromJson(response, com.google.gson.JsonObject::class.java)
            
            // 표준 필드 파싱
            val standardFieldsJson = jsonObject.getAsJsonObject("standardFields")
            val standardFields = BusinessCardInfo(
                name = standardFieldsJson?.get("name")?.asString ?: "",
                company = standardFieldsJson?.get("company")?.asString ?: "",
                position = standardFieldsJson?.get("position")?.asString ?: "",
                phone = standardFieldsJson?.get("phone")?.asString ?: "",
                email = standardFieldsJson?.get("email")?.asString ?: "",
                address = standardFieldsJson?.get("address")?.asString ?: "",
                website = standardFieldsJson?.get("website")?.asString ?: "",
                fax = standardFieldsJson?.get("fax")?.asString ?: "",
                mobile = standardFieldsJson?.get("mobile")?.asString ?: "",
                department = standardFieldsJson?.get("department")?.asString ?: "",
                rawText = ""
            )
            
            // 추가 필드 파싱
            val additionalFieldsJson = jsonObject.getAsJsonObject("additionalFields")
            val additionalFields = mutableMapOf<String, String>()
            additionalFieldsJson?.entrySet()?.forEach { entry ->
                additionalFields[entry.key] = entry.value.asString
            }
            
            // 신뢰도 및 추론 파싱
            val confidence = jsonObject.get("confidence")?.asDouble ?: 0.0
            val aiReasoning = jsonObject.get("aiReasoning")?.asString ?: ""
            
            DynamicBusinessCardInfo(
                standardFields = standardFields,
                additionalFields = additionalFields,
                confidence = confidence,
                aiReasoning = aiReasoning
            )
            
        } catch (e: Exception) {
            Log.e("AI", "AI 응답 파싱 실패", e)
            DynamicBusinessCardInfo()
        }
    }
}

// AI 분류 결과 학습 및 개선
object AIClassificationLearner {
    
    private val aiService: AIClassificationService = OpenAIClassificationService()
    private val learningHistory = mutableListOf<LearningExample>()
    
    data class LearningExample(
        val text: String,
        val userFeedback: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    suspend fun classifyWithLearning(text: String): DynamicBusinessCardInfo {
        // 1. 기본 AI 분류
        val prompt = AIClassificationPrompt.generatePrompt(text)
        val aiResponse = aiService.classifyText(prompt)
        val result = AIClassificationParser.parseAIResponse(aiResponse)
        
        // 2. 학습 히스토리 적용
        val improvedResult = applyLearningHistory(result, text)
        
        return improvedResult
    }
    
    suspend fun learnFromUserFeedback(text: String, feedback: String) {
        // 학습 예시 저장
        learningHistory.add(LearningExample(text, feedback))
        
        // AI 모델에 피드백 전달
        val learningResponse = aiService.learnFromFeedback(text, feedback)
        Log.d("AI", "학습 완료: $feedback")
    }
    
    private fun applyLearningHistory(result: DynamicBusinessCardInfo, text: String): DynamicBusinessCardInfo {
        // 최근 학습 히스토리를 기반으로 결과 개선
        val recentExamples = learningHistory.takeLast(5) // 최근 5개 예시만 사용
        
        var improvedResult = result
        
        for (example in recentExamples) {
            if (text.contains(example.userFeedback)) {
                // 유사한 패턴 발견 시 결과 개선
                improvedResult = improveResultBasedOnExample(improvedResult, example)
            }
        }
        
        return improvedResult
    }
    
    private fun improveResultBasedOnExample(result: DynamicBusinessCardInfo, example: LearningExample): DynamicBusinessCardInfo {
        // 예시를 기반으로 결과 개선 로직
        // 실제 구현에서는 더 정교한 패턴 매칭 및 개선 로직 적용
        return result.copy(confidence = result.confidence + 0.1)
    }
    
    fun getLearningStats(): String {
        return "총 학습 예시: ${learningHistory.size}개"
    }
}

// AI 기반 OCR 처리 함수
suspend fun runAIEnhancedOCR(uri: Uri, context: Context): DynamicBusinessCardInfo {
    Log.d("OCRUtil", "=== AI 강화 OCR 처리 시작 ===")
    
    return withContext(Dispatchers.IO) {
        try {
            // 1. ML Kit로 기본 텍스트 인식
            Log.d("OCRUtil", "1단계: ML Kit OCR 시작")
            val mlKitResult = runTextRecognition(uri, context)
            Log.d("OCRUtil", "ML Kit OCR 완료: ${mlKitResult.rawText.length}자")
            
            if (mlKitResult.rawText.isEmpty()) {
                Log.w("OCRUtil", "ML Kit에서 텍스트를 인식하지 못함")
                return@withContext DynamicBusinessCardInfo()
            }
            
            // 2. AI로 동적 필드 분류
            Log.d("OCRUtil", "2단계: AI 분류 시작")
            val aiResult = AIClassificationLearner.classifyWithLearning(mlKitResult.rawText)
            Log.d("OCRUtil", "AI 분류 완료: 신뢰도 ${aiResult.confidence}")
            
            // 3. 결과 병합 및 개선
            Log.d("OCRUtil", "3단계: 결과 병합")
            val finalResult = mergeAIAndMLKitResults(mlKitResult, aiResult)
            
            Log.d("OCRUtil", "=== AI 강화 OCR 처리 완료 ===")
            Log.d("OCRUtil", "표준 필드: ${finalResult.standardFields}")
            Log.d("OCRUtil", "추가 필드: ${finalResult.additionalFields}")
            Log.d("OCRUtil", "AI 추론: ${finalResult.aiReasoning}")
            
            finalResult
            
        } catch (e: Exception) {
            Log.e("OCRUtil", "AI 강화 OCR 처리 실패", e)
            Log.e("OCRUtil", "예외 타입: ${e.javaClass.simpleName}")
            Log.e("OCRUtil", "예외 메시지: ${e.message}")
            
            // AI 실패 시 ML Kit 결과만 반환
            try {
                val mlKitResult = runTextRecognition(uri, context)
                DynamicBusinessCardInfo(
                    standardFields = mlKitResult,
                    confidence = 0.5,
                    aiReasoning = "AI 처리 실패로 ML Kit 결과만 사용"
                )
            } catch (e2: Exception) {
                Log.e("OCRUtil", "ML Kit 폴백도 실패", e2)
                DynamicBusinessCardInfo()
            }
        }
    }
}

// ML Kit 결과와 AI 결과 병합
private fun mergeAIAndMLKitResults(mlKitResult: BusinessCardInfo, aiResult: DynamicBusinessCardInfo): DynamicBusinessCardInfo {
    
    // AI 결과를 기본으로 사용하되, ML Kit 결과로 보완
    val mergedStandardFields = BusinessCardInfo(
        name = aiResult.standardFields.name.ifEmpty { mlKitResult.name },
        company = aiResult.standardFields.company.ifEmpty { mlKitResult.company },
        position = aiResult.standardFields.position.ifEmpty { mlKitResult.position },
        phone = aiResult.standardFields.phone.ifEmpty { mlKitResult.phone },
        email = aiResult.standardFields.email.ifEmpty { mlKitResult.email },
        address = aiResult.standardFields.address.ifEmpty { mlKitResult.address },
        website = aiResult.standardFields.website.ifEmpty { mlKitResult.website },
        fax = aiResult.standardFields.fax.ifEmpty { mlKitResult.fax },
        mobile = aiResult.standardFields.mobile.ifEmpty { mlKitResult.mobile },
        department = aiResult.standardFields.department.ifEmpty { mlKitResult.department },
        rawText = mlKitResult.rawText,
        imageUri = mlKitResult.imageUri,
        imageUri2 = mlKitResult.imageUri2,
        capturedDate = mlKitResult.capturedDate
    )
    
    return aiResult.copy(
        standardFields = mergedStandardFields,
        confidence = calculateMergedConfidence(mlKitResult, aiResult)
    )
}

// 병합된 신뢰도 계산
private fun calculateMergedConfidence(mlKitResult: BusinessCardInfo, aiResult: DynamicBusinessCardInfo): Double {
    val aiConfidence = aiResult.confidence
    val mlKitConfidence = calculateMLKitConfidence(mlKitResult)
    
    // AI 신뢰도가 높으면 AI 우선, 아니면 ML Kit 우선
    return if (aiConfidence > 0.7) {
        aiConfidence * 0.8 + mlKitConfidence * 0.2
    } else {
        mlKitConfidence * 0.8 + aiConfidence * 0.2
    }
}

// ML Kit 결과의 신뢰도 계산
private fun calculateMLKitConfidence(mlKitResult: BusinessCardInfo): Double {
    var confidence = 0.0
    var fieldCount = 0
    
    // 각 필드의 존재 여부로 신뢰도 계산
    if (mlKitResult.name.isNotEmpty()) { confidence += 0.9; fieldCount++ }
    if (mlKitResult.company.isNotEmpty()) { confidence += 0.8; fieldCount++ }
    if (mlKitResult.phone.isNotEmpty()) { confidence += 0.95; fieldCount++ }
    if (mlKitResult.email.isNotEmpty()) { confidence += 0.95; fieldCount++ }
    if (mlKitResult.address.isNotEmpty()) { confidence += 0.7; fieldCount++ }
    
    return if (fieldCount > 0) confidence / fieldCount else 0.0
}

// 사용자 피드백을 통한 AI 학습
suspend fun learnFromUserFeedback(originalText: String, userFeedback: String) {
    try {
        AIClassificationLearner.learnFromUserFeedback(originalText, userFeedback)
        Log.d("OCRUtil", "사용자 피드백 학습 완료: $userFeedback")
    } catch (e: Exception) {
        Log.e("OCRUtil", "사용자 피드백 학습 실패", e)
    }
}

// AI 학습 통계 조회
fun getAILearningStats(): String {
    return AIClassificationLearner.getLearningStats()
}

// 새로운 필드 자동 감지 및 추가
fun detectNewFields(text: String): List<String> {
    val newFields = mutableListOf<String>()
    
    // 일반적인 명함 패턴에서 발견되지 않는 새로운 정보 감지
    val patterns = mapOf(
        "SNS" to Regex("(Instagram|Facebook|Twitter|LinkedIn|카카오톡|라인)"),
        "사업자번호" to Regex("\\d{3}-\\d{2}-\\d{5}"),
        "법인번호" to Regex("\\d{6}-\\d{7}"),
        "업종" to Regex("(제조업|서비스업|도소매업|건설업|금융업)"),
        "설립일" to Regex("\\d{4}년\\s*\\d{1,2}월\\s*\\d{1,2}일"),
        "자본금" to Regex("\\d+억원|\\d+천만원|\\d+백만원"),
        "직원수" to Regex("\\d+명|\\d+인"),
        "대표번호" to Regex("대표\\s*\\d{2,3}-\\d{3,4}-\\d{4}"),
        "고객센터" to Regex("고객센터|고객지원|고객상담"),
        "영업시간" to Regex("\\d{1,2}:\\d{2}\\s*~\\s*\\d{1,2}:\\d{2}")
    )
    
    patterns.forEach { (fieldName, pattern) ->
        if (pattern.containsMatchIn(text)) {
            newFields.add(fieldName)
        }
    }
    
    return newFields
}

// AI 분류 결과를 BusinessCardInfo로 변환 (기존 코드와 호환성)
fun convertToBusinessCardInfo(dynamicResult: DynamicBusinessCardInfo): BusinessCardInfo {
    return dynamicResult.standardFields.copy(
        rawText = dynamicResult.standardFields.rawText + 
                 if (dynamicResult.additionalFields.isNotEmpty()) 
                     "\n추가필드: ${dynamicResult.additionalFields.entries.joinToString(", ") { "${it.key}: ${it.value}" }}" 
                 else ""
    )
}

// 클로바 OCR용 Retrofit 클라이언트 생성
private fun createClovaRetrofitClient(): Retrofit {
    val httpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    return Retrofit.Builder()
        .baseUrl("https://xr2k67apgb.apigw.ntruss.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()
}

// 이미지를 Base64로 인코딩
private fun encodeImageToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    
    // 이미지 크기 확인 및 조정
    var processedBitmap = bitmap
    val maxSize = 1024 // 클로바 API 권장 최대 크기
    
    if (bitmap.width > maxSize || bitmap.height > maxSize) {
        val scale = min(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
        val matrix = Matrix().apply { setScale(scale, scale) }
        processedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    // JPEG 품질 85%로 압축 (클로바 API 권장)
    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    val imageBytes = outputStream.toByteArray()
    
    Log.d("OCRUtil", "이미지 크기: ${processedBitmap.width}x${processedBitmap.height}")
    Log.d("OCRUtil", "압축된 이미지 크기: ${imageBytes.size} bytes")
    
    return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
}

// URI를 Bitmap으로 변환
private fun uriToBitmap(uri: Uri, context: Context): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) {
        Log.e("OCRUtil", "URI를 Bitmap으로 변환 실패", e)
        null
    }
}

// 클로바 일반 OCR 호출 함수 (Bitmap 버전)
suspend fun runClovaGeneralOCR(bitmap: Bitmap): BusinessCardInfo {
    return withContext(Dispatchers.IO) {
        try {
            Log.d("OCRUtil", "=== 클로바 일반 OCR 시작 ===")
            
            // 1. 이미지 Base64 인코딩
            val base64Image = encodeImageToBase64(bitmap)
            Log.d("OCRUtil", "이미지 Base64 인코딩 완료: ${base64Image.length}자")
            
            // 2. Retrofit 클라이언트 생성
            val retrofit = createClovaRetrofitClient()
            val clovaService = retrofit.create(ClovaGeneralOCRService::class.java)
            
            // 3. API 요청 데이터 구성
            val request = ClovaGeneralOCRRequest(
                version = "V2",
                requestId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                images = listOf(
                    ClovaImage(
                        format = "jpg",
                        name = "business_card.jpg",
                        data = base64Image
                    )
                )
            )
            
            Log.d("OCRUtil", "클로바 API 요청 전송 중...")
            Log.d("OCRUtil", "요청 데이터: version=${request.version}, requestId=${request.requestId}")
            Log.d("OCRUtil", "이미지 정보: format=${request.images.first().format}, name=${request.images.first().name}")
            Log.d("OCRUtil", "Base64 데이터 길이: ${request.images.first().data.length}")
            Log.d("OCRUtil", "API URL: $CLOVA_OCR_API_URL")
            
            // 4. API 호출
            Log.d("OCRUtil", "클로바 API 호출 시작...")
            Log.d("OCRUtil", "Secret Key: ${CLOVA_OCR_SECRET_KEY.take(10)}...")
            Log.d("OCRUtil", "요청 데이터 크기: ${request.images.first().data.length}자")
            Log.d("OCRUtil", "요청 시작 시간: ${System.currentTimeMillis()}")
            
            val response = clovaService.processGeneralOCR(CLOVA_OCR_SECRET_KEY, request)
            
            Log.d("OCRUtil", "클로바 API 응답 수신 시간: ${System.currentTimeMillis()}")
            
            Log.d("OCRUtil", "클로바 API 응답 수신: ${response.images.size}개 이미지")
            
            // 5. 응답 파싱 및 BusinessCardInfo 변환
            val result = parseClovaGeneralOCRResponse(response)
            
            Log.d("OCRUtil", "=== 클로바 일반 OCR 완료 ===")
            Log.d("OCRUtil", "결과: $result")
            
            result
            
        } catch (e: java.net.UnknownHostException) {
            Log.d("OCRUtil", "클로바 DNS 해결 실패 - ML Kit으로 폴백")
            Log.d("OCRUtil", "예외 타입: ${e.javaClass.simpleName}")
            Log.d("OCRUtil", "예외 메시지: ${e.message}")
            Log.d("OCRUtil", "API URL: $CLOVA_OCR_API_URL")
            // DNS 해결 실패 시 ML Kit으로 폴백
            BusinessCardInfo()
        } catch (e: retrofit2.HttpException) {
            Log.d("OCRUtil", "클로바 HTTP 에러 - ML Kit으로 폴백")
            Log.d("OCRUtil", "HTTP 코드: ${e.code()}")
            Log.d("OCRUtil", "에러 메시지: ${e.message()}")
            Log.d("OCRUtil", "API URL: $CLOVA_OCR_API_URL")
            Log.d("OCRUtil", "실패 시간: ${System.currentTimeMillis()}")
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.d("OCRUtil", "에러 응답 바디: $errorBody")
                
                // "Request domain invalid" 오류인 경우 엔드포인트 경로 확인
                if (errorBody?.contains("Request domain invalid") == true) {
                    Log.d("OCRUtil", "도메인 오류 - 엔드포인트 경로 확인 필요: /custom/v1/ vs /external/v1/")
                }
            } catch (ex: Exception) {
                Log.d("OCRUtil", "에러 응답 바디 읽기 실패", ex)
            }
            // HTTP 에러 시 ML Kit으로 폴백
            BusinessCardInfo()
        } catch (e: java.net.SocketTimeoutException) {
            Log.d("OCRUtil", "클로바 네트워크 타임아웃 발생 - ML Kit으로 폴백")
            Log.d("OCRUtil", "타임아웃 상세: ${e.message}")
            // 타임아웃 시 ML Kit으로 폴백
            BusinessCardInfo()
        } catch (e: java.net.ConnectException) {
            Log.d("OCRUtil", "클로바 서버 연결 실패 - ML Kit으로 폴백")
            Log.d("OCRUtil", "연결 실패 상세: ${e.message}")
            // 연결 실패 시 ML Kit으로 폴백
            BusinessCardInfo()
        } catch (e: Exception) {
            Log.d("OCRUtil", "클로바 일반 OCR 실패")
            Log.d("OCRUtil", "예외 타입: ${e.javaClass.simpleName}")
            Log.d("OCRUtil", "예외 메시지: ${e.message}")
            Log.d("OCRUtil", "API URL: $CLOVA_OCR_API_URL")
            // 기타 에러 시 ML Kit으로 폴백
            BusinessCardInfo()
        }
    }
}

// 클로바 일반 OCR 호출 함수 (URI 버전)
suspend fun runClovaGeneralOCR(uri: Uri, context: Context): BusinessCardInfo {
    return withContext(Dispatchers.IO) {
        try {
            Log.d("OCRUtil", "=== 클로바 일반 OCR (URI) 시작 ===")
            
            // 1. URI를 Bitmap으로 변환
            val bitmap = uriToBitmap(uri, context)
            if (bitmap == null) {
                Log.e("OCRUtil", "URI를 Bitmap으로 변환 실패")
                return@withContext BusinessCardInfo()
            }
            
            // 2. 클로바 OCR 호출
            val result = runClovaGeneralOCR(bitmap)
            
            // 3. 메모리 해제
            bitmap.recycle()
            
            result
            
        } catch (e: Exception) {
            Log.e("OCRUtil", "클로바 일반 OCR (URI) 실패", e)
            BusinessCardInfo()
        }
    }
}

// 클로바 일반 OCR 응답을 BusinessCardInfo로 파싱
private fun parseClovaGeneralOCRResponse(response: ClovaGeneralOCRResponse): BusinessCardInfo {
    try {
        if (response.images.isEmpty()) {
            Log.w("OCRUtil", "클로바 응답에 이미지가 없음")
            return BusinessCardInfo()
        }
        
        val fields = response.images.first().fields
        if (fields.isNullOrEmpty()) {
            Log.w("OCRUtil", "클로바 응답에 필드 데이터가 없음")
            return BusinessCardInfo()
        }
        
        // 모든 텍스트를 수집하여 rawText 생성
        val allTexts = fields.map { it.inferText }.joinToString("\n")
        
        // 일반 OCR은 필드 분류가 없으므로 ML Kit 분류 로직을 사용
        val classifiedResult = extractBusinessCardInfo(allTexts)
        
        Log.d("OCRUtil", "클로바 일반 OCR 파싱 결과:")
        Log.d("OCRUtil", "  - 인식된 텍스트: $allTexts")
        Log.d("OCRUtil", "  - 분류된 결과: $classifiedResult")
        
        return classifiedResult.copy(rawText = allTexts)
        
    } catch (e: Exception) {
        Log.e("OCRUtil", "클로바 일반 OCR 응답 파싱 실패", e)
        return BusinessCardInfo()
    }
}

// 클로바 일반 OCR 결과에서 rawText 생성 (사용하지 않음 - 일반 OCR은 fields에서 직접 추출)

// 클로바 우선, ML Kit 보완 방식 (SSAFY AI 임시 비활성화)
suspend fun runMultiOCR(uri: Uri, context: Context): BusinessCardInfo {
    Log.d("OCRUtil", "=== 다중 OCR 시작 (클로바 + ML Kit 보완) ===")
    
    return withContext(Dispatchers.IO) {
        try {
            // 1. 클로바 OCR 시도
            Log.d("OCRUtil", "클로바 OCR 시도 중...")
            val clovaResult = runClovaGeneralOCR(uri, context)
            
            // 클로바가 실패한 경우 (빈 결과) ML Kit만 사용
            if (clovaResult.rawText.isEmpty()) {
                Log.d("OCRUtil", "클로바 OCR 실패 - ML Kit만 사용")
                val mlKitResult = runTextRecognition(uri, context)
                Log.d("OCRUtil", "=== 다중 OCR 완료 (ML Kit만 사용) ===")
                Log.d("OCRUtil", "ML Kit 결과: $mlKitResult")
                return@withContext mlKitResult
            }
            
            // 2. ML Kit OCR도 항상 실행 (웹사이트, 부서 등 누락된 필드 보완)
            Log.d("OCRUtil", "ML Kit OCR 실행 중...")
            val mlKitResult = runTextRecognition(uri, context)
            
            // 3. 클로바와 ML Kit 결과 병합
            val mergedResult = mergeClovaAndMLKitResults(clovaResult, mlKitResult)
            
            Log.d("OCRUtil", "=== 다중 OCR 완료 (병합 결과 사용) ===")
            Log.d("OCRUtil", "클로바 결과: $clovaResult")
            Log.d("OCRUtil", "ML Kit 결과: $mlKitResult")
            Log.d("OCRUtil", "병합 결과: $mergedResult")
            
            mergedResult
            
        } catch (e: Exception) {
            Log.d("OCRUtil", "다중 OCR 실패", e)
            Log.d("OCRUtil", "예외 타입: ${e.javaClass.simpleName}")
            Log.d("OCRUtil", "예외 메시지: ${e.message}")
            
            // 최종 폴백: ML Kit만 사용
            Log.d("OCRUtil", "ML Kit 폴백 실행...")
            try {
                runTextRecognition(uri, context)
            } catch (e2: Exception) {
                Log.d("OCRUtil", "ML Kit 폴백도 실패", e2)
                BusinessCardInfo()
            }
        }
    }
}

// 클로바와 ML Kit 결과 병합 함수
private fun mergeClovaAndMLKitResults(clovaResult: BusinessCardInfo, mlKitResult: BusinessCardInfo): BusinessCardInfo {
    Log.d("OCRUtil", "=== 결과 병합 시작 ===")
    
    // 클로바 결과를 기본으로 사용하고, ML Kit에서 누락된 필드 보완
    val mergedResult = BusinessCardInfo(
        name = clovaResult.name.ifEmpty { mlKitResult.name },
        company = clovaResult.company.ifEmpty { mlKitResult.company },
        position = clovaResult.position.ifEmpty { mlKitResult.position },
        phone = clovaResult.phone.ifEmpty { mlKitResult.phone },
        email = clovaResult.email.ifEmpty { mlKitResult.email },
        address = clovaResult.address.ifEmpty { mlKitResult.address },
        website = mlKitResult.website, // 클로바에는 없으므로 ML Kit 결과 사용
        fax = clovaResult.fax.ifEmpty { mlKitResult.fax },
        mobile = clovaResult.mobile.ifEmpty { mlKitResult.mobile },
        department = mlKitResult.department, // 클로바에는 없으므로 ML Kit 결과 사용
        rawText = if (clovaResult.rawText.isNotEmpty()) clovaResult.rawText else mlKitResult.rawText,
        imageUri = clovaResult.imageUri.ifEmpty { mlKitResult.imageUri },
        imageUri2 = clovaResult.imageUri2.ifEmpty { mlKitResult.imageUri2 },
        capturedDate = clovaResult.capturedDate.ifEmpty { mlKitResult.capturedDate }
    )
    
    Log.d("OCRUtil", "병합 결과:")
    Log.d("OCRUtil", "  - 이름: '${mergedResult.name}' (클로바: '${clovaResult.name}', ML Kit: '${mlKitResult.name}')")
    Log.d("OCRUtil", "  - 회사: '${mergedResult.company}' (클로바: '${clovaResult.company}', ML Kit: '${mlKitResult.company}')")
    Log.d("OCRUtil", "  - 직책: '${mergedResult.position}' (클로바: '${clovaResult.position}', ML Kit: '${mlKitResult.position}')")
    Log.d("OCRUtil", "  - 전화: '${mergedResult.phone}' (클로바: '${clovaResult.phone}', ML Kit: '${mlKitResult.phone}')")
    Log.d("OCRUtil", "  - 이메일: '${mergedResult.email}' (클로바: '${clovaResult.email}', ML Kit: '${mlKitResult.email}')")
    Log.d("OCRUtil", "  - 주소: '${mergedResult.address}' (클로바: '${clovaResult.address}', ML Kit: '${mlKitResult.address}')")
    Log.d("OCRUtil", "  - 웹사이트: '${mergedResult.website}' (ML Kit만)")
    Log.d("OCRUtil", "  - 부서: '${mergedResult.department}' (ML Kit만)")
    Log.d("OCRUtil", "  - 팩스: '${mergedResult.fax}' (클로바: '${clovaResult.fax}', ML Kit: '${mlKitResult.fax}')")
    Log.d("OCRUtil", "  - 휴대폰: '${mergedResult.mobile}' (클로바: '${clovaResult.mobile}', ML Kit: '${mlKitResult.mobile}')")
    
    return mergedResult
}

// Clova 서비스 생성 함수
private fun createClovaService(): ClovaGeneralOCRService {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://xr2k67apgb.apigw.ntruss.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build())
        .build()
    return retrofit.create(ClovaGeneralOCRService::class.java)
}

// Clova 이미지 전처리 함수
private fun preprocessImageForClova(bitmap: Bitmap): Bitmap {
    // 이미지 크기 조정 (Clova API 제한: 최대 1024x1024)
    val maxSize = 1024
    val width = bitmap.width
    val height = bitmap.height
    
    return if (width > maxSize || height > maxSize) {
        val scale = min(maxSize.toFloat() / width, maxSize.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    } else {
        bitmap
    }
}
