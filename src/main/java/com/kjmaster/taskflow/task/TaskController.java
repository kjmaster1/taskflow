package com.kjmaster.taskflow.task;

import com.kjmaster.taskflow.project.ProjectResponse;
import com.kjmaster.taskflow.project.UpdateProjectRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestParam String username,
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(username, projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getProjectTasks(
            @RequestParam String username,
            @PathVariable Long projectId) {
        List<TaskResponse> tasks = taskService.getProjectTasks(username, projectId);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @RequestParam String username,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse response = taskService.updateTask(username, projectId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @RequestParam String username,
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        taskService.deleteTask(username, projectId, taskId);
        return ResponseEntity.noContent().build();
    }
}
