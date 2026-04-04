package com.manualjudicial.chapters;

import com.manualjudicial.progress.ProgressLog;
import com.manualjudicial.progress.ProgressRepository;
import com.manualjudicial.users.User;
import com.manualjudicial.users.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChapterUnlockService {

    private final ChapterRepository chapterRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ChapterReadStatusRepository chapterReadStatusRepository;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the unlock status for every chapter in a manual, evaluated
     * against the user's personalised threshold.
     *
     * Rule: orderIndex == 1 → always unlocked.
     * orderIndex == N → unlocked when the user's best score in the
     * chapter with orderIndex N-1 (same manual) is
     * >= user.customThreshold.
     *
     * OPTIMIZED: Loads all progress for the manual in ONE query instead of
     * one query per chapter (N+1 fix).
     */
    public List<UnlockStatusDTO> getUnlockStatusForManual(String email, Long manualId) {
        User user = getUser(email);
        int threshold = user.getCustomThreshold();

        List<Chapter> chapters = chapterRepository
                .findByManualIdOrderByOrderIndexAsc(manualId);

        // ONE single query for all progress in this manual
        Map<Long, Double> bestScoreByChapterId = progressRepository
                .findByUserAndManualId(user, manualId)
                .stream()
                .collect(Collectors.groupingBy(
                    log -> log.getChapter().getId(),
                    Collectors.collectingAndThen(
                        Collectors.maxBy(Comparator.comparingDouble(ProgressLog::getScorePercentage)),
                        opt -> opt.map(ProgressLog::getScorePercentage).orElse(0.0)
                    )
                ));

        // Single query for read statuses
        Map<Long, Boolean> readStatusByChapterId = chapterReadStatusRepository
                .findByUserAndChapter_ManualId(user, manualId)
                .stream()
                .collect(Collectors.toMap(
                    status -> status.getChapter().getId(),
                    ChapterReadStatus::getIsRead
                ));

        List<UnlockStatusDTO> result = new ArrayList<>();
        for (Chapter chapter : chapters) {
            double bestScore = bestScoreByChapterId.getOrDefault(chapter.getId(), 0.0);
            boolean unlocked = isChapterUnlocked(chapter, chapters, bestScoreByChapterId, threshold);
            boolean isRead = readStatusByChapterId.getOrDefault(chapter.getId(), false);
            result.add(UnlockStatusDTO.builder()
                    .chapterId(chapter.getId())
                    .chapterTitle(chapter.getTitle())
                    .orderIndex(chapter.getOrderIndex())
                    .unlocked(unlocked)
                    .bestScore(bestScore)
                    .threshold(threshold)
                    .isRead(isRead)
                    .build());
        }
        return result;
    }

    /**
     * Toggles the read status of a chapter for the current user.
     */
    public void toggleReadStatus(String email, Long chapterId, boolean isRead) {
        User user = getUser(email);
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + chapterId));

        Optional<ChapterReadStatus> optStatus = chapterReadStatusRepository.findByUserAndChapter(user, chapter);
        if (optStatus.isPresent()) {
            ChapterReadStatus status = optStatus.get();
            status.setIsRead(isRead);
            status.setReadAt(isRead ? java.time.LocalDateTime.now() : null);
            chapterReadStatusRepository.save(status);
        } else {
            ChapterReadStatus newStatus = ChapterReadStatus.builder()
                    .user(user)
                    .chapter(chapter)
                    .isRead(isRead)
                    .readAt(isRead ? java.time.LocalDateTime.now() : null)
                    .build();
            chapterReadStatusRepository.save(newStatus);
        }
    }

    /**
     * Checks whether all chapters in a manual have been passed by the user
     * (i.e., best score >= threshold). Used to enable the Final Exam.
     *
     * OPTIMIZED: Uses the same single-query approach.
     */
    public boolean allChaptersPassed(String email, Long manualId) {
        User user = getUser(email);
        int threshold = user.getCustomThreshold();
        List<Chapter> chapters = chapterRepository
                .findByManualIdOrderByOrderIndexAsc(manualId);

        Map<Long, Double> bestScoreByChapterId = progressRepository
                .findByUserAndManualId(user, manualId)
                .stream()
                .collect(Collectors.groupingBy(
                    log -> log.getChapter().getId(),
                    Collectors.collectingAndThen(
                        Collectors.maxBy(Comparator.comparingDouble(ProgressLog::getScorePercentage)),
                        opt -> opt.map(ProgressLog::getScorePercentage).orElse(0.0)
                    )
                ));

        return chapters.stream()
                .allMatch(ch -> bestScoreByChapterId.getOrDefault(ch.getId(), 0.0) >= threshold);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean isChapterUnlocked(Chapter chapter, List<Chapter> allChapters,
            Map<Long, Double> bestScores, int threshold) {
        if (chapter.getOrderIndex() == 1)
            return true; // first chapter always open

        Optional<Chapter> previous = allChapters.stream()
                .filter(c -> c.getOrderIndex() == chapter.getOrderIndex() - 1)
                .findFirst();

        if (previous.isEmpty())
            return false; // safety guard

        double bestInPrev = bestScores.getOrDefault(previous.get().getId(), 0.0);
        return bestInPrev >= threshold;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    // ── DTO ───────────────────────────────────────────────────────────────────

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UnlockStatusDTO {
        private Long chapterId;
        private String chapterTitle;
        private Integer orderIndex;
        private boolean unlocked;
        private double bestScore;
        private int threshold;
        private boolean isRead;
    }
}
