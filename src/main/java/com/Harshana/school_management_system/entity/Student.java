package com.Harshana.school_management_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Getter
@Setter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @NotBlank(message = "Admission number is required")
    @Size(max = 20, message = "Admission number must not exceed 20 characters")
    @Column(name = "admission_no", nullable = false, unique = true, length = 20)
    private String admissionNo;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name must not exceed 120 characters")
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @NotNull(message = "Date of birth is required")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Column(name = "gender", nullable = false, length = 10)
    private String gender;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @NotBlank(message = "Guardian name is required")
    @Size(max = 100, message = "Guardian name must not exceed 100 characters")
    @Column(name = "guardian_name", nullable = false, length = 100)
    private String guardianName;

    @NotBlank(message = "Guardian contact is required")
    @Pattern(
            regexp = "^(?:\\+94|0)\\d{9}$",
            message = "Guardian contact must be a valid Sri Lankan phone number"
    )
    @Column(name = "guardian_contact", nullable = false, length = 20)
    private String guardianContact;

    @Email(message = "Guardian email must be valid")
    @Size(max = 100, message = "Guardian email must not exceed 100 characters")
    @Column(name = "guardian_email", length = 100)
    private String guardianEmail;

    @Column(name = "photo_path", length = 255)
    private String photoPath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassRoom classRoom;

    @NotNull(message = "Admission date is required")
    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}