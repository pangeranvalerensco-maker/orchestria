# 06. Database Design Draft

Dokumen ini menjelaskan rancangan database Orchestria berdasarkan current MVP implementation dan full scope roadmap.

## Prinsip Database

Orchestria menggunakan pendekatan database per service.

Prinsip utama:

* Setiap service memiliki database sendiri.
* Tidak ada shared database antar service.
* Tidak ada foreign key lintas database.
* Relasi antar service menggunakan ID referensi.
* Data penting yang berasal dari service lain dapat disimpan sebagai snapshot.
* Setiap service bertanggung jawab atas data domain miliknya sendiri.
* Soft delete digunakan untuk data yang tidak sebaiknya dihapus permanen.
* Audit field digunakan untuk melacak pembuatan dan perubahan data.

## Database List

| Service                                  | Database                            | Status          |
| ---------------------------------------- | ----------------------------------- | --------------- |
| auth-service                             | `orchestria_auth_db`                | Implemented     |
| organization-service                     | `orchestria_organization_db`        | Implemented     |
| request-service                          | `orchestria_request_db`             | Implemented MVP |
| finance-service                          | `orchestria_finance_db`             | Implemented MVP |
| archive-service / archive module         | `orchestria_archive_db`             | Planned         |
| asset-service / asset module             | `orchestria_asset_db`               | Planned         |
| cleanliness-service / cleanliness module | `orchestria_cleanliness_db`         | Planned         |
| activity-service / activity module       | `orchestria_activity_db`            | Planned         |
| notification-report-service              | `orchestria_notification_report_db` | Planned         |

Catatan:

```text id="cmrq17"
Nama database planned masih dapat berubah sesuai keputusan arsitektur berikutnya.
```

---

# 1. Auth Service Database

Database:

```text id="v27fvr"
orchestria_auth_db
```

Status:

```text id="rkvfuj"
Implemented
```

Auth-service menyimpan data user, role, permission, dan relasi authorization.

## 1.1 users

Menyimpan data akun user.

| Column       | Type      | Description                 |
| ------------ | --------- | --------------------------- |
| id           | BIGINT    | Primary key                 |
| email        | VARCHAR   | Email user, unique          |
| password     | VARCHAR   | Password yang sudah di-hash |
| full_name    | VARCHAR   | Nama lengkap                |
| phone_number | VARCHAR   | Nomor HP                    |
| is_active    | BOOLEAN   | Status aktif user           |
| created_at   | TIMESTAMP | Waktu dibuat                |
| updated_at   | TIMESTAMP | Waktu diubah                |

Catatan:

```text id="uw3lzr"
Data organisasi detail seperti divisi dan jabatan tidak disimpan di users.
Data tersebut menjadi tanggung jawab organization-service.
```

## 1.2 roles

Menyimpan role security.

| Column      | Type      | Description    |
| ----------- | --------- | -------------- |
| id          | BIGINT    | Primary key    |
| name        | VARCHAR   | Nama role      |
| description | TEXT      | Deskripsi role |
| created_at  | TIMESTAMP | Waktu dibuat   |
| updated_at  | TIMESTAMP | Waktu diubah   |

Contoh role:

```text id="dvtsbj"
SUPER_ADMIN
PEMBINA
KETUA_PUB
KETUA_DIVISI
SEKRETARIS
BENDAHARA_INTERNAL
BENDAHARA_EKSTERNAL
ANGGOTA
```

Role planned:

```text id="g38pks"
KOORDINATOR
CHECKER
```

## 1.3 permissions

Menyimpan permission sistem.

| Column      | Type      | Description          |
| ----------- | --------- | -------------------- |
| id          | BIGINT    | Primary key          |
| name        | VARCHAR   | Nama permission      |
| description | TEXT      | Deskripsi permission |
| created_at  | TIMESTAMP | Waktu dibuat         |
| updated_at  | TIMESTAMP | Waktu diubah         |

Contoh permission current MVP:

```text id="rag5ad"
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

## 1.4 user_roles

Relasi many-to-many antara user dan role.

| Column  | Type   | Description |
| ------- | ------ | ----------- |
| user_id | BIGINT | ID user     |
| role_id | BIGINT | ID role     |

## 1.5 role_permissions

Relasi many-to-many antara role dan permission.

| Column        | Type   | Description   |
| ------------- | ------ | ------------- |
| role_id       | BIGINT | ID role       |
| permission_id | BIGINT | ID permission |

---

# 2. Organization Service Database

Database:

```text id="vyfqbi"
orchestria_organization_db
```

Status:

```text id="wkl2mw"
Implemented
```

Organization-service menyimpan data struktur organisasi PUB.

## 2.1 organization_periods

Menyimpan periode kepengurusan.

| Column     | Type      | Description          |
| ---------- | --------- | -------------------- |
| id         | BIGINT    | Primary key          |
| name       | VARCHAR   | Nama periode         |
| start_date | DATE      | Tanggal mulai        |
| end_date   | DATE      | Tanggal selesai      |
| is_active  | BOOLEAN   | Apakah periode aktif |
| created_at | TIMESTAMP | Waktu dibuat         |
| updated_at | TIMESTAMP | Waktu diubah         |

Contoh:

```text id="w4zu0n"
PUB Akt 23 Syntax Tahun 2025
```

## 2.2 divisions

Menyimpan data divisi organisasi.

| Column      | Type      | Description      |
| ----------- | --------- | ---------------- |
| id          | BIGINT    | Primary key      |
| name        | VARCHAR   | Nama divisi      |
| description | TEXT      | Deskripsi divisi |
| is_active   | BOOLEAN   | Status aktif     |
| created_at  | TIMESTAMP | Waktu dibuat     |
| updated_at  | TIMESTAMP | Waktu diubah     |

Contoh divisi:

```text id="k7j1mn"
Kesejahteraan
Kebersihan
Bahasa Inggris
HUMAS
Divdik
Sekretaris
Asset
```

## 2.3 positions

Menyimpan jabatan organisasi.

| Column      | Type      | Description       |
| ----------- | --------- | ----------------- |
| id          | BIGINT    | Primary key       |
| name        | VARCHAR   | Nama jabatan      |
| description | TEXT      | Deskripsi jabatan |
| level_order | INTEGER   | Urutan jabatan    |
| is_active   | BOOLEAN   | Status aktif      |
| created_at  | TIMESTAMP | Waktu dibuat      |
| updated_at  | TIMESTAMP | Waktu diubah      |

Contoh position:

```text id="ttdzgv"
Ketua PUB
Wakil Ketua PUB
Sekretaris
Bendahara
Ketua Divisi
Koordinator
Anggota
```

## 2.4 members

Menyimpan data anggota organisasi.

| Column       | Type      | Description                      |
| ------------ | --------- | -------------------------------- |
| id           | BIGINT    | Primary key                      |
| auth_user_id | BIGINT    | Referensi user dari auth-service |
| full_name    | VARCHAR   | Nama lengkap anggota             |
| email        | VARCHAR   | Email anggota                    |
| phone_number | VARCHAR   | Nomor HP                         |
| student_id   | VARCHAR   | NIM atau identifier kampus       |
| is_active    | BOOLEAN   | Status aktif                     |
| created_at   | TIMESTAMP | Waktu dibuat                     |
| updated_at   | TIMESTAMP | Waktu diubah                     |

Catatan:

```text id="ytj0pe"
auth_user_id bukan foreign key lintas database.
auth_user_id hanya ID referensi ke auth-service.
```

## 2.5 member_assignments

Menyimpan penempatan anggota pada periode, divisi, dan jabatan.

| Column      | Type      | Description              |
| ----------- | --------- | ------------------------ |
| id          | BIGINT    | Primary key              |
| member_id   | BIGINT    | ID member                |
| period_id   | BIGINT    | ID periode               |
| division_id | BIGINT    | ID divisi                |
| position_id | BIGINT    | ID jabatan               |
| start_date  | DATE      | Tanggal mulai assignment |
| end_date    | DATE      | Tanggal akhir assignment |
| is_active   | BOOLEAN   | Status aktif assignment  |
| created_at  | TIMESTAMP | Waktu dibuat             |
| updated_at  | TIMESTAMP | Waktu diubah             |

## 2.6 division_tasks

Menyimpan tugas divisi.

| Column               | Type      | Description     |
| -------------------- | --------- | --------------- |
| id                   | BIGINT    | Primary key     |
| division_id          | BIGINT    | ID divisi       |
| title                | VARCHAR   | Judul tugas     |
| description          | TEXT      | Deskripsi tugas |
| due_date             | DATE      | Deadline tugas  |
| status               | VARCHAR   | Status tugas    |
| created_by_member_id | BIGINT    | Pembuat tugas   |
| created_at           | TIMESTAMP | Waktu dibuat    |
| updated_at           | TIMESTAMP | Waktu diubah    |

Contoh status:

```text id="lvhtea"
TODO
IN_PROGRESS
DONE
CANCELLED
```

## 2.7 division_task_evidences

Menyimpan bukti pengerjaan tugas divisi.

| Column                 | Type      | Description     |
| ---------------------- | --------- | --------------- |
| id                     | BIGINT    | Primary key     |
| task_id                | BIGINT    | ID tugas divisi |
| submitted_by_member_id | BIGINT    | Pengirim bukti  |
| evidence_url           | TEXT      | URL bukti       |
| note                   | TEXT      | Catatan         |
| submitted_at           | TIMESTAMP | Waktu submit    |
| created_at             | TIMESTAMP | Waktu dibuat    |
| updated_at             | TIMESTAMP | Waktu diubah    |

---

# 3. Request Service Database

Database:

```text id="e4wstb"
orchestria_request_db
```

Status:

```text id="ze3zb0"
Implemented MVP
```

Request-service menyimpan data pengajuan dana, item, approval, status history, dan settlement.

## 3.1 fund_requests

Menyimpan data utama pengajuan dana.

| Column                 | Type      | Description                                |
| ---------------------- | --------- | ------------------------------------------ |
| id                     | BIGINT    | Primary key                                |
| division_id            | BIGINT    | Referensi divisi dari organization-service |
| division_name          | VARCHAR   | Snapshot nama divisi                       |
| requester_member_id    | BIGINT    | Referensi member dari organization-service |
| requester_name         | VARCHAR   | Snapshot nama pengaju                      |
| requester_auth_user_id | BIGINT    | Referensi user dari auth-service           |
| title                  | VARCHAR   | Judul pengajuan                            |
| description            | TEXT      | Deskripsi pengajuan                        |
| activity_date          | DATE      | Tanggal kegiatan                           |
| priority               | VARCHAR   | Prioritas pengajuan                        |
| status                 | VARCHAR   | Status pengajuan                           |
| total_amount           | DECIMAL   | Total nominal pengajuan                    |
| submitted_at           | TIMESTAMP | Waktu submit                               |
| disbursed_at           | TIMESTAMP | Waktu ditandai cair                        |
| received_at            | TIMESTAMP | Waktu dana diterima                        |
| completed_at           | TIMESTAMP | Waktu selesai                              |
| created_by_email       | VARCHAR   | Email pembuat dari JWT                     |
| updated_by_email       | VARCHAR   | Email pengubah terakhir                    |
| created_at             | TIMESTAMP | Waktu dibuat                               |
| updated_at             | TIMESTAMP | Waktu diubah                               |

Contoh priority:

```text id="i25dit"
LOW
MEDIUM
HIGH
URGENT
```

Contoh status:

```text id="zrmum4"
DRAFT
SUBMITTED
DIVISION_APPROVED
PUB_APPROVED
PEMBINA_APPROVED
READY_FOR_DISBURSEMENT
DISBURSED
FUND_RECEIVED
SETTLEMENT_SUBMITTED
SETTLEMENT_APPROVED
COMPLETED
REVISION_REQUESTED
REJECTED
CANCELLED
```

Flow status utama current MVP:

```text id="g0e8kj"
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

## 3.2 request_items

Menyimpan item kebutuhan pada pengajuan.

| Column          | Type      | Description           |
| --------------- | --------- | --------------------- |
| id              | BIGINT    | Primary key           |
| fund_request_id | BIGINT    | ID pengajuan          |
| item_name       | VARCHAR   | Nama item             |
| description     | TEXT      | Deskripsi item        |
| quantity        | INTEGER   | Jumlah item           |
| unit_price      | DECIMAL   | Harga satuan          |
| subtotal        | DECIMAL   | quantity × unit_price |
| is_active       | BOOLEAN   | Status aktif item     |
| created_at      | TIMESTAMP | Waktu dibuat          |
| updated_at      | TIMESTAMP | Waktu diubah          |

Catatan:

```text id="7bd8hm"
total_amount pada fund_requests dihitung dari subtotal request_items aktif.
```

## 3.3 request_approvals

Menyimpan histori keputusan approval.

| Column          | Type      | Description             |
| --------------- | --------- | ----------------------- |
| id              | BIGINT    | Primary key             |
| fund_request_id | BIGINT    | ID pengajuan            |
| level           | VARCHAR   | Level approval          |
| decision        | VARCHAR   | Keputusan approval      |
| approver_email  | VARCHAR   | Email approver dari JWT |
| approver_name   | VARCHAR   | Nama approver           |
| note            | TEXT      | Catatan approval        |
| decided_at      | TIMESTAMP | Waktu keputusan         |
| created_at      | TIMESTAMP | Waktu dibuat            |

Contoh level:

```text id="sxqap9"
DIVISION
PUB
PEMBINA
```

Contoh decision:

```text id="nx03dj"
APPROVED
REJECTED
REVISION_REQUESTED
```

## 3.4 request_status_histories

Menyimpan histori perubahan status pengajuan.

| Column           | Type      | Description       |
| ---------------- | --------- | ----------------- |
| id               | BIGINT    | Primary key       |
| fund_request_id  | BIGINT    | ID pengajuan      |
| old_status       | VARCHAR   | Status sebelumnya |
| new_status       | VARCHAR   | Status baru       |
| changed_by_email | VARCHAR   | Email pengubah    |
| note             | TEXT      | Catatan perubahan |
| changed_at       | TIMESTAMP | Waktu perubahan   |
| created_at       | TIMESTAMP | Waktu dibuat      |

Fungsi:

* Melacak alur pengajuan.
* Menjadi audit trail dasar.
* Membantu debugging demo.
* Menampilkan timeline status di frontend.

## 3.5 request_settlements

Menyimpan pertanggungjawaban dana.

| Column             | Type      | Description                   |
| ------------------ | --------- | ----------------------------- |
| id                 | BIGINT    | Primary key                   |
| fund_request_id    | BIGINT    | ID pengajuan                  |
| spent_amount       | DECIMAL   | Dana yang benar-benar dipakai |
| remaining_amount   | DECIMAL   | Dana tersisa                  |
| shortage_amount    | DECIMAL   | Dana kurang                   |
| proof_url          | TEXT      | URL bukti/struk               |
| note               | TEXT      | Catatan settlement            |
| submitted_by_email | VARCHAR   | Email pengaju settlement      |
| submitted_at       | TIMESTAMP | Waktu submit settlement       |
| approved_by_email  | VARCHAR   | Email approver settlement     |
| approved_at        | TIMESTAMP | Waktu approval settlement     |
| created_at         | TIMESTAMP | Waktu dibuat                  |
| updated_at         | TIMESTAMP | Waktu diubah                  |

Rumus:

```text id="qqnl30"
Jika spent_amount < total_amount:
remaining_amount = total_amount - spent_amount
shortage_amount = 0

Jika spent_amount > total_amount:
remaining_amount = 0
shortage_amount = spent_amount - total_amount

Jika spent_amount = total_amount:
remaining_amount = 0
shortage_amount = 0
```

---

# 4. Finance Service Database

Database:

```text id="oa9opr"
orchestria_finance_db
```

Status:

```text id="iqjp9o"
Implemented MVP
```

Finance-service menyimpan data pencairan dana.

## 4.1 fund_disbursements

Menyimpan data pencairan dana.

| Column             | Type      | Description                                |
| ------------------ | --------- | ------------------------------------------ |
| id                 | BIGINT    | Primary key                                |
| fund_request_id    | BIGINT    | Referensi pengajuan dari request-service   |
| request_title      | VARCHAR   | Snapshot judul pengajuan                   |
| division_id        | BIGINT    | Referensi divisi dari organization-service |
| division_name      | VARCHAR   | Snapshot nama divisi                       |
| requester_name     | VARCHAR   | Snapshot nama pengaju                      |
| amount             | DECIMAL   | Nominal pencairan                          |
| method             | VARCHAR   | Metode pencairan                           |
| status             | VARCHAR   | Status pencairan                           |
| receiver_name      | VARCHAR   | Nama penerima                              |
| receiver_note      | TEXT      | Catatan penerima                           |
| proof_url          | TEXT      | URL bukti pencairan                        |
| note               | TEXT      | Catatan bendahara                          |
| disbursed_by_email | VARCHAR   | Email bendahara dari JWT                   |
| disbursed_at       | TIMESTAMP | Waktu pencairan                            |
| created_at         | TIMESTAMP | Waktu dibuat                               |
| updated_at         | TIMESTAMP | Waktu diubah                               |

Contoh method:

```text id="eoqlzq"
CASH
BANK_TRANSFER
E_WALLET
```

Contoh status:

```text id="s0c6tu"
DISBURSED
CANCELLED
```

Catatan:

```text id="8j9rj1"
fund_request_id bukan foreign key lintas database.
finance-service hanya menyimpan ID referensi dan snapshot data penting.
```

## 4.2 Planned: fund_returns

Rencana tabel untuk pengembalian dana lebih.

Status:

```text id="ccvvt1"
Planned
```

| Column            | Type      | Description                               |
| ----------------- | --------- | ----------------------------------------- |
| id                | BIGINT    | Primary key                               |
| fund_request_id   | BIGINT    | Referensi pengajuan                       |
| settlement_id     | BIGINT    | Referensi settlement dari request-service |
| amount            | DECIMAL   | Nominal dana yang dikembalikan            |
| method            | VARCHAR   | Metode pengembalian                       |
| proof_url         | TEXT      | Bukti pengembalian                        |
| returned_by_email | VARCHAR   | Email pengembali                          |
| verified_by_email | VARCHAR   | Email verifier                            |
| status            | VARCHAR   | Status pengembalian                       |
| returned_at       | TIMESTAMP | Waktu pengembalian                        |
| verified_at       | TIMESTAMP | Waktu verifikasi                          |
| created_at        | TIMESTAMP | Waktu dibuat                              |
| updated_at        | TIMESTAMP | Waktu diubah                              |

Contoh status:

```text id="ost033"
PENDING
RETURNED
VERIFIED
REJECTED
```

## 4.3 Planned: fund_shortages

Rencana tabel untuk dana kurang.

Status:

```text id="auopjc"
Planned
```

| Column             | Type      | Description          |
| ------------------ | --------- | -------------------- |
| id                 | BIGINT    | Primary key          |
| fund_request_id    | BIGINT    | Referensi pengajuan  |
| settlement_id      | BIGINT    | Referensi settlement |
| amount             | DECIMAL   | Nominal dana kurang  |
| reason             | TEXT      | Alasan dana kurang   |
| status             | VARCHAR   | Status dana kurang   |
| requested_by_email | VARCHAR   | Email pengaju        |
| approved_by_email  | VARCHAR   | Email approver       |
| approved_at        | TIMESTAMP | Waktu approval       |
| created_at         | TIMESTAMP | Waktu dibuat         |
| updated_at         | TIMESTAMP | Waktu diubah         |

Contoh status:

```text id="gcjs22"
PENDING_REVIEW
APPROVED
REJECTED
PAID
```

---

# 5. Archive Database

Database planned:

```text id="h28zn7"
orchestria_archive_db
```

Status:

```text id="q91rhu"
Planned
```

Archive module menyimpan metadata dokumen. File fisik dapat disimpan di local storage, cloud storage, atau object storage pada fase berikutnya.

## 5.1 archive_categories

Menyimpan kategori arsip.

| Column      | Type      | Description        |
| ----------- | --------- | ------------------ |
| id          | BIGINT    | Primary key        |
| name        | VARCHAR   | Nama kategori      |
| description | TEXT      | Deskripsi kategori |
| is_active   | BOOLEAN   | Status aktif       |
| created_at  | TIMESTAMP | Waktu dibuat       |
| updated_at  | TIMESTAMP | Waktu diubah       |

Contoh kategori:

```text id="qnyms8"
Surat
Bukti Pengajuan
Bukti Pencairan
Settlement
Dokumen Divisi
Dokumen Aset
Dokumen Organisasi
```

## 5.2 archive_documents

Menyimpan metadata dokumen.

| Column            | Type      | Description           |
| ----------------- | --------- | --------------------- |
| id                | BIGINT    | Primary key           |
| category_id       | BIGINT    | ID kategori           |
| title             | VARCHAR   | Judul dokumen         |
| description       | TEXT      | Deskripsi dokumen     |
| file_name         | VARCHAR   | Nama file             |
| file_url          | TEXT      | Lokasi file           |
| file_type         | VARCHAR   | Tipe file             |
| file_size         | BIGINT    | Ukuran file           |
| related_domain    | VARCHAR   | Domain terkait        |
| related_entity_id | BIGINT    | ID data terkait       |
| owner_email       | VARCHAR   | Email pemilik dokumen |
| visibility        | VARCHAR   | Visibility dokumen    |
| is_deleted        | BOOLEAN   | Soft delete           |
| created_by_email  | VARCHAR   | Email pembuat         |
| updated_by_email  | VARCHAR   | Email pengubah        |
| created_at        | TIMESTAMP | Waktu dibuat          |
| updated_at        | TIMESTAMP | Waktu diubah          |

Contoh related_domain:

```text id="4b7mvl"
FUND_REQUEST
FINANCE_DISBURSEMENT
SETTLEMENT
ORGANIZATION
ASSET
DIVISION_TASK
```

Contoh visibility:

```text id="ba9bbd"
PRIVATE
DIVISION
MANAGEMENT
PUBLIC
```

## 5.3 archive_access_rules

Rencana tabel untuk aturan akses dokumen.

| Column          | Type      | Description                |
| --------------- | --------- | -------------------------- |
| id              | BIGINT    | Primary key                |
| document_id     | BIGINT    | ID dokumen                 |
| role_name       | VARCHAR   | Role yang boleh mengakses  |
| permission_name | VARCHAR   | Permission yang dibutuhkan |
| created_at      | TIMESTAMP | Waktu dibuat               |

---

# 6. Asset Database

Database planned:

```text id="jvogqm"
orchestria_asset_db
```

Status:

```text id="4j42hj"
Planned
```

Asset module menyimpan data aset dan peminjaman aset.

## 6.1 assets

Menyimpan data aset.

| Column                | Type      | Description           |
| --------------------- | --------- | --------------------- |
| id                    | BIGINT    | Primary key           |
| asset_code            | VARCHAR   | Kode unik aset        |
| asset_name            | VARCHAR   | Nama aset             |
| category              | VARCHAR   | Kategori aset         |
| description           | TEXT      | Deskripsi aset        |
| current_status        | VARCHAR   | Status aset           |
| current_condition     | VARCHAR   | Kondisi aset          |
| location              | VARCHAR   | Lokasi aset           |
| responsible_member_id | BIGINT    | Penanggung jawab aset |
| is_active             | BOOLEAN   | Status aktif          |
| created_at            | TIMESTAMP | Waktu dibuat          |
| updated_at            | TIMESTAMP | Waktu diubah          |

Contoh status:

```text id="a28abe"
AVAILABLE
BORROWED
MAINTENANCE
LOST
INACTIVE
```

Contoh condition:

```text id="txd2xn"
GOOD
MINOR_DAMAGE
DAMAGED
UNKNOWN
```

## 6.2 asset_borrowings

Menyimpan data peminjaman aset.

| Column                   | Type      | Description                        |
| ------------------------ | --------- | ---------------------------------- |
| id                       | BIGINT    | Primary key                        |
| asset_id                 | BIGINT    | ID aset                            |
| borrower_member_id       | BIGINT    | ID member peminjam                 |
| borrower_auth_user_id    | BIGINT    | ID user peminjam dari auth-service |
| borrower_name            | VARCHAR   | Snapshot nama peminjam             |
| purpose                  | TEXT      | Tujuan peminjaman                  |
| borrow_date              | DATE      | Tanggal pinjam                     |
| expected_return_date     | DATE      | Estimasi kembali                   |
| actual_return_date       | DATE      | Tanggal kembali aktual             |
| status                   | VARCHAR   | Status peminjaman                  |
| approved_by_email        | VARCHAR   | Email approver                     |
| handed_over_by_email     | VARCHAR   | Email yang menyerahkan             |
| return_verified_by_email | VARCHAR   | Email verifier pengembalian        |
| condition_before         | TEXT      | Kondisi sebelum dipinjam           |
| condition_after          | TEXT      | Kondisi setelah dikembalikan       |
| note                     | TEXT      | Catatan                            |
| created_at               | TIMESTAMP | Waktu dibuat                       |
| updated_at               | TIMESTAMP | Waktu diubah                       |

Contoh status:

```text id="gwob0f"
REQUESTED
APPROVED
REJECTED
BORROWED
RETURNED
RETURN_VERIFIED
OVERDUE
CANCELLED
```

## 6.3 asset_condition_histories

Menyimpan histori kondisi aset.

| Column           | Type      | Description                |
| ---------------- | --------- | -------------------------- |
| id               | BIGINT    | Primary key                |
| asset_id         | BIGINT    | ID aset                    |
| borrowing_id     | BIGINT    | ID peminjaman jika terkait |
| old_condition    | VARCHAR   | Kondisi sebelumnya         |
| new_condition    | VARCHAR   | Kondisi baru               |
| checked_by_email | VARCHAR   | Email checker              |
| note             | TEXT      | Catatan                    |
| checked_at       | TIMESTAMP | Waktu pengecekan           |
| created_at       | TIMESTAMP | Waktu dibuat               |

---

# 7. Cleanliness Database

Database planned:

```text id="s8qn0x"
orchestria_cleanliness_db
```

Status:

```text id="78jlp0"
Planned
```

Cleanliness module menyimpan jadwal piket, pelaksanaan, poin, dan pelanggaran.

## 7.1 picket_schedules

Menyimpan jadwal piket.

| Column           | Type      | Description           |
| ---------------- | --------- | --------------------- |
| id               | BIGINT    | Primary key           |
| schedule_date    | DATE      | Tanggal piket         |
| division_id      | BIGINT    | ID divisi             |
| member_id        | BIGINT    | ID member             |
| member_name      | VARCHAR   | Snapshot nama anggota |
| role_note        | VARCHAR   | Tugas dalam piket     |
| status           | VARCHAR   | Status jadwal         |
| created_by_email | VARCHAR   | Email pembuat         |
| created_at       | TIMESTAMP | Waktu dibuat          |
| updated_at       | TIMESTAMP | Waktu diubah          |

Contoh status:

```text id="nsv1pr"
SCHEDULED
DONE
MISSED
CANCELLED
```

## 7.2 picket_attendances

Menyimpan pelaksanaan piket.

| Column            | Type      | Description       |
| ----------------- | --------- | ----------------- |
| id                | BIGINT    | Primary key       |
| schedule_id       | BIGINT    | ID jadwal         |
| attendance_status | VARCHAR   | Status kehadiran  |
| evidence_url      | TEXT      | Bukti pelaksanaan |
| note              | TEXT      | Catatan           |
| recorded_by_email | VARCHAR   | Email pencatat    |
| recorded_at       | TIMESTAMP | Waktu pencatatan  |
| created_at        | TIMESTAMP | Waktu dibuat      |

Contoh attendance_status:

```text id="509hgz"
PRESENT
LATE
ABSENT
EXCUSED
```

## 7.3 cleanliness_points

Menyimpan poin kebersihan.

| Column              | Type      | Description           |
| ------------------- | --------- | --------------------- |
| id                  | BIGINT    | Primary key           |
| member_id           | BIGINT    | ID member             |
| division_id         | BIGINT    | ID divisi             |
| point               | INTEGER   | Nilai poin            |
| reason              | TEXT      | Alasan pemberian poin |
| related_schedule_id | BIGINT    | ID jadwal terkait     |
| created_by_email    | VARCHAR   | Email pencatat        |
| created_at          | TIMESTAMP | Waktu dibuat          |

## 7.4 cleanliness_violations

Menyimpan pelanggaran kebersihan.

| Column            | Type      | Description       |
| ----------------- | --------- | ----------------- |
| id                | BIGINT    | Primary key       |
| member_id         | BIGINT    | ID member         |
| division_id       | BIGINT    | ID divisi         |
| violation_type    | VARCHAR   | Jenis pelanggaran |
| note              | TEXT      | Catatan           |
| point_deduction   | INTEGER   | Pengurangan poin  |
| evidence_url      | TEXT      | Bukti             |
| recorded_by_email | VARCHAR   | Email pencatat    |
| recorded_at       | TIMESTAMP | Waktu pencatatan  |
| created_at        | TIMESTAMP | Waktu dibuat      |

---

# 8. Activity Database

Database planned:

```text id="c4a2ji"
orchestria_activity_db
```

Status:

```text id="fzlocl"
Planned / partially covered by organization-service
```

Activity database dapat digunakan jika division activity dan English activity dipisahkan dari organization-service.

## 8.1 division_activities

Menyimpan agenda atau aktivitas divisi.

| Column           | Type      | Description       |
| ---------------- | --------- | ----------------- |
| id               | BIGINT    | Primary key       |
| division_id      | BIGINT    | ID divisi         |
| title            | VARCHAR   | Judul aktivitas   |
| description      | TEXT      | Deskripsi         |
| activity_date    | DATE      | Tanggal aktivitas |
| status           | VARCHAR   | Status aktivitas  |
| created_by_email | VARCHAR   | Email pembuat     |
| created_at       | TIMESTAMP | Waktu dibuat      |
| updated_at       | TIMESTAMP | Waktu diubah      |

## 8.2 division_activity_evidences

Menyimpan bukti aktivitas divisi.

| Column                 | Type      | Description      |
| ---------------------- | --------- | ---------------- |
| id                     | BIGINT    | Primary key      |
| activity_id            | BIGINT    | ID aktivitas     |
| submitted_by_member_id | BIGINT    | ID member        |
| evidence_url           | TEXT      | Bukti aktivitas  |
| note                   | TEXT      | Catatan          |
| verified_by_email      | VARCHAR   | Email verifier   |
| verified_at            | TIMESTAMP | Waktu verifikasi |
| created_at             | TIMESTAMP | Waktu dibuat     |
| updated_at             | TIMESTAMP | Waktu diubah     |

## 8.3 english_deposits

Menyimpan setoran Bahasa Inggris.

| Column            | Type      | Description           |
| ----------------- | --------- | --------------------- |
| id                | BIGINT    | Primary key           |
| member_id         | BIGINT    | ID member             |
| member_name       | VARCHAR   | Snapshot nama anggota |
| schedule_date     | DATE      | Tanggal setoran       |
| topic             | VARCHAR   | Topik/materi          |
| status            | VARCHAR   | Status setoran        |
| score             | DECIMAL   | Nilai jika diperlukan |
| note              | TEXT      | Catatan pembinaan     |
| recorded_by_email | VARCHAR   | Email pencatat        |
| verified_by_email | VARCHAR   | Email verifier        |
| created_at        | TIMESTAMP | Waktu dibuat          |
| updated_at        | TIMESTAMP | Waktu diubah          |

Contoh status:

```text id="3aa6so"
SCHEDULED
SUBMITTED
VERIFIED
MISSED
```

---

# 9. Notification Report Database

Database planned:

```text id="b4qbj9"
orchestria_notification_report_db
```

Status:

```text id="e9xnv9"
Planned
```

Notification-report-service menyimpan notifikasi, scheduler, dan laporan.

## 9.1 notification_templates

Menyimpan template notifikasi.

| Column     | Type      | Description                 |
| ---------- | --------- | --------------------------- |
| id         | BIGINT    | Primary key                 |
| code       | VARCHAR   | Kode template               |
| subject    | VARCHAR   | Subject email               |
| body       | TEXT      | Isi template                |
| channel    | VARCHAR   | EMAIL, IN_APP, atau lainnya |
| is_active  | BOOLEAN   | Status aktif                |
| created_at | TIMESTAMP | Waktu dibuat                |
| updated_at | TIMESTAMP | Waktu diubah                |

## 9.2 notification_requests

Menyimpan request pengiriman notifikasi.

| Column            | Type      | Description       |
| ----------------- | --------- | ----------------- |
| id                | BIGINT    | Primary key       |
| template_code     | VARCHAR   | Kode template     |
| recipient_email   | VARCHAR   | Email penerima    |
| subject           | VARCHAR   | Subject final     |
| body              | TEXT      | Body final        |
| status            | VARCHAR   | Status pengiriman |
| related_domain    | VARCHAR   | Domain terkait    |
| related_entity_id | BIGINT    | ID data terkait   |
| created_at        | TIMESTAMP | Waktu dibuat      |
| sent_at           | TIMESTAMP | Waktu terkirim    |

Contoh status:

```text id="mgw9yh"
PENDING
SENT
FAILED
CANCELLED
```

## 9.3 notification_logs

Menyimpan log pengiriman notifikasi.

| Column                  | Type      | Description             |
| ----------------------- | --------- | ----------------------- |
| id                      | BIGINT    | Primary key             |
| notification_request_id | BIGINT    | ID notification request |
| status                  | VARCHAR   | Status percobaan        |
| error_message           | TEXT      | Pesan error jika gagal  |
| attempted_at            | TIMESTAMP | Waktu percobaan         |
| created_at              | TIMESTAMP | Waktu dibuat            |

## 9.4 scheduled_jobs

Menyimpan konfigurasi scheduler.

| Column          | Type      | Description          |
| --------------- | --------- | -------------------- |
| id              | BIGINT    | Primary key          |
| job_name        | VARCHAR   | Nama job             |
| job_type        | VARCHAR   | Jenis job            |
| cron_expression | VARCHAR   | Cron expression      |
| is_active       | BOOLEAN   | Status aktif         |
| last_run_at     | TIMESTAMP | Waktu terakhir jalan |
| next_run_at     | TIMESTAMP | Waktu berikutnya     |
| created_at      | TIMESTAMP | Waktu dibuat         |
| updated_at      | TIMESTAMP | Waktu diubah         |

## 9.5 job_execution_logs

Menyimpan log eksekusi scheduler.

| Column      | Type      | Description         |
| ----------- | --------- | ------------------- |
| id          | BIGINT    | Primary key         |
| job_id      | BIGINT    | ID scheduler job    |
| status      | VARCHAR   | SUCCESS atau FAILED |
| message     | TEXT      | Pesan eksekusi      |
| started_at  | TIMESTAMP | Waktu mulai         |
| finished_at | TIMESTAMP | Waktu selesai       |
| created_at  | TIMESTAMP | Waktu dibuat        |

## 9.6 report_snapshots

Menyimpan snapshot laporan jika diperlukan.

| Column             | Type      | Description           |
| ------------------ | --------- | --------------------- |
| id                 | BIGINT    | Primary key           |
| report_type        | VARCHAR   | Jenis laporan         |
| period_start       | DATE      | Awal periode laporan  |
| period_end         | DATE      | Akhir periode laporan |
| generated_by_email | VARCHAR   | Email pembuat         |
| data_json          | JSONB     | Data snapshot laporan |
| created_at         | TIMESTAMP | Waktu dibuat          |

## 9.7 report_export_logs

Menyimpan log export laporan.

| Column            | Type      | Description        |
| ----------------- | --------- | ------------------ |
| id                | BIGINT    | Primary key        |
| report_type       | VARCHAR   | Jenis laporan      |
| file_url          | TEXT      | Lokasi file export |
| exported_by_email | VARCHAR   | Email user         |
| exported_at       | TIMESTAMP | Waktu export       |
| created_at        | TIMESTAMP | Waktu dibuat       |

---

# 10. Cross-Service References

Karena setiap service memiliki database sendiri, referensi antar service disimpan sebagai ID dan snapshot.

| Service              | Field                                  | Referensi                               |
| -------------------- | -------------------------------------- | --------------------------------------- |
| organization-service | `members.auth_user_id`                 | `auth-service.users.id`                 |
| request-service      | `fund_requests.division_id`            | `organization-service.divisions.id`     |
| request-service      | `fund_requests.requester_member_id`    | `organization-service.members.id`       |
| request-service      | `fund_requests.requester_auth_user_id` | `auth-service.users.id`                 |
| finance-service      | `fund_disbursements.fund_request_id`   | `request-service.fund_requests.id`      |
| finance-service      | `fund_disbursements.division_id`       | `organization-service.divisions.id`     |
| archive module       | `archive_documents.related_entity_id`  | ID data terkait sesuai `related_domain` |
| asset module         | `asset_borrowings.borrower_member_id`  | `organization-service.members.id`       |
| cleanliness module   | `picket_schedules.member_id`           | `organization-service.members.id`       |
| activity module      | `english_deposits.member_id`           | `organization-service.members.id`       |

Catatan:

```text id="kxkxq2"
Field referensi lintas service tidak menggunakan foreign key database.
Validasi dilakukan melalui service API atau snapshot data.
```

## Snapshot Data

Snapshot digunakan agar histori tetap stabil walaupun data asli berubah.

Contoh:

```text id="c0lwcn"
fund_requests.division_name
fund_requests.requester_name
fund_disbursements.request_title
fund_disbursements.division_name
fund_disbursements.requester_name
asset_borrowings.borrower_name
english_deposits.member_name
```

Manfaat snapshot:

* Histori tetap konsisten.
* Laporan tetap bisa dibaca walaupun nama divisi/member berubah.
* Mengurangi kebutuhan join lintas service.

---

# 11. Audit Fields

Field audit umum yang disarankan:

| Column           | Description                |
| ---------------- | -------------------------- |
| created_at       | Waktu data dibuat          |
| updated_at       | Waktu data terakhir diubah |
| created_by_email | Email pembuat data         |
| updated_by_email | Email pengubah data        |
| is_active        | Status aktif               |
| is_deleted       | Soft delete                |

Untuk workflow:

| Column       | Description              |
| ------------ | ------------------------ |
| submitted_at | Waktu submit             |
| decided_at   | Waktu approval/rejection |
| changed_at   | Waktu perubahan status   |
| disbursed_at | Waktu pencairan          |
| received_at  | Waktu dana diterima      |
| approved_at  | Waktu disetujui          |
| completed_at | Waktu selesai            |

## Soft Delete

Soft delete digunakan untuk data yang sebaiknya tidak hilang permanen.

Contoh:

```text id="s7kbsp"
users.is_active
divisions.is_active
members.is_active
request_items.is_active
archive_documents.is_deleted
assets.is_active
```

---

# 12. Current MVP Database Status

| Database                     | Tables                                                                                                           | Status          |
| ---------------------------- | ---------------------------------------------------------------------------------------------------------------- | --------------- |
| `orchestria_auth_db`         | users, roles, permissions, user_roles, role_permissions                                                          | Implemented     |
| `orchestria_organization_db` | organization_periods, divisions, positions, members, member_assignments, division_tasks, division_task_evidences | Implemented     |
| `orchestria_request_db`      | fund_requests, request_items, request_approvals, request_status_histories, request_settlements                   | Implemented MVP |
| `orchestria_finance_db`      | fund_disbursements                                                                                               | Implemented MVP |

## Planned Database Status

| Database                            | Purpose                             | Status  |
| ----------------------------------- | ----------------------------------- | ------- |
| `orchestria_archive_db`             | Arsip dan dokumen                   | Planned |
| `orchestria_asset_db`               | Aset dan peminjaman                 | Planned |
| `orchestria_cleanliness_db`         | Piket, poin, pelanggaran            | Planned |
| `orchestria_activity_db`            | Aktivitas divisi dan Bahasa Inggris | Planned |
| `orchestria_notification_report_db` | Notifikasi, scheduler, laporan      | Planned |

## Prioritas Database Berikutnya

Prioritas desain database berikutnya:

```text id="kfw9zg"
1. Finalisasi current MVP tables.
2. Pastikan audit field current MVP konsisten.
3. Tentukan next module: Archive atau Asset.
4. Buat schema detail untuk module yang dipilih.
5. Tambahkan migration/DDL jika diperlukan.
6. Update dokumentasi API sesuai module baru.
```
