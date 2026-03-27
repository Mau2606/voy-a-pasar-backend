package com.manualjudicial.exam;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class FinalExamController {

    private final FinalExamService finalExamService;

    /**
     * GET /api/exams/manual/{manualId}
     * Returns 30 randomly-selected questions WITHOUT correct answers.
     * Returns 403 if the user has not yet passed all chapters.
     */
    @GetMapping("/manual/{manualId}")
    public ResponseEntity<?> getFinalExam(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("manualId") Long manualId) {
        try {
            List<ExamQuestionDTO> questions = finalExamService
                    .getFinalExamQuestions(userDetails.getUsername(), manualId);
            return ResponseEntity.ok(questions);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        }
    }

    /**
     * POST /api/exams/submit
     * Receives the complete exam submission, evaluates it,
     * and returns detailed results with explanations.
     */
    @PostMapping("/submit")
    public ResponseEntity<ExamResultDTO> submitExam(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ExamSubmissionDTO submission) {
        ExamResultDTO result = finalExamService.evaluateExam(
                userDetails.getUsername(), submission);
        return ResponseEntity.ok(result);
    }
}
