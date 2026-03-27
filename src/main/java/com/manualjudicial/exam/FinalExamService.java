package com.manualjudicial.exam;

import com.manualjudicial.chapters.Chapter;
import com.manualjudicial.chapters.ChapterRepository;
import com.manualjudicial.chapters.ChapterUnlockService;
import com.manualjudicial.questions.Question;
import com.manualjudicial.questions.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinalExamService {

    private static final int EXAM_QUESTION_COUNT = 30;
    private static final double PASS_THRESHOLD = 70.0;

    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final ChapterUnlockService chapterUnlockService;

    /**
     * Returns 30 randomly-selected questions as DTOs (without answers)
     * from all chapters of the given manual.
     */
    public List<ExamQuestionDTO> getFinalExamQuestions(String email, Long manualId) {
        if (!chapterUnlockService.allChaptersPassed(email, manualId)) {
            throw new IllegalStateException(
                    "Debes aprobar todos los capítulos antes de acceder al Examen Final.");
        }

        List<Chapter> chapters = chapterRepository
                .findByManualIdOrderByOrderIndexAsc(manualId);

        List<Question> allQuestions = chapters.stream()
                .flatMap(ch -> questionRepository.findByChapterId(ch.getId()).stream())
                .collect(Collectors.toList());

        Collections.shuffle(allQuestions);

        return allQuestions.stream()
                .limit(EXAM_QUESTION_COUNT)
                .map(q -> ExamQuestionDTO.builder()
                        .id(q.getId())
                        .statement(q.getStatement())
                        .optA(q.getOptA())
                        .optB(q.getOptB())
                        .optC(q.getOptC())
                        .optD(q.getOptD())
                        .optE(q.getOptE())
                        .questionType(q.getType().name())
                        .chapterId(q.getChapter().getId())
                        .chapterTitle(q.getChapter().getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Evaluates a submitted exam: compares each answer to the correct one,
     * computes the score, and returns detailed results with explanations.
     */
    public ExamResultDTO evaluateExam(String email, ExamSubmissionDTO submission) {
        List<ExamResultDTO.QuestionResultDTO> results = new ArrayList<>();
        int correctCount = 0;

        for (ExamSubmissionDTO.AnswerDTO answer : submission.getAnswers()) {
            Question q = questionRepository.findById(answer.getQuestionId())
                    .orElse(null);
            if (q == null) continue;

            boolean isCorrect = q.getCorrectOpt().equalsIgnoreCase(answer.getSelectedOpt());
            if (isCorrect) correctCount++;

            results.add(ExamResultDTO.QuestionResultDTO.builder()
                    .questionId(q.getId())
                    .statement(q.getStatement())
                    .optA(q.getOptA())
                    .optB(q.getOptB())
                    .optC(q.getOptC())
                    .optD(q.getOptD())
                    .optE(q.getOptE())
                    .selectedOpt(answer.getSelectedOpt())
                    .correctOpt(q.getCorrectOpt())
                    .isCorrect(isCorrect)
                    .explanation(q.getExplanation())
                    .pageReference(q.getPageReference())
                    .questionType(q.getType().name())
                    .build());
        }

        int total = results.size();
        double pct = total > 0 ? ((double) correctCount / total) * 100 : 0;

        return ExamResultDTO.builder()
                .totalQuestions(total)
                .correctCount(correctCount)
                .scorePercentage(Math.round(pct * 100.0) / 100.0)
                .passed(pct >= PASS_THRESHOLD)
                .results(results)
                .build();
    }
}
