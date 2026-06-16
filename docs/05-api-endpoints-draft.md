# 05. API Endpoints Draft

Dokumen ini menjelaskan endpoint API Orchestria berdasarkan current MVP implementation dan full scope roadmap.

## Base URL

Pada local development, semua request dari client sebaiknya masuk melalui API Gateway.

```text id="0gijsd"
http://localhost:8000
```

Service internal berjalan pada port:

| Service              | Port |
| -------------------- | ---: |
| api-gateway          | 8000 |
| auth-service         | 8001 |
| organization-service | 8002 |
| request-service      | 8003 |
| finance-service      | 8004 |

## Gateway Routing

| Gateway Path            | Target Service                                 | Status      |
| ----------------------- | ---------------------------------------------- | ----------- |
| `/api/auth/**`          | auth-service                                   | Implemented |
| `/api/organization/**`  | organization-service                           | Implemented |
| `/api/requests/**`      | request-service                                | Implemented |
| `/api/finance/**`       | finance-service                                | Implemented |
| `/api/notifications/**` | notification-report-service                    | Planned     |
| `/api/reports/**`       | notification-report-service / reporting module | Planned     |
| `/api/archive/**`       | archive module/service                         | Planned     |
| `/api/assets/**`        | asset module/service                           | Planned     |
| `/api/cleanliness/**`   | cleanliness module/service                     | Planned     |
| `/api/english/**`       | english activity module/service                | Planned     |

---

# 1. Auth Service Endpoints

Base path:

```text id="0ljx37"
/api/auth
```

Status:

```text id="dn5y20"
Implemented
```

## Public Auth Endpoints

| Method | Endpoint             | Deskripsi                      | Permission                   |
| ------ | -------------------- | ------------------------------ | ---------------------------- |
| POST   | `/api/auth/register` | Register user                  | Public atau admin-controlled |
| POST   | `/api/auth/login`    | Login user dan mendapatkan JWT | Public                       |
| GET    | `/api/auth/health`   | Health check auth-service      | Public / internal            |

## Current User Endpoint

| Method | Endpoint       | Deskripsi                            | Permission |
| ------ | -------------- | ------------------------------------ | ---------- |
| GET    | `/api/auth/me` | Mengambil data user login dari token | JWT valid  |

## Admin User Endpoints

| Method | Endpoint                     | Deskripsi                    | Permission         |
| ------ | ---------------------------- | ---------------------------- | ------------------ |
| GET    | `/api/auth/admin/users`      | Melihat daftar user          | `auth.user.read`   |
| GET    | `/api/auth/admin/users/{id}` | Melihat detail user          | `auth.user.read`   |
| POST   | `/api/auth/admin/users`      | Membuat user admin-side      | `auth.user.manage` |
| PUT    | `/api/auth/admin/users/{id}` | Mengubah user                | `auth.user.manage` |
| DELETE | `/api/auth/admin/users/{id}` | Menonaktifkan/menghapus user | `auth.user.manage` |

## Admin Role and Permission Endpoints

| Method | Endpoint                                        | Deskripsi                 | Permission         |
| ------ | ----------------------------------------------- | ------------------------- | ------------------ |
| GET    | `/api/auth/admin/roles`                         | Melihat daftar role       | `auth.role.manage` |
| GET    | `/api/auth/admin/permissions`                   | Melihat daftar permission | `auth.role.manage` |
| POST   | `/api/auth/admin/users/{userId}/roles`          | Assign role ke user       | `auth.role.manage` |
| DELETE | `/api/auth/admin/users/{userId}/roles/{roleId}` | Remove role dari user     | `auth.role.manage` |

Catatan:

```text id="vl1c58"
Endpoint admin auth dapat disesuaikan lagi mengikuti nama controller aktual.
Dokumen ini menjadi acuan API contract MVP.
```

---

# 2. Organization Service Endpoints

Base path:

```text id="ae6nfz"
/api/organization
```

Status:

```text id="eh4upn"
Implemented
```

## Organization Period Endpoints

| Method | Endpoint                         | Deskripsi                     | Permission            |
| ------ | -------------------------------- | ----------------------------- | --------------------- |
| POST   | `/api/organization/periods`      | Membuat periode kepengurusan  | `organization.manage` |
| GET    | `/api/organization/periods`      | Melihat daftar periode        | `organization.read`   |
| GET    | `/api/organization/periods/{id}` | Melihat detail periode        | `organization.read`   |
| PUT    | `/api/organization/periods/{id}` | Mengubah periode              | `organization.manage` |
| DELETE | `/api/organization/periods/{id}` | Menghapus/nonaktifkan periode | `organization.manage` |

## Division Endpoints

| Method | Endpoint                           | Deskripsi                    | Permission            |
| ------ | ---------------------------------- | ---------------------------- | --------------------- |
| POST   | `/api/organization/divisions`      | Membuat divisi               | `organization.manage` |
| GET    | `/api/organization/divisions`      | Melihat daftar divisi        | `organization.read`   |
| GET    | `/api/organization/divisions/{id}` | Melihat detail divisi        | `organization.read`   |
| PUT    | `/api/organization/divisions/{id}` | Mengubah divisi              | `organization.manage` |
| DELETE | `/api/organization/divisions/{id}` | Menghapus/nonaktifkan divisi | `organization.manage` |

## Position Endpoints

| Method | Endpoint                           | Deskripsi                     | Permission            |
| ------ | ---------------------------------- | ----------------------------- | --------------------- |
| POST   | `/api/organization/positions`      | Membuat jabatan organisasi    | `organization.manage` |
| GET    | `/api/organization/positions`      | Melihat daftar jabatan        | `organization.read`   |
| GET    | `/api/organization/positions/{id}` | Melihat detail jabatan        | `organization.read`   |
| PUT    | `/api/organization/positions/{id}` | Mengubah jabatan              | `organization.manage` |
| DELETE | `/api/organization/positions/{id}` | Menghapus/nonaktifkan jabatan | `organization.manage` |

## Member Endpoints

| Method | Endpoint                         | Deskripsi                     | Permission            |
| ------ | -------------------------------- | ----------------------------- | --------------------- |
| POST   | `/api/organization/members`      | Membuat data anggota          | `organization.manage` |
| GET    | `/api/organization/members`      | Melihat daftar anggota        | `organization.read`   |
| GET    | `/api/organization/members/{id}` | Melihat detail anggota        | `organization.read`   |
| PUT    | `/api/organization/members/{id}` | Mengubah data anggota         | `organization.manage` |
| DELETE | `/api/organization/members/{id}` | Menghapus/nonaktifkan anggota | `organization.manage` |

## Member Assignment Endpoints

| Method | Endpoint                                    | Deskripsi                                | Permission            |
| ------ | ------------------------------------------- | ---------------------------------------- | --------------------- |
| POST   | `/api/organization/member-assignments`      | Assign anggota ke periode/divisi/jabatan | `organization.manage` |
| GET    | `/api/organization/member-assignments`      | Melihat daftar assignment                | `organization.read`   |
| GET    | `/api/organization/member-assignments/{id}` | Melihat detail assignment                | `organization.read`   |
| PUT    | `/api/organization/member-assignments/{id}` | Mengubah assignment                      | `organization.manage` |
| DELETE | `/api/organization/member-assignments/{id}` | Menghapus/nonaktifkan assignment         | `organization.manage` |

## Division Task Endpoints

| Method | Endpoint                                | Deskripsi                          | Permission             |
| ------ | --------------------------------------- | ---------------------------------- | ---------------------- |
| POST   | `/api/organization/division-tasks`      | Membuat tugas divisi               | `division.task.manage` |
| GET    | `/api/organization/division-tasks`      | Melihat daftar tugas divisi        | `division.task.read`   |
| GET    | `/api/organization/division-tasks/{id}` | Melihat detail tugas divisi        | `division.task.read`   |
| PUT    | `/api/organization/division-tasks/{id}` | Mengubah tugas divisi              | `division.task.manage` |
| DELETE | `/api/organization/division-tasks/{id}` | Menghapus/nonaktifkan tugas divisi | `division.task.manage` |

## Division Task Evidence Endpoints

| Method | Endpoint                                         | Deskripsi                   | Permission             |
| ------ | ------------------------------------------------ | --------------------------- | ---------------------- |
| POST   | `/api/organization/division-task-evidences`      | Mengirim bukti tugas divisi | `division.task.manage` |
| GET    | `/api/organization/division-task-evidences`      | Melihat bukti tugas divisi  | `division.task.read`   |
| GET    | `/api/organization/division-task-evidences/{id}` | Melihat detail bukti tugas  | `division.task.read`   |

## Public Organization Endpoints

| Method | Endpoint                                  | Deskripsi                          | Permission |
| ------ | ----------------------------------------- | ---------------------------------- | ---------- |
| GET    | `/api/organization/public/structure`      | Melihat struktur organisasi publik | Public     |
| GET    | `/api/organization/public/periods/active` | Melihat periode aktif              | Public     |
| GET    | `/api/organization/public/divisions`      | Melihat divisi publik              | Public     |
| GET    | `/api/organization/public/members`        | Melihat anggota publik             | Public     |

Catatan:

```text id="8r6p95"
Endpoint public hanya boleh menampilkan data yang aman untuk publik.
Data internal seperti auth reference, finance, dan dokumen private tidak boleh ditampilkan.
```

---

# 3. Request Service Endpoints

Base path:

```text id="ys9rw0"
/api/requests
```

Status:

```text id="xphkty"
Implemented MVP
```

## Fund Request Endpoints

| Method | Endpoint                              | Deskripsi                          | Permission                             |
| ------ | ------------------------------------- | ---------------------------------- | -------------------------------------- |
| POST   | `/api/requests`                       | Membuat pengajuan dana             | `request.create`                       |
| GET    | `/api/requests`                       | Melihat semua pengajuan            | `request.read.all`                     |
| GET    | `/api/requests/my`                    | Melihat pengajuan milik user login | `request.read.own`                     |
| GET    | `/api/requests/{id}`                  | Melihat detail pengajuan           | `request.read.all` atau ownership rule |
| POST   | `/api/requests/{id}/submit`           | Submit pengajuan                   | `request.create`                       |
| POST   | `/api/requests/{id}/mark-disbursed`   | Menandai pengajuan sudah dicairkan | `finance.disburse`                     |
| POST   | `/api/requests/{id}/confirm-received` | Konfirmasi dana diterima           | `request.create`                       |

## Request Item Endpoints

| Method | Endpoint                                   | Deskripsi                  | Permission       |
| ------ | ------------------------------------------ | -------------------------- | ---------------- |
| POST   | `/api/requests/{requestId}/items`          | Menambahkan item pengajuan | `request.create` |
| PUT    | `/api/requests/{requestId}/items/{itemId}` | Mengubah item pengajuan    | `request.create` |
| DELETE | `/api/requests/{requestId}/items/{itemId}` | Menghapus/nonaktifkan item | `request.create` |

Catatan:

```text id="b17jgb"
PUT dan DELETE item dapat disesuaikan dengan implementasi aktual.
Jika belum dibuat, endpoint ini masuk planned enhancement untuk request-service.
```

## Approval Endpoints

| Method | Endpoint                                       | Deskripsi                 | Permission            |
| ------ | ---------------------------------------------- | ------------------------- | --------------------- |
| POST   | `/api/requests/{requestId}/approvals/approve`  | Menyetujui pengajuan      | Sesuai level approval |
| POST   | `/api/requests/{requestId}/approvals/reject`   | Menolak pengajuan         | Sesuai level approval |
| POST   | `/api/requests/{requestId}/approvals/revision` | Meminta revisi pengajuan  | Sesuai level approval |
| GET    | `/api/requests/{requestId}/approvals`          | Melihat timeline approval | `request.read.all`    |
| GET    | `/api/requests/{requestId}/histories`          | Melihat histori status    | `request.read.all`    |

Approval permission berdasarkan level:

| Level    | Permission                 |
| -------- | -------------------------- |
| DIVISION | `request.approve.division` |
| PUB      | `request.approve.pub`      |
| PEMBINA  | `request.approve.pembina`  |

## Settlement Endpoints

| Method | Endpoint                                | Deskripsi                    | Permission                             |
| ------ | --------------------------------------- | ---------------------------- | -------------------------------------- |
| POST   | `/api/requests/{id}/settlement`         | Submit settlement            | `request.create`                       |
| POST   | `/api/requests/{id}/settlement/approve` | Approve settlement           | `finance.settlement.verify`            |
| GET    | `/api/requests/{id}/settlement`         | Melihat settlement pengajuan | `request.read.all` atau ownership rule |

## Current MVP Request Flow

```text id="em2dis"
POST /api/requests
→ POST /api/requests/{id}/items
→ POST /api/requests/{id}/submit
→ POST /api/requests/{id}/approvals/approve
→ POST /api/requests/{id}/approvals/approve
→ POST /api/requests/{id}/approvals/approve
→ POST /api/requests/{id}/mark-disbursed
→ POST /api/requests/{id}/confirm-received
→ POST /api/requests/{id}/settlement
→ POST /api/requests/{id}/settlement/approve
```

---

# 4. Finance Service Endpoints

Base path:

```text id="1wsabe"
/api/finance
```

Status:

```text id="a0nbt7"
Implemented MVP
```

## Test Endpoint

| Method | Endpoint               | Deskripsi                                           | Permission |
| ------ | ---------------------- | --------------------------------------------------- | ---------- |
| GET    | `/api/finance/test/me` | Melihat user login dan authority di finance-service | JWT valid  |

## Fund Disbursement Endpoints

| Method | Endpoint                                                | Deskripsi                                  | Permission                                    |
| ------ | ------------------------------------------------------- | ------------------------------------------ | --------------------------------------------- |
| POST   | `/api/finance/disbursements`                            | Membuat data pencairan dana                | `finance.disburse`                            |
| GET    | `/api/finance/disbursements`                            | Melihat daftar pencairan dana              | `finance.report.read` atau `finance.disburse` |
| GET    | `/api/finance/disbursements/{id}`                       | Melihat detail pencairan dana              | `finance.report.read` atau `finance.disburse` |
| GET    | `/api/finance/disbursements/by-request/{fundRequestId}` | Melihat pencairan berdasarkan ID pengajuan | `finance.report.read` atau `finance.disburse` |

## Planned Finance Endpoints

| Method | Endpoint                       | Deskripsi                        | Permission                  |
| ------ | ------------------------------ | -------------------------------- | --------------------------- |
| POST   | `/api/finance/returns`         | Mencatat pengembalian dana lebih | `finance.settlement.verify` |
| GET    | `/api/finance/returns`         | Melihat daftar pengembalian dana | `finance.report.read`       |
| POST   | `/api/finance/shortages`       | Mencatat kebutuhan dana kurang   | `finance.disburse`          |
| GET    | `/api/finance/shortages`       | Melihat daftar dana kurang       | `finance.report.read`       |
| GET    | `/api/finance/reports/summary` | Ringkasan finance                | `finance.report.read`       |

---

# 5. Archive & Document Endpoints

Base path:

```text id="za94f6"
/api/archive
```

Status:

```text id="9oe3w9"
Planned
```

## Document Endpoints

| Method | Endpoint                               | Deskripsi                           | Permission                                      |
| ------ | -------------------------------------- | ----------------------------------- | ----------------------------------------------- |
| POST   | `/api/archive/documents`               | Membuat/mengunggah metadata dokumen | `archive.document.create`                       |
| GET    | `/api/archive/documents`               | Melihat semua dokumen               | `archive.document.read.all`                     |
| GET    | `/api/archive/documents/my`            | Melihat dokumen milik user login    | `archive.document.read.own`                     |
| GET    | `/api/archive/documents/{id}`          | Melihat detail dokumen              | `archive.document.read.all` atau ownership rule |
| PUT    | `/api/archive/documents/{id}`          | Mengubah metadata dokumen           | `archive.document.manage`                       |
| DELETE | `/api/archive/documents/{id}`          | Soft delete dokumen                 | `archive.document.delete`                       |
| GET    | `/api/archive/documents/{id}/download` | Download dokumen                    | `archive.document.download`                     |

## Category Endpoints

| Method | Endpoint                       | Deskripsi                | Permission                  |
| ------ | ------------------------------ | ------------------------ | --------------------------- |
| POST   | `/api/archive/categories`      | Membuat kategori dokumen | `archive.category.manage`   |
| GET    | `/api/archive/categories`      | Melihat kategori dokumen | `archive.document.read.all` |
| PUT    | `/api/archive/categories/{id}` | Mengubah kategori        | `archive.category.manage`   |
| DELETE | `/api/archive/categories/{id}` | Menghapus kategori       | `archive.category.manage`   |

---

# 6. Asset Management Endpoints

Base path:

```text id="t3pyxg"
/api/assets
```

Status:

```text id="ml8lq7"
Planned
```

## Asset Endpoints

| Method | Endpoint           | Deskripsi           | Permission     |
| ------ | ------------------ | ------------------- | -------------- |
| POST   | `/api/assets`      | Membuat data aset   | `asset.manage` |
| GET    | `/api/assets`      | Melihat daftar aset | `asset.read`   |
| GET    | `/api/assets/{id}` | Melihat detail aset | `asset.read`   |
| PUT    | `/api/assets/{id}` | Mengubah data aset  | `asset.manage` |
| DELETE | `/api/assets/{id}` | Menonaktifkan aset  | `asset.manage` |

## Asset Borrowing Endpoints

| Method | Endpoint                                    | Deskripsi                             | Permission                                  |
| ------ | ------------------------------------------- | ------------------------------------- | ------------------------------------------- |
| POST   | `/api/assets/borrowings`                    | Mengajukan peminjaman aset            | `asset.borrow.create`                       |
| GET    | `/api/assets/borrowings`                    | Melihat semua peminjaman aset         | `asset.borrow.read.all`                     |
| GET    | `/api/assets/borrowings/my`                 | Melihat peminjaman milik user login   | `asset.borrow.read.own`                     |
| GET    | `/api/assets/borrowings/{id}`               | Melihat detail peminjaman             | `asset.borrow.read.all` atau ownership rule |
| POST   | `/api/assets/borrowings/{id}/approve`       | Menyetujui peminjaman                 | `asset.borrow.approve`                      |
| POST   | `/api/assets/borrowings/{id}/reject`        | Menolak peminjaman                    | `asset.borrow.approve`                      |
| POST   | `/api/assets/borrowings/{id}/handover`      | Mencatat penyerahan aset              | `asset.borrow.handover`                     |
| POST   | `/api/assets/borrowings/{id}/return`        | Mengajukan/mencatat pengembalian aset | `asset.borrow.create`                       |
| POST   | `/api/assets/borrowings/{id}/verify-return` | Verifikasi pengembalian aset          | `asset.return.verify`                       |

---

# 7. Cleanliness / Picket Endpoints

Base path:

```text id="219hla"
/api/cleanliness
```

Status:

```text id="7ucxcc"
Planned
```

## Schedule Endpoints

| Method | Endpoint                          | Deskripsi                 | Permission                    |
| ------ | --------------------------------- | ------------------------- | ----------------------------- |
| POST   | `/api/cleanliness/schedules`      | Membuat jadwal piket      | `cleanliness.schedule.manage` |
| GET    | `/api/cleanliness/schedules`      | Melihat jadwal piket      | `cleanliness.schedule.read`   |
| GET    | `/api/cleanliness/schedules/my`   | Melihat jadwal user login | `cleanliness.schedule.read`   |
| PUT    | `/api/cleanliness/schedules/{id}` | Mengubah jadwal piket     | `cleanliness.schedule.manage` |
| DELETE | `/api/cleanliness/schedules/{id}` | Menghapus jadwal piket    | `cleanliness.schedule.manage` |

## Attendance and Point Endpoints

| Method | Endpoint                           | Deskripsi                      | Permission                      |
| ------ | ---------------------------------- | ------------------------------ | ------------------------------- |
| POST   | `/api/cleanliness/attendances`     | Mencatat pelaksanaan piket     | `cleanliness.attendance.create` |
| GET    | `/api/cleanliness/attendances`     | Melihat data pelaksanaan piket | `cleanliness.attendance.read`   |
| POST   | `/api/cleanliness/points`          | Mencatat poin kebersihan       | `cleanliness.point.manage`      |
| POST   | `/api/cleanliness/violations`      | Mencatat pelanggaran           | `cleanliness.violation.manage`  |
| GET    | `/api/cleanliness/reports/summary` | Melihat ringkasan kebersihan   | `cleanliness.report.read`       |

---

# 8. Division Activity Endpoints

Base path:

```text id="k3owtf"
/api/division-activities
```

Status:

```text id="0c0sek"
Partially implemented / Planned enhancement
```

Catatan:

```text id="qkeuhi"
Sebagian fitur tugas divisi saat ini berada di organization-service.
Endpoint di bawah adalah rencana jika aktivitas divisi dipisahkan menjadi module/service khusus.
```

## Activity Endpoints

| Method | Endpoint                        | Deskripsi                       | Permission                 |
| ------ | ------------------------------- | ------------------------------- | -------------------------- |
| POST   | `/api/division-activities`      | Membuat agenda/aktivitas divisi | `division.activity.manage` |
| GET    | `/api/division-activities`      | Melihat aktivitas divisi        | `division.activity.read`   |
| GET    | `/api/division-activities/{id}` | Melihat detail aktivitas        | `division.activity.read`   |
| PUT    | `/api/division-activities/{id}` | Mengubah aktivitas              | `division.activity.manage` |
| DELETE | `/api/division-activities/{id}` | Menghapus aktivitas             | `division.activity.manage` |

## Task Evidence Endpoints

| Method | Endpoint                                            | Deskripsi                        | Permission                      |
| ------ | --------------------------------------------------- | -------------------------------- | ------------------------------- |
| POST   | `/api/division-activities/tasks/{taskId}/evidences` | Mengirim bukti tugas             | `division.task.evidence.create` |
| POST   | `/api/division-activities/evidences/{id}/verify`    | Memverifikasi bukti tugas        | `division.task.evidence.verify` |
| GET    | `/api/division-activities/reports/summary`          | Melihat laporan aktivitas divisi | `division.activity.report.read` |

---

# 9. English Activity Endpoints

Base path:

```text id="0qcn8o"
/api/english
```

Status:

```text id="teq4rj"
Planned
```

## English Deposit Endpoints

| Method | Endpoint                            | Deskripsi                        | Permission                                     |
| ------ | ----------------------------------- | -------------------------------- | ---------------------------------------------- |
| POST   | `/api/english/deposits`             | Mencatat setoran Bahasa Inggris  | `english.deposit.create`                       |
| GET    | `/api/english/deposits`             | Melihat semua setoran            | `english.deposit.read.all`                     |
| GET    | `/api/english/deposits/my`          | Melihat setoran milik user login | `english.deposit.read.own`                     |
| GET    | `/api/english/deposits/{id}`        | Melihat detail setoran           | `english.deposit.read.all` atau ownership rule |
| POST   | `/api/english/deposits/{id}/verify` | Memverifikasi setoran            | `english.deposit.verify`                       |
| GET    | `/api/english/reports/summary`      | Melihat laporan Bahasa Inggris   | `english.report.read`                          |

---

# 10. Notification and Scheduler Endpoints

Base path:

```text id="res37w"
/api/notifications
```

Status:

```text id="tkpyrf"
Planned
```

## Notification Endpoints

| Method | Endpoint                            | Deskripsi                    | Permission                     |
| ------ | ----------------------------------- | ---------------------------- | ------------------------------ |
| POST   | `/api/notifications/send`           | Mengirim notifikasi manual   | `notification.send`            |
| GET    | `/api/notifications`                | Melihat daftar notifikasi    | `notification.read`            |
| GET    | `/api/notifications/logs`           | Melihat log pengiriman       | `notification.manage`          |
| POST   | `/api/notifications/templates`      | Membuat template notifikasi  | `notification.template.manage` |
| GET    | `/api/notifications/templates`      | Melihat template notifikasi  | `notification.read`            |
| PUT    | `/api/notifications/templates/{id}` | Mengubah template notifikasi | `notification.template.manage` |

## Scheduler Endpoints

Base path:

```text id="wmljke"
/api/schedulers
```

| Method | Endpoint                         | Deskripsi                    | Permission             |
| ------ | -------------------------------- | ---------------------------- | ---------------------- |
| GET    | `/api/schedulers/jobs`           | Melihat daftar scheduler job | `scheduler.read`       |
| POST   | `/api/schedulers/jobs`           | Membuat scheduler job        | `scheduler.manage`     |
| PUT    | `/api/schedulers/jobs/{id}`      | Mengubah scheduler job       | `scheduler.manage`     |
| POST   | `/api/schedulers/jobs/{id}/run`  | Menjalankan job manual       | `scheduler.run.manual` |
| GET    | `/api/schedulers/jobs/{id}/logs` | Melihat log eksekusi job     | `scheduler.read`       |

---

# 11. Reporting Endpoints

Base path:

```text id="2g57f6"
/api/reports
```

Status:

```text id="6cw2u1"
Planned
```

## Report Endpoints

| Method | Endpoint                    | Deskripsi                | Permission                 |
| ------ | --------------------------- | ------------------------ | -------------------------- |
| GET    | `/api/reports/summary`      | Ringkasan dashboard umum | `report.read`              |
| GET    | `/api/reports/requests`     | Laporan pengajuan        | `report.request.read`      |
| GET    | `/api/reports/finance`      | Laporan finance          | `report.finance.read`      |
| GET    | `/api/reports/organization` | Laporan organisasi       | `report.organization.read` |
| GET    | `/api/reports/assets`       | Laporan aset             | `report.asset.read`        |
| GET    | `/api/reports/cleanliness`  | Laporan kebersihan       | `report.cleanliness.read`  |
| GET    | `/api/reports/activities`   | Laporan aktivitas divisi | `report.activity.read`     |
| GET    | `/api/reports/export`       | Export laporan           | `report.export`            |

---

# 12. Standard API Response

Format response sukses yang disarankan:

```json id="oxip85"
{
  "success": true,
  "message": "Success",
  "data": {},
  "timestamp": "2026-06-16T12:00:00"
}
```

Format response error yang disarankan:

```json id="h3zxiv"
{
  "success": false,
  "message": "Resource not found",
  "error": "NOT_FOUND",
  "timestamp": "2026-06-16T12:00:00",
  "path": "/api/requests/99"
}
```

## HTTP Status

| Status                    | Makna                              |
| ------------------------- | ---------------------------------- |
| 200 OK                    | Request berhasil                   |
| 201 Created               | Data berhasil dibuat               |
| 400 Bad Request           | Request body/parameter tidak valid |
| 401 Unauthorized          | Token tidak ada/tidak valid        |
| 403 Forbidden             | Permission tidak cukup             |
| 404 Not Found             | Data tidak ditemukan               |
| 409 Conflict              | Konflik state/business rule        |
| 500 Internal Server Error | Error server                       |

---

# 13. Current MVP Demo Endpoint Order

Urutan endpoint untuk demo MVP:

```text id="s3x033"
POST /api/auth/login
POST /api/requests
POST /api/requests/{id}/items
POST /api/requests/{id}/submit
POST /api/requests/{id}/approvals/approve
POST /api/requests/{id}/approvals/approve
POST /api/requests/{id}/approvals/approve
POST /api/finance/disbursements
POST /api/requests/{id}/mark-disbursed
POST /api/requests/{id}/confirm-received
POST /api/requests/{id}/settlement
POST /api/requests/{id}/settlement/approve
GET /api/requests/{id}
GET /api/requests/{id}/approvals
GET /api/requests/{id}/histories
```

## Current MVP Test Focus

| Test                                | Expected               |
| ----------------------------------- | ---------------------- |
| Login berhasil                      | 200 dan mendapat token |
| Create request dengan token valid   | 201                    |
| Add item                            | 201                    |
| Submit request                      | 200                    |
| Approval sesuai level               | 200                    |
| Finance disbursement oleh bendahara | 201                    |
| Confirm received                    | 200                    |
| Submit settlement                   | 200                    |
| Approve settlement                  | 200                    |
| Final status                        | `COMPLETED`            |
| Tanpa token                         | 401                    |
| Permission kurang                   | 403                    |

## Catatan

Dokumen ini adalah API contract draft. Endpoint implemented harus mengikuti kode aktual. Endpoint planned digunakan sebagai acuan pengembangan module berikutnya.
