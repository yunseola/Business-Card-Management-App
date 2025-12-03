package org.example.group.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.digitalcard.entity.DigitalCard;
import org.example.oauth.entity.User;
import org.example.papercard.entity.PaperCard;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "group_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"group_id", "digital_card_id"}),
                @UniqueConstraint(columnNames = {"group_id", "paper_card_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "digital_card_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DigitalCard digitalCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_card_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PaperCard paperCard;

    public void convertPaperToDigital(DigitalCard target) {
        this.paperCard = null;
        this.digitalCard = target;
    }
}
