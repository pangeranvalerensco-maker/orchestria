package com.pangeranvalerensco.orchestria.organization_service.config;

import com.pangeranvalerensco.orchestria.organization_service.entity.Division;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.MemberAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.OrganizationPeriod;
import com.pangeranvalerensco.orchestria.organization_service.entity.Position;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssignmentStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.MemberStatus;
import com.pangeranvalerensco.orchestria.organization_service.repository.DivisionRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberAssignmentRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.OrganizationPeriodRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Component
@Profile("local")
@RequiredArgsConstructor
public class PubStructureOrganizationSeeder {

    private static final String PERIOD_NAME = "PUB 2025/2026";
    private static final String COHORT_NAME = "PUB 2025";
    private static final String EMAIL_DOMAIN = "@orchestria.local";

    private final OrganizationPeriodRepository periodRepository;
    private final DivisionRepository divisionRepository;
    private final PositionRepository positionRepository;
    private final MemberRepository memberRepository;
    private final MemberAssignmentRepository memberAssignmentRepository;

    @Value("${app.bootstrap-pub-structure.enabled:true}")
    private boolean enabled;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedPubStructure() {
        if (!enabled) {
            return;
        }

        OrganizationPeriod period = periodRepository.findByName(PERIOD_NAME)
                .orElseThrow(() -> new IllegalStateException(
                        "Periode organisasi tidak ditemukan: " + PERIOD_NAME));

        Division coreDivision = ensureCoreDivision();

        for (AssignmentSeed seed : assignmentSeeds()) {
            Member member = upsertMember(seed.fullName());
            Division division = "PENGURUS_INTI".equals(seed.divisionCode())
                    ? coreDivision
                    : findDivision(seed.divisionCode());
            Position position = findPosition(seed.positionCode());

            upsertAssignment(member, period, division, position);
        }
    }

    private Division ensureCoreDivision() {
        return divisionRepository.findByCode("PENGURUS_INTI")
                .map(existing -> {
                    existing.setName("Pengurus Inti");
                    existing.setDescription("Struktur inti PUB di luar pembagian divisi operasional.");
                    existing.setDisplayOrder(0);
                    existing.setPublicVisible(true);
                    existing.setActive(true);
                    return divisionRepository.save(existing);
                })
                .orElseGet(() -> divisionRepository.save(
                        Division.builder()
                                .code("PENGURUS_INTI")
                                .name("Pengurus Inti")
                                .description("Struktur inti PUB di luar pembagian divisi operasional.")
                                .displayOrder(0)
                                .publicVisible(true)
                                .active(true)
                                .build()));
    }

    private Member upsertMember(String fullName) {
        String email = toLocalEmail(fullName);

        Member member = memberRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> memberRepository.findAll()
                        .stream()
                        .filter(existing -> existing.getFullName().equalsIgnoreCase(fullName))
                        .findFirst()
                        .orElseGet(() -> Member.builder()
                                .fullName(fullName)
                                .email(email)
                                .cohort(COHORT_NAME)
                                .status(MemberStatus.ACTIVE)
                                .publicVisible(true)
                                .displayOrder(99)
                                .active(true)
                                .build()));

        member.setFullName(fullName);
        member.setEmail(email);
        member.setCohort(COHORT_NAME);
        member.setStatus(MemberStatus.ACTIVE);
        member.setPublicVisible(true);
        member.setActive(true);

        return memberRepository.save(member);
    }

    private void upsertAssignment(
            Member member,
            OrganizationPeriod period,
            Division division,
            Position position) {

        MemberAssignment existing = memberAssignmentRepository.findAll()
                .stream()
                .filter(assignment -> assignment.getMember().getId().equals(member.getId()))
                .filter(assignment -> assignment.getPeriod().getId().equals(period.getId()))
                .filter(assignment -> assignment.getDivision().getId().equals(division.getId()))
                .filter(assignment -> assignment.getPosition().getId().equals(position.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setStatus(AssignmentStatus.ACTIVE);
            existing.setActive(true);
            memberAssignmentRepository.save(existing);
            return;
        }

        memberAssignmentRepository.save(
                MemberAssignment.builder()
                        .member(member)
                        .period(period)
                        .division(division)
                        .position(position)
                        .status(AssignmentStatus.ACTIVE)
                        .active(true)
                        .build());
    }

    private Division findDivision(String code) {
        return divisionRepository.findByCode(code)
                .filter(Division::getActive)
                .orElseThrow(() -> new IllegalStateException(
                        "Divisi seed PUB tidak ditemukan: " + code));
    }

    private Position findPosition(String code) {
        return positionRepository.findByCode(code)
                .filter(Position::getActive)
                .orElseThrow(() -> new IllegalStateException(
                        "Posisi seed PUB tidak ditemukan: " + code));
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

    private List<AssignmentSeed> assignmentSeeds() {
        return List.of(
                assignment("Abdul Hafiz Tanjung", "PENGURUS_INTI", "PEMBINA"),
                assignment("Pangeran Valerensco Rivaldi Hutabarat", "PENGURUS_INTI", "KETUA_PUB"),
                assignment("Ikram Fuadi Rambe", "PENGURUS_INTI", "KEAMANAN"),
                assignment("Khalisha Ulfa Marsha", "PENGURUS_INTI", "SEKRETARIS"),
                assignment("Andini Siti Nuriyanti", "PENGURUS_INTI", "BENDAHARA_INTERNAL"),
                assignment("Sri Rahayu Lestari", "PENGURUS_INTI", "BENDAHARA_EKSTERNAL"),

                assignment("Dedy Darmawan Simanjuntak", "DIVDIK", "COACH_INSTRUKTUR"),
                assignment("Firman Suherman", "DIVDIK", "COACH_INSTRUKTUR"),
                assignment("Rickhy Ramadhan", "DIVDIK", "KETUA_DIVISI"),
                assignment("Raysha Fauziyah Andani", "DIVDIK", "SEKRETARIS"),
                assignment("Yudistira Syahputra", "DIVDIK", "ANGGOTA"),
                assignment("Taufik Rahman Tanjung", "DIVDIK", "ANGGOTA"),
                assignment("Izhar Harahap", "DIVDIK", "ANGGOTA"),
                assignment("M Faiq Emil Fuadi", "DIVDIK", "ANGGOTA"),
                assignment("Alif Rifki Pratama", "DIVDIK", "ANGGOTA"),
                assignment("Dhea Firmasari", "DIVDIK", "ANGGOTA"),
                assignment("Nabila Monica", "DIVDIK", "ANGGOTA"),
                assignment("Ines Karlina", "DIVDIK", "ANGGOTA"),
                assignment("Miftahul Jannah Harahap", "DIVDIK", "ANGGOTA"),
                assignment("Dea Afrilia", "DIVDIK", "ANGGOTA"),

                assignment("Zaky Setiawan", "BAHASA_INGGRIS", "KETUA_DIVISI"),
                assignment("Yudistira Syahputra", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("Taufik Rahman Tanjung", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("Fajar Sidik", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("M Faiq Emil Fuadi", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("Alif Rifki Pratama", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("Alfarizi", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("Dhea Firmasari", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("Apriliyanti", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("Raysha Fauziyah Andani", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("Sri Rahayu Lestari", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("Dea Afrilia", "BAHASA_INGGRIS", "ANGGOTA"),
                assignment("Sri Muthian", "BAHASA_INGGRIS", "ANGGOTA"),

                assignment("Muhammad Farid", "KEASRAMAAN", "KETUA_DIVISI"),
                assignment("Azhar Farizi", "KEASRAMAAN", "ANGGOTA"),
                assignment("Ewi Lestari Harahap", "KEASRAMAAN", "ANGGOTA"),
                assignment("Yusri Hasanah", "KEASRAMAAN", "ANGGOTA"),

                assignment("Ahmad Zaki Hosammido", "KEROHANIAN", "KETUA_DIVISI"),
                assignment("Ade Dermawan", "KEROHANIAN", "ANGGOTA"),
                assignment("Nabila Monica", "KEROHANIAN", "ANGGOTA"),
                assignment("Andini Siti Nuriyanti", "KEROHANIAN", "ANGGOTA"),

                assignment("M Saroni", "KESEJAHTERAAN", "KETUA_DIVISI"),
                assignment("Ali Sahroji", "KESEJAHTERAAN", "ANGGOTA"),
                assignment("Sri Muthia Ningrum", "KESEJAHTERAAN", "ANGGOTA"),
                assignment("Galang Ponco Maulana", "KESEJAHTERAAN", "ANGGOTA"),

                assignment("Fajar Sidik", "KESEHATAN", "KETUA_DIVISI"),
                assignment("Padellan Riski", "KESEHATAN", "ANGGOTA"),
                assignment("Miftahul Jannah Harahap", "KESEHATAN", "ANGGOTA"),
                assignment("Apriliyanti", "KESEHATAN", "ANGGOTA"),

                assignment("M Haikal", "KEBERSIHAN", "KETUA_DIVISI"),
                assignment("Alfa Rizi", "KEBERSIHAN", "ANGGOTA"),
                assignment("Raja Tegar Albaihaqi", "KEBERSIHAN", "ANGGOTA"),
                assignment("Ulil Arsyad Ramadhan", "KEBERSIHAN", "ANGGOTA"),

                assignment("Yudistira Syahputra", "HUMAS", "KETUA_DIVISI"),
                assignment("M Faiq Emil Fuadi", "HUMAS", "ANGGOTA"),
                assignment("Dea Afrilia", "HUMAS", "ANGGOTA"),
                assignment("Nabila Monica", "HUMAS", "ANGGOTA")
        );
    }

    private AssignmentSeed assignment(
            String fullName,
            String divisionCode,
            String positionCode) {
        return new AssignmentSeed(fullName, divisionCode, positionCode);
    }

    private record AssignmentSeed(
            String fullName,
            String divisionCode,
            String positionCode) {
    }
}
