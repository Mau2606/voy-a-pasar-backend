package com.manualjudicial.telemetry;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @PostMapping("/session")
    public ResponseEntity<Void> logSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SessionLogDTO dto,
            HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        telemetryService.saveSessionLog(userDetails.getUsername(), dto, ip);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/question-time")
    public ResponseEntity<Void> logQuestionTime(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody QuestionTimeDTO dto) {
        telemetryService.saveQuestionTime(userDetails.getUsername(), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/question-times/batch")
    public ResponseEntity<Void> logQuestionTimesBatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<QuestionTimeDTO> dtos) {
        telemetryService.saveQuestionTimesBatch(userDetails.getUsername(), dtos);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sessions")
    public Page<UserSession> getSessions(Pageable pageable) {
        return telemetryService.getSessions(pageable);
    }

    @GetMapping("/my-sessions")
    public Page<UserSession> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return telemetryService.getUserSessions(userDetails.getUsername(), pageable);
    }

    @PostMapping("/session/login")
    public ResponseEntity<Void> logLogin(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        telemetryService.login(userDetails.getUsername(), ip, userAgent);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/session/logout")
    public ResponseEntity<Void> logLogout(@AuthenticationPrincipal UserDetails userDetails) {
        telemetryService.logout(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/quiz-attempt")
    public ResponseEntity<Void> logQuizAttempt(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody QuizAttemptRequest request) {
        telemetryService.saveQuizAttempt(
                userDetails.getUsername(),
                request.getChapterId(),
                request.getScore(),
                request.getStartTime(),
                request.getSecondsUsed()
        );
        return ResponseEntity.ok().build();
    }
}
