package my.mma.admin.stream.event.service;

import lombok.RequiredArgsConstructor;
import my.mma.event.dto.StreamFightEventDto.StreamFighterFightEventDto;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.stream.dto.bet_and_vote.TodayBetResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

import static my.mma.global.redis.prefix.RedisKeyPrefix.*;

@Service
@RequiredArgsConstructor
public class StreamBetUpdateService {

    private final RedisUtils<TodayBetResponse> todayBetRedisUtils;
    private final StreamBetUpdateFromUserIdService singleBetUpdateService;

    @Async("betExecutor") // 별도의 스레드풀
    public void updateBetData(StreamFighterFightEventDto redisCard) {
        Map<String, TodayBetResponse> todayBets = todayBetRedisUtils.getAllWithKeyFromPrefix(BET_PREFIX.getPrefix());
        if (!todayBets.isEmpty())
            todayBets.forEach((betPrefixWithUserId, userBet) -> {
                singleBetUpdateService.updateSingleBetData(redisCard, betPrefixWithUserId, userBet);
            });
    }

}
