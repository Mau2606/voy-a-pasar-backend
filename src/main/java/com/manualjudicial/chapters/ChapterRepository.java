package com.manualjudicial.chapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findByManualIdOrderByOrderIndexAsc(Long manualId);
}
