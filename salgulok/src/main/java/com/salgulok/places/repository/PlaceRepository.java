package com.salgulok.places.repository;

import com.salgulok.places.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByContentId(Long contentId);

    List<Place> findByPlaceNameContaining(String keyword);

    @Query("SELECT p FROM Place p ORDER BY p.star DESC")
    List<Place> findPopularPlaces();

    @Query("SELECT p FROM Place p WHERE p.addr1 = :addr1 ORDER BY p.star DESC")
    List<Place> findPopularPlacesByRegion(@Param("addr1") String addr1);

    List<Place> findAll();

    @Query("SELECT p.star FROM Place p WHERE p.placeId = :placeId")
    Double findAverageRatingByPlaceId(@Param("placeId") Long placeId);
}
