package com.manualjudicial.telemetry;

import com.manualjudicial.chapters.Chapter;
import com.manualjudicial.chapters.ChapterRepository;
import com.manualjudicial.questions.Question;
import com.manualjudicial.questions.QuestionRepository;
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
public class TelemetryService {

    private final UserSessionLogRepository sessionLogRepository;
    private final QuestionTimeLogRepository questionTimeLogRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ChapterRepository chapterRepository;

    @Transactional
    public UserSessionLog saveSessionLog(String email, SessionLogDTO dto, String ipAddress) {
        User user = findUser(email);
        UserSessionLog log = UserSessionLog.builder()
                .user(user)
                .sessionStart(dto.getSessionStart())
                .sessionEnd(dto.getSessionEnd())
                .durationMs(dto.getDurationMs())
                .ipAddress(ipAddress)
                .build();
        return sessionLogRepository.save(log);
    }

    @Transactional
    public QuestionTimeLog saveQuestionTime(String email, QuestionTimeDTO dto) {
        User user = findUser(email);
        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found: " + dto.getQuestionId()));
        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + dto.getChapterId()));

        QuestionTimeLog log = QuestionTimeLog.builder()
                .user(user)
                .question(question)
                .chapter(chapter)
                .answerTimeMs(dto.getAnswerTimeMs())
                .answeredAt(LocalDateTime.now())
                .isCorrect(dto.getIsCorrect())
                .build();
        return questionTimeLogRepository.save(log);
    }

    @Transactional
    public void saveQuestionTimesBatch(String email, List<QuestionTimeDTO> dtos) {
        for (QuestionTimeDTO dto : dtos) {
            saveQuestionTime(email, dto);
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
