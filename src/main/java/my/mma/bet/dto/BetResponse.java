package my.mma.stream.dto.bet_and_vote;

import lombok.*;
import my.mma.bet.entity.Bet;
import my.mma.bet.entity.BetCard;
import my.mma.bet.entity.BetPrediction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BetResponse {

    private String eventName;

    @Builder.Default
    private List<SingleBetResponse> singleBets = new ArrayList<>();

    public void addBetDto(SingleBetResponse betDto){
        this.singleBets.add(betDto);
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SingleBetResponse {

        private long betId;

        private List<SingleBetCardResponse> betCards;

        private Boolean succeed;

        private LocalDateTime createdDateTime;

        private int seedPoint;

        public static SingleBetResponse toDto(Bet bet){
            return SingleBetResponse.builder()
                    .betId(bet.getId())
                    .succeed(bet.getSucceed())
                    .createdDateTime(bet.getCreatedDateTime())
                    .betCards(bet.getBetCards().stream().map(
                            SingleBetCardResponse::toDto
                    ).toList())
                    .seedPoint(bet.getSeedPoint())
                    .build();
        }

    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SingleBetCardResponse {

        /** redName is ranked higher than blueName
         * 어차피 해당 이벤트 시작 직후에는 betCard 생성이 불가하므로, 각 corner의 name이 경기 끝난 이후에도 switch 될 일은 없음
         */
        private String redName;

        private String blueName;

        private Boolean succeed;

        private BetPrediction betPrediction;

        public static SingleBetCardResponse toDto(BetCard betCard){
            return SingleBetCardResponse.builder()
                    .redName(betCard.getFighterFightEvent().getWinner().getName())
                    .blueName(betCard.getFighterFightEvent().getLoser().getName())
                    .succeed(betCard.getSucceed())
                    .betPrediction(betCard.getBetPrediction())
                    .build();
        }

    }

}
