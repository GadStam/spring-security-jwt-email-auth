package com.security.jwt_spring_security_api.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class LoginResponse {

    public String token;
    private long expiresIn;
    public LoginResponse(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }
   

}
