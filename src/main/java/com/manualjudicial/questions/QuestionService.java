package com.manualjudicial.questions;

import com.manualjudicial.chapters.Chapter;
import com.manualjudicial.chapters.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ChapterRepository chapterRepository;

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    @Cacheable(value = "questionsByChapter", key = "#chapterId")
    public List<Question> findByChapter(Long chapterId) {
        return questionRepository.findByChapterId(chapterId);
    }

    /**
     * Returns up to {@code limit} randomly-selected questions for a chapter.
     * If limit is null or >= total question count, returns all questions shuffled.
     * NOTE: Not cached because shuffle produces different results each time.
     */
    public List<Question> findByChapterWithLimit(Long chapterId, Integer limit) {
        List<Question> all = new ArrayList<>(questionRepository.findByChapterId(chapterId));
        Collections.shuffle(all);
        if (limit == null || limit <= 0 || limit >= all.size())
            return all;
        return all.subList(0, Math.min(limit, 100)); // hard cap at 100
    }

    public Question findById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found: " + id));
    }

    @Transactional
    @CacheEvict(value = "questionsByChapter", allEntries = true)
    public Question create(QuestionDTO dto) {
        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + dto.getChapterId()));
        Question q = Question.builder()
                .chapter(chapter)
                .type(dto.getType())
                .statement(dto.getStatement())
                .optA(dto.getOptA())
                .optB(dto.getOptB())
                .optC(dto.getOptC())
                .optD(dto.getOptD())
                .optE(dto.getOptE())
                .correctOpt(dto.getCorrectOpt())
                .explanation(dto.getExplanation())
                .pageReference(dto.getPageReference())
                .build();
        return questionRepository.save(q);
    }

    @Transactional
    @CacheEvict(value = "questionsByChapter", allEntries = true)
    public Question update(Long id, QuestionDTO dto) {
        Question q = findById(id);
        if (dto.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(dto.getChapterId())
                    .orElseThrow(() -> new RuntimeException("Chapter not found: " + dto.getChapterId()));
            q.setChapter(chapter);
        }
        if (dto.getType() != null)
            q.setType(dto.getType());
        if (dto.getStatement() != null)
            q.setStatement(dto.getStatement());
        if (dto.getOptA() != null)
            q.setOptA(dto.getOptA());
        if (dto.getOptB() != null)
            q.setOptB(dto.getOptB());
        if (dto.getOptC() != null)
            q.setOptC(dto.getOptC());
        if (dto.getOptD() != null)
            q.setOptD(dto.getOptD());
        if (dto.getOptE() != null)
            q.setOptE(dto.getOptE());
        if (dto.getCorrectOpt() != null)
            q.setCorrectOpt(dto.getCorrectOpt());
        if (dto.getExplanation() != null)
            q.setExplanation(dto.getExplanation());
        if (dto.getPageReference() != null)
            q.setPageReference(dto.getPageReference());
        return questionRepository.save(q);
    }

    @Transactional
    @CacheEvict(value = "questionsByChapter", allEntries = true)
    public void delete(Long id) {
        questionRepository.deleteById(id);
    }
}
