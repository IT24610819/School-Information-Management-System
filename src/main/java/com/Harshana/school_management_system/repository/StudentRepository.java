package com.Harshana.school_management_system.repository;

import com.Harshana.school_management_system.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByAdmissionNo(String admissionNo);

    boolean existsByAdmissionNoAndStudentIdNot(String admissionNo, Long studentId);

    Page<Student> findByAdmissionNoContainingIgnoreCaseAndFullNameContainingIgnoreCaseAndStatusContainingIgnoreCaseOrderByAdmissionNoAsc(
            String admissionNo, String fullName, String status, Pageable pageable
    );

    Page<Student> findByAdmissionNoContainingIgnoreCaseAndFullNameContainingIgnoreCaseAndClassRoom_ClassIdAndStatusContainingIgnoreCaseOrderByAdmissionNoAsc(
            String admissionNo, String fullName, Long classId, String status, Pageable pageable
    );

    long countByStatus(String status);

    @Query("""
           SELECT s.classRoom.className, COUNT(s)
           FROM Student s
           GROUP BY s.classRoom.className
           ORDER BY s.classRoom.className
           """)
    java.util.List<Object[]> countStudentsGroupByClass();
}