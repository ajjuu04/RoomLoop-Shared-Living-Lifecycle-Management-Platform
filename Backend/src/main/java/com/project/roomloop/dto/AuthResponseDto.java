package com.project.roomloop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class AuthResponseDto {
    private Long userId;
    private String jwtToken;
}