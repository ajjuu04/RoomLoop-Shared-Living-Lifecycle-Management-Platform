package com.project.roomloop.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyDto {
    private String mobileNumber;
    private String otp;
}