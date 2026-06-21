package com.pangeranvalerensco.orchestria.auth_service.config;

import com.pangeranvalerensco.orchestria.auth_service.entity.Permission;
import com.pangeranvalerensco.orchestria.auth_service.entity.Role;
import com.pangeranvalerensco.orchestria.auth_service.repository.PermissionRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.pangeranvalerensco.orchestria.auth_service.repository.UserRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class AuthCleanlinessSeederTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AuthDataSeeder authDataSeeder;

    @BeforeEach
    public void setup() {
        authDataSeeder = new AuthDataSeeder(roleRepository, permissionRepository, userRepository, passwordEncoder);
    }

    @Test
    @Transactional
    public void testIdempotentSeeder() {
        authDataSeeder.run();
        long rolesCount = roleRepository.count();
        long permissionsCount = permissionRepository.count();

        // Run again to test idempotency
        authDataSeeder.run();

        org.junit.jupiter.api.Assertions.assertEquals(rolesCount, roleRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(permissionsCount, permissionRepository.count());
    }

    @Test
    @Transactional
    public void testKoordinatorPermissions() {
        authDataSeeder.run();

        Role koordinatorRole = roleRepository.findByName("KOORDINATOR").orElseThrow();
        Set<String> permissions = koordinatorRole.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        assertTrue(permissions.contains("cleanliness.schedule.read"));
        assertTrue(permissions.contains("cleanliness.schedule.manage"));
        assertTrue(permissions.contains("cleanliness.attendance.create"));
        assertTrue(permissions.contains("cleanliness.attendance.read"));
        assertTrue(permissions.contains("cleanliness.point.manage"));
        assertTrue(permissions.contains("cleanliness.violation.manage"));
        assertTrue(permissions.contains("cleanliness.report.read"));
    }

    @Test
    @Transactional
    public void testAnggotaCleanlinessPermissions() {
        authDataSeeder.run();

        Role anggotaRole = roleRepository.findByName("ANGGOTA").orElseThrow();
        Set<String> permissions = anggotaRole.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        assertTrue(permissions.contains("cleanliness.schedule.read"));
        assertTrue(permissions.contains("cleanliness.attendance.create"));

        assertFalse(permissions.contains("cleanliness.schedule.manage"));
        assertFalse(permissions.contains("cleanliness.point.manage"));
        assertFalse(permissions.contains("cleanliness.violation.manage"));
    }
}
