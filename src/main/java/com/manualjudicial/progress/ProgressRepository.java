package com.manualjudicial.progress;

import com.manualjudicial.chapters.Chapter;
import com.manualjudicial.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProgressRepository extends JpaRepository<ProgressLog, Long> {
    List<ProgressLog> findByUserOrderByAttemptDateDesc(User user);

    List<ProgressLog> findByUserAndChapterIdOrderByAttemptDateDesc(User user, Long chapterId);

    List<ProgressLog> findByUserAndChapter(User user, Chapter chapter);

    /**
     * Fetches all progress logs for a user within a specific manual in one query,
     * avoiding the N+1 problem in ChapterUnlockService.
     */
    @Query("SELECT p FROM ProgressLog p WHERE p.user = :user AND p.chapter.manual.id = :manualId")
    List<ProgressLog> findByUserAndManualId(@Param("user") User user, @Param("manualId") Long manualId);
}
