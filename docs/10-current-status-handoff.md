# 10. Current Status Handoff

Dokumen ini adalah sumber konteks utama untuk melanjutkan pengembangan Orchestria di percakapan, perangkat, atau sesi kerja baru.

Terakhir diperbarui: 19 Juni 2026.

## Aturan Utama

Orchestria **bukan hanya aplikasi pengajuan dana**.

Pengajuan dana adalah vertical slice pertama yang membuktikan komunikasi antarmicroservice, autentikasi, otorisasi, approval bertingkat, pencairan, settlement, dan histori proses.

Target akhir project memiliki dua kewajiban yang berjalan bersamaan:

1. Seluruh materi Java Lanjutan yang sudah diajarkan memiliki implementasi nyata dan dapat didemokan.
2. Seluruh domain full scope Orchestria memiliki minimal satu alur fungsional, walaupun kedalaman implementasi tiap domain disesuaikan dengan tenggat.

Tidak boleh menghapus domain dari scope hanya untuk mempercepat penyelesaian. Jika waktu terbatas, kurangi kedalaman fitur, bukan menghilangkan domain.

## Sumber Kebenaran

Urutan sumber kebenaran:

1. Branch `main` repository GitHub.
2. Dokumen dalam folder `docs`.
3. Kontrak API dan source code tiap service.
4. Catatan percakapan hanya sebagai konteks tambahan.

Sebelum memberi instruksi perubahan kode, selalu periksa versi terbaru repository.

Repository:

```text
https://github.com/pangeranvalerensco-maker/orchestria
```

Commit checkpoint saat dokumen ini dibuat:

```text
89795da feat: add approval fundrequest flow
```

## Identitas Project

Nama:

```text
Orchestria
```

Deskripsi:

```text
Sistem manajemen alur operasional organisasi berbasis microservices dengan studi kasus PUB Universitas Nasional PASIM.
```

Tujuan:

- memenuhi UAS Java Lanjutan;
- membuktikan penerapan materi pelatihan secara nyata;
- menghasilkan backend dan frontend yang dapat didemokan end-to-end;
- menjadi project portofolio backend/full-stack;
- memodelkan proses organisasi PUB yang benar-benar terjadi.

## Teknologi Utama

### Backend

- Java 21
- Spring Boot 4.x
- Spring Security
- Spring Data JPA
- Spring Cloud Gateway
- JWT menggunakan JJWT
- PostgreSQL
- Maven
- Lombok

### Frontend

- React
- TypeScript
- Vite
- React Router
- Fetch API

### Tools

- VS Code
- DBeaver
- Postman
- Git dan GitHub

## Arsitektur Service Saat Ini

| Service | Port | Database | Status |
| --- | ---: | --- | --- |
| `api-gateway` | 8000 | - | Implemented |
| `auth-service` | 8001 | `orchestria_auth_db` | Implemented |
| `organization-service` | 8002 | `orchestria_organization_db` | Implemented, perlu perluasan domain operasional |
| `request-service` | 8003 | `orchestria_request_db` | Implemented MVP |
| `finance-service` | 8004 | `orchestria_finance_db` | Implemented MVP |
| `notification-report-service` | direncanakan 8005 | `orchestria_notification_report_db` | Belum diimplementasikan |

Prinsip database:

- satu database per service;
- tidak ada foreign key lintas database;
- service lain menyimpan ID referensi;
- komunikasi antarmicroservice menggunakan REST API pada tahap saat ini.

## Status Backend Saat Ini

### Auth Service

Sudah tersedia:

- register dan login;
- JWT generation dan validation;
- endpoint current user;
- user, role, dan permission;
- password hashing;
- Spring Security;
- method-level authorization;
- custom security response;
- global exception handling.

### Organization Service

Sudah tersedia:

- periode kepengurusan;
- divisi;
- position organisasi;
- member;
- member assignment;
- tugas divisi;
- bukti tugas divisi;
- current member context;
- public organization API;
- validasi JWT dan permission dasar.

### Request Service

Sudah tersedia:

- create fund request;
- add request item;
- kalkulasi total;
- submit request;
- my requests;
- detail request milik user;
- approval Ketua Divisi;
- approval Ketua PUB;
- approval Pembina;
- reject dan request revision;
- pending approval queue;
- approval timeline;
- status history;
- confirm fund received;
- settlement backend;
- validasi ownership dan division authorization.

### Finance Service

Sudah tersedia:

- create fund disbursement;
- daftar dan detail disbursement;
- pencarian berdasarkan fund request ID;
- validasi request ke request-service;
- sinkronisasi status request;
- permission finance;
- validasi JWT.

### API Gateway

Sudah tersedia:

- route `/api/auth/**`;
- route `/api/organization/**`;
- route `/api/requests/**`;
- route `/api/finance/**`;
- CORS untuk frontend local;
- request logging.

Belum tersedia:

- route notification/report;
- monitoring Actuator yang lengkap;
- centralized authentication enforcement. Validasi JWT masih dilakukan masing-masing service.

## Status Frontend Saat Ini

Route yang sudah tersedia:

```text
/login
/dashboard
/requests
/requests/new
/requests/:id
/approvals
```

Fitur yang sudah tersedia:

- login melalui API Gateway;
- penyimpanan JWT di local storage;
- restore session melalui `/api/auth/me`;
- protected route;
- role dan permission helper;
- dashboard user;
- My Requests;
- Create Request;
- Request Detail;
- add item anggaran;
- submit pengajuan;
- pending approval queue;
- approve, reject, dan request revision;
- sidebar berbasis permission.

Fitur frontend yang belum tersedia:

- finance disbursement;
- confirm fund received;
- submit settlement;
- approve settlement;
- all requests untuk role tertentu;
- notification center;
- reporting dashboard;
- Excel import/export UI;
- Archive;
- Asset;
- Cleanliness/Picket;
- English Activity;
- public/HUMAS view yang lengkap.

## Alur Core yang Wajib Berjalan

```text
Login
→ Create Fund Request
→ Add Request Items
→ Submit Request
→ Division Approval
→ PUB Approval
→ Pembina Approval
→ Finance Disbursement
→ Mark Request as Disbursed
→ Confirm Fund Received
→ Submit Settlement
→ Approve Settlement
→ Completed
```

Alur ini wajib dapat dilakukan melalui browser, bukan hanya Postman.

## Full Scope Domain yang Tidak Boleh Dihapus

1. Auth dan Access Control
2. Organization Management
3. Fund Request dan Approval
4. Finance dan Settlement
5. Notification dan Scheduler
6. Reporting dan Export
7. Archive dan Document
8. Asset Management
9. Cleanliness/Picket
10. Division Activity
11. English Activity
12. HUMAS/Public Web

Minimal satu alur fungsional harus tersedia untuk setiap domain.

## Materi Java Lanjutan yang Masih Menjadi Gap

- pengiriman email melalui Java/Spring Mail;
- scheduler fixed rate;
- scheduler fixed delay;
- scheduler cron;
- application event dan event listener;
- authentication stateful berbasis session sebagai demonstrasi terisolasi;
- Excel upload/import menggunakan Apache POI;
- Excel download/export;
- download template Excel;
- reporting dashboard;
- PDF report jika waktu memungkinkan, karena disebut pada overview materi tetapi belum ditemukan sebagai pertemuan khusus;
- notification log dan scheduler execution log.

Matriks lengkap tersedia pada `docs/11-course-coverage-matrix.md`.

## Strategi Implementasi Cepat

### Jangan Membuat Service Berlebihan

Service utama tetap dipertahankan. Untuk mengejar full scope:

- `notification-report-service` wajib dibuat karena sekaligus membuktikan email, scheduler, listener, Excel, dan reporting;
- Archive, Asset, Cleanliness, dan English Activity dapat dibuat sebagai module terpisah di `organization-service` selama boundary package, entity, service, repository, controller, permission, dan tabelnya jelas;
- jika kompleksitas mulai tinggi, modul tersebut dapat dipindahkan ke `operations-service` setelah UAS, tetapi jangan melakukan pemindahan besar menjelang deadline.

### Jangan Melakukan Refactor Kosmetik Besar

Prioritaskan:

1. build berhasil;
2. alur fungsional;
3. permission dan ownership;
4. persistence;
5. frontend demo;
6. dokumentasi;
7. baru kemudian kosmetik.

## Definition of Done Global

Project dianggap siap demo apabila:

- semua service dapat di-build;
- frontend dapat di-build;
- semua service dapat dijalankan lokal;
- gateway menjadi satu pintu frontend;
- core flow selesai sampai `COMPLETED`;
- semua domain full scope mempunyai minimal satu alur fungsional;
- semua materi wajib pada course coverage matrix berstatus `IMPLEMENTED` atau `DEMO READY`;
- role dan permission dapat dibuktikan;
- skenario 401, 403, ownership, dan division restriction dapat dibuktikan;
- seed data dan akun demo tersedia;
- README, roadmap, testing flow, dan handoff sesuai source code terbaru.

## Instruksi untuk AI atau Sesi Baru

Sebelum menulis kode:

1. periksa commit terbaru;
2. baca dokumen `10` sampai `13`;
3. baca kontrak endpoint yang akan digunakan;
4. identifikasi file yang benar-benar perlu diubah;
5. jangan membuat ulang struktur yang sudah ada;
6. jangan menyederhanakan project hanya menjadi pengajuan dana;
7. kerjakan berdasarkan urutan prioritas dalam rencana 48 jam;
8. gunakan commit kecil dan spesifik;
9. setelah setiap slice, jalankan build dan test;
10. perbarui status dokumentasi setelah implementasi berubah.

## Pekerjaan Berikutnya

Urutan terdekat:

1. implementasi frontend Finance dan Settlement hingga core flow selesai;
2. scaffold `notification-report-service`;
3. implementasi email, scheduler, event listener, Excel, dan reporting;
4. implementasi module Archive, Asset, Cleanliness, dan English Activity;
5. tambah frontend minimal untuk seluruh domain;
6. buat demo stateful terisolasi di auth-service;
7. end-to-end test;
8. update dokumentasi dan materi presentasi.
