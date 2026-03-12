package com.kjmaster.taskflow.project;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String userAToken;
    private String userBToken;
    private User userA;

    @BeforeEach
    void setUp() {
        userA = new User();
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
    }

    private Project createProject(String name, String description, User owner) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setOwner(owner);
        return projectRepository.save(project);
    }

    // --- CREATE PROJECT ---

    @Test
    void createProject_success() throws Exception {
        mockMvc.perform(post("/projects")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateProjectRequest("My Project", "Description"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Project"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    void createProject_noToken_returns401() throws Exception {
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateProjectRequest("My Project", "Description"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProject_blankName_returns400() throws Exception {
        mockMvc.perform(post("/projects")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateProjectRequest("", "Description"))))
                .andExpect(status().isBadRequest());
    }

    // --- GET PROJECTS ---

    @Test
    void getProjects_success() throws Exception {
        createProject("Project One", "Desc", userA);

        mockMvc.perform(get("/projects")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Project One"));
    }

    @Test
    void getProjects_noProjects_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/projects")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getProjects_noToken_returns401() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isUnauthorized());
    }

    // --- UPDATE PROJECT ---

    @Test
    void updateProject_success() throws Exception {
        Project project = createProject("Old Name", "Old Desc", userA);

        mockMvc.perform(patch("/projects/" + project.getId())
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateProjectRequest("New Name", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.description").value("Old Desc"));
    }

    @Test
    void updateProject_wrongUser_returns404() throws Exception {
        Project project = createProject("User A Project", "Desc", userA);

        mockMvc.perform(patch("/projects/" + project.getId())
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateProjectRequest("Hacked", null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProject_wrongId_returns404() throws Exception {
        mockMvc.perform(patch("/projects/99999")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateProjectRequest("New Name", null))))
                .andExpect(status().isNotFound());
    }

    // --- DELETE PROJECT ---

    @Test
    void deleteProject_success() throws Exception {
        Project project = createProject("To Delete", "Desc", userA);

        mockMvc.perform(delete("/projects/" + project.getId())
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProject_wrongUser_returns404() throws Exception {
        Project project = createProject("User A Project", "Desc", userA);

        mockMvc.perform(delete("/projects/" + project.getId())
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProject_wrongId_returns404() throws Exception {
        mockMvc.perform(delete("/projects/99999")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNotFound());
    }
}