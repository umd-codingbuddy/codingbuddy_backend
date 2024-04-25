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
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"course", "sequenceNumber"})
})
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int moduleId;

    @Column(length = 1000, nullable = false)
    private String name;

    @Column(nullable = false)
    private Long sequenceNumber;

    @ManyToOne
    private Course course;
}
