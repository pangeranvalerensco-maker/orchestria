# 08. Development Roadmap

Dokumen ini menjelaskan roadmap pengembangan Orchestria berdasarkan full scope project dan status implementasi saat ini.

## Prinsip Roadmap

Orchestria dikembangkan secara bertahap menggunakan pendekatan vertical slice.

Artinya, pengembangan tidak menunggu seluruh backend selesai sebelum frontend dimulai. Setiap slice dikembangkan sampai API stabil, diuji melalui Postman, lalu frontend dapat mengikuti slice tersebut.

Strategi utama:

```text
Backend slice stabil
→ Postman flow stabil
→ Frontend untuk slice tersebut
→ Backend module berikutnya
→ Frontend module berikutnya
```

Dengan strategi ini, project dapat tetap berkembang besar, tetapi tetap memiliki demo yang bisa berjalan pada setiap checkpoint.

## Full Scope Orchestria

Full scope Orchestria mencakup domain berikut:

| Domain                   | Status                |
| ------------------------ | --------------------- |
| Auth & Access Control    | Implemented           |
| Organization Management  | Implemented           |
| Fund Request Workflow    | Implemented MVP       |
| Finance Management       | Implemented MVP       |
| API Gateway              | Implemented           |
| Archive & Document       | Planned               |
| Asset Management         | Planned               |
| Cleanliness / Picket     | Planned               |
| Division Activity        | Partially implemented |
| English Activity         | Planned               |
| HUMAS / Public Web       | Partially implemented |
| Notification & Scheduler | Planned               |
| Reporting Dashboard      | Planned               |
| Frontend React           | Planned               |

## Current MVP Scope

Current MVP adalah vertical slice pertama:

```text
Auth
→ Organization
→ Fund Request
→ Approval Workflow
→ Finance Disbursement
→ Settlement
→ API Gateway
```

Flow utama:

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

Tujuan MVP ini adalah membuktikan:

* multi-service backend berjalan;
* JWT authentication berjalan;
* role dan permission berjalan;
* permission-based authorization berjalan;
* approval bertingkat berjalan;
* status transition berjalan;
* finance-service terpisah dari request-service;
* API Gateway dapat menjadi satu pintu akses;
* Postman demo flow dapat dilakukan end-to-end.

## Checkpoint 0 — Documentation & Architecture

Status:

```text
In Progress
```

Target:

* README full scope.
* Project overview.
* Business flow.
* Microservices architecture.
* Role and permission.
* API endpoints.
* Database design.
* Security design.
* Development roadmap.
* Postman testing flow.

Output:

```text
Dokumentasi project rapi dan membedakan:
1. Full Scope Orchestria.
2. Current MVP Implementation.
3. Planned Modules.
```

Catatan:

Dokumentasi tidak boleh membuat Orchestria terlihat hanya sebagai aplikasi pengajuan dana. Fund request hanyalah vertical slice pertama.

## Checkpoint 1 — Backend Foundation

Status:

```text
Mostly Implemented
```

Service utama:

| Service              | Status          |
| -------------------- | --------------- |
| auth-service         | Implemented     |
| organization-service | Implemented     |
| api-gateway          | Implemented     |
| request-service      | Implemented MVP |
| finance-service      | Implemented MVP |

Fitur foundation:

* Struktur multi-service.
* Database per service.
* JWT stateless.
* Role dan permission seeder.
* Spring Security per service.
* Standard API response.
* Custom error response.
* API Gateway routing.
* CORS di gateway.
* Request logging di gateway.

Target penyelesaian checkpoint:

* Semua service compile.
* Semua service bisa run local.
* API Gateway berhasil routing ke service internal.
* Token dari auth-service dapat digunakan di service lain.

## Checkpoint 2 — Auth Service

Status:

```text
Implemented
```

Fitur:

* Register user.
* Login user.
* JWT generation.
* Current user endpoint.
* Role management.
* Permission management.
* Role-permission seeder.
* Admin auth endpoints.
* Password hashing.
* Custom security error response.

Output:

```text
User dapat login dan mendapatkan JWT yang berisi roles dan permissions.
```

## Checkpoint 3 — Organization Service

Status:

```text
Implemented
```

Fitur:

* Division management.
* Position management.
* Organization period management.
* Member management.
* Member assignment management.
* Division task management.
* Division task evidence management.
* Public organization API.
* JWT validation.
* Permission protection dasar.

Output:

```text
Struktur organisasi PUB dapat dikelola dan sebagian data dapat disediakan untuk public web.
```

Catatan penting:

```text
Position organisasi berbeda dari role security.
```

Contoh:

```text
KETUA_PUB = role security.
Ketua PUB = position organisasi.
```

## Checkpoint 4 — Fund Request Vertical Slice

Status:

```text
Implemented MVP
```

Service utama:

```text
request-service
```

Fitur:

* Create fund request.
* Add request item.
* Total amount calculation.
* Submit request.
* Division approval.
* PUB approval.
* Pembina approval.
* Reject request.
* Request revision.
* Approval timeline.
* Status history.
* Mark request as disbursed.
* Confirm fund received.
* Submit settlement.
* Approve settlement.
* My requests endpoint.

Flow status utama:

```text
DRAFT
→ SUBMITTED
→ DIVISION_APPROVED
→ PUB_APPROVED
→ READY_FOR_DISBURSEMENT
→ DISBURSED
→ FUND_RECEIVED
→ SETTLEMENT_SUBMITTED
→ COMPLETED
```

Output:

```text
Pengajuan dana dapat diproses dari draft sampai completed.
```

## Checkpoint 5 — Finance Service MVP

Status:

```text
Implemented MVP
```

Service utama:

```text
finance-service
```

Fitur:

* Create fund disbursement.
* Get all fund disbursements.
* Get fund disbursement detail.
* Get disbursement by fund request ID.
* Finance permission protection.

Output:

```text
Pencairan dana dicatat pada finance-service sebagai service terpisah dari request-service.
```

Catatan MVP:

```text
Sinkronisasi finance-service dan request-service masih manual melalui API flow.
Belum menggunakan event/message broker.
```

## Checkpoint 6 — API Gateway

Status:

```text
Implemented
```

Service utama:

```text
api-gateway
```

Fitur:

* Routing ke auth-service.
* Routing ke organization-service.
* Routing ke request-service.
* Routing ke finance-service.
* CORS untuk frontend local development.
* Request logging.

Routing utama:

| Gateway Path           | Target Service       |
| ---------------------- | -------------------- |
| `/api/auth/**`         | auth-service         |
| `/api/organization/**` | organization-service |
| `/api/requests/**`     | request-service      |
| `/api/finance/**`      | finance-service      |

Output:

```text
Client cukup mengakses port 8000 sebagai satu pintu API.
```

## Checkpoint 7 — Security Hardening MVP

Status:

```text
In Progress
```

Target:

* Mengaktifkan method-level security.
* Menambahkan `@PreAuthorize`.
* Mengetes permission-based access.
* Memastikan 401 dan 403 berjalan.
* Memastikan user biasa tidak dapat mengakses endpoint admin/finance.
* Memastikan endpoint `my requests` hanya menampilkan data milik user login.

Permission utama:

| Permission                  | Fungsi                                               |
| --------------------------- | ---------------------------------------------------- |
| `request.create`            | Membuat pengajuan dan menjalankan aksi milik pengaju |
| `request.read.own`          | Melihat pengajuan milik sendiri                      |
| `request.read.all`          | Melihat semua pengajuan                              |
| `request.approve.division`  | Approval level Ketua Divisi                          |
| `request.approve.pub`       | Approval level Ketua PUB                             |
| `request.approve.pembina`   | Approval level Pembina                               |
| `finance.disburse`          | Melakukan pencairan dana                             |
| `finance.settlement.verify` | Memverifikasi settlement                             |
| `finance.report.read`       | Melihat data/laporan finance                         |

Test wajib:

| Skenario                                          | Expected         |
| ------------------------------------------------- | ---------------- |
| Tanpa token akses protected endpoint              | 401 Unauthorized |
| Token valid tetapi permission kurang              | 403 Forbidden    |
| Token valid dan permission cukup                  | 200/201          |
| Anggota akses `/api/requests/my`                  | 200              |
| Anggota akses `/api/requests`                     | 403              |
| Anggota akses `POST /api/finance/disbursements`   | 403              |
| Bendahara akses `POST /api/finance/disbursements` | 201              |

Output:

```text
Authorization tidak hanya berdasarkan login, tetapi juga berdasarkan permission.
```

## Checkpoint 8 — Postman Demo Flow

Status:

```text
Planned
```

Target:

* Membuat dokumentasi testing flow.
* Menyediakan request body untuk setiap endpoint.
* Menyediakan expected status.
* Menyediakan permission matrix.
* Menyediakan urutan demo end-to-end.

Flow Postman minimal:

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
→ Check Detail
→ Check History
→ Test 401/403
```

Output:

```text
Backend dapat didemokan tanpa frontend menggunakan Postman.
```

## Checkpoint 9 — Frontend MVP Slice 1

Status:

```text
Planned
```

Frontend tidak menunggu seluruh backend selesai. Frontend mulai setelah current MVP API stabil dan Postman flow sudah aman.

Frontend pertama fokus pada vertical slice pertama:

```text
Auth
Organization basic
Fund Request
Approval
Finance Disbursement
Settlement
```

Halaman minimal:

| Page                 | Fungsi                                             |
| -------------------- | -------------------------------------------------- |
| Login                | User login dan menyimpan token                     |
| Dashboard            | Ringkasan awal                                     |
| My Requests          | Melihat pengajuan milik user login                 |
| All Requests         | Melihat semua pengajuan untuk role tertentu        |
| Create Request       | Membuat pengajuan                                  |
| Request Detail       | Melihat detail, item, status, approval, settlement |
| Approval Action      | Approve, reject, request revision                  |
| Finance Disbursement | Mencatat pencairan dana                            |
| Settlement           | Submit dan approve settlement                      |

Strategi frontend:

```text
Frontend Slice 1 jalan
→ Backend lanjut module berikutnya
→ Frontend menambah halaman sesuai module yang sudah stabil
```

Output:

```text
Demo end-to-end dapat dilakukan melalui browser.
```

## Checkpoint 10 — Archive & Document Module

Status:

```text
Planned
```

Alasan prioritas:

Archive & Document cocok menjadi module lanjutan karena terhubung ke banyak proses:

* pengajuan dana;
* approval;
* bukti pencairan;
* struk settlement;
* surat organisasi;
* dokumen divisi;
* arsip sekretaris.

Rencana fitur:

* Upload metadata dokumen.
* Kategori dokumen.
* Arsip surat.
* Arsip bukti transaksi.
* Arsip dokumen pengajuan.
* Hak akses dokumen.
* Soft delete dokumen.
* Search dokumen.
* Download dokumen.

Kemungkinan service:

```text
archive-service
```

atau module awal di service yang sudah ada, tergantung kompleksitas.

Output:

```text
Orchestria mulai terlihat sebagai sistem administrasi organisasi, bukan hanya workflow keuangan.
```

## Checkpoint 11 — Asset Management Module

Status:

```text
Planned
```

Rencana fitur:

* Data aset/laptop.
* Status aset.
* Peminjaman aset.
* Pengembalian aset.
* Checker.
* Kondisi sebelum peminjaman.
* Kondisi setelah pengembalian.
* Histori peminjaman.
* Approval peminjaman jika dibutuhkan.

Kemungkinan flow:

```text
User mengajukan peminjaman aset
→ Checker mengecek ketersediaan
→ Aset dipinjamkan
→ Aset dikembalikan
→ Checker memverifikasi kondisi
→ Histori tersimpan
```

Output:

```text
Operasional asset PUB terdokumentasi.
```

## Checkpoint 12 — Cleanliness / Picket Module

Status:

```text
Planned
```

Rencana fitur:

* Jadwal piket.
* Absensi piket.
* Poin kebersihan.
* Pelanggaran.
* Rekap anggota.
* Rekap divisi.
* Laporan kebersihan.

Kemungkinan flow:

```text
Koordinator membuat jadwal
→ Anggota melaksanakan piket
→ Bukti atau status dicatat
→ Poin dihitung
→ Rekap dapat dilihat
```

Output:

```text
Aktivitas kebersihan dan poin menjadi terdokumentasi.
```

## Checkpoint 13 — Division Activity Enhancement

Status:

```text
Partially Implemented / Planned Enhancement
```

Saat ini organization-service sudah memiliki division task dan division task evidence.

Pengembangan berikutnya:

* Agenda divisi.
* Tugas rutin divisi.
* Progress tugas.
* Bukti pengerjaan.
* Rekap kegiatan.
* Approval atau validasi tugas.
* Dashboard tugas divisi.

Output:

```text
Aktivitas divisi dapat dipantau dan dibuktikan.
```

## Checkpoint 14 — English Activity Module

Status:

```text
Planned
```

Rencana fitur:

* Data setoran Bahasa Inggris.
* Jadwal setoran.
* Progress anggota.
* Rekap kehadiran atau capaian.
* Catatan pembinaan.
* Laporan kegiatan Bahasa Inggris.

Output:

```text
Kegiatan Bahasa Inggris PUB dapat direkap secara sistematis.
```

## Checkpoint 15 — Notification & Scheduler

Status:

```text
Planned
```

Rencana service:

```text
notification-report-service
```

Rencana fitur:

* Email notification.
* Reminder approval.
* Reminder settlement.
* Reminder tugas divisi.
* Reminder pengembalian aset.
* Template notifikasi.
* Log pengiriman.
* Retry mechanism.
* Scheduler berkala.

Trigger notifikasi:

* Pengajuan baru.
* Approval menunggu tindakan.
* Pengajuan ditolak.
* Pengajuan diminta revisi.
* Dana dicairkan.
* Dana diterima.
* Settlement dikirim.
* Settlement disetujui.
* Aset harus dikembalikan.
* Tugas mendekati deadline.

Output:

```text
User mendapat reminder otomatis dan proses tidak bergantung pada pengingat manual.
```

## Checkpoint 16 — Reporting Dashboard

Status:

```text
Planned
```

Rencana fitur:

* Laporan pengajuan.
* Laporan approval.
* Laporan pencairan dana.
* Laporan settlement.
* Laporan aset.
* Laporan kebersihan/piket.
* Laporan tugas divisi.
* Export PDF.
* Export Excel.
* Dashboard statistik.

Kemungkinan pendekatan:

```text
1. Query agregasi via API antar service.
2. Read model khusus laporan.
3. Event-driven projection pada tahap lanjutan.
```

Output:

```text
Pimpinan organisasi dapat melihat data operasional melalui dashboard.
```

## Checkpoint 17 — Deployment & Production Preparation

Status:

```text
Planned
```

Target:

* Environment config.
* Secret management.
* Docker.
* Docker Compose.
* Database initialization.
* Deployment backend.
* Deployment frontend.
* API documentation final.
* Postman collection export.
* Basic CI/CD.
* Logging improvement.
* Production CORS.
* Rate limiting.

Output:

```text
Project siap dipresentasikan sebagai portofolio yang lebih matang.
```

## Timeline Strategis

Timeline disusun berdasarkan urutan prioritas, bukan tanggal absolut.

### Tahap A — Stabilkan MVP Saat Ini

Target:

* Semua service compile.
* Semua service run local.
* Gateway routing aman.
* Permission matrix aman.
* Postman flow selesai.
* Dokumentasi full scope rapi.

Status:

```text
Sedang dikerjakan
```

### Tahap B — Frontend Slice 1

Target:

* Login.
* Dashboard.
* My Requests.
* All Requests.
* Create Request.
* Request Detail.
* Approval.
* Finance Disbursement.
* Settlement.

Status:

```text
Mulai setelah Tahap A aman
```

### Tahap C — Next Backend Module

Pilihan prioritas:

```text
Archive & Document
atau
Asset Management
```

Rekomendasi:

```text
Archive & Document lebih dahulu jika ingin memperkuat sisi administrasi organisasi.
Asset Management lebih dahulu jika ingin menunjukkan domain operasional yang berbeda dari finance.
```

### Tahap D — Expand Frontend

Target:

* Tambahkan halaman untuk module yang sudah stabil.
* Tambahkan role-based menu.
* Tambahkan permission-based button.
* Tambahkan dashboard awal.

### Tahap E — Notification, Reporting, Deployment

Target:

* Notification-report-service.
* Scheduler.
* Reporting.
* Export.
* Docker/deployment.

## Prioritas Terdekat

Urutan kerja terdekat:

```text
1. Selesaikan dokumentasi full scope.
2. Selesaikan security hardening MVP.
3. Selesaikan Postman testing flow.
4. Commit checkpoint backend MVP.
5. Mulai frontend MVP slice 1.
6. Tentukan next backend module: Archive atau Asset.
7. Lanjut backend module berikutnya secara vertical slice.
```

## Definisi Selesai untuk MVP Backend

MVP backend dianggap selesai jika:

* Semua service bisa compile.
* Semua service bisa run local.
* Login menghasilkan JWT valid.
* Token bisa dipakai di service lain.
* Gateway routing berjalan.
* Fund request flow bisa selesai dari DRAFT sampai COMPLETED.
* Finance disbursement bisa dicatat.
* Settlement bisa dikirim dan disetujui.
* Permission matrix 401/403 berjalan.
* Postman flow terdokumentasi.
* Dokumentasi README dan docs utama sudah rapi.
