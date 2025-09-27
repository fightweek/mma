package my.mma.admin.stream.event.service;

import lombok.RequiredArgsConstructor;
import my.mma.bet.entity.BetCard;
import my.mma.bet.repository.BetRepository;
import my.mma.event.dto.StreamFightEventDto.StreamFighterFightEventDto;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.bet.dto.BetResponse;
import my.mma.bet.dto.BetResponse.SingleBetCardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StreamBetUpdateFromUserIdService {

    private final RedisUtils<BetResponse> todayBetRedisUtils;
    private final BetRepository betRepository;

    @Transactional
    public void updateUserBetData(StreamFighterFightEventDto redisCard, String betPrefixWithUserId, BetResponse userBet) {
        userBet.getSingleBets().forEach(
                redisSingleBet -> {
                    betRepository.findByIdWithBetCards(redisSingleBet.getBetId()).ifPresent(
                            dbSingleBet -> {
                                String actualWinner = redisCard.getWinner().getName();
                                redisSingleBet.setSucceed(true);
                                List<BetCard> dbBetCards = dbSingleBet.getBetCards();
                                List<SingleBetCardResponse> redisBetCards = redisSingleBet.getBetCards();
                                updateSucceedStatus(dbBetCards,actualWinner);
                                updateSucceedStatus(redisBetCards,actualWinner);

                                redisSingleBet.setSucceed(redisBetCards.stream().allMatch(SingleBetCardResponse::getSucceed));
                                dbSingleBet.updateSucceed(dbBetCards.stream().allMatch(BetCard::getSucceed));
                            }
                    );
                }
        );
        todayBetRedisUtils.saveData(betPrefixWithUserId, userBet);
    }

    private <T> void updateSucceedStatus(List<T> betCards, String actualWinner) {
        for (T card : betCards) {
            String winnerForUser;
            boolean succeed;
            if (card instanceof SingleBetCardResponse) {
                SingleBetCardResponse c = (SingleBetCardResponse) card;
                winnerForUser = c.getBetPrediction().getMyWinnerName();
                succeed = winnerForUser.equals(actualWinner);
                c.setSucceed(succeed);
            } else if (card instanceof BetCard) {
                BetCard c = (BetCard) card;
                winnerForUser = c.getBetPrediction().getMyWinnerName();
                succeed = winnerForUser.equals(actualWinner);
                c.updateSucceed(succeed);
            }
        }
    }
}
