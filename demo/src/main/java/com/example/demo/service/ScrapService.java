package com.example.demo.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.demo.model.Scrap;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.ScrapRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public void addScrap(String userId, Long postId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + postId));

        // 중복 스크랩 방지
        boolean exists = scrapRepository.existsByUserAndPost(user, post);
        if (exists) {
            throw new IllegalStateException("이미 스크랩한 게시글입니다.");
        }

        Scrap scrap = new Scrap();
        scrap.setUser(user);
        scrap.setPost(post);
        scrap.setCreatedDate(LocalDateTime.now());

        scrapRepository.save(scrap);
    }

    public void removeScrap(String userId, Long postId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + postId));

        Scrap scrap = scrapRepository.findByUserAndPost(user, post)
            .orElseThrow(() -> new IllegalArgumentException("스크랩되어 있지 않은 게시글입니다."));

        scrapRepository.delete(scrap);
    }
}
