package com.redhat.virality.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSweeper {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<Object, Object> redisTemplate;
    private static final String SET_KEY = "pending_notif_users";

    @Scheduled(fixedRate = 300000)
    public void sweepNotifications() {

        Set<String> users = stringRedisTemplate.
                opsForSet().members(SET_KEY);

        if(users==null||users.isEmpty()){
            return;
        }

        for(String userId:users){
            processUserNotifications(userId);
        }
    }

    private void processUserNotifications(String userId) {

        String listKey =
                "user:" +
                        userId +
                        ":pending_notifs";

        List<String> notifications =
                Objects.requireNonNull(redisTemplate.opsForList()
                                .range(listKey, 0, -1))
                        .stream().map(Object::toString)
                        .toList();

        if(!notifications.isEmpty()) {
            String first = notifications.get(0);

            int others = notifications.size() - 1;

            log.info("Summarized Push Notification: {} and {} others " +
                    "interacted with your posts.", first, others);

            redisTemplate.delete(listKey);
        }

        redisTemplate.opsForSet()
                .remove(
                        SET_KEY,
                        userId
                );
    }
}
