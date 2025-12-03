package org.example.memo.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.digitalcard.entity.DigitalCard;
import org.example.memo.dto.MemoUpdateRequest;
import org.example.oauth.entity.User;
import org.example.papercard.entity.PaperCard;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "memos",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "digital_cards_id"}),
                @UniqueConstraint(columnNames = {"user_id", "paper_cards_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DigitalCard digitalCard;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PaperCard paperCard;

    @Column(columnDefinition = "TEXT")
    private String relationship;

    @Column(columnDefinition = "TEXT")
    private String personality;

    @Column(columnDefinition = "TEXT")
    private String workStyle;

    @Column(columnDefinition = "TEXT")
    private String meetingNotes;

    @Column(columnDefinition = "TEXT")
    private String etc;

    @Column(columnDefinition = "TEXT")
    private String summary;

    public void updateMemo(MemoUpdateRequest request) {
        this.relationship = request.getRelationship();
        this.personality = request.getPersonality();
        this.workStyle = request.getWorkStyle();
        this.meetingNotes = request.getMeetingNotes();
        this.etc = request.getEtc();
        this.summary = request.getSummary();
    }

    public void linkToDigitalCard(DigitalCard digitalCard) {
        this.paperCard = null;
        this.digitalCard = digitalCard;
    }
}
