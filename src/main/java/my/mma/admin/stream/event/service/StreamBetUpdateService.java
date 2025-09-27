package my.mma.admin.stream.event.service;

import lombok.RequiredArgsConstructor;
import my.mma.event.dto.StreamFightEventDto.StreamFighterFightEventDto;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.bet.dto.BetResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

import static my.mma.global.redis.prefix.RedisKeyPrefix.*;

@Service
@RequiredArgsConstructor
public class StreamBetUpdateService {

    private final RedisUtils<BetResponse> todayBetRedisUtils;
    private final StreamBetUpdateFromUserIdService singleBetUpdateService;

    @Async("betExecutor") // 별도의 스레드풀
    public void updateBetData(StreamFighterFightEventDto redisCard) {
        Map<String, BetResponse> todayBets = todayBetRedisUtils.getAllWithKeyFromPrefix(BET_PREFIX.getPrefix());
        if (!todayBets.isEmpty())
            todayBets.forEach((betPrefixWithUserId, userBet) -> {
                singleBetUpdateService.updateUserBetData(redisCard, betPrefixWithUserId, userBet);
            });
    }

}
