package com.codingbuddy.models.academics;

import com.codingbuddy.models.courses.*;
import com8.codingbuddy.models.courses.*;
import com.codingbuddy.models.courses.Module;
import com.codingbuddy.models.user.Student;
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
public class StudentScore {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    private Student student;

    @Enumerated(EnumType.STRING)
    private PageType pageType;

    @OneToOne
    private Page page;

    @OneToOne
    private Course course;

    @OneToOne
    private Module module;

    private int score;
}
