package com.kjmaster.taskflow.project;

import com.kjmaster.taskflow.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwner(User owner);
    Optional<Project> findByIdAndOwner(Long id, User owner);
}