package org.example.company.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.util.CompanyNameNormalizer;
import org.example.company.integrations.CorpCodeCache;
import org.example.company.integrations.DartClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyDomainService {

    private final CorpCodeCache corpCodeCache;
    private final DartClient dartClient;

    @Value("#{${app.company.domain-overrides:{}}}")
    private Map<String, String> overridesRaw;
    private Map<String, String> overridesNormalized;

    @PostConstruct
    void buildOverrides() {
        overridesNormalized = new HashMap<>();
        if (overridesRaw != null) {
            overridesRaw.forEach((k, v) ->
                    overridesNormalized.put(CompanyNameNormalizer.normalize(k), v)
            );
        }
    }

    /** 호스트/URL → 베이스 도메인(eTLD+1) */
    public static String toBase(String urlOrHost) {
        if (urlOrHost == null || urlOrHost.isBlank()) return "";
        String s = urlOrHost.trim();
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "https://" + s;
        try {
            String host = java.net.URI.create(s).getHost();
            if (host == null || host.isBlank()) return "";
            String h = host.toLowerCase(Locale.ROOT);
            String[] p = h.split("\\.");
            if (h.endsWith(".co.kr") || h.endsWith(".or.kr") || h.endsWith(".go.kr") || h.endsWith(".ac.kr")) {
                if (p.length >= 3) return p[p.length - 3] + "." + p[p.length - 2] + "." + p[p.length - 1];
            }
            if (p.length >= 2) return p[p.length - 2] + "." + p[p.length - 1];
            return h;
        } catch (Exception e) {
            String u = urlOrHost.toLowerCase(Locale.ROOT).replaceFirst("^https?://", "");
            int slash = u.indexOf('/');
            if (slash > 0) u = u.substring(0, slash);
            String[] p = u.split("\\.");
            if (u.endsWith(".co.kr") || u.endsWith(".or.kr") || u.endsWith(".go.kr") || u.endsWith(".ac.kr")) {
                if (p.length >= 3) return p[p.length - 3] + "." + p[p.length - 2] + "." + p[p.length - 1];
            }
            if (p.length >= 2) return p[p.length - 2] + "." + p[p.length - 1];
            return u;
        }
    }

    private static String sld(String base) {
        if (base == null || base.isBlank()) return "";
        String b = base.toLowerCase(Locale.ROOT);
        String[] p = b.split("\\.");
        if (b.endsWith(".co.kr") || b.endsWith(".or.kr") || b.endsWith(".go.kr") || b.endsWith(".ac.kr")) {
            if (p.length >= 3) return p[p.length - 3];
        }
        if (p.length >= 2) return p[p.length - 2];
        return p[0];
    }

    private static String tld(String base) {
        if (base == null || base.isBlank()) return "";
        String b = base.toLowerCase(Locale.ROOT);
        if (b.endsWith(".co.kr") || b.endsWith(".or.kr") || b.endsWith(".go.kr") || b.endsWith(".ac.kr")) return "kr";
        String[] p = b.split("\\.");
        return p.length >= 2 ? p[p.length - 1] : "";
    }

    /** 느슨한 매칭: TLD 동일 && SLD 공통 prefix 길이 ≥ 5 (또는 완전 동일) */
    public static boolean looseMatch(String a, String b) {
        String A = toBase(a), B = toBase(b);
        if (A.isBlank() || B.isBlank()) return false;
        if (A.equalsIgnoreCase(B)) return true;
        if (!tld(A).equalsIgnoreCase(tld(B))) return false;
        String sa = sld(A), sb = sld(B);
        if (sa.isBlank() || sb.isBlank()) return false;
        int common = 0;
        for (int i = 0; i < Math.min(sa.length(), sb.length()); i++) {
            if (sa.charAt(i) != sb.charAt(i)) break;
            common++;
        }
        return common >= 5;
    }

    /** 기존 시그니처 유지(선호 도메인 없이) */
    public String findDomainByCompanyName(String companyName) {
        return findDomainByCompanyName(companyName, "");
    }

    /**
     * 회사명으로 공식 도메인 찾기 (오버라이드 → DART 후보들 순회)
     * @param companyName   명함에 적힌 회사명
     * @param preferDomain  비교/선택에 참고할 선호 도메인(예: 이메일 도메인)
     * @return 베이스 도메인(eTLD+1), 없으면 ""
     */
    public String findDomainByCompanyName(String companyName, String preferDomain) {
        if (companyName == null || companyName.isBlank()) return "";

        // 1) 오버라이드 우선
        String key = CompanyNameNormalizer.normalize(companyName);
        String ov = overridesNormalized != null ? overridesNormalized.get(key) : null;
        if (ov != null && !ov.isBlank()) {
            return toBase(ov);
        }

        // 2) DART 후보 조회
        List<String> corpCodes = new ArrayList<>();
        try {
            Method m = corpCodeCache.getClass().getMethod("findCorpCodes", String.class, int.class);
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) m.invoke(corpCodeCache, companyName, 20);
            if (list != null) corpCodes.addAll(list);
        } catch (NoSuchMethodException nsme) {
            String one = corpCodeCache.findCorpCode(companyName);
            if (one != null) corpCodes.add(one);
        } catch (Exception e) {
            log.warn("findCorpCodes reflection failed: {}", e.getMessage());
            String one = corpCodeCache.findCorpCode(companyName);
            if (one != null) corpCodes.add(one);
        }

        corpCodes = corpCodes.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());

        String preferBase = toBase(preferDomain);
        String firstNonEmpty = "";

        for (String corpCode : corpCodes) {
            JsonNode body = dartClient.getCompanyByCorpCode(corpCode);
            String status = body != null ? body.path("status").asText("") : "";
            String corpName = body != null ? body.path("corp_name").asText("") : "";
            String hmUrl = body != null ? body.path("hm_url").asText("") : "";
            String irUrl = body != null ? body.path("ir_url").asText("") : "";
            String pick = !hmUrl.isBlank() ? hmUrl : irUrl;
            String pickBase = toBase(pick);

            log.info("[Domain] try corpCode={} corpName='{}' status={} hm='{}' ir='{}' => pick='{}' (base='{}')",
                    corpCode, corpName, status, hmUrl, irUrl, pick, pickBase);

            if (!"000".equals(status) || pickBase.isBlank()) continue;

            if (firstNonEmpty.isBlank()) firstNonEmpty = pickBase;

            // 선호 도메인과 느슨 매칭되면 바로 반환
            if (!preferBase.isBlank() && looseMatch(pickBase, preferBase)) {
                return pickBase;
            }
        }

        // 3) 폴백: DART/오버라이드 모두 실패 → emailBase의 SLD == 회사명(정규화)면 emailBase 채택
        if (!preferBase.isBlank()) {
            String label = sld(preferBase); // 예: naver.com -> naver
            String labelNorm = CompanyNameNormalizer.normalize(label);
            String companyNorm = CompanyNameNormalizer.normalize(companyName);
            if (!labelNorm.isBlank() && labelNorm.equals(companyNorm)) {
                return preferBase;
            }
        }

        return firstNonEmpty; // 그래도 없으면 첫 비어있지 않은 후보(있다면)
    }
}
