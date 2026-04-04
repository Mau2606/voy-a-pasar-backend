package com.manualjudicial.reports;

import lombok.Data;

@Data
public class QuestionReportRequest {
    private Long questionId;
    private String description;
}
