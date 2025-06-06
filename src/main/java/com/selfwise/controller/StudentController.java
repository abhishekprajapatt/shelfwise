package com.selfwise.controller;

import com.selfwise.model.Student;
import com.selfwise.service.StudentService;
import com.selfwise.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private IssueService issueService;

    @GetMapping
    public String listStudents(Model model, @RequestParam(value = "search", required = false) String search) {
        List<Student> students;
        if (search != null && !search.trim().isEmpty()) {
            students = studentService.searchStudents(search.trim());
        } else {
            students = studentService.getAllStudents();
        }
        model.addAttribute("students", students);
        model.addAttribute("search", search);
        return "student-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("departments", studentService.getAllDepartments());
        return "student-add";
    }

    @PostMapping("/add")
    public String addStudent(@Valid @ModelAttribute("student") Student student,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("departments", studentService.getAllDepartments());
            return "student-add";
        }
        if (studentService.isStudentIdExists(student.getStudentId())) {
            model.addAttribute("errorMessage", "Student ID already exists. Please enter a unique Student ID.");
            model.addAttribute("departments", studentService.getAllDepartments());
            return "student-add";
        }
        if (studentService.isEmailExists(student.getEmail())) {
            model.addAttribute("errorMessage", "Email already exists. Please enter a unique email.");
            model.addAttribute("departments", studentService.getAllDepartments());
            return "student-add";
        }
        studentService.saveStudent(student);
        redirectAttrs.addFlashAttribute("successMessage", "Student registered successfully!");
        return "redirect:/students";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttrs) {
        Student student = studentService.getStudentById(id).orElse(null);
        if (student == null) {
            redirectAttrs.addFlashAttribute("errorMessage", "Student not found.");
            return "redirect:/students";
        }
        model.addAttribute("student", student);
        model.addAttribute("departments", studentService.getAllDepartments());
        return "student-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateStudent(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("student") Student student,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("departments", studentService.getAllDepartments());
            return "student-edit";
        }
        if (studentService.isStudentIdExistsForOtherStudent(student.getStudentId(), id)) {
            model.addAttribute("errorMessage", "Student ID exists for another student. Enter a unique Student ID.");
            model.addAttribute("departments", studentService.getAllDepartments());
            return "student-edit";
        }
        if (studentService.isEmailExistsForOtherStudent(student.getEmail(), id)) {
            model.addAttribute("errorMessage", "Email exists for another student. Enter a unique email.");
            model.addAttribute("departments", studentService.getAllDepartments());
            return "student-edit";
        }
        try {
            studentService.updateStudent(id, student);
            redirectAttrs.addFlashAttribute("successMessage", "Student updated successfully!");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/students";
    }

    @PostMapping("/delete/{id}")
    public String deleteStudent(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        try {
            studentService.deleteStudent(id);
            redirectAttrs.addFlashAttribute("successMessage", "Student deleted successfully!");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/students";
    }
}
