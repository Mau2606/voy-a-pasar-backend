package com.manualjudicial.subscription;

import com.manualjudicial.telemetry.QuestionTimeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Enforces freemium restrictions:
 * - FREE users: max 3 questions per chapter
 * - PREMIUM users: unlimited access
 */
@Service
@RequiredArgsConstructor
public class FreemiumService {

    private static final int FREE_QUESTION_LIMIT = 3;

    private final SubscriptionService subscriptionService;
    private final QuestionTimeLogRepository questionTimeLogRepository;

    /**
     * Check if a user can access more questions in a given chapter.
     * Premium users always return true. Free users are limited to 3.
     */
    public boolean canAccessQuestions(Long userId, Long chapterId) {
        if (subscriptionService.hasActivePremium(userId)) {
            return true;
        }
        long answered = questionTimeLogRepository.countByUserIdAndChapterId(userId, chapterId);
        return answered < FREE_QUESTION_LIMIT;
    }

    /**
     * Returns the number of remaining free questions for this chapter.
     * Returns Integer.MAX_VALUE for premium users.
     */
    public int getRemainingFreeQuestions(Long userId, Long chapterId) {
        if (subscriptionService.hasActivePremium(userId)) {
            return Integer.MAX_VALUE;
        }
        long answered = questionTimeLogRepository.countByUserIdAndChapterId(userId, chapterId);
        return Math.max(0, (int) (FREE_QUESTION_LIMIT - answered));
    }

    public boolean isPremium(Long userId) {
        return subscriptionService.hasActivePremium(userId);
    }
}
