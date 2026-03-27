package com.manualjudicial.progress;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/me")
    public List<ProgressLog> getMyProgress(Authentication authentication) {
        return progressService.getMyProgress(authentication.getName());
    }

    @PostMapping
    public ResponseEntity<ProgressLog> save(@RequestBody ProgressDTO dto,
            Authentication authentication) {
        ProgressLog saved = progressService.save(authentication.getName(), dto);
        return ResponseEntity.ok(saved);
    }
}
