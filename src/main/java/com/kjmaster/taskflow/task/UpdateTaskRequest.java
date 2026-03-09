package com.kjmaster.taskflow.task;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UpdateTaskRequest(
        @Size(max = 255, message = "Task name cannot exceed 255 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueDate
) {}