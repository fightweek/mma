package my.mma.bet.dto;

import my.mma.bet.entity.Bet;
import my.mma.bet.entity.BetCard;
import my.mma.bet.entity.BetPrediction;
import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FighterFightEvent;
import my.mma.user.entity.User;

import java.util.List;

public record SingleBetRequest(long eventId, int seedPoint, List<SingleBetCardRequest> singleBetCards) {

    public Bet toEntity(User user, FightEvent fightEvent){
        return Bet.builder()
                .seedPoint(seedPoint)
                .fightEvent(fightEvent)
                .user(user)
                .succeed(null)
                .build();
    }

    public record SingleBetCardRequest(long fighterFightEventId, BetPrediction betPrediction) {
        public BetCard toEntity(FighterFightEvent ffe, Bet bet){
            BetCard betCard = BetCard.builder()
                    .fighterFightEvent(ffe)
                    .betPrediction(betPrediction)
                    .bet(bet)
                    .succeed(null)
                    .build();
            if(betPrediction.isDraw()){
                betCard.getBetPrediction().setMyWinnerName(ffe.getWinner().getName());
                betCard.getBetPrediction().setMyLoserName(ffe.getLoser().getName());
                betCard.getBetPrediction().setWinMethod(null);
                betCard.getBetPrediction().setWinRound(null);
            }
            return betCard;
        }
    }

}
