package com.redhat.virality.services;

import com.redhat.virality.dtos.CreateCommentRequest;
import com.redhat.virality.entites.Comment;
import com.redhat.virality.entites.Post;
import com.redhat.virality.exceptions.ResourceNotFoundException;
import com.redhat.virality.repositiories.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;

    @Override
    public int getCommentDepth(Long parentCommentID) {
        if(parentCommentID == null){
            return 1;
        }

        Comment parent = commentRepository.findById(parentCommentID)
                .orElseThrow(() -> new ResourceNotFoundException("parent comment not found"));

        return parent.getDepthLevel() + 1;
    }

    @Override
    public Long determineTargetHuman(Post post, CreateCommentRequest request) {

        if(request.getParentCommentId() == null) {
            return post.getAuthorId();
        }

        Comment parent = commentRepository.findById(
                request.getParentCommentId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Parent comment not found"
                ));

        return parent.getAuthorId();
    }

}
