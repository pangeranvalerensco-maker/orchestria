# 02. Business Flow

Dokumen ini menjelaskan alur bisnis Orchestria berdasarkan full scope project dan current MVP implementation.

## Ringkasan

Orchestria adalah sistem operasional organisasi PUB berbasis microservices. Sistem ini tidak hanya berfokus pada pengajuan dana, tetapi juga mencakup pengelolaan organisasi, pengajuan operasional, approval, keuangan, arsip, aset, tugas divisi, kebersihan/piket, aktivitas Bahasa Inggris, notifikasi, dan laporan.

Fund request workflow adalah vertical slice pertama yang diimplementasikan untuk membuktikan alur backend dari authentication sampai proses selesai.

## Full Scope Business Domain

| Domain                   | Deskripsi                                               | Status                |
| ------------------------ | ------------------------------------------------------- | --------------------- |
| Auth & Access Control    | Login, JWT, user, role, permission                      | Implemented           |
| Organization Management  | Periode, divisi, jabatan, anggota, assignment           | Implemented           |
| Fund Request Workflow    | Pengajuan dana, approval, pencairan, settlement         | Implemented MVP       |
| Finance Management       | Pencairan dana dan data pencairan                       | Implemented MVP       |
| Archive & Document       | Arsip surat, dokumen organisasi, bukti transaksi        | Planned               |
| Asset Management         | Peminjaman laptop/aset, checker, pengembalian           | Planned               |
| Cleanliness / Picket     | Jadwal piket, poin, pelanggaran, rekap                  | Planned               |
| Division Activity        | Tugas divisi, bukti tugas, progress agenda              | Partially implemented |
| English Activity         | Setoran Bahasa Inggris, progress, rekap                 | Planned               |
| HUMAS / Public Web       | Data publik PUB untuk website                           | Partially implemented |
| Notification & Scheduler | Reminder, email notification, scheduler                 | Planned               |
| Reporting Dashboard      | Laporan pengajuan, finance, aset, kebersihan, aktivitas | Planned               |

## Aktor Utama

| Aktor                 | Fungsi Umum                                                          |
| --------------------- | -------------------------------------------------------------------- |
| SUPER_ADMIN           | Mengelola sistem secara penuh                                        |
| PEMBINA               | Memberi approval akhir dan melihat laporan                           |
| KETUA_PUB             | Memantau organisasi dan memberi approval tingkat PUB                 |
| KETUA_DIVISI          | Mengelola pengajuan/tugas pada level divisi                          |
| SEKRETARIS            | Mengelola administrasi, arsip, dan dokumen                           |
| BENDAHARA_INTERNAL    | Mengelola pencairan dana dan settlement                              |
| BENDAHARA_EKSTERNAL   | Melihat laporan finance sesuai kebutuhan                             |
| KOORDINATOR / CHECKER | Menangani proses operasional tertentu seperti aset atau piket        |
| ANGGOTA               | Membuat pengajuan, menjalankan tugas, dan melihat data milik sendiri |

Catatan:

```text
Role security dan jabatan organisasi tidak selalu sama.
```

Contoh:

```text
KETUA_PUB sebagai role dipakai untuk authorization sistem.
Ketua PUB sebagai position dipakai untuk struktur organisasi.
```

---

# 1. Auth & Access Control Flow

## Tujuan

Mengatur siapa yang dapat masuk ke sistem dan apa saja yang boleh dilakukan.

## Alur Login

```text
User membuka aplikasi
→ User memasukkan email dan password
→ auth-service memvalidasi credential
→ auth-service membuat JWT
→ JWT berisi userId, email, fullName, roles, permissions
→ Client menggunakan JWT untuk mengakses service lain
```

## Output

* User mendapatkan token.
* Token digunakan untuk authorization.
* Service lain dapat membaca role dan permission dari token.

## Status

```text
Implemented
```

---

# 2. Organization Management Flow

## Tujuan

Mengelola struktur organisasi PUB, termasuk periode, divisi, jabatan, anggota, dan assignment.

## Alur Data Organisasi

```text
Admin membuat periode organisasi
→ Admin membuat divisi
→ Admin membuat position/jabatan
→ Admin menambahkan member
→ Admin membuat assignment member ke periode, divisi, dan position
→ Struktur organisasi dapat dilihat secara internal atau publik
```

## Data yang Dikelola

* Organization period.
* Division.
* Position.
* Member.
* Member assignment.
* Division task.
* Division task evidence.

## Public Organization Flow

```text
Visitor/public web mengakses endpoint publik
→ Sistem mengambil periode aktif
→ Sistem mengambil struktur organisasi
→ Sistem menampilkan data yang aman untuk publik
```

## Status

```text
Implemented
```

---

# 3. Fund Request Workflow

## Tujuan

Mengelola pengajuan dana operasional divisi dari draft sampai selesai.

## Alur Utama

```text
Pengaju membuat pengajuan
→ Pengaju menambahkan item kebutuhan
→ Pengaju submit pengajuan
→ Ketua Divisi melakukan approval
→ Ketua PUB melakukan approval
→ Pembina melakukan approval
→ Bendahara mencatat pencairan dana
→ Request ditandai sudah dicairkan
→ Pengaju mengonfirmasi dana diterima
→ Pengaju mengirim settlement
→ Bendahara memverifikasi settlement
→ Pengajuan selesai
```

## Status Flow Current MVP

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

## Detail Flow

### 3.1 Membuat Pengajuan

Pengaju membuat pengajuan dana operasional.

Data utama:

| Data                | Keterangan                |
| ------------------- | ------------------------- |
| divisionId          | ID divisi                 |
| divisionName        | Snapshot nama divisi      |
| requesterMemberId   | ID member pengaju         |
| requesterName       | Nama pengaju              |
| requesterAuthUserId | ID user dari auth-service |
| title               | Judul pengajuan           |
| description         | Deskripsi kebutuhan       |
| activityDate        | Tanggal kegiatan          |
| priority            | Prioritas                 |

Status awal:

```text
DRAFT
```

### 3.2 Menambahkan Item

Pengaju menambahkan item kebutuhan.

Contoh:

| Item        | Quantity | Unit Price | Subtotal |
| ----------- | -------: | ---------: | -------: |
| Nasi Box    |       10 |      25000 |   250000 |
| Air Mineral |        2 |      20000 |    40000 |

Total pengajuan dihitung dari subtotal item aktif.

```text
totalAmount = sum(subtotal request_items aktif)
```

### 3.3 Submit Pengajuan

Perubahan status:

```text
DRAFT → SUBMITTED
```

Setelah submit, pengajuan masuk ke approval Ketua Divisi.

### 3.4 Approval Ketua Divisi

Syarat status:

```text
SUBMITTED
```

Jika approved:

```text
SUBMITTED → DIVISION_APPROVED
```

Jika rejected:

```text
SUBMITTED → REJECTED
```

Jika revision requested:

```text
SUBMITTED → REVISION_REQUESTED
```

Permission:

```text
request.approve.division
```

### 3.5 Approval Ketua PUB

Syarat status:

```text
DIVISION_APPROVED
```

Jika approved:

```text
DIVISION_APPROVED → PUB_APPROVED
```

Jika rejected:

```text
DIVISION_APPROVED → REJECTED
```

Jika revision requested:

```text
DIVISION_APPROVED → REVISION_REQUESTED
```

Permission:

```text
request.approve.pub
```

### 3.6 Approval Pembina

Syarat status:

```text
PUB_APPROVED
```

Jika approved:

```text
PUB_APPROVED → READY_FOR_DISBURSEMENT
```

Jika rejected:

```text
PUB_APPROVED → REJECTED
```

Jika revision requested:

```text
PUB_APPROVED → REVISION_REQUESTED
```

Permission:

```text
request.approve.pembina
```

### 3.7 Pencairan Dana

Setelah pengajuan berstatus:

```text
READY_FOR_DISBURSEMENT
```

bendahara mencatat pencairan dana pada finance-service.

Data pencairan:

| Data          | Keterangan                    |
| ------------- | ----------------------------- |
| fundRequestId | ID pengajuan                  |
| requestTitle  | Snapshot judul pengajuan      |
| divisionId    | ID divisi                     |
| divisionName  | Snapshot nama divisi          |
| requesterName | Snapshot nama pengaju         |
| amount        | Nominal pencairan             |
| method        | CASH, BANK_TRANSFER, E_WALLET |
| receiverName  | Nama penerima                 |
| proofUrl      | Bukti pencairan               |
| note          | Catatan pencairan             |

Permission:

```text
finance.disburse
```

### 3.8 Mark Request as Disbursed

Setelah finance-service mencatat pencairan, request-service ditandai sudah dicairkan.

Perubahan status:

```text
READY_FOR_DISBURSEMENT → DISBURSED
```

Catatan MVP:

```text
Pencatatan finance-service dan update status request-service masih dilakukan melalui dua endpoint.
Belum menggunakan event/message broker.
```

### 3.9 Konfirmasi Dana Diterima

Pengaju mengonfirmasi bahwa dana sudah diterima.

Perubahan status:

```text
DISBURSED → FUND_RECEIVED
```

### 3.10 Submit Settlement

Pengaju mengirim settlement penggunaan dana.

Data settlement:

| Data        | Keterangan         |
| ----------- | ------------------ |
| spentAmount | Nominal realisasi  |
| proofUrl    | Bukti/struk        |
| note        | Catatan penggunaan |

Perubahan status:

```text
FUND_RECEIVED → SETTLEMENT_SUBMITTED
```

### 3.11 Perhitungan Settlement

Jika dana tersisa:

```text
spentAmount < totalAmount
remainingAmount = totalAmount - spentAmount
shortageAmount = 0
```

Jika dana kurang:

```text
spentAmount > totalAmount
remainingAmount = 0
shortageAmount = spentAmount - totalAmount
```

Jika dana pas:

```text
spentAmount = totalAmount
remainingAmount = 0
shortageAmount = 0
```

### 3.12 Approve Settlement

Bendahara memverifikasi settlement.

Permission:

```text
finance.settlement.verify
```

Perubahan status:

```text
SETTLEMENT_SUBMITTED → COMPLETED
```

## Status

```text
Implemented MVP
```

---

# 4. Finance Management Flow

## Tujuan

Mengelola pencairan dana dan data keuangan awal.

## Alur Current MVP

```text
Pengajuan sudah READY_FOR_DISBURSEMENT
→ Bendahara mencatat pencairan di finance-service
→ Finance-service menyimpan data fund_disbursement
→ Request-service ditandai DISBURSED
→ Pengaju mengonfirmasi dana diterima
→ Pengaju submit settlement
→ Bendahara approve settlement
```

## Flow Lanjutan yang Direncanakan

```text
Settlement memiliki remainingAmount
→ Sistem membuat data pengembalian dana lebih
→ Pengaju mengembalikan dana
→ Bendahara memverifikasi pengembalian
→ Pengajuan ditutup
```

```text
Settlement memiliki shortageAmount
→ Sistem membuat data dana kurang
→ Bendahara/Ketua PUB/Pembina meninjau
→ Dana kurang disetujui atau ditolak
→ Pengajuan ditutup sesuai keputusan
```

## Status

```text
Implemented MVP, planned expansion for shortage and return flow
```

---

# 5. Archive & Document Flow

## Tujuan

Mengelola dokumen organisasi agar tidak tersebar di chat atau folder tidak terstruktur.

## Rencana Alur

```text
User mengunggah dokumen
→ User memilih kategori dokumen
→ Sistem menyimpan metadata dokumen
→ Sistem menentukan hak akses dokumen
→ Dokumen dapat dicari, dilihat, dan diunduh oleh role terkait
```

## Jenis Dokumen

| Dokumen            | Contoh                                |
| ------------------ | ------------------------------------- |
| Dokumen organisasi | SK, surat keputusan, surat tugas      |
| Dokumen pengajuan  | Lampiran pengajuan, proposal kegiatan |
| Dokumen approval   | Bukti approval, catatan revisi        |
| Dokumen finance    | Bukti pencairan, struk settlement     |
| Dokumen divisi     | Laporan kegiatan, bukti tugas         |
| Dokumen aset       | Bukti peminjaman, bukti pengembalian  |

## Aktor

* SEKRETARIS.
* KETUA_PUB.
* BENDAHARA_INTERNAL.
* KETUA_DIVISI.
* SUPER_ADMIN.

## Status

```text
Planned
```

---

# 6. Asset Management Flow

## Tujuan

Mengelola peminjaman aset PUB, terutama laptop atau aset operasional lain.

## Rencana Alur

```text
User melihat daftar aset
→ User mengajukan peminjaman aset
→ Checker memeriksa ketersediaan aset
→ Checker menyetujui atau menolak peminjaman
→ Aset diserahkan kepada peminjam
→ Sistem mencatat tanggal pinjam dan kondisi awal
→ User mengembalikan aset
→ Checker memverifikasi kondisi akhir
→ Sistem menyimpan histori peminjaman
```

## Data Utama

| Data            | Keterangan                             |
| --------------- | -------------------------------------- |
| assetName       | Nama aset                              |
| assetCode       | Kode unik aset                         |
| category        | Kategori aset                          |
| status          | AVAILABLE, BORROWED, MAINTENANCE, LOST |
| borrower        | Peminjam                               |
| checker         | Pemeriksa aset                         |
| borrowDate      | Tanggal pinjam                         |
| returnDate      | Tanggal kembali                        |
| conditionBefore | Kondisi sebelum dipinjam               |
| conditionAfter  | Kondisi setelah dikembalikan           |

## Status

```text
Planned
```

---

# 7. Cleanliness / Picket Flow

## Tujuan

Mengelola jadwal piket, pelaksanaan kebersihan, poin, dan pelanggaran.

## Rencana Alur

```text
Koordinator membuat jadwal piket
→ Anggota/divisi mendapat jadwal
→ Anggota melaksanakan piket
→ Koordinator mencatat kehadiran atau bukti
→ Sistem menghitung poin atau pelanggaran
→ Rekap dapat dilihat oleh pengurus
```

## Data Utama

| Data          | Keterangan              |
| ------------- | ----------------------- |
| scheduleDate  | Tanggal piket           |
| divisionId    | Divisi terkait          |
| memberId      | Anggota yang bertugas   |
| status        | SCHEDULED, DONE, MISSED |
| point         | Poin kebersihan         |
| violationNote | Catatan pelanggaran     |
| evidenceUrl   | Bukti pelaksanaan       |

## Output

* Rekap piket.
* Rekap poin.
* Rekap pelanggaran.
* Laporan kebersihan.

## Status

```text
Planned
```

---

# 8. Division Activity Flow

## Tujuan

Mengelola agenda, tugas, dan bukti aktivitas divisi.

## Alur Implementasi Saat Ini

Organization-service sudah memiliki division task dan division task evidence.

Alur dasar:

```text
Pengurus membuat tugas divisi
→ Tugas diberikan ke anggota/divisi
→ Anggota atau pengurus mengunggah evidence
→ Status tugas diperbarui
→ Tugas dapat direkap
```

## Rencana Pengembangan

```text
Kadiv membuat agenda divisi
→ Tugas diturunkan dari agenda
→ Anggota mengerjakan tugas
→ Bukti dikumpulkan
→ Kadiv memvalidasi
→ Sistem membuat rekap aktivitas
```

## Status

```text
Partially implemented, planned enhancement
```

---

# 9. English Activity Flow

## Tujuan

Mengelola aktivitas Bahasa Inggris seperti setoran, progress anggota, dan rekap kegiatan.

## Rencana Alur

```text
Koordinator Bahasa Inggris membuat jadwal setoran
→ Anggota melakukan setoran
→ Koordinator mencatat hasil setoran
→ Sistem menyimpan progress anggota
→ Rekap dapat dilihat oleh pengurus
```

## Data Utama

| Data         | Keterangan                |
| ------------ | ------------------------- |
| memberId     | Anggota                   |
| scheduleDate | Tanggal setoran           |
| topic        | Materi/topik              |
| score/status | Nilai atau status setoran |
| note         | Catatan pembinaan         |
| recordedBy   | Pencatat setoran          |

## Status

```text
Planned
```

---

# 10. HUMAS / Public Web Flow

## Tujuan

Menyediakan data PUB yang aman untuk ditampilkan pada website publik.

## Alur Current

```text
Admin mengelola data organisasi
→ Organization-service menyimpan data periode, divisi, member, assignment
→ Public endpoint menyediakan data yang aman
→ Website PUB menampilkan struktur atau anggota publik
```

## Data Publik

* Periode aktif.
* Struktur organisasi.
* Anggota yang boleh ditampilkan.
* Divisi.
* Jabatan publik.
* Aktivitas publik pada fase berikutnya.

## Data yang Tidak Ditampilkan Publik

* Nomor telepon sensitif.
* Auth reference internal.
* Data private anggota.
* Dokumen internal.
* Data finance.
* Data pengajuan internal.

## Status

```text
Partially implemented
```

---

# 11. Notification & Scheduler Flow

## Tujuan

Mengurangi ketergantungan pada pengingat manual.

## Rencana Alur Notification

```text
Event penting terjadi
→ Sistem membuat notification request
→ Notification-service membaca template
→ Email dikirim ke user terkait
→ Log pengiriman disimpan
```

## Rencana Alur Scheduler

```text
Scheduler berjalan berkala
→ Sistem mencari proses yang perlu reminder
→ Sistem membuat notification request
→ Email/reminder dikirim
```

## Trigger Notifikasi

| Trigger                  | Penerima        |
| ------------------------ | --------------- |
| Pengajuan baru           | Ketua Divisi    |
| Approval Divisi selesai  | Ketua PUB       |
| Approval PUB selesai     | Pembina         |
| Pengajuan ditolak        | Pengaju         |
| Pengajuan diminta revisi | Pengaju         |
| Dana dicairkan           | Pengaju         |
| Dana diterima            | Bendahara       |
| Settlement dikirim       | Bendahara       |
| Settlement disetujui     | Pengaju         |
| Aset harus dikembalikan  | Peminjam        |
| Tugas mendekati deadline | Anggota terkait |

## Status

```text
Planned
```

---

# 12. Reporting Dashboard Flow

## Tujuan

Menyediakan laporan operasional organisasi.

## Rencana Alur

```text
Data dari beberapa service tersedia
→ Reporting membaca data melalui API atau read model
→ Sistem membuat agregasi laporan
→ Pengurus melihat dashboard
→ Data dapat diekspor ke PDF/Excel
```

## Jenis Laporan

| Laporan                     | Sumber Data                            |
| --------------------------- | -------------------------------------- |
| Laporan pengajuan           | request-service                        |
| Laporan approval            | request-service                        |
| Laporan pencairan           | finance-service                        |
| Laporan settlement          | request-service + finance-service      |
| Laporan struktur organisasi | organization-service                   |
| Laporan aset                | asset module                           |
| Laporan kebersihan          | cleanliness module                     |
| Laporan tugas divisi        | organization-service / activity module |
| Laporan Bahasa Inggris      | english activity module                |

## Status

```text
Planned
```

---

# 13. Frontend Flow

## Prinsip

Frontend tidak menunggu seluruh backend selesai.

Strategi:

```text
Backend slice stabil
→ Postman flow stabil
→ Frontend untuk slice tersebut dibuat
→ Backend module berikutnya dikembangkan
→ Frontend diperluas mengikuti module yang sudah stabil
```

## Frontend Slice 1

Fokus awal frontend:

```text
Auth
Fund Request
Approval
Finance Disbursement
Settlement
```

Halaman awal:

* Login.
* Dashboard.
* My Requests.
* All Requests.
* Create Request.
* Request Detail.
* Approval Actions.
* Finance Disbursement.
* Settlement.

## Status

```text
Planned
```

---

# 14. Current MVP Demo Flow

Flow yang sudah menjadi target demo backend saat ini:

```text
Login
→ Create Fund Request
→ Add Request Item
→ Submit Request
→ Division Approval
→ PUB Approval
→ Pembina Approval
→ Create Finance Disbursement
→ Mark Request as Disbursed
→ Confirm Fund Received
→ Submit Settlement
→ Approve Settlement
→ Check Request Detail
→ Check Approval Timeline
→ Check Status History
```

## Expected Final Status

```text
COMPLETED
```

## Permission Test

| Test                                          | Expected         |
| --------------------------------------------- | ---------------- |
| Tanpa token akses endpoint protected          | 401 Unauthorized |
| Token valid tapi permission kurang            | 403 Forbidden    |
| ANGGOTA akses `/api/requests/my`              | 200              |
| ANGGOTA akses `/api/requests`                 | 403              |
| ANGGOTA akses finance disbursement            | 403              |
| BENDAHARA_INTERNAL akses finance disbursement | 201              |
| SUPER_ADMIN akses semua endpoint utama        | 200/201          |

---

# 15. Batasan MVP

Batasan current MVP:

* Upload file fisik belum dibuat.
* Bukti transaksi masih berupa URL.
* Integrasi request-service dan finance-service masih manual.
* Event/message broker belum digunakan.
* Archive module belum dibuat.
* Asset module belum dibuat.
* Cleanliness module belum dibuat.
* English activity module belum dibuat.
* Notification-report-service belum dibuat.
* Frontend belum dibuat.
* Reporting dashboard belum dibuat.

## Kesimpulan

Orchestria memiliki full scope sebagai sistem operasional organisasi PUB.

Current MVP hanya vertical slice pertama:

```text
Auth + Organization + Fund Request + Finance + Gateway
```

Vertical slice ini menjadi fondasi sebelum module lain seperti Archive, Asset, Cleanliness, English Activity, Notification, Reporting, dan Frontend dikembangkan.
