# 08. Development Roadmap

## Update Progress per 9 Juni 2026

Status implementasi:

- auth-service core selesai:
  - Register.
  - Login.
  - JWT generation.
  - JWT validation.
  - Current user endpoint.
  - Role and permission seeder.
  - Auth admin endpoints.
  - Custom exception handling.
  - Custom security error response.

- organization-service core selesai:
  - JWT security stateless berbasis token dari auth-service.
  - Division management.
  - Position management.
  - Organization period management.
  - Member management.
  - Member assignment management.
  - Public organization API.
  - Division task management.
  - Division task evidence management.
  - Organization master data seeder.

Fokus berikutnya:

- request-service:
  - Pengajuan dana operasional.
  - Request item.
  - Approval flow.
  - PostgreSQL trigger untuk total amount.
  - Leave permit request untuk izin keluar/asrama.

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

## Prioritas MVP Terbaru

1. Auth-service dan role/permission dasar.
2. Organization-service untuk struktur organisasi, anggota, divisi, jabatan, task, dan public API.
3. Request-service untuk pengajuan dana dan izin keluar.
4. Approval flow Ketua Divisi/Ketua PUB/Pembina untuk pengajuan dana.
5. Approval flow Keasramaan/Keamanan/Ketua PUB/Pembina untuk izin keluar.
6. Finance-service untuk pencairan, settlement, dana kurang, dan pengembalian dana lebih.
7. Notification-report-service untuk email, scheduler, PDF, Excel, dan laporan.
8. API Gateway dengan routing, path rewrite, CORS, dan rate limiting.
9. Frontend React untuk demo end-to-end.
10. Dokumentasi final dan Postman collection.
