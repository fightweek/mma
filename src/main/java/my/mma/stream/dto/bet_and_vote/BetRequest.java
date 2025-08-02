package my.mma.stream.dto.bet_and_vote;

import lombok.*;
import my.mma.bet.entity.Bet;
import my.mma.bet.entity.BetCard;
import my.mma.bet.entity.BetPrediction;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.entity.property.WinMethod;
import my.mma.user.entity.User;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BetRequest {

    private List<SingleBetRequest> singleBets;

    public Bet toEntity(User user){
        return Bet.builder()
                .user(user)
                .build();
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SingleBetRequest{
        private long fighterFightEventId;
        private long winnerId;
        private long loserId;
        private int seedPoint;
        private WinMethod winMethod; // nullable
        private Integer winRound; // nullable

        public BetCard toEntity(FighterFightEvent fighterFightEvent, Bet bet){
            return BetCard.builder()
                    .fighterFightEvent(fighterFightEvent)
                    .prediction(BetPrediction.builder()
                            .winnerId(winnerId)
                            .loserId(loserId)
                            .winMethod(winMethod)
                            .winRound(winRound)
                            .build())
                    .seedPoint(seedPoint)
                    .bet(bet)
                    .build();
        }

    }

}
