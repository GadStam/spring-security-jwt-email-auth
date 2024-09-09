package com.security.jwt_spring_security_api.service;


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.security.jwt_spring_security_api.model.User;
import com.security.jwt_spring_security_api.repository.UserRepository;

@Service

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;

    }


}
