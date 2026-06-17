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

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class OrganizationDataSeeder implements CommandLineRunner {

    private final OrganizationPeriodRepository periodRepository;
    private final DivisionRepository divisionRepository;
    private final PositionRepository positionRepository;

    @Override
    public void run(String... args) {
        seedPeriods();
        seedDivisions();
        seedPositions();
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
                                .build()
                ));
    }

    private void seedDivisions() {
        List<DivisionSeed> divisions = List.of(
                new DivisionSeed("DIVDIK", "Divisi Pendidikan dan Pelatihan",
                        "Mengelola pelatihan, kurikulum, instruktur, dan pembelajaran PUB.", 1),

                new DivisionSeed("HUMAS", "Hubungan Masyarakat",
                        "Mengelola publikasi, dokumentasi, media sosial, web, dan citra PUB.", 2),

                new DivisionSeed("KESEJAHTERAAN", "Divisi Kesejahteraan",
                        "Mengelola kebutuhan konsumsi, menu, dan kesejahteraan mahasiswa PUB.", 3),

                new DivisionSeed("KEBERSIHAN", "Divisi Kebersihan",
                        "Mengelola jadwal piket, area kebersihan, dan kedisiplinan kebersihan lingkungan.", 4),

                new DivisionSeed("BAHASA_INGGRIS", "Divisi Bahasa Inggris",
                        "Mengelola setoran vocabulary, latihan bahasa Inggris, dan evaluasi pembelajaran bahasa.", 5),

                new DivisionSeed("KEROHANIAN", "Divisi Kerohanian",
                        "Mengelola pengajian, setoran bacaan Al-Qur'an, dan pembinaan ibadah.", 6),

                new DivisionSeed("KEASRAMAAN", "Divisi Keasramaan",
                        "Mengelola kedisiplinan asrama, keberadaan mahasiswa, dan administrasi izin asrama.", 7),

                new DivisionSeed("KESEHATAN", "Divisi Kesehatan",
                        "Mengelola perhatian kesehatan, pendataan sakit, dan kebutuhan kesehatan mahasiswa PUB.", 8),

                new DivisionSeed("PPMB", "Divisi PPMB",
                        "Mengelola proses sosialisasi, seleksi, survei, dan penerimaan mahasiswa baru PUB.", 9),

                new DivisionSeed("ASET", "Divisi Aset",
                        "Mengelola pendataan aset, peminjaman laptop, dan pemeriksaan kondisi barang.", 10)
        );

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
                                    .build()
                    ));
        }
    }

    private void seedPositions() {
        List<PositionSeed> positions = List.of(
                new PositionSeed("PEMBINA", "Pembina",
                        "Pembina utama organisasi dan pengambil persetujuan akhir.", 1),

                new PositionSeed("KETUA_PUB", "Ketua PUB",
                        "Pemimpin utama mahasiswa PUB dan penanggung jawab koordinasi organisasi.", 2),

                new PositionSeed("KEAMANAN", "Keamanan",
                        "Jabatan struktural yang bertanggung jawab atas penertiban, kedisiplinan, dan izin keluar mahasiswa PUB.", 3),

                new PositionSeed("SEKRETARIS", "Sekretaris",
                        "Mengelola administrasi, arsip, surat, dan dokumentasi organisasi.", 4),

                new PositionSeed("BENDAHARA_INTERNAL", "Bendahara Internal",
                        "Mengelola pengajuan, pencairan internal, bukti pembayaran, dan settlement.", 5),

                new PositionSeed("BENDAHARA_EKSTERNAL", "Bendahara Eksternal",
                        "Mencatat laporan keuangan keseluruhan dan penarikan dana dari pihak pembina/yayasan.", 6),

                new PositionSeed("KETUA_DIVISI", "Ketua Divisi",
                        "Memimpin dan bertanggung jawab atas operasional divisi.", 7),

                new PositionSeed("KOORDINATOR_DIVISI", "Koordinator Divisi",
                        "Membantu koordinasi teknis pekerjaan divisi.", 8),

                new PositionSeed("KETUA_ASRAMA", "Ketua Asrama",
                        "Mengelola koordinasi keasramaan dan administrasi izin asrama.", 9),

                new PositionSeed("COACH_INSTRUKTUR", "Coach Instruktur",
                        "Membina instruktur agar mengajar sesuai kurikulum dan standar pelatihan.", 10),

                new PositionSeed("INSTRUKTUR", "Instruktur",
                        "Mengajar dan membimbing peserta pelatihan sesuai materi yang ditetapkan.", 11),

                new PositionSeed("ANGGOTA", "Anggota",
                        "Mahasiswa PUB aktif yang mengikuti kegiatan dan kewajiban organisasi.", 99)
        );

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
                                    .build()
                    ));
        }
    }

    private record DivisionSeed(
            String code,
            String name,
            String description,
            Integer displayOrder
    ) {
    }

    private record PositionSeed(
            String code,
            String name,
            String description,
            Integer levelOrder
    ) {
    }
}