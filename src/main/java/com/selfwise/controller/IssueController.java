package com.selfwise.controller;

import com.selfwise.model.Issue;
import com.selfwise.model.Issue.IssueStatus;
import com.selfwise.model.Book;
import com.selfwise.model.Student;
import com.selfwise.service.IssueService;
import com.selfwise.service.BookService;
import com.selfwise.service.StudentService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private BookService bookService;

    @Autowired
    private StudentService studentService;

    @GetMapping
    public String listIssues(Model model, @RequestParam(value = "status", required = false) String status) {
        List<Issue> issues;
        if (status != null && status.equalsIgnoreCase("OVERDUE")) {
            issues = issueService.findIssuesByStatus(IssueStatus.OVERDUE);
        } else {
            issues = issueService.getAllIssues();
        }
        model.addAttribute("issues", issues);
        model.addAttribute("filterStatus", status);
        return "issue-list";
    }

    @GetMapping("/issue")
    public String showIssueForm(Model model) {
        model.addAttribute("issue", new Issue());
        model.addAttribute("books", bookService.getAvailableBooks());
        model.addAttribute("students", studentService.getAllStudents());
        return "issue-add";
    }

    @PostMapping("/issue")
    public String issueBook(@Valid @ModelAttribute("issue") Issue issue,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("books", bookService.getAvailableBooks());
            model.addAttribute("students", studentService.getAllStudents());
            return "issue-add";
        }
        
        if (!studentService.canIssueMoreBooks(issue.getStudent().getId())) {
            model.addAttribute("errorMessage", "Student has reached maximum allowed issued books.");
            model.addAttribute("books", bookService.getAvailableBooks());
            model.addAttribute("students", studentService.getAllStudents());
            return "issue-add";
        }
       
        Book book = bookService.getBookById(issue.getBook().getId()).orElse(null);
        if (book == null || !book.isAvailable()) {
            model.addAttribute("errorMessage", "Selected book is not available for issuing.");
            model.addAttribute("books", bookService.getAvailableBooks());
            model.addAttribute("students", studentService.getAllStudents());
            return "issue-add";
        }
        
        issueService.issueBook(issue);
        redirectAttrs.addFlashAttribute("successMessage", "Book issued successfully!");
        return "redirect:/issues";
    }

    @GetMapping("/return/{id}")
    public String showReturnForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttrs) {
        Issue issue = issueService.getIssueById(id).orElse(null);
        if (issue == null || issue.getStatus() != IssueStatus.ISSUED) {
            redirectAttrs.addFlashAttribute("errorMessage", "Issue record not found or already returned.");
            return "redirect:/issues";
        }
        model.addAttribute("issue", issue);
        return "issue-return";
    }

    @PostMapping("/return/{id}")
    public String returnBook(@PathVariable("id") Long id,
                             RedirectAttributes redirectAttrs) {
        try {
            issueService.returnBook(id);
            redirectAttrs.addFlashAttribute("successMessage", "Book returned successfully!");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/issues";
    }
}
