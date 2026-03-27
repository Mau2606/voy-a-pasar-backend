package com.manualjudicial.progress;

import com.manualjudicial.chapters.Chapter;
import com.manualjudicial.chapters.ChapterRepository;
import com.manualjudicial.users.User;
import com.manualjudicial.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

        private final ProgressRepository progressRepository;
        private final UserRepository userRepository;
        private final ChapterRepository chapterRepository;

        public List<ProgressLog> getMyProgress(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
                return progressRepository.findByUserOrderByAttemptDateDesc(user);
        }

        @Transactional
        public ProgressLog save(String email, ProgressDTO dto) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
                Chapter chapter = chapterRepository.findById(dto.getChapterId())
                                .orElseThrow(() -> new RuntimeException("Chapter not found: " + dto.getChapterId()));

                boolean passed = dto.getScorePercentage() >= user.getCustomThreshold();

                ProgressLog log = ProgressLog.builder()
                                .user(user)
                                .chapter(chapter)
                                .scorePercentage(dto.getScorePercentage())
                                .correctAnswers(dto.getCorrectAnswers() != null ? dto.getCorrectAnswers() : 0)
                                .totalQuestions(dto.getTotalQuestions() != null ? dto.getTotalQuestions() : 0)
                                .isPassed(passed)
                                .attemptDate(LocalDateTime.now())
                                .build();
                return progressRepository.save(log);
        }
}
