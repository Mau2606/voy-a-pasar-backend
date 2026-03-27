package com.manualjudicial.enrollment;

import com.manualjudicial.manual.Manual;
import com.manualjudicial.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByUser(User user);

    Optional<Enrollment> findByUserAndManual(User user, Manual manual);

    boolean existsByUserAndManual(User user, Manual manual);
}
