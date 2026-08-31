package com.project.roomloop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponceDto {
    private Long id;
    private String email;
    private String jwtToken;
}
