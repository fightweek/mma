package my.mma.global.redis.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@RequiredArgsConstructor
public class RedisUtils<T> {

    private final RedisTemplate<String, T> redisTemplate;

    public void saveData(String key, T data) {
        redisTemplate.opsForValue().set(key, data);
    }

    public void saveDataWithTTL(String key, T data, Duration ttl) {
        redisTemplate.opsForValue().set(key, data, ttl);
    }

    public void updateData(String key, T data) {
        redisTemplate.opsForValue().set(key, data);
    }

    public T getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Map<String,T> getAllWithKeyFromPrefix(String prefix) {
        Map<String,T> keyValues = new HashMap<>();
        List<String> keys = new ArrayList<>();
        // executeWithStickyConnection : 하나의 물리 커넥션에서 여러 명령어를 수행하기에 적합한 메서드
        redisTemplate.executeWithStickyConnection(redisConnection -> {
            /** Cursor는 SCAN 결과를 순회하는 반복자(Iterator)
             * try-with-resources로 닫아줌 (커넥션 리소스 해제)
             */
            try (Cursor<byte[]> cursor = redisConnection.keyCommands().scan(
                    ScanOptions.scanOptions().match(prefix+"*").count(100).build())) {
                while (cursor.hasNext()) {
                    byte[] keyBytes = cursor.next();
                    String key = new String(keyBytes, StandardCharsets.UTF_8);
                    keys.add(key);
                }
            }
            return null;
        });
        List<T> values = redisTemplate.opsForValue().multiGet(keys);
        for(int i=0;i<keys.size();i++){
            if(values.get(i)!=null)
                keyValues.put(keys.get(i),values.get(i));
        }
        return keyValues;
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

}
