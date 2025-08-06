package com.salgulok.places.repository;

import com.salgulok.places.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByPlaceNameContaining(String keyword);

    @Query("SELECT p FROM Place p ORDER BY p.star DESC")
    List<Place> findPopularPlaces();

    @Query("SELECT p FROM Place p WHERE p.addr1 LIKE %:region% ORDER BY p.star DESC")
    List<Place> findPopularPlacesByRegion(@Param("region") String region);

    List<Place> findAll();

    @Query("SELECT p.star FROM Place p WHERE p.placeId = :placeId")
    Double findAverageRatingByPlaceId(@Param("placeId") Long placeId);
}