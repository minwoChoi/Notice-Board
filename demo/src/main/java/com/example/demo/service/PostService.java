package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.post.request.PostCreateRequest;
import com.example.demo.dto.post.request.PostEditRequest;
import com.example.demo.dto.post.response.PostListResponse;
import com.example.demo.dto.post.response.PostPageResponse;
import com.example.demo.model.Category;
import com.example.demo.model.Post;
import com.example.demo.model.PostLike;
import com.example.demo.model.User;
import org.springframework.data.domain.Pageable;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;

    // 내가 작성한 게시물 목록 조회
    @Transactional(readOnly = true)
    public List<PostListResponse> findMyPosts(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. 기존 로직으로 DTO 리스트를 먼저 조회합니다.
        List<PostListResponse> responseList = postRepository.findPostsByUserWithCommentCount(user);

        // 2. 사용자의 프로필 사진 URL을 생성합니다.
        String profilePictureUrl = (user.getProfilePicture() != null && user.getProfilePicture().length > 0)
                ? "/users/" + userId + "/photo"
                : null;

        // 3. 각 DTO에 작성자의 프로필 사진 URL을 설정해줍니다.
        responseList.forEach(dto -> dto.setAuthorProfilePictureUrl(profilePictureUrl));

        // 4. URL이 추가된 리스트를 반환합니다.
        return responseList;
    }

    // 게시글 작성
    @Transactional
    public Post createPost(PostCreateRequest req, String userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Category category = categoryRepository.findByCategoryId(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.getCategoryId()));

        Post post = new Post();
        post.setUser(user);
        post.setCategory(category);
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setPhoto(req.getPhoto());
        post.setCreatedDate(LocalDateTime.now());
        post.setLikeCount(0);
        post.setViewCount(0);

        return postRepository.save(post);
    }

    // 게시글 업데이트
    @Transactional
    public Post updatePost(Long postId, PostEditRequest request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getUserId().equals(username)) {
            throw new SecurityException("이 게시물의 작성자가 아닙니다.");
        }

        // 오직 필요할 때만 카테고리 교체
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));
            post.setCategory(category);
        }
        if (request.getTitle() != null)
            post.setTitle(request.getTitle());
        if (request.getContent() != null)
            post.setContent(request.getContent());
        if (request.getPhoto() != null)
            post.setPhoto(request.getPhoto());

        return postRepository.save(post);
    }

    // 게시물 삭제
    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자 검증(로그인한 사용자가 게시글 주인인지)
        if (!post.getUser().getUserId().equals(username)) {
            throw new SecurityException("You are not the author of this post.");
        }

        postRepository.delete(post);
    }

    // 게시물 검색
    @Transactional(readOnly = true)
    public PostPageResponse searchPosts(String keyword, Pageable pageable) {
        Page<Post> postPage = postRepository.findByTitleContainingOrContentContaining(keyword, pageable);

        List<PostListResponse> postListResponses = postPage.getContent().stream()
                .map(post -> {
                    String photoUrl = (post.getPhoto() != null && post.getPhoto().length > 0)
                            ? "/posts/" + post.getPostId() + "/photo"
                            : null;

                    String authorProfilePictureUrl = (post.getUser().getProfilePicture() != null
                            && post.getUser().getProfilePicture().length > 0)
                                    ? "/users/" + post.getUser().getUserId() + "/photo"
                                    : null;

                    // ▼▼▼ [수정] DTO 생성자와 파라미터 순서를 정확히 일치시킵니다 ▼▼▼
                    return new PostListResponse(
                            post.getPostId(),
                            post.getUser().getUserId(), // 2. userId
                            post.getCategory().getCategoryId(), // 3. categoryId
                            post.getCategory().getCategoryName(), // 4. categoryName
                            post.getTitle(), // 5. title
                            post.getContent(),
                            post.getUser().getNickname(), // 6. nickname
                            post.getCreatedDate(), // 7. createdDate
                            post.getViewCount(), // 8. viewCount
                            post.getLikeCount(), // 9. likeCount
                            (long) post.getComments().size(), // 10. commentCount
                            photoUrl, // 11. photoUrl
                            authorProfilePictureUrl // 12. authorProfilePictureUrl
                    );
                })
                .toList();

        return new PostPageResponse(postListResponses, postPage.getTotalElements());
    }

    // 전체 게시물 조회
    @Transactional(readOnly = true)
    public PostPageResponse findAllPosts(Pageable pageable,Long category) {
        Page<Post> postPage = postRepository.findAll(pageable);

         if (category == 0L) {
            // category가 0이면 기존처럼 전체 게시글 조회
            postPage = postRepository.findAll(pageable);
        } else {
            postPage = postRepository.findByCategory(category, pageable);
        }

        List<PostListResponse> postListResponses = postPage.getContent().stream()
                .map(post -> {
                    String photoUrl = (post.getPhoto() != null && post.getPhoto().length > 0)
                            ? "/posts/" + post.getPostId() + "/photo"
                            : null;

                    String authorProfilePictureUrl = (post.getUser().getProfilePicture() != null
                            && post.getUser().getProfilePicture().length > 0)
                                    ? "/users/" + post.getUser().getUserId() + "/photo"
                                    : null;

                    // ▼▼▼ [수정] DTO 생성자와 파라미터 순서를 정확히 일치시킵니다 ▼▼▼
                    return new PostListResponse(
                            post.getPostId(),
                            post.getUser().getUserId(), // 2. userId
                            post.getCategory().getCategoryId(), // 3. categoryId
                            post.getCategory().getCategoryName(), // 4. categoryName
                            post.getTitle(), // 5. title
                            post.getContent(),
                            post.getUser().getNickname(), // 6. nickname
                            post.getCreatedDate(), // 7. createdDate
                            post.getViewCount(), // 8. viewCount
                            post.getLikeCount(), // 9. likeCount
                            (long) post.getComments().size(), // 10. commentCount
                            photoUrl, // 11. photoUrl
                            authorProfilePictureUrl // 12. authorProfilePictureUrl
                    );
                })
                .toList();

        return new PostPageResponse(postListResponses, postPage.getTotalElements());
    }

    // 특정 게시물 조회
    @Transactional
    public Post findPostById(Long postId, String username) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 로그인 여부와 관계없이 조회수는 증가시킵니다.
        post.increaseViewCount();
        return post;
    }

    // 게시글 좋아요
    @Transactional
    public void likePost(Long postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 좋아요 눌렀는지 체크
        boolean alreadyLiked = postLikeRepository.existsByPostAndUser(post, user);
        if (alreadyLiked) {
            throw new IllegalStateException("이미 추천한 게시글입니다.");
        }

        // 새 좋아요 저장
        PostLike like = new PostLike();
        like.setPost(post);
        like.setUser(user);
        postLikeRepository.save(like);

        // 게시글 likeCount +1
        post.increaseLikeCount();
        postRepository.save(post);

        // [추가] 게시글 작성자에게 좋아요 알림 전송 (본인 글 아닐 때만)
        if (!user.getUserId().equals(post.getUser().getUserId())) {
            String message = user.getNickname() + "님이 회원님의 게시물을 추천했습니다.";
            notificationService.createNotification(post.getUser(), message, post, null /* 댓글 x */);
        }
    }

    // 게시물 추천 취소
    @Transactional
    public void unlikePost(Long postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        PostLike like = postLikeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new IllegalArgumentException("추천하지 않은 게시글입니다."));

        postLikeRepository.delete(like);

        // 게시글 likeCount -1
        post.decreaseLikeCount();
        postRepository.save(post);
    }

    // 게시물 사진 출력
    @Transactional(readOnly = true)
    public byte[] getPhotoById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return post.getPhoto();
    }

    // 토글 좋아요
    @Transactional
    public boolean toggleLike(Long postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다"));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자가 없습니다"));

        Optional<PostLike> existing = postLikeRepository.findByPostAndUser(post, user);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            post.decreaseLikeCount();
            postRepository.save(post);
            return false; // 좋아요 취소됨
        } else {
            PostLike newLike = new PostLike();
            newLike.setPost(post);
            newLike.setUser(user);
            postLikeRepository.save(newLike);
            post.increaseLikeCount();
            postRepository.save(post);

            // 좋아요 알림 전송: 작성자와 좋아요 누른 사용자가 다를 경우에만
            if (!user.getUserId().equals(post.getUser().getUserId())) {
                String message = user.getNickname() + "님이 회원님의 게시물을 추천했습니다.";
                notificationService.createNotification(post.getUser(), message, post, null /* 댓글 없음 */);
            }

            return true; // 좋아요 추가됨
        }
    }

}
