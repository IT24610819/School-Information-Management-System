package com.Harshana.school_management_system.controller;

import com.Harshana.school_management_system.entity.ClassRoom;
import com.Harshana.school_management_system.entity.Student;
import com.Harshana.school_management_system.repository.ClassRoomRepository;
import com.Harshana.school_management_system.repository.StudentRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class StudentController {

    private final StudentRepository studentRepository;
    private final ClassRoomRepository classRoomRepository;

    private final String uploadDir = "src/main/resources/static/uploads/";

    public StudentController(StudentRepository studentRepository,
                             ClassRoomRepository classRoomRepository) {
        this.studentRepository = studentRepository;
        this.classRoomRepository = classRoomRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/students";
    }

    @GetMapping("/students")
    public String showStudentList(
            @RequestParam(value = "admissionNo", required = false) String admissionNo,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        String admissionNoValue = admissionNo == null ? "" : admissionNo.trim();
        String fullNameValue = fullName == null ? "" : fullName.trim();

        Pageable pageable = PageRequest.of(page, 5);
        Page<Student> studentPage = getFilteredStudents(admissionNoValue, fullNameValue, classId, pageable);

        model.addAttribute("studentPage", studentPage);
        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("classes", classRoomRepository.findAll());
        model.addAttribute("admissionNo", admissionNoValue);
        model.addAttribute("fullName", fullNameValue);
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());

        model.addAttribute("totalStudents", studentRepository.count());
        model.addAttribute("activeStudents", studentRepository.countByStatus("Active"));
        model.addAttribute("inactiveStudents", studentRepository.countByStatus("Inactive"));
        model.addAttribute("totalClasses", classRoomRepository.count());
        model.addAttribute("classStudentCounts", studentRepository.countStudentsGroupByClass());

        return "students/list";
    }

    @GetMapping("/students/export")
    public void exportStudentsToCsv(
            @RequestParam(value = "admissionNo", required = false) String admissionNo,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "classId", required = false) Long classId,
            HttpServletResponse response) throws IOException {

        String admissionNoValue = admissionNo == null ? "" : admissionNo.trim();
        String fullNameValue = fullName == null ? "" : fullName.trim();

        List<Student> students = getFilteredStudentsForExport(admissionNoValue, fullNameValue, classId);

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=students.csv");

        PrintWriter writer = response.getWriter();

        writer.println("Student ID,Admission Number,Full Name,Gender,Date of Birth,Guardian Name,Guardian Contact,Guardian Email,Class,Admission Date,Status");

        for (Student student : students) {
            writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    student.getStudentId(),
                    safe(student.getAdmissionNo()),
                    safe(student.getFullName()),
                    safe(student.getGender()),
                    student.getDateOfBirth() != null ? student.getDateOfBirth() : "",
                    safe(student.getGuardianName()),
                    safe(student.getGuardianContact()),
                    safe(student.getGuardianEmail()),
                    student.getClassRoom() != null ? safe(student.getClassRoom().getClassName()) : "",
                    student.getAdmissionDate() != null ? student.getAdmissionDate() : "",
                    safe(student.getStatus())
            );
        }

        writer.flush();
        writer.close();
    }

    @GetMapping("/students/new")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("classes", classRoomRepository.findAll());
        return "students/add";
    }

    @PostMapping("/students")
    public String saveStudent(@Valid @ModelAttribute("student") Student student,
                              BindingResult bindingResult,
                              @RequestParam("classId") Long classId,
                              @RequestParam("photoFile") MultipartFile photoFile,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("classes", classRoomRepository.findAll());
            return "students/add";
        }

        if (studentRepository.existsByAdmissionNo(student.getAdmissionNo())) {
            model.addAttribute("error", "Admission number already exists!");
            model.addAttribute("classes", classRoomRepository.findAll());
            return "students/add";
        }

        Optional<ClassRoom> optionalClassRoom = classRoomRepository.findById(classId);
        if (optionalClassRoom.isEmpty()) {
            model.addAttribute("error", "Selected class is invalid!");
            model.addAttribute("classes", classRoomRepository.findAll());
            return "students/add";
        }

        student.setClassRoom(optionalClassRoom.get());

        if (!photoFile.isEmpty()) {
            String fileName = savePhoto(photoFile);
            student.setPhotoPath(fileName);
        }

        studentRepository.save(student);
        redirectAttributes.addFlashAttribute("success", "Student added successfully!");
        return "redirect:/students";
    }

    @GetMapping("/students/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Optional<Student> optionalStudent = studentRepository.findById(id);

        if (optionalStudent.isEmpty()) {
            return "redirect:/students";
        }

        model.addAttribute("student", optionalStudent.get());
        model.addAttribute("classes", classRoomRepository.findAll());
        return "students/edit";
    }

    @PostMapping("/students/update/{id}")
    public String updateStudent(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("student") Student student,
                                BindingResult bindingResult,
                                @RequestParam("classId") Long classId,
                                @RequestParam("photoFile") MultipartFile photoFile,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        student.setStudentId(id);

        Optional<Student> existingStudentOpt = studentRepository.findById(id);
        if (existingStudentOpt.isEmpty()) {
            return "redirect:/students";
        }

        Student existingStudent = existingStudentOpt.get();

        Optional<ClassRoom> optionalClassRoom = classRoomRepository.findById(classId);
        if (optionalClassRoom.isPresent()) {
            student.setClassRoom(optionalClassRoom.get());
        }

        if (bindingResult.hasErrors()) {
            student.setPhotoPath(existingStudent.getPhotoPath());
            model.addAttribute("classes", classRoomRepository.findAll());
            return "students/edit";
        }

        if (studentRepository.existsByAdmissionNoAndStudentIdNot(student.getAdmissionNo(), id)) {
            student.setPhotoPath(existingStudent.getPhotoPath());
            model.addAttribute("error", "Admission number already exists!");
            model.addAttribute("classes", classRoomRepository.findAll());
            return "students/edit";
        }

        if (optionalClassRoom.isEmpty()) {
            student.setPhotoPath(existingStudent.getPhotoPath());
            model.addAttribute("error", "Selected class is invalid!");
            model.addAttribute("classes", classRoomRepository.findAll());
            return "students/edit";
        }

        if (!photoFile.isEmpty()) {
            String fileName = savePhoto(photoFile);
            student.setPhotoPath(fileName);
        } else {
            student.setPhotoPath(existingStudent.getPhotoPath());
        }

        studentRepository.save(student);
        redirectAttributes.addFlashAttribute("success", "Student updated successfully!");
        return "redirect:/students";
    }

    @GetMapping("/students/view/{id}")
    public String viewStudentProfile(@PathVariable("id") Long id, Model model) {
        Optional<Student> optionalStudent = studentRepository.findById(id);

        if (optionalStudent.isEmpty()) {
            return "redirect:/students";
        }

        model.addAttribute("student", optionalStudent.get());
        return "students/view";
    }

    @GetMapping("/students/deactivate/{id}")
    public String deactivateStudent(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Student> optionalStudent = studentRepository.findById(id);

        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            student.setStatus("Inactive");
            studentRepository.save(student);
            redirectAttributes.addFlashAttribute("success", "Student deactivated successfully!");
        }

        return "redirect:/students";
    }

    @GetMapping("/students/activate/{id}")
    public String activateStudent(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Student> optionalStudent = studentRepository.findById(id);

        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            student.setStatus("Active");
            studentRepository.save(student);
            redirectAttributes.addFlashAttribute("success", "Student activated successfully!");
        }

        return "redirect:/students";
    }

    private Page<Student> getFilteredStudents(String admissionNo, String fullName, Long classId, Pageable pageable) {
        if (classId != null) {
            return studentRepository
                    .findByAdmissionNoContainingIgnoreCaseAndFullNameContainingIgnoreCaseAndClassRoom_ClassIdOrderByAdmissionNoAsc(
                            admissionNo, fullName, classId, pageable
                    );
        } else {
            return studentRepository
                    .findByAdmissionNoContainingIgnoreCaseAndFullNameContainingIgnoreCaseOrderByAdmissionNoAsc(
                            admissionNo, fullName, pageable
                    );
        }
    }

    private List<Student> getFilteredStudentsForExport(String admissionNo, String fullName, Long classId) {
        if (classId != null) {
            return studentRepository
                    .findByAdmissionNoContainingIgnoreCaseAndFullNameContainingIgnoreCaseAndClassRoom_ClassIdOrderByAdmissionNoAsc(
                            admissionNo, fullName, classId, Pageable.unpaged()
                    ).getContent();
        } else {
            return studentRepository
                    .findByAdmissionNoContainingIgnoreCaseAndFullNameContainingIgnoreCaseOrderByAdmissionNoAsc(
                            admissionNo, fullName, Pageable.unpaged()
                    ).getContent();
        }
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String savePhoto(MultipartFile photoFile) {
        try {
            String originalFilename = StringUtils.cleanPath(photoFile.getOriginalFilename());
            String extension = "";

            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex);
            }

            String fileName = UUID.randomUUID() + extension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(photoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save photo: " + e.getMessage());
        }
    }
}