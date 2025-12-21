package my.mma.global.redis.prefix;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisKeyPrefix {

    CHAT_LOG_PREFIX("chat:"),
    BET_PREFIX("bet:"),
    BLOCKED_USERS_PREFIX("blocked:");

    private final String prefix;

}
