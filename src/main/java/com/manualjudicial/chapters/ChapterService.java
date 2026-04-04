package com.manualjudicial.chapters;

import com.manualjudicial.manual.Manual;
import com.manualjudicial.manual.ManualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final ManualRepository manualRepository;

    @Cacheable("chapters")
    public List<Chapter> findAll() {
        return chapterRepository.findAll();
    }

    @Cacheable(value = "chaptersByManual", key = "#manualId")
    public List<Chapter> findByManual(Long manualId) {
        return chapterRepository.findByManualIdOrderByOrderIndexAsc(manualId);
    }

    public Chapter findById(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + id));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "chapters", allEntries = true),
        @CacheEvict(value = "chaptersByManual", allEntries = true)
    })
    public Chapter create(ChapterDTO dto) {
        Manual manual = manualRepository.findById(dto.getManualId())
                .orElseThrow(() -> new RuntimeException("Manual not found: " + dto.getManualId()));
        // Default number to orderIndex if not provided
        Integer number = dto.getNumber() != null ? dto.getNumber() : dto.getOrderIndex();
        Chapter chapter = Chapter.builder()
                .manual(manual)
                .orderIndex(dto.getOrderIndex())
                .number(number)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .pdfUrl(dto.getPdfUrl())
                .startPage(dto.getStartPage())
                .build();
        return chapterRepository.save(chapter);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "chapters", allEntries = true),
        @CacheEvict(value = "chaptersByManual", allEntries = true)
    })
    public Chapter update(Long id, ChapterDTO dto) {
        Chapter chapter = findById(id);
        if (dto.getManualId() != null) {
            Manual manual = manualRepository.findById(dto.getManualId())
                    .orElseThrow(() -> new RuntimeException("Manual not found: " + dto.getManualId()));
            chapter.setManual(manual);
        }
        if (dto.getOrderIndex() != null)
            chapter.setOrderIndex(dto.getOrderIndex());
        if (dto.getNumber() != null)
            chapter.setNumber(dto.getNumber());
        if (dto.getTitle() != null)
            chapter.setTitle(dto.getTitle());
        if (dto.getDescription() != null)
            chapter.setDescription(dto.getDescription());
        if (dto.getPdfUrl() != null)
            chapter.setPdfUrl(dto.getPdfUrl());
        if (dto.getStartPage() != null)
            chapter.setStartPage(dto.getStartPage());
        return chapterRepository.save(chapter);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "chapters", allEntries = true),
        @CacheEvict(value = "chaptersByManual", allEntries = true)
    })
    public Chapter setPdfUrl(Long id, String pdfUrl) {
        Chapter chapter = findById(id);
        chapter.setPdfUrl(pdfUrl);
        return chapterRepository.save(chapter);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "chapters", allEntries = true),
        @CacheEvict(value = "chaptersByManual", allEntries = true)
    })
    public void delete(Long id) {
        chapterRepository.deleteById(id);
    }
}
