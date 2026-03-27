package com.manualjudicial.telemetry;

import com.manualjudicial.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSessionLogRepository extends JpaRepository<UserSessionLog, Long> {
    List<UserSessionLog> findByUserOrderBySessionStartDesc(User user);
}
