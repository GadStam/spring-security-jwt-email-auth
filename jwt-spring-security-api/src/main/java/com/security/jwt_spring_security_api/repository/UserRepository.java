package com.security.jwt_spring_security_api.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.security.jwt_spring_security_api.model.User;

@Repository

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);
    Optional<User> findByGoogleId(String googleId); // New method
}
