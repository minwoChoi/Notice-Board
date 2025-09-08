package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.scrap.response.ScrapResponseDto;
import com.example.demo.model.Post;
import com.example.demo.model.Scrap;
import com.example.demo.model.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ScrapRepository;
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
        // 1. userId로 User 엔티티를 조회합니다.
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 새로 만든 Repository 메소드를 호출하여 모든 관련 정보가 포함된 스크랩 목록을 가져옵니다.
        List<Scrap> scraps = scrapRepository.findScrapsWithDetailsByUser(user);

        // 3. Scrap 엔티티 목록을 ScrapResponseDto 목록으로 변환합니다.
        return scraps.stream()
                .map(scrap -> {
                    Post post = scrap.getPost();       // 스크랩된 게시물
                    User author = post.getUser();      // 게시물 작성자

                    ScrapResponseDto dto = new ScrapResponseDto();
                    dto.setScrapId(scrap.getScrapId());
                    dto.setPostId(post.getPostId());
                    dto.setPostTitle(post.getTitle());
                    dto.setPostContent(post.getContent());
                    dto.setPostCreatedDate(post.getCreatedDate());
                    dto.setAuthorNickname(author.getNickname());

                    // 4. 게시물 사진 URL을 생성하여 설정합니다.
                    if (post.getPhoto() != null && post.getPhoto().length > 0) {
                        dto.setPostPhotoUrl("/posts/" + post.getPostId() + "/photo");
                    }

                    // 5. 작성자 프로필 사진 URL을 생성하여 설정합니다.
                    if (author.getProfilePicture() != null && author.getProfilePicture().length > 0) {
                        dto.setAuthorProfilePictureUrl("/users/" + author.getUserId() + "/photo");
                    }
                    
                    return dto;
                })
                .toList();
    }
    @Transactional
    public boolean toggleScrap(String userId, Long postId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Optional<Scrap> existingScrap = scrapRepository.findByUserAndPost(user, post);

        if (existingScrap.isPresent()) {
            scrapRepository.delete(existingScrap.get());  // 스크랩 취소
            return false;  // 스크랩 해제됨
        } else {
            Scrap scrap = new Scrap();
            scrap.setUser(user);
            scrap.setPost(post);
            scrap.setCreatedDate(LocalDateTime.now());
            scrapRepository.save(scrap);  // 스크랩 추가
            return true;  // 스크랩 활성화됨
        }
    }
}
