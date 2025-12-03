package org.example.digitalcard.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "digital_card_fields")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DigitalCardField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DigitalCard digitalCard;

    @Column(length = 100, nullable = false)
    private String fieldName;

    @Column(length = 100, nullable = false)
    private String fieldValue;

    @Column(nullable = false)
    private Integer fieldOrder;
}


