package com.salgulok.log.repository;

import com.salgulok.log.domain.Log;
import com.salgulok.region.domain.Region;
import com.salgulok.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {
    // 회원별 로그 조회
    List<Log> findByUserOrderByCreatedAtDesc(User user);

    // 지역별 로그 조회 (공개 로그만)
    List<Log> findByRegionAndIsPublicTrue(Region region);

    // 로그 검색 (공개 로그만)
    List<Log> findByTitleContainingAndIsPublicTrue(String search);
  
    // 전체 공개 살구록 리스트
    List<Log> findByIsPublicTrue();

    // 특정 유저 살구록 리스트
    List<Log> findByUser_UserId(Long userId);

    // 내 살구록 리스트 (컨트롤러 단에서 userId 주입하면 위 메서드 재사용)
}
