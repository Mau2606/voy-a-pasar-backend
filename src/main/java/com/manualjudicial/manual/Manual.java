package com.manualjudicial.manual;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.manualjudicial.chapters.Chapter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "manuals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "pdf_url", columnDefinition = "TEXT")
    private String pdfUrl;

    /** Chapters are ordered by their own orderIndex field. */
    @OneToMany(mappedBy = "manual", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();
}
