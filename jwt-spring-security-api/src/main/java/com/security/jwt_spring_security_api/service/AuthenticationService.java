package com.security.jwt_spring_security_api.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import com.security.jwt_spring_security_api.dto.ResetPasswordDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import com.security.jwt_spring_security_api.dto.LoginUserDto;
import com.security.jwt_spring_security_api.dto.RegisterUserDto;
import com.security.jwt_spring_security_api.dto.VerifyUserDto;
import com.security.jwt_spring_security_api.model.User;
import com.security.jwt_spring_security_api.repository.UserRepository;

import jakarta.mail.MessagingException;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public User signup(RegisterUserDto input){
        User user = new User(input.getUsername(),input.getEmail(), passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
        return userRepository.save(user);

    }

    public User authenticate(LoginUserDto input){
        User user = userRepository.findByEmail(input.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

        if(!user.isEnabled()){
            throw new RuntimeException("Account not verified");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword()));
        return user;
    }

    public void verifyUser(VerifyUserDto input){
        Optional<User> userOptional = userRepository.findByEmail(input.getEmail());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            if(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())){
                throw new RuntimeException("Verification code expired");
            }

            if(user.getVerificationCode().equals(input.getVerificationCode())){
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            }else{
                throw new RuntimeException("Invalid verification code");
            }
        }else{
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationCode(String email){
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            if(user.isEnabled()){
                throw new RuntimeException("Account already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
            sendVerificationEmail(user);
            userRepository.save(user);
        }else{
            throw new RuntimeException("User not found");
        }
    }

    public void sendVerificationEmail(User user){
        String subject = "Verify your account";
        String verificationCode = "VERIFICATION CODE: " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try{
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        }catch(MessagingException e){
            e.printStackTrace();
        }
    }

    private String generateVerificationCode(){
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    public void forgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            sendPasswordResetEmail(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void resetPassword(ResetPasswordDto input) {
        Optional<User> userOptional = userRepository.findByEmail(input.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Reset code expired");
            }

            if (!user.getVerificationCode().equals(input.getVerificationCode())) {
                throw new RuntimeException("Invalid reset code");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(input.getNewPassword()));
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    private void sendPasswordResetEmail(User user) {
        String subject = "Reset Your Password";
        String verificationCode = "RESET CODE: " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Password Reset Request</h2>"
                + "<p style=\"font-size: 16px;\">We received a request to reset your password. Please use the code below to reset your password:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Reset Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "<p style=\"font-size: 14px; color: #666; margin-top: 20px;\">If you didn't request this, please ignore this email or contact support if you have concerns.</p>"
                + "<p style=\"font-size: 14px; color: #666;\">This code will expire in 15 minutes.</p>"
                + "</div>"
                + "</body>"
                + "</html>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}