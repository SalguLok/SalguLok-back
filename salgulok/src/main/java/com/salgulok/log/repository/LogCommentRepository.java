package com.salgulok.log.repository;

import com.salgulok.log.domain.LogComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogCommentRepository extends JpaRepository<LogComment, Long> {
    Page<LogComment> findByLogLogIdOrderByCreatedAtDesc(Long logId, Pageable pageable);
}