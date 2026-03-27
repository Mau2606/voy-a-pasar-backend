package com.manualjudicial.progress;

import com.manualjudicial.chapters.Chapter;
import com.manualjudicial.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressRepository extends JpaRepository<ProgressLog, Long> {
    List<ProgressLog> findByUserOrderByAttemptDateDesc(User user);

    List<ProgressLog> findByUserAndChapterIdOrderByAttemptDateDesc(User user, Long chapterId);

    List<ProgressLog> findByUserAndChapter(User user, Chapter chapter);
}
