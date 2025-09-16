package com.salgulok.log.repository;

import com.salgulok.log.domain.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {
    // 전체 공개 살구록 리스트
    List<Log> findByIsPublicTrue();

    // 특정 유저 살구록 리스트
    List<Log> findByUser_UserId(Long userId);

    // 공개 로그만 좋아요 순으로 정렬
    List<Log> findByIsPublicTrueOrderByLikesDesc();


    // 내 살구록 리스트 (컨트롤러 단에서 userId 주입하면 위 메서드 재사용)
}
