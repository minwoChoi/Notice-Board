package com.example.demo.repository;


import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Scrap;
import com.example.demo.model.User;
import com.example.demo.model.Post;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<Scrap> findByUserAndPost(User user, Post post);
}
