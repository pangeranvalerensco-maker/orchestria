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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AuthEnglishSeederTest {

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
    void shouldSeedEnglishPermissions() {
        authDataSeeder.run(); // idempotent check
        
        List<String> expectedPermissions = List.of(
                "english.activity.read",
                "english.activity.manage",
                "english.deposit.create",
                "english.deposit.read.own",
                "english.deposit.read.all",
                "english.deposit.verify",
                "english.report.read"
        );

        for (String permName : expectedPermissions) {
            Optional<Permission> perm = permissionRepository.findByName(permName);
            assertThat(perm).isPresent();
        }
    }

    @Test
    @Transactional
    void superAdminShouldHaveAllEnglishPermissions() {
        authDataSeeder.run();

        Role role = roleRepository.findByName("SUPER_ADMIN").orElseThrow();
        Set<String> rolePermissions = role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());

        assertThat(rolePermissions).contains(
                "english.activity.read",
                "english.activity.manage",
                "english.deposit.create",
                "english.deposit.read.own",
                "english.deposit.read.all",
                "english.deposit.verify",
                "english.report.read"
        );
    }

    @Test
    @Transactional
    void koordinatorShouldHaveAllEnglishPermissions() {
        authDataSeeder.run();

        Role role = roleRepository.findByName("KOORDINATOR").orElseThrow();
        Set<String> rolePermissions = role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());

        assertThat(rolePermissions).contains(
                "english.activity.read",
                "english.activity.manage",
                "english.deposit.create",
                "english.deposit.read.own",
                "english.deposit.read.all",
                "english.deposit.verify",
                "english.report.read"
        );
    }

    @Test
    @Transactional
    void anggotaShouldHaveMemberEnglishPermissions() {
        authDataSeeder.run();

        Role role = roleRepository.findByName("ANGGOTA").orElseThrow();
        Set<String> rolePermissions = role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());

        assertThat(rolePermissions).contains(
                "english.activity.read",
                "english.deposit.create",
                "english.deposit.read.own"
        );
        assertThat(rolePermissions).doesNotContain(
                "english.activity.manage",
                "english.deposit.read.all",
                "english.deposit.verify",
                "english.report.read"
        );
    }
}
