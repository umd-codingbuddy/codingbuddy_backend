package com.codingbuddy.models.academics;

import com.codingbuddy.models.courses.Page;
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
public class StudentPageMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @OneToOne
    private Page page;

    private Long timeTaken;

    private int hintsTaken;

    private int errors;
}
