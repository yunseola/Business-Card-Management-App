package org.example.common.util;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DomainUtils {
    private DomainUtils() {}

    /** 이메일에서 도메인 부분만 추출 */
    public static String emailDomain(String email) {
        if (email == null) return "";
        String s = email.trim();
        int at = s.lastIndexOf('@');
        if (at < 0 || at == s.length() - 1) return "";
        String host = s.substring(at + 1).trim().toLowerCase();
        if (host.startsWith("www.")) host = host.substring(4);
        // 공백/괄호 등 제거
        host = host.replaceAll("[>)]$", "").replaceAll("^[<(]", "");
        return host;
    }

    /** URL 혹은 호스트 문자열에서 최상위 호스트를 추출 (스킴 없어도 동작) */
    public static String extractDomain(String url) {
        if (url == null) return "";
        String s = url.trim();
        if (s.isBlank()) return "";

        // 스킴이 없으면 붙여서 파싱 시도
        if (!s.startsWith("http://") && !s.startsWith("https://")) {
            s = "http://" + s;
        }
        try {
            URI u = new URI(s);
            String host = u.getHost();
            if (host != null && !host.isBlank()) {
                host = host.toLowerCase();
                if (host.startsWith("www.")) host = host.substring(4);
                return host;
            }
        } catch (Exception ignored) {}

        // URI 파싱이 실패하면 정규식 폴백
        Matcher m = Pattern
                .compile("([a-z0-9-]+\\.)+[a-z]{2,}", Pattern.CASE_INSENSITIVE)
                .matcher(s.toLowerCase());
        if (m.find()) {
            String host = m.group();
            if (host.startsWith("www.")) host = host.substring(4);
            return host;
        }
        return "";
    }
}
