package com.codingbuddy.models.courses;

import com.codingbuddy.models.user.Instructor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int courseId;

    @Column(unique = true, length = 1000, nullable = false)
    private String name;

    @Column(unique = true, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @OneToMany
    private List<Tag> tags;

    @OneToMany
    private List<Instructor> instructors;


}
