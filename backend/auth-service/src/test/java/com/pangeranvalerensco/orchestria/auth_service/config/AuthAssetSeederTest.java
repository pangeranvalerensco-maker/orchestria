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

@SpringBootTest
@ActiveProfiles("test")
public class AuthAssetSeederTest {

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
    public void testCheckerPermissions() {
        // Run seeder manually since it's disabled in test profile
        authDataSeeder.run();

        Role checkerRole = roleRepository.findByName("CHECKER").orElseThrow();
        Set<String> permissions = checkerRole.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        assertTrue(permissions.contains("asset.read"));
        assertTrue(permissions.contains("asset.manage"));
        assertTrue(permissions.contains("asset.borrow.read.all"));
        assertTrue(permissions.contains("asset.borrow.approve"));
        assertTrue(permissions.contains("asset.borrow.handover"));
        assertTrue(permissions.contains("asset.return.verify"));
        assertTrue(permissions.contains("asset.condition.manage"));

        // Checker should not be able to create borrowing unless they are also ANGGOTA
        assertFalse(permissions.contains("asset.borrow.create"));
    }

    @Test
    @Transactional
    public void testAnggotaPermissions() {
        authDataSeeder.run();

        Role anggotaRole = roleRepository.findByName("ANGGOTA").orElseThrow();
        Set<String> permissions = anggotaRole.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        assertTrue(permissions.contains("asset.read"));
        assertTrue(permissions.contains("asset.borrow.create"));
        assertTrue(permissions.contains("asset.borrow.read.own"));

        // Anggota should not have operational permissions
        assertFalse(permissions.contains("asset.manage"));
        assertFalse(permissions.contains("asset.borrow.approve"));
        assertFalse(permissions.contains("asset.borrow.handover"));
        assertFalse(permissions.contains("asset.return.verify"));
        assertFalse(permissions.contains("asset.condition.manage"));
    }
}
