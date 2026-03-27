package com.manualjudicial.questions;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public List<Question> getAll() {
        return questionService.findAll();
    }

    @GetMapping("/chapter/{chapterId}")
    public List<Question> getByChapter(
            @PathVariable("chapterId") Long chapterId,
            @RequestParam(name = "limit", required = false) Integer limit) {
        if (limit != null && limit > 0) {
            return questionService.findByChapterWithLimit(chapterId, limit);
        }
        return questionService.findByChapter(chapterId);
    }

    @GetMapping("/{id}")
    public Question getById(@PathVariable("id") Long id) {
        return questionService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Question create(@RequestBody QuestionDTO dto) {
        return questionService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Question update(@PathVariable("id") Long id, @RequestBody QuestionDTO dto) {
        return questionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
