package com.manualjudicial.chapters;

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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChapterUnlockService {

    private final ChapterRepository chapterRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the unlock status for every chapter in a manual, evaluated
     * against the user's personalised threshold.
     *
     * Rule: orderIndex == 1 → always unlocked.
     * orderIndex == N → unlocked when the user's best score in the
     * chapter with orderIndex N-1 (same manual) is
     * >= user.customThreshold.
     */
    public List<UnlockStatusDTO> getUnlockStatusForManual(String email, Long manualId) {
        User user = getUser(email);
        int threshold = user.getCustomThreshold();

        List<Chapter> chapters = chapterRepository
                .findByManualIdOrderByOrderIndexAsc(manualId);

        List<UnlockStatusDTO> result = new ArrayList<>();
        for (Chapter chapter : chapters) {
            boolean unlocked = isChapterUnlocked(user, chapter, chapters, threshold);
            double bestScore = getBestScore(user, chapter);
            result.add(UnlockStatusDTO.builder()
                    .chapterId(chapter.getId())
                    .chapterTitle(chapter.getTitle())
                    .orderIndex(chapter.getOrderIndex())
                    .unlocked(unlocked)
                    .bestScore(bestScore)
                    .threshold(threshold)
                    .build());
        }
        return result;
    }

    /**
     * Checks whether all chapters in a manual have been passed by the user
     * (i.e., best score >= threshold). Used to enable the Final Exam.
     */
    public boolean allChaptersPassed(String email, Long manualId) {
        User user = getUser(email);
        int threshold = user.getCustomThreshold();
        List<Chapter> chapters = chapterRepository
                .findByManualIdOrderByOrderIndexAsc(manualId);
        return chapters.stream()
                .allMatch(ch -> getBestScore(user, ch) >= threshold);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean isChapterUnlocked(User user, Chapter chapter,
            List<Chapter> allChapters, int threshold) {
        if (chapter.getOrderIndex() == 1)
            return true; // first chapter always open

        Optional<Chapter> previous = allChapters.stream()
                .filter(c -> c.getOrderIndex() == chapter.getOrderIndex() - 1)
                .findFirst();

        if (previous.isEmpty())
            return false; // safety guard

        double bestInPrev = getBestScore(user, previous.get());
        return bestInPrev >= threshold;
    }

    private double getBestScore(User user, Chapter chapter) {
        return progressRepository
                .findByUserAndChapter(user, chapter)
                .stream()
                .mapToDouble(log -> log.getScorePercentage())
                .max()
                .orElse(0.0);
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
    }
}
