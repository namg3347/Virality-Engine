package com.redhat.virality.controller;

import com.redhat.virality.dtos.CreateCommentRequest;
import com.redhat.virality.dtos.CreatePostRequest;
import com.redhat.virality.entites.Post;
import com.redhat.virality.entites.Comment;
import com.redhat.virality.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(
            @Valid @RequestBody CreatePostRequest request
    ) {
        return postService.createPost(request);
    }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return postService.addComment(postId, request);
    }

    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likePost(@PathVariable Long postId) {
        postService.likePost(postId);
    }
}