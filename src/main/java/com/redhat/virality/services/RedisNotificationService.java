package com.redhat.virality.services;

import com.redhat.virality.repositiories.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationService {

    private final StringRedisTemplate redisTemplate;

    private static final int NOTIFICATION_THROTTLE_MINUTES = 15;

    public void handleBotInteraction(
            Long userId,
            String notification
    ) {

        String cooldownKey =
                "notif:user:" + userId;

        Boolean allowed =
                redisTemplate.opsForValue()
                        .setIfAbsent(
                                cooldownKey,
                                "active",
                                Duration.ofMinutes(
                                        NOTIFICATION_THROTTLE_MINUTES
                                )
                        );

        if(Boolean.TRUE.equals(allowed)) {

           log.info("Push notification sent to user");
            return;
        }

        queuePendingNotification(
                userId,
                notification
        );
    }

    private void queuePendingNotification(
            Long userId,
            String notification
    ) {
        String listKey = "user:" + userId + ":pending_notifs";

        redisTemplate.opsForList()
                .rightPush(listKey, notification);

        redisTemplate.opsForSet()
                .add(
                        "pending_notif_users",
                        String.valueOf(userId)
                );
    }


}
