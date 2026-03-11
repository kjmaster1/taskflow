package com.kjmaster.taskflow.task;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String name,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime dueDate
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedAt(),
                task.getStartedAt(),
                task.getCompletedAt(),
                task.getDueDate()
        );
    }
}
