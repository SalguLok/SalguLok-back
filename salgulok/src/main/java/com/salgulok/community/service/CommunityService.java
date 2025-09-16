// service/CommunityService.java
package com.salgulok.community.service;

import com.salgulok.community.domain.PostSearchCond;
import com.salgulok.community.dto.request.CommentCreateRequest;
import com.salgulok.community.dto.request.PostCreateRequest;
import com.salgulok.community.dto.response.CommentResponse;
import com.salgulok.community.dto.response.PostResponse;
import com.salgulok.community.entity.Comment;
import com.salgulok.community.entity.Post;
import com.salgulok.community.repository.CommentRepository;
import com.salgulok.community.repository.PostRepository;
import com.salgulok.community.spec.PostSpecs;
import com.salgulok.user.domain.User;
import com.salgulok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public Page<PostResponse> searchPosts(PostSearchCond cond, Pageable pageable) {
        Specification<Post> spec = Specification.allOf();

        if (StringUtils.hasText(cond.getRegion())) {
            spec = spec.and(PostSpecs.authorRegionEq(cond.getRegion()));
        }
        if (StringUtils.hasText(cond.getTopic())) {
            spec = spec.and(PostSpecs.topicEq(cond.getTopic()));
        }
        if ("STAYING".equalsIgnoreCase(cond.getStatus())) {
            spec = spec.and(PostSpecs.authorIsStaying());
        }
        return postRepository.findAll(spec, pageable).map(PostResponse::from);
    }

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        return PostResponse.from(post);
    }

    @Transactional
    public Long createPost(PostCreateRequest req) {
        User author = userRepository.findById(req.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getAuthorId()));
        Post post = new Post(author, req.getTopic(), req.getContent());
        return postRepository.save(post).getId();
    }

    @Transactional
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) throw new IllegalArgumentException("Post not found: " + postId);
        postRepository.deleteById(postId);
    }

    @Transactional
    public Long createComment(Long postId, CommentCreateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        User author = userRepository.findById(req.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getAuthorId()));
        Comment comment = new Comment(post, author, req.getContent());
        return commentRepository.save(comment).getId();
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        if (!c.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Comment does not belong to post");
        }
        commentRepository.delete(c);
    }
}
