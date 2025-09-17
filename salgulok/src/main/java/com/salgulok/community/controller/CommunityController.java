// controller/CommunityController.java
package com.salgulok.community.controller;

import com.salgulok.community.domain.PostSearchCond;
import com.salgulok.community.dto.request.CommentCreateRequest;
import com.salgulok.community.dto.request.PostCreateRequest;
import com.salgulok.community.dto.response.PostResponse;
import com.salgulok.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // 전체/지역/주제/체류여부 조합 조회
    @GetMapping("/posts")
    public Page<PostResponse> getPosts(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return communityService.searchPosts(new PostSearchCond(region, topic, status), pageable);
    }

    // 상세
    @GetMapping("/posts/{postId}")
    public PostResponse getPost(@PathVariable Long postId) {
        return communityService.getPost(postId);
    }

    // 생성
    @PostMapping("/posts")
    public ResponseEntity<Long> createPost(@Valid @RequestBody PostCreateRequest req) {
        return ResponseEntity.ok(communityService.createPost(req));
    }

    // 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        communityService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    // 댓글 생성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Long> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest req
    ) {
        return ResponseEntity.ok(communityService.createComment(postId, req));
    }

    // 댓글 삭제
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        communityService.deleteComment(postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
