package com.manualjudicial.chapters;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;
    private final ChapterUnlockService chapterUnlockService;

    @GetMapping
    public List<Chapter> getAll() {
        return chapterService.findAll();
    }

    @GetMapping("/manual/{manualId}")
    public List<Chapter> getByManual(@PathVariable("manualId") Long manualId) {
        return chapterService.findByManual(manualId);
    }

    @GetMapping("/{id}")
    public Chapter getById(@PathVariable("id") Long id) {
        return chapterService.findById(id);
    }

    /**
     * Returns unlock status for all chapters in a manual, evaluated against
     * the current user's threshold.
     */
    @GetMapping("/manual/{manualId}/unlock-status")
    public List<ChapterUnlockService.UnlockStatusDTO> getUnlockStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("manualId") Long manualId) {
        return chapterUnlockService.getUnlockStatusForManual(userDetails.getUsername(), manualId);
    }

    @PostMapping("/{chapterId}/read-status")
    public ResponseEntity<Void> toggleReadStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("chapterId") Long chapterId,
            @RequestParam("isRead") boolean isRead) {
        chapterUnlockService.toggleReadStatus(userDetails.getUsername(), chapterId, isRead);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Chapter create(@RequestBody ChapterDTO dto) {
        return chapterService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Chapter update(@PathVariable("id") Long id, @RequestBody ChapterDTO dto) {
        return chapterService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        chapterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/chapters/{id}/pdf
     * Accepts a multipart PDF file, saves it to disk under /uploads/pdfs/,
     * and stores the relative URL in chapter.pdfUrl.
     */
    @PostMapping(value = "/{id}/pdf", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Chapter> uploadPdf(
            @PathVariable("id") Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads/pdfs/").toAbsolutePath().normalize();
        java.nio.file.Files.createDirectories(uploadPath);
        String filename = java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
        java.nio.file.Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());
        String pdfUrl = "/uploads/pdfs/" + filename;
        Chapter chapter = chapterService.setPdfUrl(id, pdfUrl);
        return ResponseEntity.ok(chapter);
    }
}
