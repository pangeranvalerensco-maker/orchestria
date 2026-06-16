# 01. Project Overview

## Ringkasan

Orchestria adalah Sistem Manajemen Operasional Organisasi berbasis microservices. Sistem ini dirancang untuk membantu organisasi mengelola proses operasional lintas divisi secara terdokumentasi, terukur, dan dapat diaudit.

Studi kasus utama project ini adalah operasional PUB UNAS PASIM. Orchestria tidak hanya berfokus pada pengajuan dana, tetapi juga mencakup manajemen struktur organisasi, pengajuan operasional, approval bertingkat, pencairan dana, settlement, arsip dokumen, peminjaman aset, tugas divisi, kebersihan/piket, aktivitas Bahasa Inggris, HUMAS/public web, notifikasi, scheduler, dan reporting.

Fund request workflow adalah vertical slice pertama yang diimplementasikan untuk membuktikan fondasi microservices, authentication, authorization, approval workflow, finance flow, dan API Gateway.

## Latar Belakang

Operasional organisasi sering berjalan melalui banyak media terpisah seperti chat, spreadsheet, dokumen manual, dan catatan pribadi. Kondisi ini menyebabkan beberapa masalah:

* Pengajuan kebutuhan divisi sulit dilacak.
* Approval tidak selalu memiliki histori yang jelas.
* Data pencairan dana dan pertanggungjawaban sering terpisah.
* Arsip dokumen tidak terpusat.
* Peminjaman aset organisasi tidak selalu tercatat rapi.
* Tugas divisi dan bukti pengerjaan tidak selalu terdokumentasi.
* Rekap kebersihan, piket, poin, dan aktivitas rutin masih manual.
* Notifikasi dan reminder masih bergantung pada pengingat personal.
* Laporan membutuhkan rekap manual dari banyak sumber.

Orchestria dibuat untuk menyatukan proses tersebut ke dalam sistem backend yang modular dan dapat dikembangkan bertahap.

## Tujuan Sistem

Tujuan utama Orchestria adalah:

* Menyediakan sistem operasional organisasi yang terdokumentasi.
* Memisahkan domain bisnis ke beberapa microservices.
* Menyediakan authentication dan authorization berbasis JWT, role, dan permission.
* Menyediakan workflow approval yang jelas dan dapat diaudit.
* Menyimpan histori proses penting.
* Membantu pengurus memantau pengajuan, tugas, aset, arsip, dan laporan.
* Menjadi fondasi untuk frontend React, notifikasi, scheduler, reporting, dan deployment.

## Full Scope Orchestria

Scope besar Orchestria mencakup beberapa domain berikut.

| Domain                   | Deskripsi                                                                      |
| ------------------------ | ------------------------------------------------------------------------------ |
| Auth & Access Control    | User, role, permission, JWT, dan akses admin                                   |
| Organization Management  | Periode, divisi, jabatan, anggota, dan assignment                              |
| Fund Request Workflow    | Pengajuan dana, item, approval, pencairan, dan settlement                      |
| Finance Management       | Pencairan dana, bukti pencairan, settlement verification, dan laporan keuangan |
| Archive & Document       | Arsip surat, dokumen approval, bukti transaksi, dan dokumen organisasi         |
| Asset Management         | Peminjaman laptop/aset, checker, kondisi barang, dan histori peminjaman        |
| Cleanliness / Picket     | Jadwal piket, poin, pelanggaran, dan rekap kebersihan                          |
| Division Activity        | Tugas divisi, bukti tugas, agenda divisi, dan progress kegiatan                |
| English Activity         | Setoran Bahasa Inggris, progress anggota, dan rekap aktivitas                  |
| HUMAS / Public Web       | Data publik PUB, struktur organisasi, anggota, dan aktivitas publik            |
| Notification & Scheduler | Email notification, reminder approval, reminder settlement, dan reminder tugas |
| Reporting Dashboard      | Laporan pengajuan, finance, aktivitas divisi, aset, dan kebersihan             |
| Frontend React           | Interface pengguna untuk demo dan penggunaan sistem                            |

## Current MVP Implementation

Current MVP adalah vertical slice pertama yang berfokus pada:

```text id="zsl91d"
Auth
→ Organization
→ Fund Request
→ Approval
→ Finance Disbursement
→ Settlement
→ API Gateway
```

Flow utama MVP:

```text id="3954fj"
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

MVP ini membuktikan bahwa sistem sudah mampu menjalankan:

* JWT authentication.
* Role dan permission.
* Permission-based authorization.
* Multi-service backend.
* API Gateway routing.
* Approval bertingkat.
* Status transition.
* Status history.
* Finance disbursement.
* Settlement calculation.
* Basic audit trail.

## Aktor Utama

Aktor utama Orchestria dari sisi security role:

| Role                | Fungsi Umum                                                      |
| ------------------- | ---------------------------------------------------------------- |
| SUPER_ADMIN         | Mengelola sistem dan memiliki akses penuh                        |
| PEMBINA             | Memberi approval akhir dan melihat laporan                       |
| KETUA_PUB           | Memantau organisasi, memberi approval PUB, dan melihat pengajuan |
| KETUA_DIVISI        | Membuat pengajuan dan memberi approval awal                      |
| SEKRETARIS          | Mengelola administrasi, arsip, dan laporan                       |
| BENDAHARA_INTERNAL  | Mengelola pencairan, settlement, dan laporan finance             |
| BENDAHARA_EKSTERNAL | Melihat laporan dan data finance sesuai kebutuhan                |
| ANGGOTA             | Membuat pengajuan dan melihat data milik sendiri                 |

Catatan penting:

```text id="nct2rz"
Role security berbeda dengan position organisasi.
```

Contoh:

```text id="ekvdqg"
KETUA_PUB sebagai role digunakan untuk authorization sistem.
Ketua PUB sebagai position digunakan untuk struktur kepengurusan organisasi.
```

## Microservices

| Service                     | Tanggung Jawab                                            | Status          |
| --------------------------- | --------------------------------------------------------- | --------------- |
| api-gateway                 | Satu pintu akses API, routing, CORS, request logging      | Implemented     |
| auth-service                | Login, JWT, user, role, permission                        | Implemented     |
| organization-service        | Periode, divisi, jabatan, anggota, assignment, public API | Implemented     |
| request-service             | Pengajuan dana, item, approval, timeline, settlement      | Implemented MVP |
| finance-service             | Pencairan dana                                            | Implemented MVP |
| notification-report-service | Email, reminder, scheduler, laporan                       | Planned         |

## Implemented Modules

### Auth Service

Fitur yang sudah menjadi bagian fondasi:

* Register/login.
* JWT generation.
* JWT validation.
* Current user endpoint.
* Role dan permission seeder.
* Admin auth endpoints.
* Custom security response.

### Organization Service

Fitur yang sudah menjadi bagian fondasi:

* Division management.
* Position management.
* Organization period management.
* Member management.
* Member assignment management.
* Division task management.
* Division task evidence management.
* Public organization API.
* Stateless JWT security.

### Request Service

Fitur MVP:

* Create fund request.
* Add request item.
* Submit request.
* Approval division.
* Approval PUB.
* Approval pembina.
* Reject request.
* Request revision.
* Status history.
* Approval timeline.
* Mark request as disbursed.
* Confirm fund received.
* Submit settlement.
* Approve settlement.
* My requests endpoint.

### Finance Service

Fitur MVP:

* Create fund disbursement.
* Get all disbursements.
* Get disbursement detail.
* Get disbursement by fund request.
* Finance permission protection.

### API Gateway

Fitur MVP:

* Routing ke auth-service.
* Routing ke organization-service.
* Routing ke request-service.
* Routing ke finance-service.
* CORS configuration.
* Request logging.

## Planned Modules

### Archive & Document Module

Rencana fitur:

* Arsip surat organisasi.
* Upload dokumen kegiatan.
* Arsip bukti approval.
* Arsip bukti pencairan dan settlement.
* Metadata dokumen.
* Pembatasan akses dokumen berdasarkan role dan relasi data.

### Asset Management Module

Rencana fitur:

* Data aset/laptop.
* Status aset.
* Peminjaman aset.
* Pengembalian aset.
* Checker aset.
* Kondisi sebelum dan sesudah peminjaman.
* Histori peminjaman.

### Cleanliness / Picket Module

Rencana fitur:

* Jadwal piket.
* Kehadiran piket.
* Poin kebersihan.
* Pelanggaran.
* Rekap divisi atau anggota.
* Laporan kebersihan.

### Division Activity Module

Rencana fitur lanjutan:

* Agenda divisi.
* Tugas rutin divisi.
* Bukti pengerjaan.
* Progress tugas.
* Rekap aktivitas per divisi.

### English Activity Module

Rencana fitur:

* Setoran Bahasa Inggris.
* Jadwal setoran.
* Progress anggota.
* Rekap aktivitas Bahasa Inggris.
* Catatan pembinaan.

### HUMAS / Public Web Module

Rencana fitur:

* Data struktur publik.
* Data anggota publik.
* Kegiatan publik PUB.
* Konten untuk website PUB.
* Endpoint publik yang aman.

### Notification & Scheduler Module

Rencana fitur:

* Email notification.
* Reminder approval.
* Reminder settlement.
* Reminder tugas.
* Scheduler berkala.
* Log pengiriman notifikasi.
* Retry mechanism.

### Reporting Dashboard

Rencana fitur:

* Laporan pengajuan.
* Laporan approval.
* Laporan pencairan.
* Laporan settlement.
* Laporan aktivitas divisi.
* Laporan aset.
* Laporan kebersihan.
* Export PDF/Excel.

## Strategi Pengembangan

Orchestria dikembangkan menggunakan pendekatan vertical slice.

Artinya, sistem tidak menunggu semua backend selesai baru membuat frontend. Setiap slice dikembangkan dari backend sampai siap diuji, lalu frontend dapat mengikuti slice tersebut.

Strategi:

```text id="f9z2z2"
Backend slice stabil
→ Postman flow stabil
→ Frontend untuk slice tersebut
→ Backend module berikutnya
→ Frontend module berikutnya
```

Current vertical slice:

```text id="79tszs"
Auth + Organization + Fund Request + Finance + Gateway
```

Next vertical slice kandidat:

```text id="bv5b3t"
Archive & Document
atau
Asset Management
```

## Batasan Saat Ini

Batasan project pada kondisi saat ini:

* Frontend React belum dibuat.
* Notification-report-service belum dibuat.
* Docker dan deployment belum dibuat.
* CI/CD belum dibuat.
* Upload file fisik belum difokuskan.
* Event-driven communication belum digunakan.
* Service-to-service authentication khusus belum dibuat.
* Reporting dashboard belum dibuat.
* Beberapa planned modules masih berada pada tahap desain.

## Prioritas Terdekat

Prioritas pengembangan terdekat:

1. Merapikan dokumentasi full scope Orchestria.
2. Memastikan permission-based authorization berjalan.
3. Membuat Postman testing flow untuk MVP.
4. Commit checkpoint backend MVP.
5. Menentukan next backend module.
6. Mulai frontend MVP untuk vertical slice pertama.
7. Lanjut module Archive atau Asset.
