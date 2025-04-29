package my.mma.event.entity;

import jakarta.persistence.*;
import lombok.*;
import my.mma.fighter.entity.BaseEntity;
import my.mma.user.entity.User;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class FightEventReply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fight_event_reply_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fight_event_id")
    private FightEvent fightEvent;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String content;

}
