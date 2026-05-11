package com.redhat.virality.services;

import com.redhat.virality.dtos.CreateCommentRequest;
import com.redhat.virality.dtos.CreatePostRequest;
import com.redhat.virality.entites.Comment;
import com.redhat.virality.entites.Post;
import com.redhat.virality.enums.AuthorType;
import com.redhat.virality.exceptions.BadRequestException;
import com.redhat.virality.exceptions.ResourceNotFoundException;
import com.redhat.virality.repositiories.CommentRepository;
import com.redhat.virality.repositiories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;
    private final RedisViralityService redisViralityService;
    private final RedisGuardrailService redisGuardrailService;
    private final RedisNotificationService redisNotificationService;

    @Override
    public Post createPost(CreatePostRequest request) {

        Post post = Post.builder()
                .authorId(request.getAuthorId())
                .authorType(request.getAuthorType())
                .content(request.getContent())
                .likeCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        if(post.getAuthorType().equals(AuthorType.BOT)){
            throw new BadRequestException("Bots can't post");
        }

        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Comment addComment(Long postId, CreateCommentRequest request) {

        boolean horizontalReserved = false;
        boolean cooldownReserved = false;

        String cooldownKey = null;
        Long targetHuman = null;

        try {

            Post post = postRepository.findById(postId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Post not found"
                            ));

            int depth = commentService.getCommentDepth(request.getParentCommentId());

            redisGuardrailService.validateVerticalCap(depth);

            if(request.getAuthorType() == AuthorType.BOT) {

                redisGuardrailService.validateHorizontalCap(postId);

                horizontalReserved = true;

                targetHuman = commentService.determineTargetHuman(post, request);

                cooldownKey = redisGuardrailService.validateCooldown(request.getAuthorId(), targetHuman);

                cooldownReserved = true;
            }

            Comment comment = Comment.builder()
                    .postId(postId)
                    .authorId(request.getAuthorId())
                    .authorType(request.getAuthorType())
                    .parentCommentId(
                            request.getParentCommentId()
                    )
                    .content(request.getContent())
                    .depthLevel(depth)
                    .createdAt(LocalDateTime.now())
                    .build();

            Comment saved =
                    commentRepository.save(comment);

            if(request.getAuthorType() == AuthorType.USER) {

                redisViralityService
                        .incrementViralityScore(
                                postId,
                                50
                        );

            } else {

                String notification = "Bot:"+request.getAuthorId()+" replied to you";
                redisNotificationService
                        .handleBotInteraction(
                                targetHuman,
                                notification
                                );

                redisViralityService
                        .incrementViralityScore(
                                postId,
                                1
                        );
            }

            return saved;

        } catch(Exception ex) {

            if(horizontalReserved) {
                redisGuardrailService
                        .rollbackHorizontalCap(postId);
            }

            if(cooldownReserved && cooldownKey != null) {
                redisGuardrailService
                        .removeCooldown(cooldownKey);
            }

            throw ex;
        }
    }

    @Override
    @Transactional
    public void likePost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if(post.getAuthorType().equals(AuthorType.BOT)) {
            throw new BadRequestException("bot cant like a post");
        }

        //user likes
        redisViralityService.incrementViralityScore(postId,20);

        post.setLikeCount(post.getLikeCount() + 1);

        postRepository.save(post);
    }
}