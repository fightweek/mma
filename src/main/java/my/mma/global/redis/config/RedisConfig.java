package my.mma.global.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import my.mma.admin.fighter.dto.ChosenGameFighterNamesDto;
import my.mma.admin.fighter.dto.RankersDto;
import my.mma.fightevent.dto.StreamFightEventDto;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.stream.dto.BlockedUserIdsDto;
import my.mma.stream.dto.UserChatLog;
import my.mma.bet.dto.BetResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Redis 연결을 위한 'Connection' 생성.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * Redis 데이터 처리를 위한 템플릿을 구성
     * 구성된 RedisTemplate 을 통해서 데이터 통신으로 처리되는 대한 직렬화 수행
     * @return RedisTemplate<String, Object>
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // Redis 연결
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        // Key-Value 형태로 직렬화 수행
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        // Hash Key-Value 형태로 직렬화 수행
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        // 기본적 직렬화 수행
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public RedisUtils<StreamFightEventDto> streamFightEventRedisUtils(
            @Qualifier("streamFightEventRedisTemplate") RedisTemplate<String, StreamFightEventDto> redisTemplate) {
        return new RedisUtils<>(redisTemplate);
    }

    @Bean
    public RedisTemplate<String, StreamFightEventDto> streamFightEventRedisTemplate()
    {
        return setRedisTemplate(StreamFightEventDto.class);
    }

    @Bean
    public RedisUtils<RankersDto> rankerRedisUtils(
            @Qualifier("rankersRedisTemplate") RedisTemplate<String, RankersDto> redisTemplate) {
        return new RedisUtils<>(redisTemplate);
    }

    @Bean
    public RedisTemplate<String, RankersDto> rankersRedisTemplate()
    {
        return setRedisTemplate(RankersDto.class);
    }

    @Bean
    public RedisUtils<ChosenGameFighterNamesDto> adminChosenGameFightersRedisUtils(
            @Qualifier("adminChosenGameFightersRedisTemplate") RedisTemplate<String, ChosenGameFighterNamesDto> redisTemplate) {
        return new RedisUtils<>(redisTemplate);
    }

    @Bean
    public RedisTemplate<String, ChosenGameFighterNamesDto> adminChosenGameFightersRedisTemplate()
    {
        return setRedisTemplate(ChosenGameFighterNamesDto.class);
    }

    @Bean
    public RedisUtils<BetResponse> todayBetRedisUtils(
            @Qualifier("todayBetRedisTemplate") RedisTemplate<String, BetResponse> redisTemplate) {
        return new RedisUtils<>(redisTemplate);
    }

    @Bean
    public RedisTemplate<String, BetResponse> todayBetRedisTemplate()
    {
        return setRedisTemplate(BetResponse.class);
    }

    @Bean
    public RedisUtils<UserChatLog> streamChatLogRedisUtils(
            @Qualifier("streamChatLogRedisTemplate") RedisTemplate<String, UserChatLog> redisTemplate) {
        return new RedisUtils<>(redisTemplate);
    }

    @Bean
    public RedisTemplate<String, UserChatLog> streamChatLogRedisTemplate()
    {
        return setRedisTemplate(UserChatLog.class);
    }

    @Bean
    public RedisUtils<BlockedUserIdsDto> blockedUsersRedisUtils(
            @Qualifier("blockedUsersRedisTemplate") RedisTemplate<String, BlockedUserIdsDto> redisTemplate) {
        return new RedisUtils<>(redisTemplate);
    }

    @Bean
    public RedisTemplate<String, BlockedUserIdsDto> blockedUsersRedisTemplate()
    {
        return setRedisTemplate(BlockedUserIdsDto.class);
    }

    private <T> RedisTemplate<String, T> setRedisTemplate(Class<T> clazz){
        RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // key는 문자열
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        objectMapper.registerModule(new JavaTimeModule()); // LocalDateTime 등 Java 8 날짜 객체 직렬화 지원
        Jackson2JsonRedisSerializer<T> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,clazz);
        redisTemplate.setValueSerializer(serializer); // value는 json type
        return redisTemplate;
    }

}
