package com.selfwise.service;

import com.selfwise.model.Issue;
import com.selfwise.model.Issue.IssueStatus;
import com.selfwise.model.Book;
import com.selfwise.model.Student;
import com.selfwise.repository.IssueRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private StudentService studentService;

    private static final double FINE_PER_DAY = 5.0;

    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    public Optional<Issue> getIssueById(Long id) {
        return issueRepository.findById(id);
    }

    public List<Issue> findIssuesByStatus(IssueStatus status) {
        return issueRepository.findByStatus(status);
    }

    public List<Issue> getIssuesByBookId(Long bookId) {
        return issueRepository.findByBookId(bookId);
    }

    public List<Issue> getIssuesByStudentId(Long studentId) {
        return issueRepository.findByStudentId(studentId);
    }

    public List<Issue> findOverdueIssues() {
        return issueRepository.findOverdueIssues(LocalDate.now());
    }

    public void issueBook(Issue issue) {
        Book book = bookService.getBookById(issue.getBook().getId())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available for this book");
        }

        Student student = studentService.getStudentById(issue.getStudent().getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (!studentService.canIssueMoreBooks(student.getId())) {
            throw new RuntimeException("Student cannot issue more books");
        }

        issue.setIssueDate(LocalDate.now());
        issue.setDueDate(LocalDate.now().plusDays(14));
        issue.setStatus(IssueStatus.ISSUED);
        issue.setFineAmount(0.0);
        issue.setReturnDate(null);

        bookService.issueBook(book.getId());
        issueRepository.save(issue);
    }

    public void returnBook(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue record not found"));

        if (issue.getStatus() != IssueStatus.ISSUED) {
            throw new RuntimeException("Book already returned or invalid issue status");
        }

        LocalDate returnDate = LocalDate.now();
        issue.setReturnDate(returnDate);

        long overdueDays = 0;
        if (returnDate.isAfter(issue.getDueDate())) {
            overdueDays = java.time.temporal.ChronoUnit.DAYS.between(issue.getDueDate(), returnDate);
            issue.setFineAmount(overdueDays * FINE_PER_DAY);
            issue.setStatus(IssueStatus.OVERDUE);
        } else {
            issue.setFineAmount(0.0);
            issue.setStatus(IssueStatus.RETURNED);
        }
        bookService.returnBook(issue.getBook().getId());

        issueRepository.save(issue);
    }
}
