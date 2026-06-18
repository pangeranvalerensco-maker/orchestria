package com.pangeranvalerensco.orchestria.organization_service.config;

import com.pangeranvalerensco.orchestria.organization_service.entity.Division;
import com.pangeranvalerensco.orchestria.organization_service.entity.OrganizationPeriod;
import com.pangeranvalerensco.orchestria.organization_service.entity.Position;
import com.pangeranvalerensco.orchestria.organization_service.repository.DivisionRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.OrganizationPeriodRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.MemberAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssignmentStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.MemberStatus;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberAssignmentRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;

import org.springframework.beans.factory.annotation.Value;

import java.util.Locale;
import java.util.Objects;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class OrganizationDataSeeder implements CommandLineRunner {

        private final OrganizationPeriodRepository periodRepository;
        private final DivisionRepository divisionRepository;
        private final PositionRepository positionRepository;
        private final MemberRepository memberRepository;
        private final MemberAssignmentRepository memberAssignmentRepository;

        @Value("${app.bootstrap-member.enabled:false}")
        private boolean bootstrapMemberEnabled;

        @Value("${app.bootstrap-member.auth-user-id:0}")
        private Long bootstrapAuthUserId;

        @Value("${app.bootstrap-member.full-name:}")
        private String bootstrapFullName;

        @Value("${app.bootstrap-member.email:}")
        private String bootstrapEmail;

        @Value("${app.bootstrap-member.division-code:DIVDIK}")
        private String bootstrapDivisionCode;

        @Value("${app.bootstrap-member.position-code:KETUA_DIVISI}")
        private String bootstrapPositionCode;

        @Override
        public void run(String... args) {
                seedPeriods();
                seedDivisions();
                seedPositions();
                seedBootstrapMemberAssignment();
        }

        private void seedPeriods() {
                periodRepository.findByName("PUB 2025/2026")
                                .orElseGet(() -> periodRepository.save(
                                                OrganizationPeriod.builder()
                                                                .name("PUB 2025/2026")
                                                                .startDate(LocalDate.of(2025, 9, 1))
                                                                .endDate(LocalDate.of(2026, 8, 31))
                                                                .currentPeriod(true)
                                                                .publicVisible(true)
                                                                .active(true)
                                                                .build()));
        }

        private void seedDivisions() {
                List<DivisionSeed> divisions = List.of(
                                new DivisionSeed("DIVDIK", "Divisi Pendidikan dan Pelatihan",
                                                "Mengelola pelatihan, kurikulum, instruktur, dan pembelajaran PUB.", 1),

                                new DivisionSeed("HUMAS", "Hubungan Masyarakat",
                                                "Mengelola publikasi, dokumentasi, media sosial, web, dan citra PUB.",
                                                2),

                                new DivisionSeed("KESEJAHTERAAN", "Divisi Kesejahteraan",
                                                "Mengelola kebutuhan konsumsi, menu, dan kesejahteraan mahasiswa PUB.",
                                                3),

                                new DivisionSeed("KEBERSIHAN", "Divisi Kebersihan",
                                                "Mengelola jadwal piket, area kebersihan, dan kedisiplinan kebersihan lingkungan.",
                                                4),

                                new DivisionSeed("BAHASA_INGGRIS", "Divisi Bahasa Inggris",
                                                "Mengelola setoran vocabulary, latihan bahasa Inggris, dan evaluasi pembelajaran bahasa.",
                                                5),

                                new DivisionSeed("KEROHANIAN", "Divisi Kerohanian",
                                                "Mengelola pengajian, setoran bacaan Al-Qur'an, dan pembinaan ibadah.",
                                                6),

                                new DivisionSeed("KEASRAMAAN", "Divisi Keasramaan",
                                                "Mengelola kedisiplinan asrama, keberadaan mahasiswa, dan administrasi izin asrama.",
                                                7),

                                new DivisionSeed("KESEHATAN", "Divisi Kesehatan",
                                                "Mengelola perhatian kesehatan, pendataan sakit, dan kebutuhan kesehatan mahasiswa PUB.",
                                                8),

                                new DivisionSeed("PPMB", "Divisi PPMB",
                                                "Mengelola proses sosialisasi, seleksi, survei, dan penerimaan mahasiswa baru PUB.",
                                                9),

                                new DivisionSeed("ASET", "Divisi Aset",
                                                "Mengelola pendataan aset, peminjaman laptop, dan pemeriksaan kondisi barang.",
                                                10));

                for (DivisionSeed seed : divisions) {
                        divisionRepository.findByCode(seed.code())
                                        .orElseGet(() -> divisionRepository.save(
                                                        Division.builder()
                                                                        .code(seed.code())
                                                                        .name(seed.name())
                                                                        .description(seed.description())
                                                                        .displayOrder(seed.displayOrder())
                                                                        .publicVisible(true)
                                                                        .active(true)
                                                                        .build()));
                }
        }

        private void seedPositions() {
                List<PositionSeed> positions = List.of(
                                new PositionSeed("PEMBINA", "Pembina",
                                                "Pembina utama organisasi dan pengambil persetujuan akhir.", 1),

                                new PositionSeed("KETUA_PUB", "Ketua PUB",
                                                "Pemimpin utama mahasiswa PUB dan penanggung jawab koordinasi organisasi.",
                                                2),

                                new PositionSeed("KEAMANAN", "Keamanan",
                                                "Jabatan struktural yang bertanggung jawab atas penertiban, kedisiplinan, dan izin keluar mahasiswa PUB.",
                                                3),

                                new PositionSeed("SEKRETARIS", "Sekretaris",
                                                "Mengelola administrasi, arsip, surat, dan dokumentasi organisasi.", 4),

                                new PositionSeed("BENDAHARA_INTERNAL", "Bendahara Internal",
                                                "Mengelola pengajuan, pencairan internal, bukti pembayaran, dan settlement.",
                                                5),

                                new PositionSeed("BENDAHARA_EKSTERNAL", "Bendahara Eksternal",
                                                "Mencatat laporan keuangan keseluruhan dan penarikan dana dari pihak pembina/yayasan.",
                                                6),

                                new PositionSeed("KETUA_DIVISI", "Ketua Divisi",
                                                "Memimpin dan bertanggung jawab atas operasional divisi.", 7),

                                new PositionSeed("KOORDINATOR_DIVISI", "Koordinator Divisi",
                                                "Membantu koordinasi teknis pekerjaan divisi.", 8),

                                new PositionSeed("KETUA_ASRAMA", "Ketua Asrama",
                                                "Mengelola koordinasi keasramaan dan administrasi izin asrama.", 9),

                                new PositionSeed("COACH_INSTRUKTUR", "Coach Instruktur",
                                                "Membina instruktur agar mengajar sesuai kurikulum dan standar pelatihan.",
                                                10),

                                new PositionSeed("INSTRUKTUR", "Instruktur",
                                                "Mengajar dan membimbing peserta pelatihan sesuai materi yang ditetapkan.",
                                                11),

                                new PositionSeed("ANGGOTA", "Anggota",
                                                "Mahasiswa PUB aktif yang mengikuti kegiatan dan kewajiban organisasi.",
                                                99));

                for (PositionSeed seed : positions) {
                        positionRepository.findByCode(seed.code())
                                        .orElseGet(() -> positionRepository.save(
                                                        Position.builder()
                                                                        .code(seed.code())
                                                                        .name(seed.name())
                                                                        .description(seed.description())
                                                                        .levelOrder(seed.levelOrder())
                                                                        .publicVisible(true)
                                                                        .active(true)
                                                                        .build()));
                }
        }

        private void seedBootstrapMemberAssignment() {
                if (!bootstrapMemberEnabled) {
                        return;
                }

                if (bootstrapAuthUserId == null
                                || bootstrapAuthUserId <= 0
                                || bootstrapFullName == null
                                || bootstrapFullName.isBlank()
                                || bootstrapEmail == null
                                || bootstrapEmail.isBlank()) {
                        throw new IllegalStateException(
                                        "Konfigurasi bootstrap member belum lengkap");
                }

                String normalizedEmail = bootstrapEmail.trim().toLowerCase(Locale.ROOT);

                String normalizedDivisionCode = bootstrapDivisionCode
                                .trim()
                                .toUpperCase(Locale.ROOT);

                String normalizedPositionCode = bootstrapPositionCode
                                .trim()
                                .toUpperCase(Locale.ROOT);

                Member memberByAuthUser = memberRepository
                                .findByAuthUserId(bootstrapAuthUserId)
                                .orElse(null);

                Member memberByEmail = memberRepository
                                .findByEmailIgnoreCase(normalizedEmail)
                                .orElse(null);

                if (memberByAuthUser != null
                                && memberByEmail != null
                                && !Objects.equals(
                                                memberByAuthUser.getId(),
                                                memberByEmail.getId())) {
                        throw new IllegalStateException(
                                        "Auth user dan email terhubung ke dua member berbeda");
                }

                Member member = memberByAuthUser != null
                                ? memberByAuthUser
                                : memberByEmail;

                if (member == null) {
                        member = Member.builder()
                                        .authUserId(bootstrapAuthUserId)
                                        .fullName(bootstrapFullName.trim())
                                        .email(normalizedEmail)
                                        .status(MemberStatus.ACTIVE)
                                        .publicVisible(true)
                                        .displayOrder(1)
                                        .active(true)
                                        .build();

                } else {
                        if (member.getAuthUserId() != null
                                        && !member.getAuthUserId()
                                                        .equals(bootstrapAuthUserId)) {
                                throw new IllegalStateException(
                                                "Member sudah terhubung dengan auth user lain");
                        }

                        member.setAuthUserId(bootstrapAuthUserId);
                        member.setFullName(bootstrapFullName.trim());
                        member.setEmail(normalizedEmail);
                        member.setStatus(MemberStatus.ACTIVE);
                        member.setPublicVisible(true);
                        member.setActive(true);
                }

                Member savedMember = memberRepository.save(member);

                OrganizationPeriod currentPeriod = periodRepository.findByCurrentPeriodTrue()
                                .filter(period -> Boolean.TRUE.equals(
                                                period.getActive()))
                                .orElseThrow(() -> new IllegalStateException(
                                                "Periode organisasi aktif tidak ditemukan"));

                Division division = divisionRepository
                                .findByCode(normalizedDivisionCode)
                                .filter(item -> Boolean.TRUE.equals(item.getActive()))
                                .orElseThrow(() -> new IllegalStateException(
                                                "Divisi tidak ditemukan: "
                                                                + normalizedDivisionCode));

                Position position = positionRepository
                                .findByCode(normalizedPositionCode)
                                .filter(item -> Boolean.TRUE.equals(item.getActive()))
                                .orElseThrow(() -> new IllegalStateException(
                                                "Jabatan tidak ditemukan: "
                                                                + normalizedPositionCode));

                boolean assignmentExists = memberAssignmentRepository
                                .existsByMemberAndPeriodAndDivisionAndPositionAndActiveTrue(
                                                savedMember,
                                                currentPeriod,
                                                division,
                                                position);

                if (assignmentExists) {
                        return;
                }

                MemberAssignment assignment = MemberAssignment.builder()
                                .member(savedMember)
                                .period(currentPeriod)
                                .division(division)
                                .position(position)
                                .status(AssignmentStatus.ACTIVE)
                                .active(true)
                                .build();

                memberAssignmentRepository.save(assignment);
        }

        private record DivisionSeed(
                        String code,
                        String name,
                        String description,
                        Integer displayOrder) {
        }

        private record PositionSeed(
                        String code,
                        String name,
                        String description,
                        Integer levelOrder) {
        }
}