package com.manualjudicial.chapters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manualjudicial.manual.Manual;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chapters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manual_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "chapters" })
    private Manual manual;

    /** Position within the manual (1-based). Used to determine unlock order. */
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "pdf_url", columnDefinition = "TEXT")
    private String pdfUrl;

    @Column(name = "start_page")
    private Integer startPage;
}
