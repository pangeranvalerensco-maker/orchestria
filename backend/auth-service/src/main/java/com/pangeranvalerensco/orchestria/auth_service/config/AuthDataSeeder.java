package com.pangeranvalerensco.orchestria.auth_service.config;

import com.pangeranvalerensco.orchestria.auth_service.entity.Permission;
import com.pangeranvalerensco.orchestria.auth_service.entity.Role;
import com.pangeranvalerensco.orchestria.auth_service.repository.PermissionRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.pangeranvalerensco.orchestria.auth_service.entity.User;
import com.pangeranvalerensco.orchestria.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class AuthDataSeeder implements CommandLineRunner {

        private final RoleRepository roleRepository;
        private final PermissionRepository permissionRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        @Value("${app.bootstrap-admin.enabled:false}")
        private boolean bootstrapAdminEnabled;

        @Value("${app.bootstrap-admin.full-name:}")
        private String bootstrapAdminFullName;

        @Value("${app.bootstrap-admin.email:}")
        private String bootstrapAdminEmail;

        @Value("${app.bootstrap-admin.password:}")
        private String bootstrapAdminPassword;

        @Override
        public void run(String... args) {
                seedPermissions();
                seedRoles();
                assignPermissionsToRoles();
                seedBootstrapAdmin();
        }

        private void seedPermissions() {
                List<PermissionSeed> permissions = List.of(
                                new PermissionSeed("auth.user.read", "Melihat data user"),
                                new PermissionSeed("auth.user.manage", "Mengelola user"),
                                new PermissionSeed("auth.role.manage", "Mengelola role dan permission"),

                                new PermissionSeed("organization.read", "Melihat data organisasi"),
                                new PermissionSeed("organization.manage", "Mengelola anggota, divisi, dan jabatan"),

                                new PermissionSeed("division.task.read", "Melihat tugas divisi"),
                                new PermissionSeed("division.task.manage", "Mengelola tugas dan agenda divisi"),

                                new PermissionSeed("request.create", "Membuat pengajuan operasional"),
                                new PermissionSeed("request.read.own", "Melihat pengajuan milik sendiri"),
                                new PermissionSeed("request.read.all", "Melihat seluruh pengajuan"),
                                new PermissionSeed("request.approve.division", "Approval pengajuan oleh Ketua Divisi"),
                                new PermissionSeed("request.approve.pub", "Approval pengajuan oleh Ketua PUB"),
                                new PermissionSeed("request.approve.pembina", "Approval pengajuan oleh Pembina"),

                                new PermissionSeed("finance.disburse", "Melakukan pencairan dana"),
                                new PermissionSeed("finance.settlement.verify", "Verifikasi settlement keuangan"),
                                new PermissionSeed("finance.report.read", "Melihat laporan keuangan"),

                                new PermissionSeed("archive.manage", "Mengelola arsip dokumen"),
                                new PermissionSeed("notification.manage", "Mengelola notifikasi"),
                                new PermissionSeed("report.read", "Melihat laporan umum"),

                                new PermissionSeed("asset.read", "Melihat aset"),
                                new PermissionSeed("asset.manage", "Mengelola data aset"),
                                new PermissionSeed("asset.borrow.create", "Mengajukan peminjaman aset"),
                                new PermissionSeed("asset.borrow.read.own", "Melihat peminjaman sendiri"),
                                new PermissionSeed("asset.borrow.read.all", "Melihat semua peminjaman"),
                                new PermissionSeed("asset.borrow.approve", "Merespons permohonan peminjaman"),
                                new PermissionSeed("asset.borrow.handover", "Menyerahkan aset"),
                                new PermissionSeed("asset.return.verify", "Memverifikasi pengembalian aset"),
                                new PermissionSeed("asset.condition.manage", "Mengelola kondisi aset"));

                for (PermissionSeed seed : permissions) {
                        permissionRepository.findByName(seed.name())
                                        .orElseGet(() -> permissionRepository.save(
                                                        Permission.builder()
                                                                        .name(seed.name())
                                                                        .description(seed.description())
                                                                        .active(true)
                                                                        .build()));
                }
        }

        private void seedRoles() {
                List<RoleSeed> roles = List.of(
                                new RoleSeed("SUPER_ADMIN", "Akses penuh sistem"),
                                new RoleSeed("PEMBINA", "Pembina organisasi"),
                                new RoleSeed("KETUA_PUB", "Ketua PUB"),
                                new RoleSeed("KETUA_DIVISI", "Ketua Divisi"),
                                new RoleSeed("SEKRETARIS", "Sekretaris organisasi"),
                                new RoleSeed("BENDAHARA_INTERNAL", "Bendahara internal"),
                                new RoleSeed("BENDAHARA_EKSTERNAL", "Bendahara eksternal"),
                                new RoleSeed("CHECKER", "Pemeriksa dan pengelola operasional aset"),
                                new RoleSeed("ANGGOTA", "Anggota organisasi"));

                for (RoleSeed seed : roles) {
                        roleRepository.findByName(seed.name())
                                        .orElseGet(() -> roleRepository.save(
                                                        Role.builder()
                                                                        .name(seed.name())
                                                                        .description(seed.description())
                                                                        .active(true)
                                                                        .build()));
                }
        }

        private void assignPermissionsToRoles() {
                assign("SUPER_ADMIN", List.of(
                                "auth.user.read",
                                "auth.user.manage",
                                "auth.role.manage",
                                "organization.read",
                                "organization.manage",
                                "division.task.read",
                                "division.task.manage",
                                "request.create",
                                "request.read.own",
                                "request.read.all",
                                "request.approve.division",
                                "request.approve.pub",
                                "request.approve.pembina",
                                "finance.disburse",
                                "finance.settlement.verify",
                                "finance.report.read",
                                "archive.manage",
                                "notification.manage",
                                "report.read",
                                "asset.read",
                                "asset.manage",
                                "asset.borrow.create",
                                "asset.borrow.read.own",
                                "asset.borrow.read.all",
                                "asset.borrow.approve",
                                "asset.borrow.handover",
                                "asset.return.verify",
                                "asset.condition.manage"));

                assign("PEMBINA", List.of(
                                "organization.read",
                                "division.task.read",
                                "request.read.all",
                                "request.approve.pembina",
                                "finance.report.read",
                                "report.read",
                                "asset.read",
                                "asset.borrow.read.all"));

                assign("KETUA_PUB", List.of(
                                "organization.read",
                                "organization.manage",
                                "division.task.read",
                                "division.task.manage",
                                "request.create",
                                "request.read.all",
                                "request.approve.pub",
                                "finance.report.read",
                                "archive.manage",
                                "report.read",
                                "asset.read",
                                "asset.manage",
                                "asset.borrow.read.all",
                                "asset.borrow.approve",
                                "asset.borrow.handover",
                                "asset.return.verify",
                                "asset.condition.manage"));

                assign("KETUA_DIVISI", List.of(
                                "organization.read",
                                "division.task.read",
                                "division.task.manage",
                                "request.create",
                                "request.read.own",
                                "request.approve.division",
                                "asset.read",
                                "asset.borrow.create",
                                "asset.borrow.read.own"));

                assign("SEKRETARIS", List.of(
                                "organization.read",
                                "organization.manage",
                                "archive.manage",
                                "report.read"));

                assign("BENDAHARA_INTERNAL", List.of(
                                "organization.read",
                                "request.read.all",
                                "finance.disburse",
                                "finance.settlement.verify",
                                "finance.report.read",
                                "report.read"));

                assign("BENDAHARA_EKSTERNAL", List.of(
                                "organization.read",
                                "finance.report.read",
                                "report.read"));

                assign("CHECKER", List.of(
                                "organization.read",
                                "asset.read",
                                "asset.manage",
                                "asset.borrow.read.all",
                                "asset.borrow.approve",
                                "asset.borrow.handover",
                                "asset.return.verify",
                                "asset.condition.manage"));

                assign("ANGGOTA", List.of(
                                "organization.read",
                                "division.task.read",
                                "request.create",
                                "request.read.own",
                                "asset.read",
                                "asset.borrow.create",
                                "asset.borrow.read.own"));
        }

        private void assign(String roleName, List<String> permissionNames) {
                Role role = roleRepository.findByName(roleName)
                                .orElseThrow(() -> new IllegalStateException("Role tidak ditemukan: " + roleName));

                Set<Permission> permissions = new LinkedHashSet<>();

                for (String permissionName : permissionNames) {
                        Permission permission = permissionRepository.findByName(permissionName)
                                        .orElseThrow(() -> new IllegalStateException(
                                                        "Permission tidak ditemukan: " + permissionName));

                        permissions.add(permission);
                }

                role.setPermissions(permissions);
                roleRepository.save(role);
        }

        private void seedBootstrapAdmin() {
                if (!bootstrapAdminEnabled) {
                        return;
                }

                if (bootstrapAdminFullName == null
                                || bootstrapAdminFullName.isBlank()
                                || bootstrapAdminEmail == null
                                || bootstrapAdminEmail.isBlank()
                                || bootstrapAdminPassword == null
                                || bootstrapAdminPassword.isBlank()) {
                        throw new IllegalStateException(
                                        "Konfigurasi bootstrap admin belum lengkap");
                }

                String normalizedEmail = bootstrapAdminEmail.trim().toLowerCase();

                if (userRepository.existsByEmail(normalizedEmail)) {
                        return;
                }

                Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                                .orElseThrow(() -> new IllegalStateException(
                                                "Role SUPER_ADMIN tidak ditemukan"));

                User admin = User.builder()
                                .fullName(bootstrapAdminFullName.trim())
                                .email(normalizedEmail)
                                .password(
                                                passwordEncoder.encode(
                                                                bootstrapAdminPassword))
                                .active(true)
                                .roles(Set.of(superAdminRole))
                                .build();

                userRepository.save(admin);
        }

        private record PermissionSeed(String name, String description) {
        }

        private record RoleSeed(String name, String description) {
        }
}