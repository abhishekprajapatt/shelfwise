package com.selfwise.repository;

import com.selfwise.model.Issue;
import com.selfwise.model.Issue.IssueStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByStudentId(Long studentId);

    List<Issue> findByBookId(Long bookId);

    List<Issue> findByStatus(IssueStatus status);

    @Query("SELECT i FROM Issue i WHERE i.status = 'ISSUED' AND i.dueDate < :currentDate")
    List<Issue> findOverdueIssues(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT i FROM Issue i WHERE i.student.id = :studentId AND i.status = 'ISSUED'")
    List<Issue> findActiveIssuesByStudent(@Param("studentId") Long studentId);

    @Query("SELECT i FROM Issue i WHERE i.book.id = :bookId AND i.status = 'ISSUED'")
    List<Issue> findActiveIssuesByBook(@Param("bookId") Long bookId);

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.student.id = :studentId AND i.status = 'ISSUED'")
    long countActiveIssuesByStudent(@Param("studentId") Long studentId);

    @Query("SELECT i FROM Issue i WHERE i.issueDate BETWEEN :startDate AND :endDate")
    List<Issue> findIssuesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Issue i WHERE i.returnDate BETWEEN :startDate AND :endDate")
    List<Issue> findReturnsBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
