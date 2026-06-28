package com.re.project.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.re.project.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Ghi đè phương thức findAll mặc định
    @Override
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    List<User> findAll();

    // Ghi đè phương thức findById mặc định
    @Override
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<User> findById(Long id);

    boolean existsUserByUsername(String username);
    boolean existsUserByEmail(String email);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<User> findUserByUsername(String username);

    Optional<User> findByEmail(String email);
}
