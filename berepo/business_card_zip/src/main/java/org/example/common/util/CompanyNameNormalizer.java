package org.example.common.util;

import java.util.regex.Pattern;

/** 회사명 정규화: 공백/특수문자 제거 + 법인표기 접두/접미 제거 */
public final class CompanyNameNormalizer {
    private CompanyNameNormalizer() {}

    // 접미: ...주식회사, ...㈜, ...유한회사 등
    private static final Pattern LEGAL_SUFFIX =
            Pattern.compile("(주식회사|\\(주\\)|㈜|유한회사|유한|합자회사)$");
    // 접두: 주식회사..., ㈜..., (주)... 등
    private static final Pattern LEGAL_PREFIX =
            Pattern.compile("^(주식회사|\\(주\\)|㈜|유한회사|유한|합자회사)");

    public static String normalize(String name) {
        if (name == null) return "";
        String s = name.trim();

        // 공백 제거
        s = s.replaceAll("\\s+", "");
        // 특수문자 제거
        s = s.replaceAll("[^가-힣a-zA-Z0-9]", "");

        // 접두/접미 법인 표기 제거
        s = LEGAL_PREFIX.matcher(s).replaceAll("");
        s = LEGAL_SUFFIX.matcher(s).replaceAll("");

        return s;
    }
}
