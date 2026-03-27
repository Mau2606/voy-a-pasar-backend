package com.manualjudicial.enrollment;

import com.manualjudicial.manual.Manual;
import com.manualjudicial.manual.ManualRepository;
import com.manualjudicial.users.User;
import com.manualjudicial.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ManualRepository manualRepository;

    /** Returns all manuals the authenticated user is enrolled in. */
    public List<Enrollment> getMyEnrollments(String email) {
        User user = getUser(email);
        return enrollmentRepository.findByUser(user);
    }

    /** Enrolls the user in a manual. Idempotent — no error if already enrolled. */
    @Transactional
    public Enrollment enroll(String email, Long manualId) {
        User user = getUser(email);
        Manual manual = manualRepository.findById(manualId)
                .orElseThrow(() -> new RuntimeException("Manual not found: " + manualId));

        return enrollmentRepository.findByUserAndManual(user, manual)
                .orElseGet(() -> enrollmentRepository.save(
                        Enrollment.builder()
                                .user(user)
                                .manual(manual)
                                .build()));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
