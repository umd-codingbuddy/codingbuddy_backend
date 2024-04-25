package com.codingbuddy.models.courses;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int pageId;

    @ManyToOne
    private Module module;

    @Column(length = 1000, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private PageType pageType;

    @Column(nullable = false)
    private Long sequenceNumber;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private Boolean hintAllowed;


}
