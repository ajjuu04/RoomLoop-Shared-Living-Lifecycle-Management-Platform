package com.project.roomloop.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDto {
    private long userId;
    private String name;
    private String email;
    private String role;
    private long roomId;
}
