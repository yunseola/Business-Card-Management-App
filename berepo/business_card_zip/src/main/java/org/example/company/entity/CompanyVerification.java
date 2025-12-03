package org.example.company.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.digitalcard.entity.DigitalCard;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_verifications",
        uniqueConstraints = @UniqueConstraint(columnNames = "card_id"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompanyVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "card_id", nullable = false, unique = true)
    private Integer cardId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DigitalCard digitalCard;

    @Column(length = 255, nullable = false)
    private String email;

    @Column(length = 6, nullable = false)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
