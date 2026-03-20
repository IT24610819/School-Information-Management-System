package com.Harshana.school_management_system.repository;

import com.Harshana.school_management_system.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByAdmissionNo(String admissionNo);

    boolean existsByAdmissionNoAndStudentIdNot(String admissionNo, Long studentId);

    List<Student> findByAdmissionNoContainingIgnoreCase(String admissionNo);

    List<Student> findByFullNameContainingIgnoreCase(String fullName);

    List<Student> findByClassRoom_ClassId(Long classId);

    List<Student> findByAdmissionNoContainingIgnoreCaseAndFullNameContainingIgnoreCase(String admissionNo, String fullName);

    List<Student> findByAdmissionNoContainingIgnoreCaseAndFullNameContainingIgnoreCaseAndClassRoom_ClassId(
            String admissionNo, String fullName, Long classId
    );

    long countByStatus(String status);

    @Query("""
           SELECT s.classRoom.className, COUNT(s)
           FROM Student s
           GROUP BY s.classRoom.className
           ORDER BY s.classRoom.className
           """)
    List<Object[]> countStudentsGroupByClass();
}