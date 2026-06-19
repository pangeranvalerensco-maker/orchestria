package com.pangeranvalerensco.orchestria.auth_service.config;

import com.pangeranvalerensco.orchestria.auth_service.entity.Role;
import com.pangeranvalerensco.orchestria.auth_service.entity.User;
import com.pangeranvalerensco.orchestria.auth_service.repository.RoleRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@Profile("local")
@RequiredArgsConstructor
public class PubStructureUserSeeder {

    private static final String EMAIL_DOMAIN = "@orchestria.local";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-pub-users.enabled:true}")
    private boolean enabled;

    @Value("${app.bootstrap-pub-users.password:PubSyntax23!}")
    private String defaultPassword;

    @EventListener(ApplicationReadyEvent.class)
    public void seedPubStructureUsers() {
        if (!enabled) {
            return;
        }

        if (defaultPassword == null || defaultPassword.length() < 6) {
            throw new IllegalStateException("Password bootstrap akun PUB minimal 6 karakter");
        }

        for (UserSeed seed : userSeeds()) {
            upsertUser(seed);
        }
    }

    private void upsertUser(UserSeed seed) {
        String email = toLocalEmail(seed.fullName());
        Set<Role> roles = resolveRoles(seed.roleNames());

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> User.builder()
                        .fullName(seed.fullName())
                        .email(email)
                        .password(passwordEncoder.encode(defaultPassword))
                        .active(true)
                        .roles(new LinkedHashSet<>())
                        .build());

        user.getRoles().addAll(roles);
        userRepository.save(user);
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new LinkedHashSet<>();

        for (String roleName : roleNames) {
            roles.add(roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException(
                            "Role bootstrap akun PUB tidak ditemukan: " + roleName)));
        }

        return roles;
    }

    private String toLocalEmail(String fullName) {
        String normalized = Normalizer.normalize(fullName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", ".")
                .replaceAll("^\\.+|\\.+$", "")
                .replaceAll("\\.{2,}", ".");

        return normalized + EMAIL_DOMAIN;
    }

    private List<UserSeed> userSeeds() {
        return List.of(
                roleUser("Abdul Hafiz Tanjung", "PEMBINA"),
                roleUser("Pangeran Valerensco Rivaldi Hutabarat", "KETUA_PUB"),
                member("Ikram Fuadi Rambe"),
                roleUser("Khalisha Ulfa Marsha", "SEKRETARIS"),
                roleUser("Andini Siti Nuriyanti", "BENDAHARA_INTERNAL"),
                roleUser("Sri Rahayu Lestari", "BENDAHARA_EKSTERNAL"),

                member("Dedy Darmawan Simanjuntak"),
                member("Firman Suherman"),
                roleUser("Rickhy Ramadhan", "KETUA_DIVISI"),
                member("Raysha Fauziyah Andani"),
                roleUser("Yudistira Syahputra", "KETUA_DIVISI"),
                member("Taufik Rahman Tanjung"),
                member("Izhar Harahap"),
                member("M Faiq Emil Fuadi"),
                member("Alif Rifki Pratama"),
                member("Dhea Firmasari"),
                member("Nabila Monica"),
                member("Ines Karlina"),
                member("Miftahul Jannah Harahap"),
                member("Dea Afrilia"),

                roleUser("Zaky Setiawan", "KETUA_DIVISI"),
                roleUser("Fajar Sidik", "KETUA_DIVISI"),
                member("Alfarizi"),
                member("Apriliyanti"),
                member("Sri Muthian"),

                roleUser("Muhammad Farid", "KETUA_DIVISI"),
                member("Azhar Farizi"),
                member("Ewi Lestari Harahap"),
                member("Yusri Hasanah"),

                roleUser("Ahmad Zaki Hosammido", "KETUA_DIVISI"),
                member("Ade Dermawan"),

                roleUser("M Saroni", "KETUA_DIVISI"),
                member("Ali Sahroji"),
                member("Sri Muthia Ningrum"),
                member("Galang Ponco Maulana"),

                member("Padellan Riski"),

                roleUser("M Haikal", "KETUA_DIVISI"),
                member("Alfa Rizi"),
                member("Raja Tegar Albaihaqi"),
                member("Ulil Arsyad Ramadhan")
        );
    }

    private UserSeed member(String fullName) {
        return roleUser(fullName, "ANGGOTA");
    }

    private UserSeed roleUser(String fullName, String... roleNames) {
        return new UserSeed(fullName, Set.of(roleNames));
    }

    private record UserSeed(String fullName, Set<String> roleNames) {
    }
}
