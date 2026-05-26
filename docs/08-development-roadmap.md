# 08. Development Roadmap

## Fase 0: Dokumentasi Konsep

Target:

- Menyusun overview project.
- Mendefinisikan business flow.
- Menentukan draft microservices.
- Menyusun draft role, permission, API, database, security, dan roadmap.

Status: berjalan.

## Fase 1: Validasi Kebutuhan

Target:

- Memvalidasi alur operasional PUB.
- Menentukan role final: SUPER_ADMIN, PEMBINA, KETUA_PUB, KETUA_DIVISI, SEKRETARIS, BENDAHARA_INTERNAL, BENDAHARA_EKSTERNAL, dan ANGGOTA.
- Menentukan status pengajuan final.
- Menentukan aturan approval Ketua Divisi, Ketua PUB, dan Pembina.
- Menentukan aturan pencairan, penerimaan dana, upload struk, settlement, dana kurang, dan pengembalian dana lebih.
- Menentukan dokumen wajib di setiap tahap.

Output:

- Business requirement yang lebih stabil.
- Workflow final versi awal.

## Fase 2: Desain Teknis Awal

Target:

- Menentukan service boundary final versi awal: api-gateway, auth-service, organization-service, request-service, finance-service, dan notification-report-service.
- Menentukan strategi komunikasi antar service.
- Menentukan desain database per service.
- Menentukan standar API response, error, pagination, dan audit.
- Menentukan strategi autentikasi dan otorisasi.

Output:

- Technical design draft.
- API contract awal.

## Fase 3: Implementasi Fondasi

Target:

- Membuat struktur project backend setelah desain disetujui.
- Membuat api-gateway dasar.
- Membuat auth-service dasar.
- Membuat organization-service dasar.
- Membuat request-service dasar.

Catatan:

Fase ini belum dilakukan pada tahap dokumentasi awal.

## Fase 4: Implementasi Operasional Keuangan

Target:

- finance-service.
- Pencairan dana.
- Konfirmasi penerimaan dana.
- Upload struk dan settlement.
- Dana kurang.
- Pengembalian dana lebih.
- Integrasi status antar proses.

## Fase 5: Notification, Scheduler, dan Reporting

Target:

- notification-report-service untuk email, reminder, dan laporan.
- Laporan pengajuan, approval, pencairan, settlement, dana kurang, dan pengembalian dana lebih.

## Fase 6: Hardening

Target:

- Audit trail lengkap.
- Validasi security.
- Testing workflow end-to-end.
- Review performa.
- Dokumentasi operasional.

## Prioritas MVP

1. Auth dan role dasar
2. Organization data: anggota, divisi, jabatan
3. Pengajuan divisi
4. Approval Ketua Divisi, Ketua PUB, dan Pembina
5. Pencairan dana
6. Penerimaan dana oleh pengaju
7. Upload struk dan settlement
8. Dana kurang dan pengembalian dana lebih
9. Arsip dokumen sederhana
10. Email notification
11. Laporan dasar
12. API Gateway
