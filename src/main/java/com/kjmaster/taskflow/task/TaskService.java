package com.kjmaster.taskflow.task;

import com.kjmaster.taskflow.exception.NotFoundException;
import com.kjmaster.taskflow.project.ProjectRepository;
import com.kjmaster.taskflow.user.User;
import com.kjmaster.taskflow.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public TaskResponse createTask(String username, Long projectId, CreateTaskRequest request) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        var project = projectRepository.findByIdAndOwner(projectId, owner)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        Task task = new Task();
        task.setName(request.name());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setProject(project);

        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    public TaskResponse updateTask(String username, Long projectId, Long taskId, UpdateTaskRequest request) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        var project = projectRepository.findByIdAndOwner(projectId, owner)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
        var task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));

        if (request.name() != null) {
            task.setName(request.name());
        }

        if (request.description() != null) {
            task.setDescription(request.description());
        }

        if (request.priority() != null) {
            task.setPriority(request.priority());
        }

        if (request.status() != null) {
            task.setStatus(request.status());
        }

        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }

        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    public void deleteTask(String username, Long projectId, Long taskId) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        var project = projectRepository.findByIdAndOwner(projectId, owner)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
        var task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
        taskRepository.delete(task);
    }

    public List<TaskResponse> getProjectTasks(String username, Long projectId) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        var project = projectRepository.findByIdAndOwner(projectId, owner)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return taskRepository.findByProject(project)
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }
}
