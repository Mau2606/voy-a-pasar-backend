package com.manualjudicial.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findTopByUserIdOrderByLoginTimestampDesc(Long userId);

    Page<UserSession> findByUserIdOrderByLoginTimestampDesc(Long userId, Pageable pageable);
}
