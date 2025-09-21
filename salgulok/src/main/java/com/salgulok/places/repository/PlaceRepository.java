package com.salgulok.places.repository;

import com.salgulok.places.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByContentId(Long contentId);

    List<Place> findByPlaceNameContaining(String keyword);

    @Query("SELECT p FROM Place p ORDER BY p.star DESC")
    List<Place> findPopularPlaces();

    //평점 없는곳 제외
    @Query("SELECT p FROM Place p WHERE p.regionId = :regionId AND p.starCount > 0 ORDER BY p.star DESC")
    List<Place> findPopularPlacesByRegion(@Param("regionId") Long regionId);

//    @Query("SELECT p FROM Place p WHERE p.addr1 = :addr1 ORDER BY p.star DESC")
//    List<Place> findPopularPlacesByRegion(@Param("addr1") String addr1);

    List<Place> findAll();

    List<Place> findByContentIdIn(Collection<Long> contentIds);

    @Query("SELECT p.star FROM Place p WHERE p.placeId = :placeId")
    Double findAverageRatingByPlaceId(@Param("placeId") Long placeId);
}
