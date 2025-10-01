package com.salgulok.community.repository;

import com.salgulok.community.entity.Post;
import com.salgulok.community.entity.PostLike;
import com.salgulok.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);

    long countByPost(Post post);
}
