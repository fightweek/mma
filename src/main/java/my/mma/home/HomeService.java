package my.mma.home;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.fightevent.dto.StreamFightEventDto;
import my.mma.global.redis.utils.RedisUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class HomeService {

    private final RedisUtils<StreamFightEventDto> redisUtils;

    public HomeScreenDto home(){
        StreamFightEventDto streamFightEventDto = redisUtils.getData("current-event");
        if(streamFightEventDto == null)
            return null;
        HomeScreenDto response = HomeScreenDto.toDto(streamFightEventDto);
        System.out.println(response.mainCardDateTimeInfo());
        return response;
    }

}
