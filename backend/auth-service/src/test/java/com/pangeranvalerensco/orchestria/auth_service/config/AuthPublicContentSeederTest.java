package com.pangeranvalerensco.orchestria.auth_service.config;

import com.pangeranvalerensco.orchestria.auth_service.entity.Permission;
import com.pangeranvalerensco.orchestria.auth_service.entity.Role;
import com.pangeranvalerensco.orchestria.auth_service.repository.PermissionRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.RoleRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthPublicContentSeederTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AuthDataSeeder authDataSeeder;

    private static final List<String> PUBLIC_PERMISSIONS = List.of(
            "public.content.read",
            "public.content.manage",
            "public.organization.manage",
            "public.activity.manage",
            "public.media.manage"
    );

    @BeforeEach
    void setup() {
        authDataSeeder = new AuthDataSeeder(roleRepository, permissionRepository, userRepository, passwordEncoder);
    }

    @Test
    @Transactional
    void seeder_ShouldCreatePublicPermissions() {
        authDataSeeder.run();

        for (String perm : PUBLIC_PERMISSIONS) {
            assertTrue(permissionRepository.findByName(perm).isPresent(), 
                "Permission " + perm + " should exist");
        }
    }

    @Test
    @Transactional
    void seeder_ShouldCreateHumasRole() {
        authDataSeeder.run();

        assertTrue(roleRepository.findByName("HUMAS").isPresent(), 
            "Role HUMAS should exist");
    }

    @Test
    @Transactional
    void seeder_ShouldAssignCorrectPermissionsToHumas() {
        authDataSeeder.run();

        Role humas = roleRepository.findByName("HUMAS").orElseThrow();
        Set<String> humasPerms = humas.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        assertEquals(PUBLIC_PERMISSIONS.size(), humasPerms.size());
        assertTrue(humasPerms.containsAll(PUBLIC_PERMISSIONS));
    }

    @Test
    @Transactional
    void seeder_ShouldAssignCorrectPermissionsToSuperAdminAndKetuaPub() {
        authDataSeeder.run();

        Role superAdmin = roleRepository.findByName("SUPER_ADMIN").orElseThrow();
        Set<String> superAdminPerms = superAdmin.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        assertTrue(superAdminPerms.containsAll(PUBLIC_PERMISSIONS));

        Role ketuaPub = roleRepository.findByName("KETUA_PUB").orElseThrow();
        Set<String> ketuaPubPerms = ketuaPub.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        assertTrue(ketuaPubPerms.containsAll(PUBLIC_PERMISSIONS));
    }

    @Test
    @Transactional
    void seeder_ShouldAssignCorrectPermissionsToSekretaris() {
        authDataSeeder.run();

        Role sekretaris = roleRepository.findByName("SEKRETARIS").orElseThrow();
        Set<String> sekPerms = sekretaris.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        assertTrue(sekPerms.contains("public.content.read"));
        assertTrue(sekPerms.contains("public.organization.manage"));
        assertFalse(sekPerms.contains("public.content.manage"));
    }

    @Test
    @Transactional
    void seeder_ShouldBeIdempotent() {
        authDataSeeder.run();
        long initialRoleCount = roleRepository.count();
        long initialPermCount = permissionRepository.count();

        // Run again
        authDataSeeder.run();

        assertEquals(initialRoleCount, roleRepository.count());
        assertEquals(initialPermCount, permissionRepository.count());
    }
}
