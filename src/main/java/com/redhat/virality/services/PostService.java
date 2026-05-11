package com.redhat.virality.services;

import com.redhat.virality.dtos.CreateCommentRequest;
import com.redhat.virality.dtos.CreatePostRequest;
import com.redhat.virality.entites.Comment;
import com.redhat.virality.entites.Post;

public interface PostService {

    Post createPost(CreatePostRequest request);

    Comment addComment(Long postId, CreateCommentRequest request);

    void likePost(Long postId);
}