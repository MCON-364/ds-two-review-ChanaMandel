package edu.touro.mcon364.finalreview.treesandthreads.homework;

import java.util.*;
import java.util.stream.*;

/**
 * Homework 2 - Student GradeBook (TreeMap + Streams + DoubleSummaryStatistics)
 *
 * Scenario: a course has many students. Each student is identified by name and
 * has a numeric grade (0.0 to 100.0). The gradebook must support sorted lookup
 * and statistical analysis.
 *
 * Before coding, think about:
 * - Should the map key be the student name or the grade? Why does it matter?
 * - What does TreeMap.firstEntry() return? What does lastEntry() return?
 * - How do we turn a numeric score into a letter grade inside a stream?
 *
 * Requirements:
 * - The constructor receives a Map of student name to grade.
 * - buildSortedGradeBook() returns a TreeMap so students are iterated alphabetically.
 * - getStatistics() returns DoubleSummaryStatistics over all grades.
 * - getLetterGradeDistribution() returns a TreeMap counting how many students
 *   received each letter grade: A (90+), B (80-89), C (70-79), D (60-69), F (below 60).
 * - getTopStudents(n) returns the names of the n highest-scoring students, highest first.
 * - getStudentsInScoreRange(low, high) returns a sorted list of student names
 *   whose grade is in [low, high] inclusive.
 *
 * Do not use explicit loops. Use streams and collectors.
 */
public class StudentGradeBook {

    private final Map<String, Double> grades;

    public StudentGradeBook(Map<String, Double> grades) {
        // TODO: validate non-null; store a defensive copy
        this.grades = Map.copyOf(Objects.requireNonNull(grades, "Grades map cannot be null"));
    }

    /**
     * Returns a TreeMap so iteration visits students alphabetically.
     *
     */
    public TreeMap<String, Double> buildSortedGradeBook() {
        // TODO
        return new TreeMap<>(grades);
    }

    /**
     * Returns summary statistics (count, min, max, average, sum) over all grades.
     *
     */
    public DoubleSummaryStatistics getStatistics() {
        // TODO
        return grades.values().stream()
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
    }

    /**
     * Returns a TreeMap counting students per letter grade.
     *
     */
    public TreeMap<String, Long> getLetterGradeDistribution() {
        // TODO
        return grades.values().stream()
                .collect(Collectors.groupingBy(
                        this::convertToLetter, // Use a helper method or lambda to determine the key
                        TreeMap::new,          // Ensure the result is sorted alphabetically
                        Collectors.counting()  // Count the number of students per grade
                ));
    }

    // Internal helper for clarity
    private String convertToLetter(double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    /**
     * Returns the names of the n highest-scoring students, highest first.
     */
    public List<String> getTopStudents(int n) {
        // TODO
        if (n <= 0) {
            return List.of();
        }

        return grades.entrySet().stream()
                // 1. Sort the entries by value, reversed (highest score first)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                // 2. Extract just the student's name from the entry
                .map(Map.Entry::getKey)
                // 3. Limit to the top n students
                .limit(n)
                // 4. Collect into an immutable list
                .toList();
    }

    /**
     * Returns a sorted list of names whose grade falls in [low, high] inclusive.
     *
     */
    public List<String> getStudentsInScoreRange(double low, double high) {
        // TODO
        return grades.entrySet().stream()
                // 1. Keep only entries where the score is within [low, high]
                .filter(entry -> entry.getValue() >= low && entry.getValue() <= high)
                // 2. Extract just the student's name
                .map(Map.Entry::getKey)
                // 3. Sort the names alphabetically
                .sorted()
                // 4. Collect into an immutable list
                .toList();
    }
}
