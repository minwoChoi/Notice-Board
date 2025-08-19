package com.example.demo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.User;
import java.util.List;


public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserId(String userId);  
    Optional<User> findByNickname(String nickname);
    Optional<User> findByName(String name);
}
