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
public class TokenService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void saveRefreshToken(Long userId, String refreshToken, long expireMillis) {
        String key = REFRESH_PREFIX + userId + ":" + refreshToken;
        try{
            // 여러 기기 로그인 경우도 고려하여 refreshToken값도 key값으로 들어가도록 수정
            stringRedisTemplate.opsForValue().set(key, "true", Duration.ofMillis(expireMillis));
        } catch (Exception e){
            log.error("redis error", e);
            throw new SalgulokException(ErrorCode.REDIS_SAVE_ERROR);
        }
    }

    public boolean existsRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_PREFIX + userId + ":" + refreshToken;
        Boolean exists = stringRedisTemplate.hasKey(key);
        return exists != null && exists;
    }

    public void deleteRefreshToken(Long userId, String refreshToken){
        String key = REFRESH_PREFIX + userId + ":" + refreshToken;
        try{
            stringRedisTemplate.delete(key);
        } catch (Exception e){
            throw new SalgulokException(ErrorCode.REDIS_DELETE_ERROR);
        }
    }

    public void saveBlacklist(String accessToken, long expireMillis){
        String key = BLACKLIST_PREFIX + accessToken;
        try{
            stringRedisTemplate.opsForValue().set(key, "true", Duration.ofMillis(expireMillis));
        } catch (Exception e){
            throw new SalgulokException(ErrorCode.REDIS_SAVE_ERROR);
        }
    }

    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        return stringRedisTemplate.hasKey(key);
    }
}
