package com.salgulok.log.repository;

import com.salgulok.log.domain.Log;
import com.salgulok.region.domain.Region;
import com.salgulok.user.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LogRepository extends JpaRepository<Log, Long> {
    // 회원별 로그 조회
    List<Log> findByUserOrderByCreatedAtDesc(User user);

    // 지역별 로그 조회 (공개 로그만)
    List<Log> findByRegionAndIsPublicTrue(Region region);

    // 로그 검색 (공개 로그만)
    List<Log> findByTitleContainingAndIsPublicTrue(String search);

    // 체류 전/중 여부 확인 후 log 반환(없을 시 null)
    @Query("""
        select l 
        from Log l
        where l.user.userId = :userId
          and :today between l.startDate and l.endDate
    """)
    Optional<Log> findCurrentTravelLog(@Param("userId") Long userId,
                                       @Param("today") LocalDate today);

    @Query("""
        select l
        from Log l
        where l.user.userId = :userId
          and not (l.endDate < :newStartDate or l.startDate > :newEndDate)
""")
    List<Log> findOverlappingLogs(@Param("userId") Long userId,
                                  @Param("newStartDate") LocalDate newStartDate,
                                  @Param("newEndDate") LocalDate newEndDate);

    // 전체 공개 살구록 리스트
    List<Log> findByIsPublicTrue();

    // 특정 유저 살구록 리스트
    List<Log> findByUser_UserId(Long userId);

    // 공개 로그만 좋아요 순으로 정렬
    List<Log> findByIsPublicTrueOrderByLikesDesc();


    // 내 살구록 리스트 (컨트롤러 단에서 userId 주입하면 위 메서드 재사용)

    // 특정 장소가 포함된 살구록 목록(최신순)
    @Query("""
       SELECT DISTINCT l
       FROM Template t
       JOIN t.logEntry le
       JOIN le.log l
       WHERE t.placeId = :placeId
         AND l.isPublic = true
       ORDER BY l.createdAt DESC
       """)
    List<Log> findPublicLogsByPlaceId(@Param("placeId") Long placeId);

    // 전체 반환 (Sort 필터링 추가)
    List<Log> findByIsPublicTrue(Sort sort);

    // 지역 필터링 (Sort 필터링 추가)
    List<Log> findByRegionAndIsPublicTrue(Region region, Sort sort);

    // 검색어 있는 경우 (Sort 필터링 추가)
    List<Log> findByTitleContainingAndIsPublicTrue(String title, Sort sort);

    // 검색어 + 지역 필터링 (Sort 필터링 추가)
    List<Log> findByTitleContainingAndRegionAndIsPublicTrue(String title, Region region, Sort sort);
}
