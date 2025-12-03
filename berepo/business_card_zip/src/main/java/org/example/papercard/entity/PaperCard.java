package org.example.papercard.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.oauth.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "paper_cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaperCard {

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

    @Column(name = "image1_url", nullable = false, length = 2048)
    private String image1Url;

    @Column(name = "image2_url", length = 2048)
    private String image2Url;

    @Column(name = "is_digital", nullable = false)
    @Builder.Default
    private Boolean digital = false;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private Boolean favorite = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void updateImages(String image1Url, String image2Url) {
        if (image1Url == null || image1Url.isBlank()) {
            throw new IllegalArgumentException("이미지1 URL은 필수입니다.");
        }
        this.image1Url = image1Url;
        this.image2Url = image2Url;
    }

    public void updateInfo(String name, String phone, String company, String position, String email) {
        this.name = name;
        this.phone = phone;
        this.company = company;
        this.position = position;
        this.email = email;
    }

    public void toggleFavorite() {
        this.favorite = !favorite;
    }
}
