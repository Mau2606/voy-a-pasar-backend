package com.manualjudicial.reports;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class QuestionReportController {

    private final QuestionReportService reportService;

    // STUDENT ENDPOINTS
    @PostMapping("/questions")
    public ResponseEntity<QuestionReportDTO> createReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody QuestionReportRequest request) {
        return ResponseEntity.ok(reportService.createReport(userDetails.getUsername(), request));
    }

    // ADMIN ENDPOINTS
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QuestionReportDTO>> getPendingReports() {
        return ResponseEntity.ok(reportService.getPendingReports());
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QuestionReportDTO>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @PutMapping("/admin/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionReportDTO> resolveReport(
            @PathVariable Long id,
            @RequestBody ResolveReportRequest request) {
        return ResponseEntity.ok(reportService.resolveReport(id, request));
    }
}
