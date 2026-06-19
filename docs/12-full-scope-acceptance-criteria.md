# 12. Full Scope Acceptance Criteria

Dokumen ini menetapkan batas minimum agar Orchestria dapat disebut benar-benar mengimplementasikan full scope, bukan hanya menampilkan placeholder atau rencana.

## Prinsip

- Semua domain wajib hadir.
- Kedalaman fitur boleh berbeda, tetapi setiap domain harus mempunyai minimal satu alur yang bekerja dari backend sampai penyimpanan data dan, untuk domain yang dipakai user, sampai frontend.
- Placeholder UI, endpoint kosong, atau dokumentasi tanpa implementasi tidak dihitung selesai.
- Core flow Request–Approval–Finance–Settlement harus lebih lengkap daripada module tambahan.
- Security, error handling, dan persistence tetap wajib.

## Status yang Digunakan

| Status | Arti |
| --- | --- |
| `DONE` | Acceptance criteria terpenuhi |
| `IN PROGRESS` | Implementasi ada tetapi belum lolos semua kriteria |
| `NOT STARTED` | Belum ada implementasi nyata |

## Acceptance Criteria Lintas Domain

Setiap domain dianggap selesai apabila:

1. mempunyai entity/model atau sumber data yang jelas;
2. mempunyai repository/persistence jika datanya perlu disimpan;
3. mempunyai service layer untuk business logic;
4. mempunyai controller/API;
5. menggunakan request/response DTO yang jelas;
6. mempunyai validation;
7. mempunyai error handling;
8. mempunyai permission atau public access rule yang jelas;
9. dapat diuji melalui Postman;
10. mempunyai frontend minimal bila fitur digunakan user internal;
11. dapat dibuktikan dengan satu happy path dan satu error path;
12. terdokumentasi pada README/API/testing flow.

## 1. Auth dan Access Control

Status awal: `IN PROGRESS` menuju final verification.

### Wajib

- login JWT;
- current user;
- user management;
- role management;
- permission management;
- password hashing;
- role dan permission masuk ke token;
- protected endpoint;
- 401 tanpa token;
- 403 ketika permission kurang;
- frontend menyimpan token dan restore session;
- logout frontend;
- stateful session demo terisolasi untuk membuktikan materi.

### Bukti Demo

```text
JWT login berhasil
→ akses endpoint sesuai permission berhasil
→ endpoint tanpa permission menghasilkan 403
→ token invalid menghasilkan 401
→ session demo login/profile/logout berjalan pada endpoint khusus
```

## 2. Organization Management

Status awal: `IN PROGRESS`.

### Wajib

- periode organisasi;
- divisi;
- position;
- member;
- member assignment;
- current member context;
- data publik struktur organisasi;
- division task;
- division task evidence;
- permission CRUD admin;
- ownership/assignment context untuk request.

### Frontend Minimum

- halaman ringkasan struktur organisasi;
- daftar divisi/member atau minimal current member profile;
- halaman tugas divisi dan bukti tugas.

### Bukti Demo

```text
Admin membuat/melihat struktur
→ user login membaca assignment aktif
→ assignment dipakai saat membuat pengajuan
→ public endpoint dapat dibuka tanpa token sesuai kebijakan
```

## 3. Fund Request dan Approval

Status awal: `IN PROGRESS`, backend dan sebagian frontend sudah tersedia.

### Wajib

- create draft;
- add item;
- update/delete item jika draft;
- kalkulasi total;
- submit;
- approval Ketua Divisi;
- approval Ketua PUB;
- approval Pembina;
- reject;
- request revision;
- resubmit setelah revision jika didukung flow;
- approval timeline;
- status history;
- ownership validation;
- restriction Ketua Divisi berdasarkan divisi;
- My Requests;
- All Requests untuk permission tertentu;
- frontend semua aksi utama.

### Bukti Demo

```text
Anggota membuat draft
→ menambah item
→ submit
→ Ketua Divisi hanya melihat divisinya
→ Ketua PUB approve
→ Pembina approve
→ status siap dicairkan
```

## 4. Finance dan Settlement

Status awal: `IN PROGRESS`, backend tersedia, frontend belum lengkap.

### Wajib

- daftar request yang siap dicairkan;
- create disbursement;
- validasi request ke request-service;
- cegah duplicate disbursement;
- mark request disbursed;
- user confirm fund received;
- submit settlement;
- settlement amount dan note/evidence metadata;
- approve/reject settlement;
- status request menjadi completed setelah settlement approved;
- permission Bendahara;
- frontend seluruh flow.

### Bukti Demo

```text
Bendahara memilih request READY_FOR_DISBURSEMENT
→ mencatat pencairan
→ request menjadi DISBURSED
→ pemohon konfirmasi menerima dana
→ pemohon submit settlement
→ bendahara verifikasi
→ request COMPLETED
```

## 5. Notification dan Scheduler

Status awal: `NOT STARTED`.

### Wajib

- `notification-report-service` berjalan pada port yang ditentukan;
- database dan tabel notification log;
- endpoint kirim email manual;
- HTML email;
- To, Cc, dan Bcc minimal pada DTO;
- status PENDING/SENT/FAILED;
- error SMTP tidak mematikan service;
- scheduler fixed rate;
- scheduler fixed delay;
- scheduler cron;
- scheduler execution log;
- event publisher dari satu workflow;
- event listener membuat notification log;
- konfigurasi SMTP dari environment variable;
- route gateway;
- JWT/permission untuk endpoint internal.

### Event Minimum

```text
Request submitted atau approved
→ event dipublish
→ listener menerima event
→ notification request/log dibuat
→ email dicoba dikirim
```

### Scheduler Minimum

- fixed rate: membuat heartbeat/report snapshot demo;
- fixed delay: memproses ulang notification berstatus PENDING/FAILED;
- cron: reminder approval/settlement;
- interval/cron configurable dari properties.

## 6. Reporting dan Export

Status awal: `NOT STARTED`.

### Wajib

- endpoint summary dashboard;
- total request per status;
- total nominal request/disbursement;
- jumlah pending approval;
- jumlah settlement pending;
- export Excel;
- import Excel;
- download template Excel;
- error summary per row import;
- frontend report summary;
- tombol download file.

### Target Data Excel

Pilih satu sumber data yang jelas untuk import, misalnya:

- member/activity data; atau
- asset data; atau
- master division task.

Export minimal mencakup laporan request/finance.

### PDF

PDF/Jasper menjadi enhancement kuat. Jika dibuat, minimal satu laporan dapat diunduh. Jika tidak selesai, dokumentasikan bahwa materi dedicated PDF tidak ditemukan pada deck pertemuan, tetapi tetap menjadi roadmap.

## 7. Archive dan Document

Status awal: `NOT STARTED`.

### Wajib

- entity metadata dokumen;
- kategori dokumen;
- nama file;
- content type;
- ukuran;
- path/storage reference;
- uploader;
- upload date;
- permission read/upload/delete;
- upload file;
- list/filter;
- download;
- delete/soft delete;
- frontend list dan upload.

### Scope Cepat

File boleh disimpan lokal di folder configurable untuk demo. Jangan commit file upload ke repository.

## 8. Asset Management

Status awal: `NOT STARTED`.

### Wajib

- master asset;
- asset code unik;
- name/type;
- condition;
- status AVAILABLE/BORROWED/MAINTENANCE;
- create loan;
- return loan;
- borrower reference;
- checker reference atau nama checker;
- before/after condition;
- loan history;
- permission pengelola aset;
- frontend daftar, pinjam, dan kembalikan.

### Bukti Demo

```text
Asset AVAILABLE
→ dipinjam
→ status BORROWED
→ dikembalikan dengan kondisi akhir
→ status kembali AVAILABLE atau MAINTENANCE
```

## 9. Cleanliness/Picket

Status awal: `NOT STARTED`.

### Wajib

- jadwal piket;
- tanggal dan divisi/anggota;
- attendance status;
- poin atau pelanggaran;
- note;
- rekap per anggota/divisi;
- permission pengelola kebersihan;
- frontend input dan rekap.

### Bukti Demo

```text
Pengelola membuat jadwal
→ mencatat hadir/tidak hadir
→ menambah poin/pelanggaran
→ rekap berubah
```

## 10. Division Activity

Status awal: `IN PROGRESS` karena task dan evidence sudah ada di organization-service.

### Wajib

- create division task;
- assignment;
- due date;
- status;
- add evidence;
- approve/verify evidence minimal;
- filter berdasarkan divisi;
- frontend task list dan evidence.

## 11. English Activity

Status awal: `NOT STARTED`.

### Wajib

- jadwal setoran;
- member reference;
- material/topic;
- status PENDING/COMPLETED/MISSED;
- score atau note pembinaan;
- progress summary;
- permission Divisi Bahasa Inggris;
- frontend input dan rekap.

### Bukti Demo

```text
Pengelola membuat jadwal/setoran
→ mencatat hasil anggota
→ progress anggota berubah
```

## 12. HUMAS/Public Web

Status awal: `IN PROGRESS` karena public organization API sudah ada.

### Wajib

- public organization profile;
- public structure/division data;
- public activity/program data;
- endpoint tanpa autentikasi yang dipilih secara sadar;
- data internal sensitif tidak ikut terbuka;
- frontend public page minimal.

### Bukti Demo

```text
Browser tanpa token
→ membuka halaman profil/struktur/program publik
→ endpoint internal tetap protected
```

## Security Acceptance Criteria

Wajib dibuktikan:

- request tanpa token → 401;
- token invalid/expired → 401;
- permission kurang → 403;
- user tidak dapat membaca/mengubah data user lain;
- Ketua Divisi tidak dapat approve divisi lain;
- role biasa tidak dapat disburse;
- endpoint publik hanya menampilkan field publik;
- file download protected sesuai permission;
- upload memvalidasi tipe dan ukuran file;
- secret tidak dikomit ke Git.

## Frontend Acceptance Criteria

- semua route protected bekerja;
- menu mengikuti permission;
- loading state;
- empty state;
- error state;
- success feedback;
- form validation;
- responsive minimum untuk laptop;
- tidak ada tombol aktif menuju fitur placeholder;
- seluruh action penting memanggil gateway port 8000;
- build `npm run build` berhasil.

## Backend Acceptance Criteria

- `mvn clean test` atau minimal `mvn clean package` berhasil untuk setiap service;
- service dapat run dengan profile/config local;
- database migration/schema tersedia melalui JPA atau SQL yang terdokumentasi;
- endpoint tidak mengakses database service lain;
- integration client menangani error;
- tidak ada hard-coded secret;
- data demo dapat dibuat ulang;
- global exception response konsisten.

## Documentation Acceptance Criteria

- README sesuai implementasi aktual;
- service list dan port aktual;
- database list aktual;
- API endpoint list aktual;
- role/permission matrix aktual;
- testing flow aktual;
- course coverage matrix aktual;
- current status handoff aktual;
- akun demo ditulis tanpa password sensitif production;
- langkah run lengkap.

## Full Scope Definition of Done

Full scope dianggap tercapai untuk UAS ketika:

1. semua 12 domain di atas mempunyai implementasi nyata;
2. core flow selesai end-to-end melalui browser;
3. semua materi wajib pada course matrix dapat didemokan;
4. semua service dan frontend berhasil build;
5. security path utama terbukti;
6. Excel dan email/scheduler/listener benar-benar berjalan;
7. dokumentasi tidak lagi menyebut fitur yang sudah ada sebagai planned;
8. tidak ada domain yang hanya berupa tulisan atau placeholder.
