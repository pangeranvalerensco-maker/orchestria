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
class AuthNotificationReportSeederTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AuthDataSeeder authDataSeeder;

    private static final List<String> NOTIF_REPORT_PERMISSIONS = List.of(
            "notification.send",
            "notification.read",
            "notification.retry",
            "report.read",
            "report.export",
            "report.import",
            "scheduler.log.read"
    );

    @BeforeEach
    void setup() {
        authDataSeeder = new AuthDataSeeder(roleRepository, permissionRepository, userRepository, passwordEncoder);
    }

    @Test
    @Transactional
    void seeder_ShouldCreateNotificationReportPermissions() {
        authDataSeeder.run();

        for (String perm : NOTIF_REPORT_PERMISSIONS) {
            assertTrue(permissionRepository.findByName(perm).isPresent(), 
                "Permission " + perm + " should exist");
        }
    }

    @Test
    @Transactional
    void seeder_ShouldAssignCorrectPermissionsToSuperAdmin() {
        authDataSeeder.run();

        Role superAdmin = roleRepository.findByName("SUPER_ADMIN").orElseThrow();
        Set<String> superAdminPerms = superAdmin.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        assertTrue(superAdminPerms.containsAll(NOTIF_REPORT_PERMISSIONS));
    }

    @Test
    @Transactional
    void seeder_ShouldAssignCorrectPermissionsToSekretaris() {
        authDataSeeder.run();

        Role sekretaris = roleRepository.findByName("SEKRETARIS").orElseThrow();
        Set<String> sekPerms = sekretaris.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        assertTrue(sekPerms.containsAll(NOTIF_REPORT_PERMISSIONS));
    }

    @Test
    @Transactional
    void seeder_ShouldAssignCorrectPermissionsToKetuaPubAndPembina() {
        authDataSeeder.run();

        Role ketuaPub = roleRepository.findByName("KETUA_PUB").orElseThrow();
        Set<String> ketuaPubPerms = ketuaPub.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        
        assertTrue(ketuaPubPerms.containsAll(List.of(
            "notification.send",
            "notification.read",
            "report.read",
            "report.export",
            "scheduler.log.read"
        )));
        assertFalse(ketuaPubPerms.contains("notification.retry"));
        assertFalse(ketuaPubPerms.contains("report.import"));

        Role pembina = roleRepository.findByName("PEMBINA").orElseThrow();
        Set<String> pembinaPerms = pembina.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        
        assertTrue(pembinaPerms.containsAll(List.of(
            "notification.send",
            "notification.read",
            "report.read",
            "report.export",
            "scheduler.log.read"
        )));
        assertFalse(pembinaPerms.contains("notification.retry"));
        assertFalse(pembinaPerms.contains("report.import"));
    }

    @Test
    @Transactional
    void seeder_ShouldAssignCorrectPermissionsToBendaharas() {
        authDataSeeder.run();

        Role benInt = roleRepository.findByName("BENDAHARA_INTERNAL").orElseThrow();
        Set<String> benIntPerms = benInt.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        
        assertTrue(benIntPerms.containsAll(List.of("report.read", "report.export")));
        assertFalse(benIntPerms.contains("notification.read"));

        Role benEks = roleRepository.findByName("BENDAHARA_EKSTERNAL").orElseThrow();
        Set<String> benEksPerms = benEks.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        
        assertTrue(benEksPerms.containsAll(List.of("report.read", "report.export")));
        assertFalse(benEksPerms.contains("notification.read"));
    }

    @Test
    @Transactional
    void seeder_NormalRolesShouldNotHaveNotificationPermissions() {
        authDataSeeder.run();

        Role anggota = roleRepository.findByName("ANGGOTA").orElseThrow();
        Set<String> anggotaPerms = anggota.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        
        for (String perm : NOTIF_REPORT_PERMISSIONS) {
            assertFalse(anggotaPerms.contains(perm), "Role ANGGOTA should not have " + perm);
        }
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
