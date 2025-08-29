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
public class SingleBetRequest {

    private List<SingleBetCardRequest> singleBetCards;

    public Bet toEntity(User user){
        return Bet.builder()
                .user(user)
                .succeed(null)
                .build();
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SingleBetCardRequest {
        private long fighterFightEventId;
        private int seedPoint;
        private BetPrediction betPrediction;

        public BetCard toEntity(FighterFightEvent fighterFightEvent, Bet bet){
            return BetCard.builder()
                    .fighterFightEvent(fighterFightEvent)
                    .betPrediction(betPrediction)
                    .seedPoint(seedPoint)
                    .bet(bet)
                    .succeed(null)
                    .build();
        }

    }

}
