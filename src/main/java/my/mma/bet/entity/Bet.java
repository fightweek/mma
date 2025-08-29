package my.mma.bet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import my.mma.fighter.entity.BaseEntity;
import my.mma.user.entity.User;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class Bet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "bet_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Boolean succeed;

    @Builder.Default
    @OneToMany(mappedBy = "bet",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<BetCard> betCards = new ArrayList<>();

    public void addBetCard(BetCard betCard){
        betCard.addBet(this);
        this.betCards.add(betCard);
    }

    public void updateSucceed(boolean succeed){
        this.succeed = succeed;
    }

}
