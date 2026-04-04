package com.manualjudicial.reports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionReportRepository extends JpaRepository<QuestionReport, Long> {
    List<QuestionReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);
    List<QuestionReport> findAllByOrderByCreatedAtDesc();
}
