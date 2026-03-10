package com.kjmaster.taskflow.task;

import com.kjmaster.taskflow.exception.NotFoundException;
import com.kjmaster.taskflow.project.Project;
import com.kjmaster.taskflow.project.ProjectRepository;
import com.kjmaster.taskflow.user.User;
import com.kjmaster.taskflow.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User existingUser;

    private Project existingProject;

    private Task existingTask;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("$2a$10$hashedpassword");

        existingProject = new Project();
        existingProject.setName("Test Project");
        existingProject.setOwner(existingUser);

        existingTask = new Task();
        existingTask.setName("Test Task");
        existingTask.setProject(existingProject);
    }

    @Test
    void createTask_Success() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));
        when(projectRepository.findByIdAndOwner(null, existingUser))
                .thenReturn(Optional.of(existingProject));
        when(taskRepository.save(any(Task.class)))
                .thenReturn(existingTask);

        CreateTaskRequest request = new CreateTaskRequest("Test Task", "A description", TaskPriority.MEDIUM, LocalDateTime.now());

        TaskResponse result = taskService.createTask("testuser", existingProject.getId(), request);

        assertNotNull(result);
        assertEquals("Test Task", result.name());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByUsername("nobody"))
                .thenReturn(Optional.empty());

        CreateTaskRequest request = new CreateTaskRequest("Test Task", "A description", TaskPriority.MEDIUM, LocalDateTime.now());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> taskService.createTask("nobody", existingProject.getId(), request)
        );

        assertEquals("User not found: nobody", exception.getMessage());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_ProjectNotFound_ThrowsNotFoundException() {
        Long wrongProjectId = 999L;

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));
        when(projectRepository.findByIdAndOwner(wrongProjectId, existingUser))
                .thenReturn(Optional.empty());


        CreateTaskRequest request = new CreateTaskRequest("Test Task", "A description", TaskPriority.MEDIUM, LocalDateTime.now());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> taskService.createTask("testuser", wrongProjectId, request)
        );

        assertEquals("Project not found: " + wrongProjectId, exception.getMessage());

        verify(taskRepository, never()).save(any());
    }
}
