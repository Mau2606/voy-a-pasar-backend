package com.manualjudicial.exam;

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

        List<Question> allQuestions = questionRepository.findRandomByManual(manualId, EXAM_QUESTION_COUNT);

        return allQuestions.stream()
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

        List<Long> questionIds = submission.getAnswers().stream()
                .map(ExamSubmissionDTO.AnswerDTO::getQuestionId)
                .collect(Collectors.toList());
        
        // N+1 Fix: Fetch all submitted questions at once
        Map<Long, Question> questionsMap = questionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        for (ExamSubmissionDTO.AnswerDTO answer : submission.getAnswers()) {
            Question q = questionsMap.get(answer.getQuestionId());
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
