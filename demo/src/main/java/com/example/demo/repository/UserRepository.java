package com.example.demo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.User;


public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserId(String userId);  
    Optional<User> findByNickname(String nickname);
    Optional<User> findByName(String name);
}
