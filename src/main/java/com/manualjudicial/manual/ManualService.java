package com.manualjudicial.manual;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualService {

    private final ManualRepository manualRepository;

    @Cacheable("manuals")
    public List<Manual> findAll() {
        return manualRepository.findAll();
    }

    @Cacheable(value = "manualById", key = "#id")
    public Manual findById(Long id) {
        return manualRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manual not found: " + id));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "manuals", allEntries = true),
        @CacheEvict(value = "manualById", allEntries = true)
    })
    public Manual create(ManualDTO dto) {
        Manual manual = Manual.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .pdfUrl(dto.getPdfUrl())
                .build();
        return manualRepository.save(manual);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "manuals", allEntries = true),
        @CacheEvict(value = "manualById", key = "#id")
    })
    public Manual update(Long id, ManualDTO dto) {
        Manual manual = findById(id);
        manual.setTitle(dto.getTitle());
        manual.setDescription(dto.getDescription());
        manual.setImageUrl(dto.getImageUrl());
        if (dto.getPdfUrl() != null) manual.setPdfUrl(dto.getPdfUrl());
        return manualRepository.save(manual);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "manuals", allEntries = true),
        @CacheEvict(value = "manualById", key = "#id")
    })
    public Manual setPdfUrl(Long id, String pdfUrl) {
        Manual manual = findById(id);
        manual.setPdfUrl(pdfUrl);
        return manualRepository.save(manual);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "manuals", allEntries = true),
        @CacheEvict(value = "manualById", key = "#id")
    })
    public void delete(Long id) {
        manualRepository.deleteById(id);
    }
}
