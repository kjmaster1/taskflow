package com.kjmaster.taskflow.task;

import com.kjmaster.taskflow.exception.InvalidStateTransitionException;
import com.kjmaster.taskflow.project.Project;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime dueDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void transitionTo(TaskStatus newStatus) {
        if (this.status == newStatus) {
            return;
        }

        switch (newStatus) {
            case IN_PROGRESS -> {
                if (this.status != TaskStatus.TODO) {
                    throw new InvalidStateTransitionException(
                            "Cannot transition from " + this.status + " to IN_PROGRESS");
                }
                this.startedAt = LocalDateTime.now();
            }
            case DONE -> {
                if (this.status != TaskStatus.IN_PROGRESS) {
                    throw new InvalidStateTransitionException(
                            "Cannot transition from " + this.status + " to DONE");
                }
                this.completedAt = LocalDateTime.now();
            }
            case TODO -> throw new InvalidStateTransitionException(
                    "Cannot transition back to TODO");
        }

        this.status = newStatus;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}