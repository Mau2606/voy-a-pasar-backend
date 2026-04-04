package com.manualjudicial.questions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByChapterId(Long chapterId);
    long countByChapterId(Long chapterId);

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT q.* FROM questions q INNER JOIN chapters c ON q.chapter_id = c.id WHERE c.manual_id = :manualId ORDER BY RANDOM() LIMIT :limit",
        nativeQuery = true
    )
    List<Question> findRandomByManual(@org.springframework.data.repository.query.Param("manualId") Long manualId, @org.springframework.data.repository.query.Param("limit") int limit);
}
