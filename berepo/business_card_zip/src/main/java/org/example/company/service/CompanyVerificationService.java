package org.example.company.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.company.dto.CompanyRequest;
import org.example.company.dto.CompanyVerifyRequest;
import org.example.company.entity.CompanyVerification;
import org.example.company.repository.CompanyVerificationRepository;
import org.example.digitalcard.entity.DigitalCard;
import org.example.digitalcard.repository.DigitalCardRepository;
import org.example.common.util.DomainUtils;
import org.example.mycard.repository.CompanyHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.InternetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyVerificationService {

    private final DigitalCardRepository cardRepo;
    private final CompanyVerificationRepository verifRepo;
    private final CompanyDomainService companyDomainService;
    private final JavaMailSender mailSender;
    private final CompanyHistoryRepository historyRepo;

    @Value("${app.mail.from:${APP_MAIL_FROM:no-reply@your-domain.com}}")
    private String mailFrom;

    @Value("${app.mail.fromName:${APP_MAIL_FROM_NAME:BusinessCard.zip}}")
    private String mailFromName;

    private static final int CODE_TTL_MINUTES = 5;

    public static final int MATCH_OK = 1;
    public static final int MATCH_NOT = 2;
    public static final int VERIFY_OK = 1;
    public static final int VERIFY_FAIL = 2;

    @Transactional
    public int requestCode(Integer cardId, CompanyRequest dto) {

        DigitalCard card = cardRepo.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("명함이 존재하지 않습니다."));

        String emailDomain = DomainUtils.emailDomain(dto.getEmail());
        String emailBase = CompanyDomainService.toBase(emailDomain);

        String officialBase = companyDomainService.findDomainByCompanyName(card.getCompany(), emailBase);

        log.info("[REQ] company='{}' email='{}' emailBase='{}' officialBase='{}'",
                card.getCompany(), dto.getEmail(), emailBase, officialBase);

        if (officialBase == null || officialBase.isBlank()) {
            log.info("[REQ] official domain not found. treat as NOT_MATCH");
            return MATCH_NOT;
        }
        if (!CompanyDomainService.looseMatch(officialBase, emailBase)) {
            log.info("[REQ] company email NOT matched. official='{}' emailBase='{}'", officialBase, emailBase);
            return MATCH_NOT;
        }

        String code = String.format("%06d", new Random().nextInt(1_000_000));
        LocalDateTime expires = LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES);

        CompanyVerification verif = verifRepo.findByCardId(cardId)
                .map(existing -> {
                    existing.setEmail(dto.getEmail());
                    existing.setCode(code);
                    existing.setExpiresAt(expires);
                    return existing;
                })
                .orElseGet(() -> CompanyVerification.builder()
                        .cardId(cardId)
                        .email(dto.getEmail())
                        .code(code)
                        .expiresAt(expires)
                        .build());

        verifRepo.save(verif);

        // --- 메일 발송: 표시이름(명함.zip) 넣어서 From 설정 ---
//        try {
//            var mime = mailSender.createMimeMessage();
//            var helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
//            helper.setTo(dto.getEmail());
//            helper.setFrom(new InternetAddress(mailFrom, mailFromName, StandardCharsets.UTF_8.name()));
//            helper.setSubject("회사 인증 코드 안내");
//            helper.setText("인증 코드: " + code + " (유효 " + CODE_TTL_MINUTES + "분)", false);
//            mailSender.send(mime);
//        } catch (Exception e) {
//            log.error("[REQ] mail send failed: {}", e.getMessage(), e);
//        }
//
//        return MATCH_OK;
//    }
        try {
            var mime = mailSender.createMimeMessage();
            // 대체본문(plain + html)을 위해 multipart 모드 사용
            var helper = new MimeMessageHelper(
                    mime,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(dto.getEmail());
            helper.setFrom(new InternetAddress(mailFrom, mailFromName, StandardCharsets.UTF_8.name()));
            helper.setSubject("회사 인증 코드 안내");

            // 텍스트 대체(일부 클라이언트용)
            String plain = "인증 코드: " + code + " (유효 " + CODE_TTL_MINUTES + "분)";

            // HTML 본문
            String html = buildVerificationHtml(code, CODE_TTL_MINUTES, mailFromName);

            // 두 가지 버전 동시 설정: HTML을 기본으로, 텍스트는 폴백
            helper.setText(plain, html);

            mailSender.send(mime);
        } catch (Exception e) {
            log.error("[REQ] mail send failed: {}", e.getMessage(), e);
        }
        return MATCH_OK;
    }

    private String buildVerificationHtml(String code, int ttlMin, String brandName) {
        String primary = "#4C3924"; // 포인트 색상
        String fg = "#111827";      // 본문 글자
        String sub = "#6b7280";     // 보조 글자
        String border = "#eceff4";  // 테두리/구분선
        String bg = "#f6f8fb";      // 배경

        return """
        <!doctype html>
        <html lang="ko">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width,initial-scale=1">
          <meta name="color-scheme" content="light only">
          <title>회사 인증 코드 안내</title>
        </head>
        <body style="margin:0;padding:0;background:%5$s;">
          <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:%5$s;">
            <tr>
              <td align="center" style="padding:32px 16px;">
                <table role="presentation" width="600" cellpadding="0" cellspacing="0"
                       style="max-width:600px;background:#ffffff;border:1px solid %4$s;border-radius:12px;">
                  <tr>
                    <td style="padding:28px 28px 18px 28px;font-family: -apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Noto Sans KR',AppleSDGothicNeo,Malgun Gothic,Helvetica,Arial,sans-serif;">
                      <div style="display:flex;align-items:center;gap:12px;margin-bottom:8px;">
                        <div style="width:36px;height:36px;border-radius:50%%;background:%1$s;display:inline-block;"></div>
                        <div style="font-size:18px;font-weight:700;color:%2$s;">%3$s</div>
                      </div>

                      <h1 style="margin:10px 0 8px 0;font-size:22px;line-height:1.3;color:%2$s;">회사 인증 코드 안내</h1>
                      <p style="margin:0 0 18px 0;font-size:14px;color:%6$s;">
                        아래 인증 코드를 입력해 회사 이메일을 인증해 주세요.
                      </p>

                      <div style="text-align:center;margin:18px 0 6px 0;">
                        <div style="display:inline-block;padding:14px 18px;border:1px dashed %4$s;border-radius:10px;">
                          <div style="font-size:40px;line-height:1.1;letter-spacing:8px;font-weight:800;color:%2$s;">
                            %7$s
                          </div>
                        </div>
                      </div>

                      <p style="text-align:center;margin:8px 0 18px 0;font-size:12px;color:%6$s;">
                        이 코드는 발송 시점부터 %8$d분 동안 유효합니다.
                      </p>

                      <hr style="border:none;height:1px;background:%4$s;margin:20px 0 6px 0;">
                      <p style="margin:0 0 2px 0;font-size:12px;color:%6$s;">
                        요청하지 않은 메일이라면 무시하셔도 됩니다.
                      </p>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:0 28px 26px 28px;text-align:center;color:#9ca3af;font-size:12px;
                               font-family: -apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Noto Sans KR',AppleSDGothicNeo,Malgun Gothic,Helvetica,Arial,sans-serif;">
                      © %9$d %3$s. All rights reserved.
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """.formatted(
                primary,         // %1$s
                fg,              // %2$s
                brandName,       // %3$s
                border,          // %4$s
                bg,              // %5$s
                sub,             // %6$s
                code,            // %7$s
                ttlMin,          // %8$d
                java.time.Year.now().getValue() // %9$d
        );
    }

    @Transactional
    public int verifyCode(Integer cardId, CompanyVerifyRequest dto) {

        CompanyVerification verification = verifRepo.findByCardId(cardId).orElse(null);
        if (verification == null) {
            log.info("[VERIFY] no request. cardId={}", cardId);
            return VERIFY_FAIL;
        }

        if (!verification.getEmail().equalsIgnoreCase(dto.getEmail())) {
            log.info("[VERIFY] email mismatch. requested='{}' stored='{}'", dto.getEmail(), verification.getEmail());
            return VERIFY_FAIL;
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("[VERIFY] code expired. cardId={}", cardId);
            verifRepo.delete(verification);
            return VERIFY_FAIL;
        }

        if (!verification.getCode().equals(dto.getCode())) {
            log.info("[VERIFY] code not matched. cardId={}", cardId);
            return VERIFY_FAIL;
        }

        DigitalCard card = cardRepo.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("명함이 존재하지 않습니다."));
        card.confirm();
        cardRepo.save(card);

        verifRepo.delete(verification);

        int updated = historyRepo.markConfirmedByCardId(cardId, LocalDateTime.now());

        log.info("[VERIFY] success. cardId={} confirmed=true, historyUpdated={}", cardId, updated);
        return VERIFY_OK;
    }
}
