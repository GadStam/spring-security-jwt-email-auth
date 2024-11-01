package com.security.jwt_spring_security_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDto {
    private String email;
    private String verificationCode;
    private String newPassword;

}
