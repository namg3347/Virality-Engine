package com.redhat.virality.entites;

import com.redhat.virality.enums.AuthorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    private Long parentCommentId; // nullable if comment is directly to the post

    private Long authorId;

    @Enumerated(EnumType.STRING)
    private AuthorType authorType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private Integer depthLevel;

    private LocalDateTime createdAt;
}