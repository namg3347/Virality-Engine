package com.redhat.virality.dtos;

import com.redhat.virality.enums.AuthorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotNull
    private Long authorId;

    @NotNull
    private AuthorType authorType;

    @NotBlank
    private String content;
}