package com.manualjudicial.telemetry;

import com.manualjudicial.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionTimeLogRepository extends JpaRepository<QuestionTimeLog, Long> {

    List<QuestionTimeLog> findByUserOrderByAnsweredAtDesc(User user);

    @Query("SELECT q FROM QuestionTimeLog q WHERE q.user = :user AND q.chapter.id = :chapterId ORDER BY q.answeredAt DESC")
    List<QuestionTimeLog> findByUserAndChapter(User user, Long chapterId);

    /** Count how many questions a user has answered in a given chapter (for freemium limits). */
    @Query("SELECT COUNT(q) FROM QuestionTimeLog q WHERE q.user.id = :userId AND q.chapter.id = :chapterId")
    long countByUserIdAndChapterId(Long userId, Long chapterId);
}
