package com.redhat.virality.services;

import com.redhat.virality.dtos.CreateCommentRequest;
import com.redhat.virality.entites.Post;

public interface CommentService {

    int getCommentDepth(Long parentCommentID);
    Long determineTargetHuman(Post post, CreateCommentRequest request);
}
