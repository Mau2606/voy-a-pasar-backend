package com.manualjudicial.telemetry;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
