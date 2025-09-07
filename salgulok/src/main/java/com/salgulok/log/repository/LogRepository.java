package com.salgulok.log.repository;

import com.salgulok.log.domain.Log;
import com.salgulok.region.domain.Region;
import com.salgulok.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findByUserOrderByCreatedAtDesc(User user);

    List<Log> findByRegion(Region region);

    List<Log> findByTitleContaining(String search);
}
