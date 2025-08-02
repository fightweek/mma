package my.mma.stream.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.event.dto.StreamFightEventDto.StreamFighterFightEventDto;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.global.s3.service.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StreamService {

    private final RedisUtils<StreamFightEventDto> redisUtils;

    public StreamFightEventDto getTodayEvent() {
        return redisUtils.getData("current-event");
    }

}
