package com.manualjudicial.chapters;

import com.manualjudicial.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterReadStatusRepository extends JpaRepository<ChapterReadStatus, Long> {
    
    // Support querying all read statuses in a manual for a user
    List<ChapterReadStatus> findByUserAndChapter_ManualId(User user, Long manualId);

    // Support querying a single read status toggle
    Optional<ChapterReadStatus> findByUserAndChapter(User user, Chapter chapter);
}
