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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final UserSessionLogRepository sessionLogRepository;
    private final QuestionTimeLogRepository questionTimeLogRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ChapterRepository chapterRepository;
    private final UserSessionRepository userSessionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    
    public Page<UserSession> getSessions(Pageable pageable) {
        return userSessionRepository.findAll(pageable);
    }

    public Page<UserSession> getUserSessions(String email, Pageable pageable) {
        User user = findUser(email);
        return userSessionRepository.findByUserIdOrderByLoginTimestampDesc(user.getId(), pageable);
    }

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

    @Transactional
    public void login(String email, String ip, String userAgent) {
        User user = findUser(email);
        
        String device = "Desconocido";
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) device = "Smartphone";
            else if (ua.contains("tablet") || ua.contains("ipad")) device = "Tablet";
            else if (ua.contains("windows")) device = "PC Windows";
            else if (ua.contains("mac")) device = "Apple Mac";
            else if (ua.contains("linux")) device = "Linux";
            else device = "PC / Otro";
        }

        UserSession userSession = UserSession.builder()
                .user(user)
                .loginTimestamp(LocalDateTime.now())
                .ipAddress(ip)
                .userAgent(userAgent)
                .deviceType(device)
                .build();
        UserSession saved = userSessionRepository.save(userSession);
        
        // Resolve GeoIP Async
        if (ip != null && !ip.isBlank() && !ip.equals("127.0.0.1") && !ip.equals("0:0:0:0:0:0:0:1")) {
            final Long sessionId = saved.getId();
            final String targetIp = ip;
            CompletableFuture.runAsync(() -> {
                try {
                    String url = "http://ip-api.com/json/" + targetIp;
                    GeoResponse geo = restTemplate.getForObject(url, GeoResponse.class);
                    if (geo != null && "success".equals(geo.status)) {
                        String loc = geo.city + ", " + geo.country;
                        saveLocationToSession(sessionId, loc);
                    }
                } catch (Exception ignored) {}
            });
        }
    }
    
    @Transactional
    public void saveLocationToSession(Long sessionId, String location) {
        userSessionRepository.findById(sessionId).ifPresent(s -> {
            s.setLocation(location);
            userSessionRepository.save(s);
        });
    }

    private static class GeoResponse {
        public String status;
        public String city;
        public String country;
    }

    @Transactional
    public void logout(String email) {
        User user = findUser(email);
        userSessionRepository.findTopByUserIdOrderByLoginTimestampDesc(user.getId())
                .ifPresent(session -> {
                    LocalDateTime now = LocalDateTime.now();
                    session.setLogoutTimestamp(now);
                    if (session.getLoginTimestamp() != null) {
                        long diff = ChronoUnit.SECONDS.between(session.getLoginTimestamp(), now);
                        session.setDurationSeconds((int) diff);
                    }
                    userSessionRepository.save(session);
                });
    }

    @Transactional
    public void saveQuizAttempt(String email, Long chapterId, Double score, LocalDateTime startTime, Integer secondsUsed) {
        User user = findUser(email);
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + chapterId));

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .chapter(chapter)
                .score(score)
                .startTime(startTime)
                .secondsUsed(secondsUsed)
                .build();
        quizAttemptRepository.save(attempt);
    }
}
