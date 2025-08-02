package my.mma.home;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.global.s3.service.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class HomeService {

    private final RedisUtils<StreamFightEventDto> redisUtils;
    private final S3Service s3Service;

    public HomeScreenDto home(){
        StreamFightEventDto streamFightEventDto = redisUtils.getData("current-event");
        if(streamFightEventDto == null)
            return null;
        HomeScreenDto response = HomeScreenDto.toDto(streamFightEventDto);
        response.setWinnerBodyUrl(s3Service.generateImgUrl(
                "body/" + response.getWinnerName().replace(' ', '-') + ".png"
        ));
        response.setLoserBodyUrl(s3Service.generateImgUrl(
                "body/" + response.getLoserName().replace(' ', '-') + ".png"
        ));
        System.out.println(response.getMainCardDateTimeInfo());
        return response;
    }

}
