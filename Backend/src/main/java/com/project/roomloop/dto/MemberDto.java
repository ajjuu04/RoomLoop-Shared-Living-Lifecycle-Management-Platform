package com.project.roomloop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberDto {
    private Long userId;
    private String name;
    private boolean isAdmin;
}