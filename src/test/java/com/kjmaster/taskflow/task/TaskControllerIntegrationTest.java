package com.kjmaster.taskflow.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjmaster.taskflow.project.Project;
import com.kjmaster.taskflow.project.ProjectRepository;
import com.kjmaster.taskflow.security.JwtUtil;
import com.kjmaster.taskflow.user.User;
import com.kjmaster.taskflow.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String userAToken;
    private String userBToken;
    private Project projectA;

    @BeforeEach
    void setUp() {
        User userA = new User();
        userA.setEmail("usera@test.com");
        userA.setUsername("usera");
        userA.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(userA);

        User userB = new User();
        userB.setEmail("userb@test.com");
        userB.setUsername("userb");
        userB.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(userB);

        userAToken = jwtUtil.generateToken("usera");
        userBToken = jwtUtil.generateToken("userb");

        projectA = new Project();
        projectA.setName("User A Project");
        projectA.setDescription("Desc");
        projectA.setOwner(userA);
        projectRepository.save(projectA);
    }

    private Task createTask(String name, TaskStatus status) {
        Task task = new Task();
        task.setName(name);
        task.setDescription("Test task");
        task.setPriority(TaskPriority.MEDIUM);
        task.setProject(projectA);
        taskRepository.save(task);
        if (status == TaskStatus.IN_PROGRESS) {
            task.transitionTo(TaskStatus.IN_PROGRESS);
            taskRepository.save(task);
        } else if (status == TaskStatus.DONE) {
            task.transitionTo(TaskStatus.IN_PROGRESS);
            task.transitionTo(TaskStatus.DONE);
            taskRepository.save(task);
        }
        return task;
    }

    // --- CREATE TASK ---

    @Test
    void createTask_success() throws Exception {
        mockMvc.perform(post("/projects/" + projectA.getId() + "/tasks")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateTaskRequest("New Task", "Desc", TaskPriority.HIGH, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.startedAt").isEmpty())
                .andExpect(jsonPath("$.completedAt").isEmpty());
    }

    @Test
    void createTask_noToken_returns401() throws Exception {
        mockMvc.perform(post("/projects/" + projectA.getId() + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateTaskRequest("New Task", "Desc", TaskPriority.HIGH, null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTask_wrongUser_returns404() throws Exception {
        mockMvc.perform(post("/projects/" + projectA.getId() + "/tasks")
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateTaskRequest("New Task", "Desc", TaskPriority.HIGH, null))))
                .andExpect(status().isNotFound());
    }

    // --- GET TASKS ---

    @Test
    void getTasks_success() throws Exception {
        createTask("Task One", TaskStatus.TODO);

        mockMvc.perform(get("/projects/" + projectA.getId() + "/tasks")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Task One"));
    }

    @Test
    void getTasks_noTasks_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/projects/" + projectA.getId() + "/tasks")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- STATE MACHINE ---

    @Test
    void transition_todoToInProgress_succeeds() throws Exception {
        Task task = createTask("Task", TaskStatus.TODO);

        mockMvc.perform(patch("/projects/" + projectA.getId() + "/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateTaskRequest(null, null, TaskStatus.IN_PROGRESS, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.startedAt").isNotEmpty())
                .andExpect(jsonPath("$.completedAt").isEmpty());
    }

    @Test
    void transition_inProgressToDone_succeeds() throws Exception {
        Task task = createTask("Task", TaskStatus.IN_PROGRESS);

        mockMvc.perform(patch("/projects/" + projectA.getId() + "/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateTaskRequest(null, null, TaskStatus.DONE, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    @Test
    void transition_todoToDone_returns409() throws Exception {
        Task task = createTask("Task", TaskStatus.TODO);

        mockMvc.perform(patch("/projects/" + projectA.getId() + "/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateTaskRequest(null, null, TaskStatus.DONE, null, null))))
                .andExpect(status().isConflict());
    }

    @Test
    void transition_toTodo_returns409() throws Exception {
        Task task = createTask("Task", TaskStatus.IN_PROGRESS);

        mockMvc.perform(patch("/projects/" + projectA.getId() + "/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateTaskRequest(null, null, TaskStatus.TODO, null, null))))
                .andExpect(status().isConflict());
    }

    // --- DELETE TASK ---

    @Test
    void deleteTask_success() throws Exception {
        Task task = createTask("To Delete", TaskStatus.TODO);

        mockMvc.perform(delete("/projects/" + projectA.getId() + "/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_wrongUser_returns404() throws Exception {
        Task task = createTask("Task", TaskStatus.TODO);

        mockMvc.perform(delete("/projects/" + projectA.getId() + "/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());
    }
}