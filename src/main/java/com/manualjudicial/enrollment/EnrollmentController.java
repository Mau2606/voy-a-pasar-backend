package com.manualjudicial.enrollment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /** Returns the list of manuals the current user is enrolled in. */
    @GetMapping("/my")
    public List<Enrollment> getMyEnrollments(@AuthenticationPrincipal UserDetails userDetails) {
        return enrollmentService.getMyEnrollments(userDetails.getUsername());
    }

    /**
     * Enrolls the current user in a manual.
     * Idempotent – calling it again when already enrolled returns the existing
     * enrollment.
     */
    @PostMapping("/{manualId}")
    public ResponseEntity<Enrollment> enroll(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("manualId") Long manualId) {
        Enrollment enrollment = enrollmentService.enroll(userDetails.getUsername(), manualId);
        return ResponseEntity.ok(enrollment);
    }
}
