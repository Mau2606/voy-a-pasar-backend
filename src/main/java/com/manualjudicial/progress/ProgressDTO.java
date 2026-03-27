package com.manualjudicial.progress;

import lombok.Data;

@Data
public class ProgressDTO {
    private Long chapterId;
    private Double scorePercentage;
    private Integer correctAnswers;
    private Integer totalQuestions;
}
