// repository/CommentRepository.java
package com.salgulok.community.repository;

import com.salgulok.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostId(Long postId, Pageable pageable);
    Optional<Comment> findByIdAndPostId(Long id, Long postId);

    long countByPostId(Long postId);

    interface CommentCount {
        Long getPostId();
        Long getCount();
    }

    @Query("""
        select c.post.id as postId, count(c) as count
        from Comment c
        where c.post.id in :postIds
        group by c.post.id
    """)
    List<CommentCount> countByPostIdIn(@Param("postIds") List<Long> postIds);
}

