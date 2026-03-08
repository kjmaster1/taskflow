package com.kjmaster.taskflow.project;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @RequestParam String username,
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getUserProjects(
            @RequestParam String username) {
        List<ProjectResponse> projects = projectService.getUserProjects(username);
        return ResponseEntity.ok(projects);
    }
}