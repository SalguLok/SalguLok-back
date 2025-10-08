package com.salgulok.log.repository;

import com.salgulok.log.domain.Log;
import com.salgulok.region.domain.Region;
import com.salgulok.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LogRepository extends JpaRepository<Log, Long> {
    // 내 로그 조회
    Page<Log> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 타인 로그 조회
    Page<Log> findByUserAndIsPublicTrueAndIsUploadTrueOrderByCreatedAtDesc(User user, Pageable pageable);

    // 지역별 로그 조회 (공개 로그만)
    List<Log> findByRegionAndIsPublicTrueAndIsUploadTrue(Region region);

    // 체류 전/중 여부 확인 후 log 반환(없을 시 null)
    @Query("""
        select l 
        from Log l
        where l.user.userId = :userId
          and :today between l.startDate and l.endDate
    """)
    Optional<Log> findCurrentTravelLog(@Param("userId") Long userId,
                                       @Param("today") LocalDate today);

    // 로그 생성 시 해당 날짜에 여행중인지 확인
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
         AND l.isUpload = true
       ORDER BY l.createdAt DESC
       """)
    List<Log> findPublicLogsByPlaceId(@Param("placeId") Long placeId);

    // 전체 공개 + 게시 목록 (최신순)
    List<Log> findByIsPublicTrueAndIsUploadTrueOrderByCreatedAtDesc();

    // 제목 검색: 공개 + 게시 (최신순)
    List<Log> findByTitleContainingAndIsPublicTrueAndIsUploadTrueOrderByCreatedAtDesc(String search);

    // 지역별: 공개 + 게시 (최신순)
    List<Log> findByRegionAndIsPublicTrueAndIsUploadTrueOrderByCreatedAtDesc(Region region);

    // 인기순: 공개 + 게시 (좋아요 desc, 조회수 desc, 생성일 desc)
    @Query("""
       select l from Log l
       where l.isPublic = true and l.isUpload = true
       order by l.likes desc, l.view desc, l.createdAt desc
       """)
    List<Log> findPopularPublicAndUploaded();

    //장소별 로그 개수 집계
    @Query("""
    select t.placeId as placeId, count(distinct l.logId) as cnt
    from Template t
    join t.logEntry le
    join le.log l
    where l.isPublic = true
      and l.isUpload = true
      and t.placeId in :placeIds
    group by t.placeId
""")
    List<Object[]> countUploadedPublicLogsByPlaceIds(@Param("placeIds") Collection<Long> placeIds);

    // 장소 기반: 공개 + 게시 (최신순)
    @Query("""
   select distinct l
   from Template t
   join t.logEntry le
   join le.log l
   where t.placeId = :placeId
     and l.isPublic = true
     and l.isUpload = true
   order by l.createdAt desc
   """)
    List<Log> findPublicAndUploadedLogsByPlaceId(@Param("placeId") Long placeId);

    // 전체 반환 (Sort 필터링 추가)
    Page<Log> findByIsPublicTrueAndIsUploadTrue(Pageable pageable);

    // 지역 필터링 (Sort 필터링 추가)
    Page<Log> findByRegionAndIsPublicTrueAndIsUploadTrue(Region region, Pageable pageable);

    // 검색어 있는 경우 (Sort 필터링 추가)
    Page<Log> findByTitleContainingAndIsPublicTrueAndIsUploadTrue(String title, Pageable pageable);

    // 검색어 + 지역 필터링 (Sort 필터링 추가)
    Page<Log> findByTitleContainingAndRegionAndIsPublicTrueAndIsUploadTrue(String title, Region region, Pageable pageable);
}