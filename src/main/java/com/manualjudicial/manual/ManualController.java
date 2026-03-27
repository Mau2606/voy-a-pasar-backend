package com.manualjudicial.manual;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manuals")
@RequiredArgsConstructor
public class ManualController {

    private final ManualService manualService;

    /** Public catalog — no auth required. */
    @GetMapping
    public List<Manual> getAll() {
        return manualService.findAll();
    }

    @GetMapping("/{id}")
    public Manual getById(@PathVariable("id") Long id) {
        return manualService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Manual create(@RequestBody ManualDTO dto) {
        return manualService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Manual update(@PathVariable("id") Long id, @RequestBody ManualDTO dto) {
        return manualService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        manualService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/manuals/{id}/pdf
     * Accepts a multipart PDF file, saves it to disk under /uploads/pdfs/,
     * and stores the relative URL in manual.pdfUrl.
     */
    @PostMapping(value = "/{id}/pdf", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Manual> uploadPdf(
            @PathVariable("id") Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads/pdfs/").toAbsolutePath().normalize();
        java.nio.file.Files.createDirectories(uploadPath);
        String filename = java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
        java.nio.file.Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());
        String pdfUrl = "/uploads/pdfs/" + filename;
        Manual manual = manualService.setPdfUrl(id, pdfUrl);
        return ResponseEntity.ok(manual);
    }
}
