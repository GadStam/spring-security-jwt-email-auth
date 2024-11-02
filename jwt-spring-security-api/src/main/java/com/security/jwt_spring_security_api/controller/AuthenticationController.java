package com.security.jwt_spring_security_api.controller;

import com.security.jwt_spring_security_api.dto.ResetPasswordDto;
import org.springframework.web.bind.annotation.*;

import com.security.jwt_spring_security_api.dto.LoginUserDto;
import com.security.jwt_spring_security_api.dto.RegisterUserDto;
import com.security.jwt_spring_security_api.dto.VerifyUserDto;
import com.security.jwt_spring_security_api.model.User;
import com.security.jwt_spring_security_api.responses.LoginResponse;
import com.security.jwt_spring_security_api.service.AuthenticationService;
import com.security.jwt_spring_security_api.service.JwtService;

import org.springframework.http.ResponseEntity;


@RequestMapping("/auth")
@RestController

public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto){
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto){
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto){
       try{
        authenticationService.verifyUser(verifyUserDto);
        return ResponseEntity.ok("User verified successfully");
       }catch(RuntimeException e){
           return ResponseEntity.badRequest().body(e.getMessage());
       }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            authenticationService.forgotPassword(email);
            return ResponseEntity.ok("Password reset email sent successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto input) {
        try {
            authenticationService.resetPassword(input);
            return ResponseEntity.ok("Password reset successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email){
        try{
            authenticationService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification email sent successfully");
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
    
    

    
}
