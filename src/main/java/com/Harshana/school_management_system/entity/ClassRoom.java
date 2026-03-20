package com.Harshana.school_management_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "classes")
@Getter
@Setter
public class ClassRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Long classId;

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @Column(name = "class_section", nullable = false, length = 5)
    private String classSection;

    @Column(name = "class_name", nullable = false, unique = true, length = 10)
    private String className;

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}