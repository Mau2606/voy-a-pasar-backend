package com.manualjudicial.questions;

import lombok.Data;

@Data
public class QuestionDTO {
    private Long chapterId;
    private QuestionType type;
    private String statement;
    private String optA;
    private String optB;
    private String optC;
    private String optD;
    private String optE;
    private String correctOpt;
    private String explanation;
    private String pageReference;
}
