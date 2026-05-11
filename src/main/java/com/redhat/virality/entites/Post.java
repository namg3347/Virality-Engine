package com.redhat.virality.entites;

import com.redhat.virality.enums.AuthorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long authorId;

    @Enumerated(EnumType.STRING)
    private AuthorType authorType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private Integer likeCount;

    private LocalDateTime createdAt;
}