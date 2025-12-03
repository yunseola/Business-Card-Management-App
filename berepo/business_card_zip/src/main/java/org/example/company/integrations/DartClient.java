package org.example.company.integrations;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class DartClient {
    @Value("${dart.api.key}")
    private String apiKey;
    private final RestTemplate rt;

    @PostConstruct
    void checkKey() {
        log.info("[DART] apiKey loaded? {}",
                (apiKey != null && !apiKey.isBlank()) ? "YES(len="+apiKey.length()+")" : "NO");
    }

    public JsonNode getCompanyByCorpCode(String corpCode) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://opendart.fss.or.kr/api/company.json")
                .queryParam("crtfc_key", apiKey)
                .queryParam("corp_code", corpCode)
                .toUriString();

        var resp = rt.getForEntity(url, JsonNode.class);
        JsonNode body = resp.getBody();
        log.info("[DART company] corpCode={} http={} status={} message={} corp_name='{}' stock_code={} hm_url={} ir_url={}",
                corpCode,
                resp.getStatusCodeValue(),
                body != null ? body.path("status").asText("") : "",
                body != null ? body.path("message").asText("") : "",
                body != null ? body.path("corp_name").asText("") : "",
                body != null ? body.path("stock_code").asText("") : "",
                body != null ? body.path("hm_url").asText("") : "",
                body != null ? body.path("ir_url").asText("") : "");
        return body;
    }
}
