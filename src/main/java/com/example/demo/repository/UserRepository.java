package com.example.demo.repository;

import com.example.demo.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);
}
