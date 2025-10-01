package com.salgulok.log.repository;

import com.salgulok.log.domain.Log;
import com.salgulok.log.domain.LogLike;
import com.salgulok.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LogLikeRepository extends JpaRepository<LogLike, Long> {

    boolean existsByUserAndLog(User user, Log log);

    Optional<LogLike> findByUserAndLog(User user, Log log);

    long countByLog(Log log);
}


