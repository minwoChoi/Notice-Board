package com.example.demo.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Post;

public interface PostRepository extends JpaRepository<Post, Integer> {
    Optional<Post> findByPostId(Integer postId); 
    
}

