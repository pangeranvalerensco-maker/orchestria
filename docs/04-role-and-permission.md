# 04. Role and Permission

Dokumen ini menjelaskan desain role dan permission Orchestria berdasarkan full scope project dan current MVP implementation.

## Konsep Utama

Orchestria memisahkan dua konsep penting:

```text id="c9zore"
Role / Permission = hak akses sistem
Position = jabatan organisasi
```

Contoh:

```text id="6wgwn1"
KETUA_PUB sebagai role security digunakan untuk authorization sistem.
Ketua PUB sebagai position organisasi digunakan untuk struktur kepengurusan PUB.
```

Dengan pemisahan ini:

* auth-service mengelola user, role, permission, dan JWT.
* organization-service mengelola periode, divisi, jabatan organisasi, anggota, dan assignment.
* service lain membaca role dan permission dari JWT untuk menentukan akses user.

## Role Security

Role security adalah role yang digunakan untuk authorization sistem.

| Role                  | Deskripsi                                                                                   |
| --------------------- | ------------------------------------------------------------------------------------------- |
| `SUPER_ADMIN`         | Akses penuh ke seluruh sistem                                                               |
| `PEMBINA`             | Approval akhir, pemantauan organisasi, dan laporan                                          |
| `KETUA_PUB`           | Pengawasan organisasi, approval tingkat PUB, dan akses laporan                              |
| `KETUA_DIVISI`        | Pengelolaan pengajuan dan aktivitas pada level divisi                                       |
| `SEKRETARIS`          | Administrasi organisasi, dokumen, arsip, dan laporan                                        |
| `BENDAHARA_INTERNAL`  | Pencairan dana, settlement, dan laporan finance                                             |
| `BENDAHARA_EKSTERNAL` | Akses laporan finance sesuai kebutuhan organisasi                                           |
| `KOORDINATOR`         | Pengelolaan aktivitas operasional tertentu seperti piket, Bahasa Inggris, atau tugas divisi |
| `CHECKER`             | Pemeriksaan aset atau proses operasional tertentu                                           |
| `ANGGOTA`             | Pengajuan, tugas, aktivitas, dan akses data milik sendiri                                   |

Catatan:

```text id="xp1w9v"
Role dapat disesuaikan lagi sesuai kebutuhan organisasi.
Tidak semua role harus langsung diimplementasikan pada MVP.
```

## Position Organisasi

Position organisasi adalah jabatan dalam struktur PUB.

Contoh position:

| Position        | Contoh Fungsi              |
| --------------- | -------------------------- |
| Ketua PUB       | Memimpin organisasi        |
| Wakil Ketua PUB | Membantu Ketua PUB         |
| Sekretaris      | Mengelola administrasi     |
| Bendahara       | Mengelola keuangan         |
| Ketua Divisi    | Memimpin divisi            |
| Koordinator     | Mengatur kegiatan spesifik |
| Anggota         | Anggota organisasi         |

Position digunakan untuk struktur organisasi dan public web, sedangkan role digunakan untuk authorization sistem.

## Permission Naming Convention

Permission menggunakan format:

```text id="7wgk4m"
domain.action.scope
```

atau:

```text id="kqpq79"
domain.action
```

Contoh:

```text id="14an1q"
request.read.own
request.read.all
finance.disburse
asset.borrow.approve
archive.document.manage
```

Tujuan naming convention:

* Mudah dibaca.
* Mudah digunakan pada `@PreAuthorize`.
* Mudah dikembangkan saat fitur bertambah.
* Memisahkan permission antar domain.

## Current Implemented Permissions

Permission yang sudah menjadi bagian current MVP:

| Permission                  | Deskripsi                                |
| --------------------------- | ---------------------------------------- |
| `auth.user.read`            | Melihat data user                        |
| `auth.user.manage`          | Mengelola user                           |
| `auth.role.manage`          | Mengelola role dan permission            |
| `organization.read`         | Melihat data organisasi                  |
| `organization.manage`       | Mengelola data organisasi                |
| `division.task.read`        | Melihat tugas divisi                     |
| `division.task.manage`      | Mengelola tugas divisi                   |
| `request.create`            | Membuat pengajuan dan aksi milik pengaju |
| `request.read.own`          | Melihat pengajuan milik sendiri          |
| `request.read.all`          | Melihat seluruh pengajuan                |
| `request.approve.division`  | Approval level Ketua Divisi              |
| `request.approve.pub`       | Approval level Ketua PUB                 |
| `request.approve.pembina`   | Approval level Pembina                   |
| `finance.disburse`          | Melakukan pencairan dana                 |
| `finance.settlement.verify` | Memverifikasi settlement                 |
| `finance.report.read`       | Melihat data/laporan finance             |
| `archive.manage`            | Mengelola arsip dasar                    |
| `notification.manage`       | Mengelola notifikasi dasar               |
| `report.read`               | Melihat laporan umum                     |

## Planned Permissions

Permission berikut disiapkan untuk pengembangan module berikutnya.

### Archive & Document Permissions

| Permission                  | Deskripsi                                                   |
| --------------------------- | ----------------------------------------------------------- |
| `archive.document.create`   | Membuat/mengunggah metadata dokumen                         |
| `archive.document.read.own` | Melihat dokumen milik sendiri atau yang terkait dengan user |
| `archive.document.read.all` | Melihat seluruh dokumen                                     |
| `archive.document.manage`   | Mengelola dokumen dan arsip                                 |
| `archive.document.delete`   | Menghapus atau soft delete dokumen                          |
| `archive.document.download` | Mengunduh dokumen                                           |
| `archive.category.manage`   | Mengelola kategori arsip                                    |

### Asset Permissions

| Permission               | Deskripsi                               |
| ------------------------ | --------------------------------------- |
| `asset.read`             | Melihat daftar aset                     |
| `asset.manage`           | Mengelola data aset                     |
| `asset.borrow.create`    | Mengajukan peminjaman aset              |
| `asset.borrow.read.own`  | Melihat peminjaman aset milik sendiri   |
| `asset.borrow.read.all`  | Melihat seluruh peminjaman aset         |
| `asset.borrow.approve`   | Menyetujui atau menolak peminjaman aset |
| `asset.borrow.handover`  | Mencatat penyerahan aset                |
| `asset.return.verify`    | Memverifikasi pengembalian aset         |
| `asset.condition.manage` | Mencatat kondisi aset                   |

### Cleanliness / Picket Permissions

| Permission                      | Deskripsi                            |
| ------------------------------- | ------------------------------------ |
| `cleanliness.schedule.read`     | Melihat jadwal piket                 |
| `cleanliness.schedule.manage`   | Mengelola jadwal piket               |
| `cleanliness.attendance.create` | Mencatat kehadiran/pelaksanaan piket |
| `cleanliness.attendance.read`   | Melihat data pelaksanaan piket       |
| `cleanliness.point.manage`      | Mengelola poin kebersihan            |
| `cleanliness.violation.manage`  | Mengelola pelanggaran kebersihan     |
| `cleanliness.report.read`       | Melihat laporan kebersihan           |

### Division Activity Permissions

| Permission                      | Deskripsi                              |
| ------------------------------- | -------------------------------------- |
| `division.activity.read`        | Melihat aktivitas divisi               |
| `division.activity.manage`      | Mengelola agenda atau aktivitas divisi |
| `division.task.read`            | Melihat tugas divisi                   |
| `division.task.manage`          | Mengelola tugas divisi                 |
| `division.task.evidence.create` | Mengirim bukti tugas                   |
| `division.task.evidence.verify` | Memverifikasi bukti tugas              |
| `division.activity.report.read` | Melihat laporan aktivitas divisi       |

### English Activity Permissions

| Permission                 | Deskripsi                         |
| -------------------------- | --------------------------------- |
| `english.activity.read`    | Melihat aktivitas Bahasa Inggris  |
| `english.activity.manage`  | Mengelola kegiatan Bahasa Inggris |
| `english.deposit.create`   | Mencatat setoran Bahasa Inggris   |
| `english.deposit.read.own` | Melihat setoran milik sendiri     |
| `english.deposit.read.all` | Melihat seluruh setoran           |
| `english.deposit.verify`   | Memverifikasi setoran             |
| `english.report.read`      | Melihat laporan Bahasa Inggris    |

### HUMAS / Public Web Permissions

| Permission                   | Deskripsi                                   |
| ---------------------------- | ------------------------------------------- |
| `public.content.read`        | Melihat konten publik                       |
| `public.content.manage`      | Mengelola konten publik                     |
| `public.organization.manage` | Mengatur data organisasi yang tampil publik |
| `public.activity.manage`     | Mengelola publikasi kegiatan                |
| `public.media.manage`        | Mengelola media publik                      |

### Notification & Scheduler Permissions

| Permission                     | Deskripsi                           |
| ------------------------------ | ----------------------------------- |
| `notification.read`            | Melihat data notifikasi             |
| `notification.manage`          | Mengelola notifikasi                |
| `notification.template.manage` | Mengelola template notifikasi       |
| `notification.send`            | Mengirim notifikasi manual          |
| `scheduler.read`               | Melihat job scheduler               |
| `scheduler.manage`             | Mengelola scheduler                 |
| `scheduler.run.manual`         | Menjalankan scheduler secara manual |

### Reporting Permissions

| Permission                 | Deskripsi                        |
| -------------------------- | -------------------------------- |
| `report.read`              | Melihat laporan umum             |
| `report.finance.read`      | Melihat laporan finance          |
| `report.request.read`      | Melihat laporan pengajuan        |
| `report.organization.read` | Melihat laporan organisasi       |
| `report.asset.read`        | Melihat laporan aset             |
| `report.cleanliness.read`  | Melihat laporan kebersihan       |
| `report.activity.read`     | Melihat laporan aktivitas divisi |
| `report.export`            | Mengekspor laporan               |

## Mapping Role ke Permission Current MVP

### SUPER_ADMIN

Role dengan akses penuh.

Permission current MVP:

```text id="gy5tkd"
auth.user.read
auth.user.manage
auth.role.manage
organization.read
organization.manage
division.task.read
division.task.manage
request.create
request.read.own
request.read.all
request.approve.division
request.approve.pub
request.approve.pembina
finance.disburse
finance.settlement.verify
finance.report.read
archive.manage
notification.manage
report.read
```

Untuk planned module, SUPER_ADMIN akan mendapatkan seluruh permission tambahan.

### PEMBINA

Permission current MVP:

```text id="2es53d"
organization.read
division.task.read
request.read.all
request.approve.pembina
finance.report.read
report.read
```

Planned permission yang cocok:

```text id="q1fzzc"
report.finance.read
report.request.read
report.organization.read
report.asset.read
report.cleanliness.read
report.activity.read
```

### KETUA_PUB

Permission current MVP:

```text id="1464bb"
organization.read
organization.manage
division.task.read
division.task.manage
request.create
request.read.all
request.approve.pub
finance.report.read
archive.manage
report.read
```

Planned permission yang cocok:

```text id="yjrtfr"
archive.document.read.all
archive.document.manage
asset.borrow.read.all
asset.borrow.approve
cleanliness.report.read
division.activity.report.read
english.report.read
public.content.manage
report.request.read
report.finance.read
report.organization.read
```

### KETUA_DIVISI

Permission current MVP:

```text id="zyb3jp"
organization.read
division.task.read
division.task.manage
request.create
request.read.own
request.approve.division
```

Planned permission yang cocok:

```text id="nvce5d"
division.activity.read
division.activity.manage
division.task.evidence.verify
archive.document.create
archive.document.read.own
asset.borrow.create
asset.borrow.read.own
cleanliness.schedule.read
english.activity.read
```

Catatan:

```text id="1w2i0v"
Pada pengembangan berikutnya, KETUA_DIVISI sebaiknya hanya bisa approval pengajuan dari divisinya sendiri.
```

### SEKRETARIS

Permission current MVP:

```text id="wzh8mz"
organization.read
organization.manage
archive.manage
report.read
```

Planned permission yang cocok:

```text id="6k9yha"
archive.document.create
archive.document.read.all
archive.document.manage
archive.category.manage
public.organization.manage
report.organization.read
```

### BENDAHARA_INTERNAL

Permission current MVP:

```text id="bhjp6x"
organization.read
request.read.all
finance.disburse
finance.settlement.verify
finance.report.read
report.read
```

Planned permission yang cocok:

```text id="w1n21w"
archive.document.create
archive.document.read.all
archive.document.download
report.finance.read
report.request.read
report.export
```

### BENDAHARA_EKSTERNAL

Permission current MVP:

```text id="46tzj7"
organization.read
finance.report.read
report.read
```

Planned permission yang cocok:

```text id="bsy2m8"
report.finance.read
archive.document.read.own
archive.document.download
```

### KOORDINATOR

Role ini disiapkan untuk kebutuhan operasional tertentu.

Planned permission yang cocok:

```text id="kxcz31"
cleanliness.schedule.manage
cleanliness.attendance.create
cleanliness.point.manage
cleanliness.violation.manage
english.activity.manage
english.deposit.create
division.activity.manage
division.task.evidence.verify
```

Catatan:

```text id="8wwdhy"
KOORDINATOR dapat dibuat lebih spesifik pada fase berikutnya, misalnya KOORDINATOR_KEBERSIHAN atau KOORDINATOR_BAHASA_INGGRIS.
```

### CHECKER

Role ini disiapkan untuk pemeriksaan aset atau proses operasional.

Planned permission yang cocok:

```text id="4thnx6"
asset.read
asset.borrow.read.all
asset.borrow.approve
asset.borrow.handover
asset.return.verify
asset.condition.manage
```

### ANGGOTA

Permission current MVP:

```text id="uxbikn"
organization.read
division.task.read
request.create
request.read.own
```

Planned permission yang cocok:

```text id="n778s1"
asset.read
asset.borrow.create
asset.borrow.read.own
archive.document.create
archive.document.read.own
division.task.evidence.create
cleanliness.schedule.read
english.deposit.read.own
```

## Permission untuk Endpoint Current MVP

### Auth Service

| Endpoint                  | Permission                                      |
| ------------------------- | ----------------------------------------------- |
| `POST /api/auth/register` | Public atau admin-controlled sesuai konfigurasi |
| `POST /api/auth/login`    | Public                                          |
| `GET /api/auth/me`        | JWT valid                                       |
| Admin user endpoints      | `auth.user.read` atau `auth.user.manage`        |
| Admin role endpoints      | `auth.role.manage`                              |

### Organization Service

| Endpoint                                         | Permission                                         |
| ------------------------------------------------ | -------------------------------------------------- |
| `GET /api/organization/**`                       | `organization.read` atau JWT valid sesuai endpoint |
| `POST /api/organization/divisions`               | `organization.manage`                              |
| `PUT /api/organization/divisions/{id}`           | `organization.manage`                              |
| `DELETE /api/organization/divisions/{id}`        | `organization.manage`                              |
| `POST /api/organization/positions`               | `organization.manage`                              |
| `POST /api/organization/periods`                 | `organization.manage`                              |
| `POST /api/organization/members`                 | `organization.manage`                              |
| `POST /api/organization/member-assignments`      | `organization.manage`                              |
| `POST /api/organization/division-tasks`          | `division.task.manage`                             |
| `POST /api/organization/division-task-evidences` | `division.task.manage`                             |

### Request Service

| Endpoint                                     | Permission                  |
| -------------------------------------------- | --------------------------- |
| `POST /api/requests`                         | `request.create`            |
| `GET /api/requests`                          | `request.read.all`          |
| `GET /api/requests/my`                       | `request.read.own`          |
| `GET /api/requests/{id}`                     | `request.read.all`          |
| `POST /api/requests/{id}/items`              | `request.create`            |
| `POST /api/requests/{id}/submit`             | `request.create`            |
| `POST /api/requests/{id}/approvals/approve`  | Sesuai level approval       |
| `POST /api/requests/{id}/approvals/reject`   | Sesuai level approval       |
| `POST /api/requests/{id}/approvals/revision` | Sesuai level approval       |
| `GET /api/requests/{id}/approvals`           | `request.read.all`          |
| `GET /api/requests/{id}/histories`           | `request.read.all`          |
| `POST /api/requests/{id}/mark-disbursed`     | `finance.disburse`          |
| `POST /api/requests/{id}/confirm-received`   | `request.create`            |
| `POST /api/requests/{id}/settlement`         | `request.create`            |
| `POST /api/requests/{id}/settlement/approve` | `finance.settlement.verify` |

### Finance Service

| Endpoint                                                    | Permission                                    |
| ----------------------------------------------------------- | --------------------------------------------- |
| `POST /api/finance/disbursements`                           | `finance.disburse`                            |
| `GET /api/finance/disbursements`                            | `finance.report.read` atau `finance.disburse` |
| `GET /api/finance/disbursements/{id}`                       | `finance.report.read` atau `finance.disburse` |
| `GET /api/finance/disbursements/by-request/{fundRequestId}` | `finance.report.read` atau `finance.disburse` |

## Approval Permission Rule

Approval pengajuan dana berjalan berdasarkan level.

| Approval Level | Status Awal         | Status Setelah Approved  | Permission                 |
| -------------- | ------------------- | ------------------------ | -------------------------- |
| `DIVISION`     | `SUBMITTED`         | `DIVISION_APPROVED`      | `request.approve.division` |
| `PUB`          | `DIVISION_APPROVED` | `PUB_APPROVED`           | `request.approve.pub`      |
| `PEMBINA`      | `PUB_APPROVED`      | `READY_FOR_DISBURSEMENT` | `request.approve.pembina`  |

Aturan penting:

```text id="gczgn5"
User hanya boleh melakukan approval sesuai permission level-nya.
Ketua Divisi tidak boleh melakukan approval level PUB.
Ketua PUB tidak boleh melakukan approval level PEMBINA.
Pembina tidak digunakan untuk menggantikan approval level lain.
```

## Ownership Rule

Beberapa endpoint harus membedakan data milik sendiri dan semua data.

### Own Data

Digunakan untuk role seperti ANGGOTA dan KETUA_DIVISI.

Contoh:

```http id="2f4l94"
GET /api/requests/my
```

Permission:

```text id="kt0rhh"
request.read.own
```

Dasar pengecekan:

```text id="vjxyai"
createdByEmail == email user login dari JWT
```

### All Data

Digunakan untuk role seperti SUPER_ADMIN, PEMBINA, KETUA_PUB, dan BENDAHARA_INTERNAL.

Contoh:

```http id="i1pt4c"
GET /api/requests
```

Permission:

```text id="hlqr5y"
request.read.all
```

## Planned Ownership Rules

Pada module berikutnya, ownership rule juga diterapkan.

| Domain           | Own Rule                                                                             |
| ---------------- | ------------------------------------------------------------------------------------ |
| Request          | User hanya melihat pengajuan milik sendiri                                           |
| Asset            | User hanya melihat peminjaman aset milik sendiri                                     |
| Archive          | User hanya melihat dokumen milik sendiri atau yang terkait                           |
| English Activity | User hanya melihat progress/setoran milik sendiri                                    |
| Division Task    | User hanya melihat tugas yang terkait dengan dirinya atau divisinya                  |
| Cleanliness      | User melihat jadwal miliknya, pengurus melihat rekap                                 |
| Finance          | Pengurus tertentu melihat data finance, user biasa tidak melihat data finance global |

## 401 dan 403

### 401 Unauthorized

Terjadi ketika:

```text id="3bixry"
Token tidak dikirim.
Token tidak valid.
Token expired.
Format Authorization header salah.
```

### 403 Forbidden

Terjadi ketika:

```text id="0mlwn9"
Token valid.
User berhasil login.
Tetapi permission tidak cukup.
```

Contoh:

```text id="58ygkg"
ANGGOTA mengakses POST /api/finance/disbursements
```

Expected:

```text id="sav7ad"
403 Forbidden
```

## Frontend Permission Strategy

Frontend tidak menentukan keamanan utama. Keamanan utama tetap berada di backend.

Namun frontend dapat menggunakan role dan permission dari endpoint current user untuk:

* menampilkan menu sesuai role;
* menyembunyikan tombol yang tidak boleh digunakan;
* menampilkan halaman sesuai akses;
* mengurangi error 403 yang tidak perlu.

Contoh:

| Permission                  | UI Behavior                         |
| --------------------------- | ----------------------------------- |
| `request.create`            | Tampilkan tombol Create Request     |
| `request.read.own`          | Tampilkan menu My Requests          |
| `request.read.all`          | Tampilkan menu All Requests         |
| `request.approve.division`  | Tampilkan tombol Approve Division   |
| `request.approve.pub`       | Tampilkan tombol Approve PUB        |
| `request.approve.pembina`   | Tampilkan tombol Approve Pembina    |
| `finance.disburse`          | Tampilkan tombol Disburse           |
| `finance.settlement.verify` | Tampilkan tombol Approve Settlement |
| `asset.borrow.create`       | Tampilkan tombol Borrow Asset       |
| `archive.document.manage`   | Tampilkan menu Archive Management   |

Catatan:

```text id="ys2qgi"
Menyembunyikan tombol di frontend bukan pengganti authorization backend.
```

## Status Implementasi

| Area                            | Status      |
| ------------------------------- | ----------- |
| Role current MVP                | Implemented |
| Permission current MVP          | Implemented |
| JWT roles claim                 | Implemented |
| JWT permissions claim           | Implemented |
| Method-level security           | In progress |
| Full planned permissions        | Draft       |
| Ownership validation per module | Planned     |
| Frontend permission-based UI    | Planned     |

## Prioritas Berikutnya

Prioritas permission berikutnya:

1. Memastikan current MVP permission berjalan melalui `@PreAuthorize`.
2. Mengetes 401 dan 403 di Postman.
3. Menyamakan permission di auth-service seeder dengan dokumentasi.
4. Menentukan permission final untuk Archive module.
5. Menentukan permission final untuk Asset module.
6. Menyiapkan frontend menu berbasis role dan permission.
