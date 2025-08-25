package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.scrap.response.ScrapResponseDto;
import com.example.demo.model.Scrap;
import com.example.demo.model.User;
import com.example.demo.repository.ScrapRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public void addScrap(String userId, Long postId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
        var post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + postId));

        if (scrapRepository.existsByUserAndPost(user, post)) {
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
        var post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + postId));

        Scrap scrap = scrapRepository.findByUserAndPost(user, post)
            .orElseThrow(() -> new IllegalArgumentException("스크랩되어 있지 않은 게시글입니다."));

        scrapRepository.delete(scrap);
    }

    @Transactional(readOnly = true)
    public List<ScrapResponseDto> getMyScraps(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));

        List<Scrap> scraps = scrapRepository.findByUser(user);

        return scraps.stream().map(scrap -> {
            ScrapResponseDto dto = new ScrapResponseDto();
            dto.setScrapId(scrap.getScrapId());
            dto.setUserId(scrap.getUser().getUserId());  // String 타입일 경우 DTO도 String로 맞출 것
            dto.setPostId(scrap.getPost().getPostId());
            dto.setCreatedDate(scrap.getCreatedDate());
            dto.setPostTitle(scrap.getPost().getTitle());
            return dto;
        }).collect(Collectors.toList());
    }
}
