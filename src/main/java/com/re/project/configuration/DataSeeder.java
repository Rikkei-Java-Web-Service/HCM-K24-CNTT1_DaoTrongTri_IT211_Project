package com.re.project.configuration;

import com.re.project.entity.Role;
import com.re.project.entity.RoleEnum;
import com.re.project.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            log.info("Seeding roles to database...");
            for (RoleEnum roleEnum : RoleEnum.values()) {
                Role role = Role.builder()
                        .name(roleEnum.name())
                        .description("System generated role")
                        .build();
                roleRepository.save(role);
            }
            log.info("Roles seeded successfully.");
        }
    }
}
