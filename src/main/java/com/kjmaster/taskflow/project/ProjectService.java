package com.kjmaster.taskflow.project;

import com.kjmaster.taskflow.exception.NotFoundException;
import com.kjmaster.taskflow.user.User;
import com.kjmaster.taskflow.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public ProjectResponse createProject(String username,
                                         CreateProjectRequest request) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setOwner(owner);

        Project saved = projectRepository.save(project);
        return ProjectResponse.from(saved);
    }

    public ProjectResponse updateProject(String username, Long projectId, UpdateProjectRequest request) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        var project = projectRepository.findByIdAndOwner(projectId, owner)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        if (request.name() != null) {
            project.setName(request.name());
        }

        if (request.description() != null) {
            project.setDescription(request.description());
        }

        Project saved = projectRepository.save(project);
        return ProjectResponse.from(saved);
    }

    public void deleteProject(String username, Long projectId) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        var project = projectRepository.findByIdAndOwner(projectId, owner)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        projectRepository.delete(project);
    }

    public List<ProjectResponse> getUserProjects(String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        return projectRepository.findByOwner(owner)
                .stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }
}