package com.kjmaster.taskflow.project;

import com.kjmaster.taskflow.exception.NotFoundException;
import com.kjmaster.taskflow.user.User;
import com.kjmaster.taskflow.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private User existingUser;

    private Project existingProject;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("$2a$10$hashedpassword");

        existingProject = new Project();
        existingProject.setName("Test Project");
        existingProject.setOwner(existingUser);
    }

    @Test
    void createProject_Success() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));
        when(projectRepository.save(any(Project.class)))
                .thenReturn(existingProject);

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project", "A description"
        );

        ProjectResponse result = projectService.createProject("testuser", request);

        assertNotNull(result);
        assertEquals("Test Project", result.name());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByUsername("nobody"))
                .thenReturn(Optional.empty());

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project", "A description"
        );

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> projectService.createProject("nobody", request)
        );

        assertEquals("User not found: nobody", exception.getMessage());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void getUserProjects_ReturnsListOfProjectResponses() {
        Project secondProject = new Project();
        secondProject.setName("Second Project");
        secondProject.setOwner(existingUser);

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));
        when(projectRepository.findByOwner(existingUser))
                .thenReturn(List.of(existingProject, secondProject));

        List<ProjectResponse> results = projectService
                .getUserProjects("testuser");

        assertEquals(2, results.size());
        assertEquals("Test Project", results.get(0).name());
        assertEquals("Second Project", results.get(1).name());
        verify(projectRepository).findByOwner(existingUser);
    }
}
