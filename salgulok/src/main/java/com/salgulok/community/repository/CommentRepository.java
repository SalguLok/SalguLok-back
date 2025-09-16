// repository/CommentRepository.java
package com.salgulok.community.repository;

import com.salgulok.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> { }
