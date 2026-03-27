package com.manualjudicial.questions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByChapterId(Long chapterId);
    long countByChapterId(Long chapterId);
}
