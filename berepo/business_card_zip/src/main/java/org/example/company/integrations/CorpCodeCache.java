package org.example.company.integrations;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.common.util.CompanyNameNormalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CorpCodeCache {

    @Value("${dart.api.key}")
    private String apiKey;

    private final Map<String, String> nameToCode = new ConcurrentHashMap<>();
    private final Map<String, String> normalizedToCode = new ConcurrentHashMap<>();

    /** 기존 단건 API를 쓰는 코드 호환용 */
    public String findCorpCode(String corpName) {
        return findCorpCodes(corpName, 1).stream().findFirst().orElse(null);
    }

    /** 입력명과 매칭되는 corp_code 후보들을 '브랜드 우선' 정렬로 반환 */
    public List<String> findCorpCodes(String corpName, int limit) {
        if (nameToCode.isEmpty()) {
            try { load(); } catch (Exception e) {
                log.warn("CORPCODE 적재 실패: {}", e.getMessage());
            }
        }
        if (corpName == null || corpName.isBlank()) return List.of();

        String norm = CompanyNameNormalizer.normalize(corpName);

        // 후보 필터: (완전일치 | startsWith | endsWith | contains)
        List<Map.Entry<String,String>> all = normalizedToCode.entrySet().stream()
                .filter(e -> {
                    String k = e.getKey();
                    return k.equals(norm) || k.startsWith(norm) || k.endsWith(norm) || k.contains(norm);
                })
                .collect(Collectors.toList());

        // 랭킹: exact(0) > startsWith(1) > endsWith(2) > contains(3)  → 길이차이 asc
        Comparator<Map.Entry<String,String>> cmp = Comparator
                .comparingInt((Map.Entry<String,String> e) -> {
                    String k = e.getKey();
                    if (k.equals(norm)) return 0;
                    if (k.startsWith(norm)) return 1;
                    if (k.endsWith(norm)) return 2;
                    return 3; // contains
                })
                .thenComparingInt(e -> Math.abs(e.getKey().length() - norm.length()));

        all.sort(cmp);

        // 중복 제거하며 상위 limit만 코드 추출
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        for (var e : all) {
            codes.add(e.getValue());
            if (codes.size() >= Math.max(limit, 1)) break;
        }
        return new ArrayList<>(codes);
    }

    @PostConstruct
    public void load() throws Exception {
        if (!nameToCode.isEmpty()) return;

        Path zipPath = Paths.get("corpCode.zip");
        if (!Files.exists(zipPath)) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key=" + apiKey))
                    .GET().build();
            HttpResponse<byte[]> res = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            Files.write(zipPath, res.body(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!"CORPCODE.xml".equalsIgnoreCase(entry.getName())) continue;

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                zis.transferTo(baos);

                try (InputStream in = new ByteArrayInputStream(baos.toByteArray())) {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder().parse(in);
                    var nodes = doc.getElementsByTagName("list");
                    for (int i = 0; i < nodes.getLength(); i++) {
                        var node = nodes.item(i).getChildNodes();
                        String name = null, code = null;
                        for (int c = 0; c < node.getLength(); c++) {
                            var n = node.item(c);
                            if ("corp_name".equals(n.getNodeName())) name = n.getTextContent();
                            else if ("corp_code".equals(n.getNodeName())) code = n.getTextContent();
                        }
                        if (name != null && code != null) {
                            nameToCode.putIfAbsent(name, code);
                            String norm = CompanyNameNormalizer.normalize(name);
                            normalizedToCode.putIfAbsent(norm, code);
                        }
                    }
                }
                break;
            }
        }
        log.info("CORPCODE 적재 완료: {}건 (정규화 키:{}건)", nameToCode.size(), normalizedToCode.size());
    }
}
