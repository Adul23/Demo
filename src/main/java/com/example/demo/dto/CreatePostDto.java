package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostDto {
    private String content;
    private String mediaUrl;
}
