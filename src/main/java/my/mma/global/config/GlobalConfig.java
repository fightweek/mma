package my.mma.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GlobalConfig {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler(){
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        //Java 8 이상 날짜/시간 API (LocalDate, LocalDateTime, Instant 등)를 직렬화/역직렬화 지원하도록 모듈을 등록
        objectMapper.registerModule(new JavaTimeModule());
        /**
         * 날짜/시간 타입을 숫자(타임스탬프, epoch 밀리초) 대신에
         * ISO-8601 문자열 형식 (예: "2025-08-10T20:00:00")으로 직렬화함
         * 사람이 읽기 쉽고, 다른 시스템과 연동할 때 가독성 높음
         */
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

}
