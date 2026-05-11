package com.redhat.virality.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisViralityService {

    private final StringRedisTemplate stringRedisTemplate;

    public void incrementViralityScore(long postId, int points) {
        String key = "postId:" +  postId + ":viralityScore";

        stringRedisTemplate.opsForValue().increment(key, points);
    }
}
