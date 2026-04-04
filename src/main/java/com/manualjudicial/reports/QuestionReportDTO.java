package com.manualjudicial.reports;

import com.manualjudicial.questions.QuestionDTO;
import com.manualjudicial.users.UserDTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuestionReportDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    
    private Long questionId;
    private Long chapterId;
    private String chapterTitle;
    private String questionStatement;
    private String optA;
    private String optB;
    private String optC;
    private String optD;
    private String optE;
    private String correctOpt;
    private String explanation;
    private String pageReference;
    
    private String description;
    private ReportStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    // Static mapper to avoid heavy libraries for now
    public static QuestionReportDTO fromEntity(QuestionReport report) {
        if (report == null) return null;
        QuestionReportDTO dto = new QuestionReportDTO();
        dto.setId(report.getId());
        
        if (report.getUser() != null) {
            dto.setUserId(report.getUser().getId());
            dto.setUserName(report.getUser().getName());
            dto.setUserEmail(report.getUser().getEmail());
        }

        if (report.getQuestion() != null) {
            dto.setQuestionId(report.getQuestion().getId());
            dto.setQuestionStatement(report.getQuestion().getStatement());
            dto.setOptA(report.getQuestion().getOptA());
            dto.setOptB(report.getQuestion().getOptB());
            dto.setOptC(report.getQuestion().getOptC());
            dto.setOptD(report.getQuestion().getOptD());
            dto.setOptE(report.getQuestion().getOptE());
            dto.setCorrectOpt(report.getQuestion().getCorrectOpt());
            dto.setExplanation(report.getQuestion().getExplanation());
            dto.setPageReference(report.getQuestion().getPageReference());
            if (report.getQuestion().getChapter() != null) {
                dto.setChapterId(report.getQuestion().getChapter().getId());
                dto.setChapterTitle(report.getQuestion().getChapter().getTitle());
            }
        }

        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus());
        dto.setAdminNotes(report.getAdminNotes());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setResolvedAt(report.getResolvedAt());
        return dto;
    }
}
