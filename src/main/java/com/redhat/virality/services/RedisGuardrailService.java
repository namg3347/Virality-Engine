package com.redhat.virality.services;

import com.redhat.virality.exceptions.BadRequestException;
import com.redhat.virality.exceptions.ResourceNotFoundException;
import com.redhat.virality.exceptions.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisGuardrailService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_BOT_REPLIES = 100;
    private static final int MAX_COMMENT_THREAD =20;
    private static final int COOLDOWN_MINUTES  = 10;

    public void validateHorizontalCap(Long postId) {

        String key = "post:" + postId + ":bot_comment_count";

        Long count = redisTemplate.opsForValue()
                .increment(key);

        if(count==null) {
            throw new ResourceNotFoundException("no value for redis key: "+key);
        }

        if(count > MAX_BOT_REPLIES) {

            redisTemplate.opsForValue()
                    .decrement(key);

            throw new TooManyRequestsException(
                    "Bot reply limit exceeded"
            );
        }
    }
    public void rollbackHorizontalCap(Long postId) {
        String key = "post:" + postId + ":bot_comment_count";
        redisTemplate.opsForValue()
                .decrement(key);
    }

    public void validateVerticalCap(int depth) {
        if(depth > MAX_COMMENT_THREAD ) {
            throw new BadRequestException("Maximum thread depth exceeded");
        }
    }

    public String validateCooldown(Long botId,Long humanId) {

        String key = "cooldown:bot_" + botId + ":human_" + humanId;

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(
                        key,
                        "locked",
                        Duration.ofMinutes(COOLDOWN_MINUTES)
                );

        //key present
        if(Boolean.FALSE.equals(success)) {
            throw new TooManyRequestsException(
                    "Cooldown active"
            );
        }
        return key;
    }

    public void removeCooldown(String key) {
        redisTemplate.opsForValue().getAndDelete(key);
    }


}