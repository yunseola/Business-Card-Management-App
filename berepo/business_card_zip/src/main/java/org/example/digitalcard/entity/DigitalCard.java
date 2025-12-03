package org.example.digitalcard.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.mycard.entity.CompanyHistory;
import org.example.oauth.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "digital_cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DigitalCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String company;

    @Column(length = 50)
    private String position;

    @Column(length = 50)
    private String email;

    @Column(length = 2048)
    private String customImageUrl;

    @Column(name = "image_url_horizontal", length=2048)
    private String imageUrlHorizontal;

    @Column(name = "image_url_vertical" , length=2048)
    private String imageUrlVertical;


    @Column(name = "is_digital", nullable = false)
    @Builder.Default
    private Boolean digital = true;

    @Column(name = "is_confirmed", nullable = false)
    @Builder.Default
    private Boolean confirmed = false;

    private Integer backgroundImageNum;

    private Boolean fontColor;

    @Column(length = 2048)
    private String shareUrl;

    @Column(length = 2048)
    private String qrCodeUrl;

    @Column(unique = true, nullable = false, length = 36)
    private String shareToken;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "digitalCard", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private Set<DigitalCardField> fields = new LinkedHashSet<>();

    @OneToMany(mappedBy = "digitalCard", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt DESC")
    @Builder.Default
    private Set<CompanyHistory> companyHistories = new LinkedHashSet<>();

    public void confirm() {
        this.confirmed = true;
    }

    public void updateInfo(String name, String phone, String company, String position, String email, Integer backgroundImageNum, boolean fontColor, String customImageUrl,
                           String imageUrlHorizontal, String imageUrlVertical) {
        this.name = name;
        this.phone = phone;
        this.company = company;
        this.position = position;
        this.email = email;
        this.backgroundImageNum = backgroundImageNum;
        this.fontColor = fontColor;
        this.customImageUrl = customImageUrl;
        this.imageUrlHorizontal = imageUrlHorizontal;
        this.imageUrlVertical = imageUrlVertical;
    }
}
