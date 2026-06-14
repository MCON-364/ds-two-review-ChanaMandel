package edu.touro.mcon364.finalreview.model;

import java.util.List;
import java.util.Map;

public record ProjectTask(
        int id,
        String title,
        Priority priority,
        Status status,
        List<String> tags,
        List<String> assignees,
        Map<String, Integer> effortByPhase
) {
    public ProjectTask {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
        if (priority == null) throw new IllegalArgumentException("priority is required");
        if (status == null) throw new IllegalArgumentException("status is required");
        tags = List.copyOf(tags == null ? List.of() : tags);
        assignees = List.copyOf(assignees == null ? List.of() : assignees);
        effortByPhase = Map.copyOf(effortByPhase == null ? Map.of() : effortByPhase);
    }
}
