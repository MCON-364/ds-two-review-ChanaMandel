package edu.touro.mcon364.finalreview.model;

import java.time.LocalDateTime;
import java.util.Optional;

public record Submission(
        String studentId,
        String assignment,
        Optional<Integer> score,
        LocalDateTime submittedAt,
        boolean late
) {
    public Submission {
        if (studentId == null || studentId.isBlank()) throw new IllegalArgumentException("studentId is required");
        if (assignment == null || assignment.isBlank()) throw new IllegalArgumentException("assignment is required");
        score = score == null ? Optional.empty() : score;
        if (submittedAt == null) throw new IllegalArgumentException("submittedAt is required");
    }
}
