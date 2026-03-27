package com.manualjudicial.chapters;

import lombok.Data;

@Data
public class ChapterDTO {
    private Long manualId;
    private Integer orderIndex;
    private Integer number;
    private String title;
    private String description;

    private String pdfUrl;
    private Integer startPage;

    /** Number of questions associated with this chapter. */
    private Long questionCount;
}
