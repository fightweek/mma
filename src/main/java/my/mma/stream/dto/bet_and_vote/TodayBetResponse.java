package my.mma.stream.dto.bet_and_vote;

import lombok.*;
import my.mma.bet.entity.Bet;
import my.mma.bet.entity.BetCard;
import my.mma.bet.entity.BetPrediction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TodayBetResponse {

    private List<SingleBetResponse> singleBets = new ArrayList<>();

    public void addBetDto(SingleBetResponse betDto){
        this.getSingleBets().add(betDto);
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

        public static SingleBetResponse toDto(Bet bet){
            return SingleBetResponse.builder()
                    .betId(bet.getId())
                    .succeed(bet.getSucceed())
                    .createdDateTime(bet.getCreatedDateTime())
                    .betCards(bet.getBetCards().stream().map(
                            SingleBetCardResponse::toDto
                    ).toList())
                    .build();
        }

    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SingleBetCardResponse {

        // 내가 선택한 승자
        private Boolean succeed;

        private int seedPoint;

        private BetPrediction betPrediction;

        public static SingleBetCardResponse toDto(BetCard betCard){
            return SingleBetCardResponse.builder()
                    .succeed(betCard.getSucceed())
                    .seedPoint(betCard.getSeedPoint())
                    .betPrediction(betCard.getBetPrediction())
                    .build();
        }

    }

}
