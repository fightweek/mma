package my.mma.admin.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.fightevent.dto.StreamFightEventDto;
import my.mma.fightevent.entity.FightEvent;
import my.mma.fightevent.repository.FightEventRepository;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.global.s3.service.S3ImgService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static my.mma.global.redis.prefix.RedisKeyPrefix.BET_PREFIX;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminSaveStreamFightEventService {

    private final FightEventRepository fightEventRepository;
    private final RedisUtils<StreamFightEventDto> redisUtils;
    private final S3ImgService s3Service;

    @Transactional
    public void saveStreamFightEvent() {
        FightEvent fightEvent = fightEventRepository.findFirstByCompletedIsFalseOrderByEventDateAsc();
        System.out.println("fightEvent = " + fightEvent);
        StreamFightEventDto streamFightEvent = StreamFightEventDto.toDto(fightEvent);
        streamFightEvent.getFighterFightEvents().forEach(
                ffe -> {
                    ffe.setWinnerVoteRate(0);
                    ffe.setLoserVoteRate(0);
                    ffe.getWinner().setHeadshotUrl(s3Service.generateImgUrl(
                            "headshot/" + ffe.getWinner().getName().replace(' ', '-') + ".png", 168)
                    );
                    ffe.getLoser().setHeadshotUrl(s3Service.generateImgUrl(
                            "headshot/" + ffe.getLoser().getName().replace(' ', '-') + ".png", 168)
                    );
                    ffe.getWinner().setBodyUrl(s3Service.generateImgUrl(
                            "body/" + ffe.getWinner().getName().replace(' ', '-') + ".png", 168)
                    );
                    ffe.getLoser().setBodyUrl(s3Service.generateImgUrl(
                            "body/" + ffe.getLoser().getName().replace(' ', '-') + ".png", 168)
                    );
                }
        );
        redisUtils.saveData("current-event", streamFightEvent);
        redisUtils.deleteByPrefix(BET_PREFIX.getPrefix());
    }

}
