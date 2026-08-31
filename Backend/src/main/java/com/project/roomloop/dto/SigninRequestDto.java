package com.project.roomloop.dto;

import lombok.Data;

@Data
public class SigninRequestDto {
    private String name;
    private String email;
    private String password;
    private String mobileNumber;
}
