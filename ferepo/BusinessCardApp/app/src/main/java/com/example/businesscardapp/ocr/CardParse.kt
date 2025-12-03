package com.example.businesscardapp.ocr

data class ParsedCard(
    val name: String? = null,
    val company: String? = null,
    val phone: String? = null,
    val position: String? = null,
    val department: String? = null,
    val email: String? = null
)

private val phoneRegex = Regex("""(?:(?:\+?82[-\s]?)?0?1[0-9]|0[2-6]\d)(?:[-\s]?\d{3,4}){2}""")
private val emailRegex = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
private val companyHints = listOf("주식회사","(주)","㈜","Co.","Corp","Ltd","유한","홀딩스","그룹","회사")
private val positionHints = listOf("대표","이사","부장","차장","과장","대리","주임","사원","팀장","센터장","실장","연구원")
private val deptHints = listOf("본부","팀","파트","부서","연구소","센터","사업부")

fun parseBusinessCard(lines: List<String>): ParsedCard {
    val clean = lines.map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    val joined = clean.joinToString("\n")

    val phone = phoneRegex.find(joined)?.value
        ?.replace(Regex("""\s+"""), "-")
        ?.replace(Regex("""-+"""), "-")

    val email = emailRegex.find(joined)?.value

    val company = clean.firstOrNull { s ->
        companyHints.any { s.contains(it, ignoreCase = true) }
    } ?: clean.firstOrNull { it.length in 2..20 && it.any { ch -> ch.isLetterOrDigit() } && it.endsWith("회사") }

    val position = clean.firstOrNull { s -> positionHints.any { s.contains(it) } }
    val department = clean.firstOrNull { s -> deptHints.any { s.contains(it) } }

    val topThird = clean.take((clean.size.coerceAtLeast(1) + 2) / 3)
    val name = topThird.firstOrNull { s ->
        s.length in 2..4 &&
                s.all { it.isLetter() } &&
                position?.contains(s) != true &&
                company?.contains(s) != true &&
                email?.contains(s) != true &&
                phone?.contains(s) != true
    }

    return ParsedCard(
        name = name,
        company = company,
        phone = phone,
        position = position,
        department = department,
        email = email
    )
}
