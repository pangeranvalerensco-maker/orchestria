# 13. 48-Hour Execution Plan

Dokumen ini adalah rencana eksekusi darurat untuk menyelesaikan Orchestria dalam dua hari tanpa menghapus full scope dan tanpa meninggalkan materi Java Lanjutan yang sudah diajarkan.

## Prinsip Eksekusi

1. Semua domain tetap masuk scope.
2. Kurangi kedalaman, bukan menghapus domain.
3. Core flow harus lengkap melalui browser.
4. Materi Java Lanjutan yang belum ada harus mendapat implementasi nyata.
5. Hindari refactor besar yang tidak blocking.
6. Gunakan pattern reusable untuk CRUD, table, form, permission, dan response.
7. Setelah setiap slice, build dan commit.
8. GitHub branch `main` dan dokumen `10`–`13` menjadi sumber kebenaran.

## Target Akhir 48 Jam

### Core Flow

```text
Login
→ Create Request
→ Add Items
→ Submit
→ Division Approval
→ PUB Approval
→ Pembina Approval
→ Finance Disbursement
→ Confirm Fund Received
→ Submit Settlement
→ Approve Settlement
→ Completed
```

### Course Coverage

- email;
- scheduler fixed rate;
- scheduler fixed delay;
- scheduler cron;
- event publisher/listener;
- stateful session demo;
- Excel import;
- Excel export;
- Excel template;
- JWT stateless;
- role/permission;
- Bean/IoC;
- exception handling;
- inter-service REST;
- API Gateway;
- frontend integration.

### Full Scope Domain

- Auth;
- Organization;
- Request/Approval;
- Finance/Settlement;
- Notification/Scheduler;
- Reporting/Export;
- Archive;
- Asset;
- Cleanliness/Picket;
- Division Activity;
- English Activity;
- HUMAS/Public.

## Prioritas

### P0 — Wajib untuk kelulusan demo

- seluruh service build;
- frontend build;
- core flow browser sampai completed;
- JWT, permission, 401, 403;
- email;
- scheduler;
- listener;
- Excel import/export/template;
- minimal satu flow tiap domain;
- dokumentasi run dan demo.

### P1 — Sangat penting

- reporting summary;
- notification log;
- scheduler log;
- file upload/download Archive;
- frontend semua domain;
- stateful session demo;
- data seed dan akun demo.

### P2 — Enhancement setelah P0/P1

- Jasper PDF;
- UI polishing;
- pagination/filter lengkap semua module;
- Docker;
- automated integration test luas;
- message broker;
- production deployment.

P2 tidak boleh menghambat P0.

# Fase Eksekusi

## Fase 0 — Baseline dan Freeze Arsitektur

Durasi: 0–2 jam.

### Tugas

- pull branch `main` terbaru;
- catat commit awal;
- jalankan build frontend;
- jalankan build seluruh backend service;
- cek database local;
- cek route gateway;
- cek endpoint core yang sudah ada;
- buat daftar error blocking;
- larang perubahan arsitektur besar setelah fase ini.

### Command Minimum

```bat
cd /d E:\orchestria\frontend
npm install
npm run build
```

Untuk setiap service:

```bat
cd /d E:\orchestria\backend\<service-name>
mvn clean package -DskipTests
```

### Gate

Tidak lanjut sebelum:

- error compile diketahui;
- port conflict diketahui;
- environment variable yang dibutuhkan dicatat;
- database yang belum ada dicatat.

## Fase 1 — Selesaikan Core Browser Flow

Durasi: jam 2–10.

### Backend yang Diverifikasi

- finance create disbursement;
- request status synchronization;
- confirm fund received;
- submit settlement;
- approve/reject settlement;
- completed transition;
- permission setiap tahap.

### Frontend yang Dibuat

- `/finance/disbursements`;
- `/finance/disbursements/new` atau action modal;
- daftar request siap dicairkan;
- action mark disbursed;
- action confirm fund received;
- settlement form;
- settlement verification page;
- status/timeline di Request Detail;
- sidebar menu berdasarkan permission.

### Acceptance

Satu request baru dapat berjalan dari draft sampai completed tanpa Postman untuk action bisnis utama.

### Commit Strategy

```text
feat: add finance disbursement frontend
feat: add fund receipt confirmation flow
feat: add settlement submission frontend
feat: add settlement verification frontend
```

## Fase 2 — Notification Report Service

Durasi: jam 10–18.

### Struktur Minimum

```text
backend/notification-report-service
├── config
├── controller
├── entity
├── exception
├── payload
├── repository
├── security
├── service
├── scheduler
├── event
└── report
```

### Database

```text
orchestria_notification_report_db
```

### Entity Minimum

- `NotificationLog`;
- `ScheduledJobLog`;
- `ReportExportLog` jika sempat.

### Endpoint Minimum

```text
POST /api/notifications/email
GET  /api/notifications/logs
POST /api/reports/excel/import
GET  /api/reports/excel/export
GET  /api/reports/excel/template
GET  /api/reports/summary
```

### Email

- Spring Mail dependency;
- SMTP config via environment;
- HTML body;
- To/Cc/Bcc DTO;
- persist SENT/FAILED;
- failure tidak menghentikan service.

### Scheduler

- `@EnableScheduling`;
- fixed rate heartbeat/report snapshot;
- fixed delay retry notification;
- cron reminder;
- configurable property;
- execution log.

### Event

- publish event dari satu proses request;
- listener membuat notification log;
- listener memanggil email service secara terkontrol.

### Excel

- Apache POI;
- upload multipart;
- import rows;
- export report;
- download template;
- return imported/failed summary.

### Gateway

Tambahkan route:

```text
/api/notifications/**
/api/reports/**
```

### Acceptance

- service build dan run;
- email endpoint menghasilkan log;
- scheduler terlihat pada log/database;
- event listener menghasilkan notification;
- Excel import/export/template dapat diuji.

## Fase 3 — Full Scope Operations Backend

Durasi: jam 18–30.

Agar cepat, domain berikut dibuat sebagai module terpisah di `organization-service` dengan package boundary jelas.

## 3A. Archive

### Minimum

- metadata dokumen;
- upload local storage;
- list;
- download;
- soft delete;
- permission.

### Endpoint Contoh

```text
POST   /api/organization/archives
GET    /api/organization/archives
GET    /api/organization/archives/{id}/download
DELETE /api/organization/archives/{id}
```

## 3B. Asset

### Minimum

- CRUD asset;
- loan;
- return;
- status transition;
- history.

### Endpoint Contoh

```text
POST /api/organization/assets
GET  /api/organization/assets
POST /api/organization/assets/{id}/borrow
POST /api/organization/assets/{id}/return
GET  /api/organization/assets/{id}/history
```

## 3C. Cleanliness/Picket

### Minimum

- create schedule;
- attendance;
- point/violation;
- recap.

### Endpoint Contoh

```text
POST /api/organization/pickets
GET  /api/organization/pickets
POST /api/organization/pickets/{id}/attendance
POST /api/organization/cleanliness/violations
GET  /api/organization/cleanliness/recap
```

## 3D. English Activity

### Minimum

- create submission schedule/activity;
- record member result;
- progress summary.

### Endpoint Contoh

```text
POST /api/organization/english-activities
GET  /api/organization/english-activities
POST /api/organization/english-activities/{id}/results
GET  /api/organization/english-progress/{memberId}
```

## 3E. Division Activity

Fondasi task/evidence sudah ada. Lengkapi:

- frontend-ready response;
- status task;
- evidence verification;
- filter current division.

## 3F. HUMAS/Public

Lengkapi endpoint publik:

```text
GET /api/organization/public/profile
GET /api/organization/public/structure
GET /api/organization/public/programs
```

### Gate Fase 3

Setiap domain:

- punya entity/repository/service/controller;
- punya permission;
- punya satu happy path;
- punya satu error path;
- build organization-service berhasil.

## Fase 4 — Full Scope Frontend

Durasi: jam 30–38.

Gunakan komponen reusable. Jangan membuat design system baru.

### Route Minimum

```text
/organization
/division-tasks
/archives
/assets
/pickets
/english-activities
/notifications
/reports
/public
```

### Komponen Reusable

- `PageHeading`;
- `DataTable`;
- `StatusBadge`;
- `FormField`;
- `ConfirmAction`;
- `LoadingState`;
- `EmptyState`;
- `ErrorAlert`.

### Acceptance

Setiap domain mempunyai halaman yang benar-benar memanggil API dan melakukan minimal satu action. Tidak boleh hanya kartu placeholder.

## Fase 5 — Stateful Demo dan Course Gap Final

Durasi: jam 38–41.

### Endpoint Terisolasi

```text
POST /api/auth/session/login
GET  /api/auth/session/profile
POST /api/auth/session/logout
```

### Aturan

- jangan mengganti JWT utama;
- gunakan session hanya untuk demo materi;
- session timeout configurable;
- dokumentasikan perbedaan stateful dan stateless.

### Course Review

Periksa ulang `docs/11-course-coverage-matrix.md` dan ubah status berdasarkan bukti aktual.

## Fase 6 — Testing dan Stabilization

Durasi: jam 41–46.

### Build

- semua Maven service;
- frontend production build.

### Core Role Test

- anggota;
- Ketua Divisi;
- Ketua PUB;
- Pembina;
- Bendahara;
- Super Admin;
- pengelola module operasional.

### Security Test

- no token 401;
- invalid token 401;
- insufficient permission 403;
- ownership;
- division restriction;
- public endpoint safe fields;
- file upload validation.

### Feature Test

- email success/failure log;
- fixed rate log;
- fixed delay retry;
- cron execution;
- event listener;
- Excel import;
- Excel export;
- Excel template;
- Archive upload/download;
- Asset borrow/return;
- Picket attendance/recap;
- English progress;
- public page.

### Bug Rule

Perbaiki hanya:

- compile error;
- runtime crash;
- broken flow;
- incorrect authorization;
- data corruption;
- demo-blocking UI.

Tunda kosmetik minor.

## Fase 7 — Documentation dan Demo Package

Durasi: jam 46–48.

### Update Dokumen

- README;
- development roadmap;
- API endpoint draft;
- role and permission;
- database design;
- testing flow;
- current handoff;
- course coverage matrix;
- acceptance criteria.

### Siapkan

- akun demo per role;
- seed data;
- sample Excel;
- sample file Archive;
- sample asset;
- sample schedule;
- urutan demo;
- screenshot cadangan;
- command run semua service;
- daftar environment variable.

### Demo Sequence

```text
1. Jelaskan arsitektur microservices
2. Login dan JWT
3. Tunjukkan role/permission
4. Jalankan core request sampai completed
5. Tunjukkan email/event/listener
6. Tunjukkan scheduler dan log
7. Import/export Excel
8. Archive upload/download
9. Asset borrow/return
10. Picket recap
11. English progress
12. Division task/evidence
13. Public/HUMAS page
14. Tunjukkan 401 dan 403
```

# Aturan Saat Tertinggal Jadwal

Jika satu fase terlambat:

- jangan menghapus domain;
- sederhanakan form;
- gunakan satu tabel utama per domain;
- gunakan satu happy path;
- batasi filter/pagination;
- gunakan local file storage;
- gunakan UI table/form yang sama;
- dokumentasikan batasan.

Yang tidak boleh dikorbankan:

- persistence;
- endpoint nyata;
- permission;
- error handling dasar;
- frontend action nyata;
- bukti demo.

# Commit Checklist

Sebelum commit:

```text
[ ] git status diperiksa
[ ] hanya file terkait task yang masuk
[ ] build module terkait berhasil
[ ] secret tidak ikut
[ ] endpoint diuji minimal happy path
[ ] commit message spesifik
```

Contoh:

```text
feat: add notification email delivery and logs
feat: add scheduled notification retry
feat: add Excel import export and template
feat: add asset borrowing module
feat: add cleanliness attendance module
feat: add English activity tracking
feat: add archive upload and download
feat: complete finance settlement frontend
```

# Final Stop Condition

Pekerjaan baru dihentikan ketika:

- core flow browser completed;
- seluruh materi course matrix memiliki bukti;
- seluruh full scope domain memiliki flow nyata;
- build seluruh project berhasil;
- demo script dapat dijalankan;
- dokumentasi sesuai keadaan terakhir.
