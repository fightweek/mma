package my.mma.global.redis.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public class RedisUtils<T> {

    private final RedisTemplate<String,T> redisTemplate;

    public void saveData(String key, T data){
        redisTemplate.opsForValue().set(key,data);
    }

    public void updateData(String key, T data){
        redisTemplate.opsForValue().set(key,data);
    }

    public T getData(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key){
        redisTemplate.delete(key);
    }

}
