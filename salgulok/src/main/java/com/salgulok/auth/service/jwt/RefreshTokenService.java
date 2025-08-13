package com.salgulok.auth.service.jwt;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String REFRESH_PREFIX = "refresh:";

    public void saveToken(Long userId, String refreshToken, long expireMillis) {
        String key = REFRESH_PREFIX + userId;
        try{
            stringRedisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(expireMillis));
        } catch (Exception e){
            log.error("redis error", e);
            throw new SalgulokException(ErrorCode.REDIS_SAVE_ERROR);
        }
    }

    public String getToken(Long userId){
        String key = REFRESH_PREFIX + userId;
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteToken(Long userId){
        String key = REFRESH_PREFIX + userId;
        try{
            stringRedisTemplate.delete(key);
        } catch (Exception e){
            throw new SalgulokException(ErrorCode.REDIS_DELETE_ERROR);
        }
    }
}
