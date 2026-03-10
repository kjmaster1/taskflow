package com.kjmaster.taskflow.user;

import com.kjmaster.taskflow.exception.ConflictException;
import com.kjmaster.taskflow.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("$2a$10$hashedpassword");
    }

    @Test
    void registerUser_Success() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(existingUser);

        User result = userService.registerUser(
                "test@example.com", "testuser", "password123"
        );

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsConflictException() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.registerUser(
                        "test@example.com", "testuser", "password123"
                )
        );

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_DuplicateUsername_ThrowsConflictException() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.registerUser(
                        "test2@example.com", "testuser", "password123"
                )
        );

        assertEquals("Username already in use", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success_ReturnsToken() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123",
                existingUser.getPassword()))
                .thenReturn(true);
        when(jwtUtil.generateToken("testuser"))
                .thenReturn("mock.jwt.token");

        String token = userService.login("testuser", "password123");

        assertEquals("mock.jwt.token", token);
        verify(jwtUtil).generateToken("testuser");
    }

    @Test
    void login_WrongPassword_ThrowsException() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login("testuser", "wrongpassword")
        );

        verify(jwtUtil, never()).generateToken(anyString());
    }
}