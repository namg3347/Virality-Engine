package com.redhat.virality.repositiories;

import com.redhat.virality.entites.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
}