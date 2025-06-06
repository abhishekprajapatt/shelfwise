package com.selfwise.service;

import com.selfwise.model.Student;
import com.selfwise.repository.StudentRepository;
import com.selfwise.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private IssueRepository issueRepository;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    public List<Student> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }
        return studentRepository.searchStudents(keyword.trim());
    }

    public List<Student> findByName(String name) {
        return studentRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Student> findByDepartment(String department) {
        return studentRepository.findByDepartmentIgnoreCase(department);
    }

    public List<String> getAllDepartments() {
        return studentRepository.findAllDepartments();
    }

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setStudentId(studentDetails.getStudentId());
        student.setDepartment(studentDetails.getDepartment());
        student.setPhone(studentDetails.getPhone());

        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }

        long activeIssues = issueRepository.countActiveIssuesByStudent(id);
        if (activeIssues > 0) {
            throw new RuntimeException("Cannot delete student with active book issues. Please return all books first.");
        }

        studentRepository.deleteById(id);
    }

    public boolean isStudentIdExists(String studentId) {
        return studentRepository.existsByStudentId(studentId);
    }

    public boolean isStudentIdExistsForOtherStudent(String studentId, Long id) {
        Optional<Student> existingStudent = studentRepository.findByStudentId(studentId);
        return existingStudent.isPresent() && !existingStudent.get().getId().equals(id);
    }

    public boolean isEmailExists(String email) {
        return studentRepository.existsByEmail(email);
    }

    public boolean isEmailExistsForOtherStudent(String email, Long id) {
        Optional<Student> existingStudent = studentRepository.findByEmail(email);
        return existingStudent.isPresent() && !existingStudent.get().getId().equals(id);
    }

    public long getActiveIssuesCount(Long studentId) {
        return issueRepository.countActiveIssuesByStudent(studentId);
    }

    public boolean canIssueMoreBooks(Long studentId) {
        return getActiveIssuesCount(studentId) < 5;
    }
}
