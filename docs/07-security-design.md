# 07. Security Design

Dokumen ini menjelaskan desain keamanan Orchestria berdasarkan current MVP implementation dan full scope roadmap.

## Tujuan Security

Tujuan security Orchestria:

* Memastikan hanya user terautentikasi yang dapat mengakses endpoint internal.
* Memastikan setiap user hanya dapat menjalankan aksi sesuai role dan permission.
* Melindungi proses penting seperti approval, pencairan dana, settlement, dokumen, aset, piket, aktivitas divisi, dan laporan.
* Mencegah akses data yang bukan milik user.
* Mencegah manipulasi ID user, role, divisi, atau approval level melalui request body.
* Memberikan response security yang jelas melalui `401 Unauthorized` dan `403 Forbidden`.
* Menyiapkan fondasi untuk frontend permission-based UI.
* Menyiapkan fondasi untuk security hardening pada fase deployment.

---

# 1. Security Architecture

Arsitektur keamanan Orchestria menggunakan JWT stateless dan permission-based authorization.

```text id="3s9uza"
Client / Frontend / Postman
        ↓
API Gateway
        ↓
Microservices
        ↓
Database masing-masing service
```

Service yang terlibat:

| Service                     | Security Responsibility                                      |
| --------------------------- | ------------------------------------------------------------ |
| api-gateway                 | Routing, CORS, request logging                               |
| auth-service                | Login, user, role, permission, JWT generation                |
| organization-service        | Validasi JWT dan permission organization                     |
| request-service             | Validasi JWT, permission request, approval, settlement       |
| finance-service             | Validasi JWT dan permission finance                          |
| archive module/service      | Planned security untuk dokumen dan arsip                     |
| asset module/service        | Planned security untuk aset dan peminjaman                   |
| cleanliness module/service  | Planned security untuk jadwal, poin, pelanggaran             |
| activity module/service     | Planned security untuk aktivitas divisi dan English activity |
| notification-report-service | Planned security untuk notifikasi, scheduler, dan laporan    |

Catatan:

```text id="ctmdpu"
API Gateway bukan satu-satunya lapisan keamanan.
Setiap service tetap wajib memvalidasi JWT dan permission.
```

---

# 2. Authentication Flow

Autentikasi dilakukan melalui auth-service.

Alur:

```text id="3o1c63"
User login menggunakan email dan password
→ auth-service memvalidasi credential
→ auth-service membuat JWT
→ client menyimpan token
→ client mengirim token pada request berikutnya
→ service lain memvalidasi token
```

Header yang digunakan:

```http id="ktb5h7"
Authorization: Bearer <TOKEN>
```

JWT bersifat stateless. Service tidak menyimpan session user.

## Token Claims

JWT membawa informasi utama:

```text id="zkw8qb"
subject/email
userId
fullName
roles
permissions
issuedAt
expiration
```

Contoh konsep token payload:

```json id="ai037d"
{
  "sub": "user@example.com",
  "userId": 1,
  "fullName": "Pangeran Valerensco Rivaldi Hutabarat",
  "roles": ["SUPER_ADMIN"],
  "permissions": [
    "request.create",
    "request.read.all",
    "finance.disburse"
  ]
}
```

---

# 3. JWT Validation per Service

Setiap service yang memiliki endpoint protected melakukan validasi JWT.

Langkah validasi:

```text id="60p0jg"
1. Mengambil token dari Authorization header.
2. Memastikan token diawali Bearer.
3. Memvalidasi signature token.
4. Memastikan token belum expired.
5. Mengambil subject/email.
6. Mengambil roles dan permissions.
7. Mengubah roles dan permissions menjadi authorities.
8. Menyimpan authentication ke SecurityContext.
```

Role dikonversi menjadi authority dengan prefix:

```text id="22m00l"
ROLE_SUPER_ADMIN
ROLE_KETUA_PUB
ROLE_ANGGOTA
```

Permission digunakan sebagai authority langsung:

```text id="zrgq8s"
request.create
request.read.own
request.read.all
finance.disburse
finance.settlement.verify
```

---

# 4. Authorization Model

Orchestria menggunakan permission-based authorization.

Role digunakan untuk mengelompokkan user, sedangkan permission digunakan untuk mengamankan endpoint.

Contoh:

| Role               | Contoh Permission                                                      |
| ------------------ | ---------------------------------------------------------------------- |
| SUPER_ADMIN        | Semua permission                                                       |
| KETUA_PUB          | `request.read.all`, `request.approve.pub`, `finance.report.read`       |
| KETUA_DIVISI       | `request.create`, `request.read.own`, `request.approve.division`       |
| PEMBINA            | `request.read.all`, `request.approve.pembina`, `finance.report.read`   |
| BENDAHARA_INTERNAL | `finance.disburse`, `finance.settlement.verify`, `finance.report.read` |
| ANGGOTA            | `request.create`, `request.read.own`                                   |

Contoh penggunaan:

```java id="itj6mq"
@PreAuthorize("hasAuthority('request.create')")
@PostMapping
public ResponseEntity<?> createRequest(...) {
    ...
}
```

---

# 5. Current MVP Permissions

Permission current MVP:

| Permission                  | Fungsi                                   |
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
| `request.read.all`          | Melihat semua pengajuan                  |
| `request.approve.division`  | Approval level Ketua Divisi              |
| `request.approve.pub`       | Approval level Ketua PUB                 |
| `request.approve.pembina`   | Approval level Pembina                   |
| `finance.disburse`          | Melakukan pencairan dana                 |
| `finance.settlement.verify` | Memverifikasi settlement                 |
| `finance.report.read`       | Melihat data/laporan finance             |
| `archive.manage`            | Mengelola arsip dasar                    |
| `notification.manage`       | Mengelola notifikasi dasar               |
| `report.read`               | Melihat laporan umum                     |

---

# 6. Planned Permissions

Permission planned disiapkan untuk module berikutnya.

## Archive & Document

```text id="g4oqo2"
archive.document.create
archive.document.read.own
archive.document.read.all
archive.document.manage
archive.document.delete
archive.document.download
archive.category.manage
```

## Asset

```text id="sy42vl"
asset.read
asset.manage
asset.borrow.create
asset.borrow.read.own
asset.borrow.read.all
asset.borrow.approve
asset.borrow.handover
asset.return.verify
asset.condition.manage
```

## Cleanliness / Picket

```text id="sv1gd7"
cleanliness.schedule.read
cleanliness.schedule.manage
cleanliness.attendance.create
cleanliness.attendance.read
cleanliness.point.manage
cleanliness.violation.manage
cleanliness.report.read
```

## Division Activity

```text id="7e5sz8"
division.activity.read
division.activity.manage
division.task.evidence.create
division.task.evidence.verify
division.activity.report.read
```

## English Activity

```text id="gnrybe"
english.activity.read
english.activity.manage
english.deposit.create
english.deposit.read.own
english.deposit.read.all
english.deposit.verify
english.report.read
```

## Notification & Scheduler

```text id="3hzs1o"
notification.read
notification.manage
notification.template.manage
notification.send
scheduler.read
scheduler.manage
scheduler.run.manual
```

## Reporting

```text id="g5x7u7"
report.read
report.finance.read
report.request.read
report.organization.read
report.asset.read
report.cleanliness.read
report.activity.read
report.export
```

---

# 7. Endpoint Protection Current MVP

## Auth Service

| Endpoint                  | Protection                               |
| ------------------------- | ---------------------------------------- |
| `POST /api/auth/login`    | Public                                   |
| `POST /api/auth/register` | Public atau admin-controlled             |
| `GET /api/auth/me`        | JWT valid                                |
| Admin user endpoints      | `auth.user.read` atau `auth.user.manage` |
| Admin role endpoints      | `auth.role.manage`                       |

## Organization Service

| Endpoint                      | Protection             |
| ----------------------------- | ---------------------- |
| Public organization endpoints | Public, filtered data  |
| Read organization endpoints   | `organization.read`    |
| Manage organization endpoints | `organization.manage`  |
| Division task read            | `division.task.read`   |
| Division task manage          | `division.task.manage` |

## Request Service

| Endpoint                                     | Protection                             |
| -------------------------------------------- | -------------------------------------- |
| `POST /api/requests`                         | `request.create`                       |
| `GET /api/requests`                          | `request.read.all`                     |
| `GET /api/requests/my`                       | `request.read.own`                     |
| `GET /api/requests/{id}`                     | `request.read.all` atau ownership rule |
| `POST /api/requests/{id}/submit`             | `request.create`                       |
| `POST /api/requests/{id}/mark-disbursed`     | `finance.disburse`                     |
| `POST /api/requests/{id}/confirm-received`   | `request.create`                       |
| `POST /api/requests/{id}/settlement`         | `request.create`                       |
| `POST /api/requests/{id}/settlement/approve` | `finance.settlement.verify`            |

## Approval Endpoints

| Level    | Required Permission        |
| -------- | -------------------------- |
| DIVISION | `request.approve.division` |
| PUB      | `request.approve.pub`      |
| PEMBINA  | `request.approve.pembina`  |

Aturan:

```text id="aa42b9"
User hanya boleh approval sesuai permission level-nya.
Ketua Divisi tidak boleh approval level PUB.
Ketua PUB tidak boleh approval level PEMBINA.
Pembina tidak boleh menggantikan approval level lain.
```

## Finance Service

| Endpoint                                                    | Protection                                    |
| ----------------------------------------------------------- | --------------------------------------------- |
| `POST /api/finance/disbursements`                           | `finance.disburse`                            |
| `GET /api/finance/disbursements`                            | `finance.report.read` atau `finance.disburse` |
| `GET /api/finance/disbursements/{id}`                       | `finance.report.read` atau `finance.disburse` |
| `GET /api/finance/disbursements/by-request/{fundRequestId}` | `finance.report.read` atau `finance.disburse` |

---

# 8. 401 vs 403

## 401 Unauthorized

Terjadi ketika user belum berhasil diautentikasi.

Penyebab:

```text id="63m1fd"
Token tidak dikirim.
Header Authorization tidak diawali Bearer.
Token tidak valid.
Token expired.
Signature token salah.
```

Contoh:

```http id="ueth2o"
GET /api/requests
```

Tanpa token.

Expected:

```text id="v14h1a"
401 Unauthorized
```

## 403 Forbidden

Terjadi ketika user sudah login, tetapi tidak memiliki permission.

Penyebab:

```text id="rzo52s"
Token valid.
User berhasil diautentikasi.
Permission tidak cukup.
```

Contoh:

```http id="gjgtqa"
POST /api/finance/disbursements
```

Menggunakan token role `ANGGOTA`.

Expected:

```text id="gjzi5s"
403 Forbidden
```

---

# 9. Ownership Rule

Ownership rule digunakan untuk mencegah user mengakses data yang bukan miliknya.

## Current MVP Ownership

Endpoint:

```http id="sa25wa"
GET /api/requests/my
```

Rule:

```text id="vu8uj8"
createdByEmail == email user login dari JWT
```

Artinya, user dengan permission `request.read.own` hanya melihat pengajuan miliknya sendiri.

Endpoint:

```http id="xxowt4"
GET /api/requests
```

hanya boleh untuk user dengan permission:

```text id="5q82tk"
request.read.all
```

## Planned Ownership Rules

| Domain           | Ownership Rule                                                          |
| ---------------- | ----------------------------------------------------------------------- |
| Request          | User hanya melihat pengajuan milik sendiri                              |
| Archive          | User hanya melihat dokumen milik sendiri atau dokumen yang diberi akses |
| Asset            | User hanya melihat peminjaman aset milik sendiri                        |
| Cleanliness      | User melihat jadwalnya sendiri, pengurus melihat rekap                  |
| English Activity | User melihat setoran milik sendiri                                      |
| Division Task    | User melihat tugas yang terkait dengan dirinya/divisinya                |
| Finance          | User biasa tidak boleh melihat data finance global                      |
| Report           | Report dibatasi berdasarkan role dan permission                         |

---

# 10. IDOR Protection

IDOR terjadi ketika user dapat mengakses atau mengubah data hanya dengan mengganti ID.

Contoh risiko:

```http id="na1hu4"
GET /api/requests/99
```

User mengganti ID untuk melihat pengajuan orang lain.

Mitigasi:

* Jangan percaya ID user dari request body.
* Gunakan email/userId dari JWT.
* Gunakan permission `read.own` dan `read.all`.
* Terapkan ownership check.
* Untuk approval, cek permission level.
* Untuk finance, batasi endpoint dengan permission finance.
* Untuk archive, cek visibility dan access rule.
* Untuk asset, cek borrower atau checker role.
* Untuk report, cek permission report.

Contoh buruk:

```http id="c64ba5"
POST /api/requests/1/approve?approverUserId=5
```

Contoh benar:

```http id="4ugwm4"
POST /api/requests/1/approvals/approve
Authorization: Bearer <TOKEN>
```

Backend mengambil approver dari token, bukan dari parameter bebas.

---

# 11. Business Rule Security

Security tidak hanya soal token dan permission. Backend juga harus mengecek business rule.

## Request Workflow Rule

Status harus berjalan sesuai urutan.

```text id="8lmgl9"
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

Contoh validasi:

| Aksi               | Status yang Diperbolehkan |
| ------------------ | ------------------------- |
| Submit             | DRAFT                     |
| Approval Divisi    | SUBMITTED                 |
| Approval PUB       | DIVISION_APPROVED         |
| Approval Pembina   | PUB_APPROVED              |
| Mark disbursed     | READY_FOR_DISBURSEMENT    |
| Confirm received   | DISBURSED                 |
| Submit settlement  | FUND_RECEIVED             |
| Approve settlement | SETTLEMENT_SUBMITTED      |

Jika status tidak sesuai, backend harus menolak request.

## Asset Rule Planned

Contoh rule:

| Aksi              | Rule                      |
| ----------------- | ------------------------- |
| Borrow asset      | Asset harus AVAILABLE     |
| Approve borrowing | Borrowing harus REQUESTED |
| Handover asset    | Borrowing harus APPROVED  |
| Return asset      | Borrowing harus BORROWED  |
| Verify return     | Borrowing harus RETURNED  |

## Archive Rule Planned

Contoh rule:

| Aksi                  | Rule                                       |
| --------------------- | ------------------------------------------ |
| Read private document | User adalah owner atau memiliki permission |
| Download document     | User memiliki permission download          |
| Delete document       | User memiliki permission delete/manage     |
| Public document       | Tidak boleh membocorkan data internal      |

---

# 12. API Gateway Security

Pada MVP, API Gateway berfungsi sebagai:

* Routing request.
* CORS configuration.
* Request logging.
* Satu pintu akses client.

Gateway routing:

| Path                   | Target               |
| ---------------------- | -------------------- |
| `/api/auth/**`         | auth-service         |
| `/api/organization/**` | organization-service |
| `/api/requests/**`     | request-service      |
| `/api/finance/**`      | finance-service      |

Catatan:

```text id="f7d1ls"
JWT validation tetap dilakukan di masing-masing service.
```

Planned gateway security:

* Rate limiting.
* Centralized CORS policy.
* Request ID / correlation ID.
* Gateway-level authentication filter.
* IP allowlist untuk endpoint internal tertentu.
* Global error response.
* API access logging.

---

# 13. CORS Policy

CORS digunakan agar frontend local dapat mengakses backend gateway.

Allowed origin local:

```text id="mmdq4m"
http://localhost:5173
http://localhost:3000
```

Allowed methods:

```text id="86c24b"
GET
POST
PUT
PATCH
DELETE
OPTIONS
```

Allowed headers:

```text id="a4bqsb"
Authorization
Content-Type
Accept
Origin
```

Production CORS harus diperketat agar hanya domain frontend resmi yang boleh mengakses API.

---

# 14. Frontend Security Strategy

Frontend bukan sumber keamanan utama. Security utama tetap di backend.

Frontend boleh menggunakan role dan permission untuk:

* Menampilkan menu sesuai akses.
* Menyembunyikan tombol yang tidak relevan.
* Mengarahkan user ke halaman yang sesuai.
* Mengurangi error 403.
* Meningkatkan user experience.

Contoh:

| Permission                  | UI Behavior                         |
| --------------------------- | ----------------------------------- |
| `request.create`            | Tampilkan tombol Create Request     |
| `request.read.own`          | Tampilkan menu My Requests          |
| `request.read.all`          | Tampilkan menu All Requests         |
| `request.approve.division`  | Tampilkan tombol Approval Divisi    |
| `request.approve.pub`       | Tampilkan tombol Approval PUB       |
| `request.approve.pembina`   | Tampilkan tombol Approval Pembina   |
| `finance.disburse`          | Tampilkan tombol Disburse           |
| `finance.settlement.verify` | Tampilkan tombol Approve Settlement |
| `archive.document.manage`   | Tampilkan menu Archive Management   |
| `asset.borrow.create`       | Tampilkan tombol Borrow Asset       |
| `asset.return.verify`       | Tampilkan tombol Verify Return      |

Catatan:

```text id="bk66uv"
Menyembunyikan tombol di frontend bukan pengganti authorization backend.
Backend tetap wajib memvalidasi permission.
```

## Token Storage

Untuk local MVP, token dapat disimpan sementara di frontend state atau localStorage.

Risiko localStorage:

```text id="gcvmx5"
Rentan terhadap XSS jika frontend tidak aman.
```

Rencana production improvement:

* Gunakan httpOnly cookie jika arsitektur frontend/backend mendukung.
* Perketat CSP.
* Sanitasi input.
* Hindari menyimpan data sensitif di localStorage.
* Tambahkan refresh token flow.

---

# 15. Secret Management

Secret yang perlu dijaga:

* JWT secret.
* Database username.
* Database password.
* Email SMTP username.
* Email SMTP password.
* Cloud storage credentials.
* API key eksternal.

Aturan:

```text id="ubdzhb"
Jangan commit secret asli ke repository.
Gunakan environment variable untuk production.
Gunakan application-local.properties untuk local development.
```

Contoh environment variable planned:

```text id="llxt2v"
JWT_SECRET
DB_USERNAME
DB_PASSWORD
SMTP_USERNAME
SMTP_PASSWORD
```

---

# 16. Password Security

Aturan password:

* Password tidak boleh disimpan plain text.
* Password harus di-hash.
* Login membandingkan raw password dengan hash.
* Password policy dapat ditambahkan pada fase berikutnya.

Planned password policy:

* Minimal panjang password.
* Kombinasi huruf dan angka.
* Reset password.
* Change password.
* Login attempt limit.
* Account lock sementara jika gagal berulang.

---

# 17. Audit Trail

Audit trail digunakan untuk melacak aksi penting.

Current MVP audit data:

| Data               | Fungsi                     |
| ------------------ | -------------------------- |
| `created_at`       | Waktu data dibuat          |
| `updated_at`       | Waktu data diubah          |
| `created_by_email` | Email pembuat              |
| `updated_by_email` | Email pengubah             |
| `submitted_at`     | Waktu submit               |
| `decided_at`       | Waktu approval/rejection   |
| `changed_at`       | Waktu perubahan status     |
| `disbursed_at`     | Waktu pencairan            |
| `approved_at`      | Waktu settlement disetujui |

Tabel audit current MVP:

```text id="l5fev6"
request_status_histories
request_approvals
```

Planned audit trail:

* Auth audit log.
* Finance audit log.
* Archive access log.
* Asset condition history.
* Scheduler execution log.
* Notification delivery log.
* Report export log.

---

# 18. Public Endpoint Security

Public endpoint digunakan untuk data yang boleh dilihat tanpa login, seperti public organization data.

Aturan public endpoint:

* Jangan menampilkan data sensitif.
* Jangan menampilkan auth_user_id jika tidak perlu.
* Jangan menampilkan phone number private.
* Jangan menampilkan data finance.
* Jangan menampilkan dokumen internal.
* Jangan menampilkan pengajuan internal.
* Batasi field response dengan DTO khusus public.

Contoh public data yang aman:

```text id="slk5qb"
Nama periode aktif
Nama divisi
Nama jabatan publik
Nama anggota yang memang ditampilkan
Struktur organisasi publik
```

---

# 19. Service-to-Service Security

Current MVP belum memiliki service-to-service authentication khusus.

Pola saat ini:

```text id="oeg3r5"
Client/Postman/Frontend memanggil gateway
→ Gateway meneruskan ke service
→ Service memvalidasi JWT user
```

Planned improvement:

* Internal service token.
* API key antar service.
* mTLS pada deployment lanjutan.
* Gateway-only access untuk service internal.
* Private network antar container.
* Event-driven communication dengan trusted broker.

---

# 20. File Security Planned

File upload belum difokuskan pada current MVP. Saat ini bukti masih berupa URL.

Untuk Archive/Document module, perlu aturan:

* Validasi tipe file.
* Validasi ukuran file.
* Scan file jika memungkinkan.
* Gunakan random filename.
* Jangan expose path internal server.
* Gunakan signed URL untuk download jika memakai object storage.
* Simpan metadata file di database.
* Batasi download berdasarkan permission.
* Catat access log untuk dokumen penting.

---

# 21. Reporting Security Planned

Reporting harus dibatasi berdasarkan permission.

Contoh:

| Report              | Permission                 |
| ------------------- | -------------------------- |
| Request report      | `report.request.read`      |
| Finance report      | `report.finance.read`      |
| Organization report | `report.organization.read` |
| Asset report        | `report.asset.read`        |
| Cleanliness report  | `report.cleanliness.read`  |
| Activity report     | `report.activity.read`     |
| Export report       | `report.export`            |

Aturan:

```text id="d0xfpj"
User biasa tidak boleh mengakses laporan global.
Export laporan harus dicatat dalam report_export_logs.
```

---

# 22. Security Testing Checklist

Checklist testing current MVP:

| Test                                                     | Expected                |
| -------------------------------------------------------- | ----------------------- |
| Akses protected endpoint tanpa token                     | 401                     |
| Akses protected endpoint dengan token invalid            | 401                     |
| Akses endpoint dengan token valid tapi permission kurang | 403                     |
| Login berhasil                                           | 200 dan mendapat token  |
| ANGGOTA akses `/api/requests/my`                         | 200                     |
| ANGGOTA akses `/api/requests`                            | 403                     |
| ANGGOTA akses `POST /api/finance/disbursements`          | 403                     |
| BENDAHARA_INTERNAL akses finance disbursement            | 201                     |
| KETUA_DIVISI approval level DIVISION                     | 200                     |
| KETUA_DIVISI approval level PUB                          | 403 atau business error |
| KETUA_PUB approval level PUB                             | 200                     |
| PEMBINA approval level PEMBINA                           | 200                     |
| Approval dengan status tidak sesuai                      | 400 atau 409            |
| Settlement approve oleh user tanpa permission            | 403                     |

Planned checklist:

| Module       | Test                                                         |
| ------------ | ------------------------------------------------------------ |
| Archive      | User tidak bisa download dokumen tanpa akses                 |
| Asset        | User hanya melihat peminjaman miliknya                       |
| Cleanliness  | User hanya melihat jadwal miliknya                           |
| English      | User hanya melihat setoran miliknya                          |
| Report       | User tanpa permission tidak bisa export laporan              |
| Notification | User tanpa permission tidak bisa trigger manual notification |

---

# 23. Current Security Status

| Area                                 | Status                |
| ------------------------------------ | --------------------- |
| JWT authentication                   | Implemented           |
| Roles claim                          | Implemented           |
| Permissions claim                    | Implemented           |
| Stateless validation per service     | Implemented           |
| API Gateway routing                  | Implemented           |
| CORS local                           | Implemented           |
| Permission-based endpoint protection | In progress           |
| Ownership rule request               | In progress           |
| 401/403 testing                      | Planned / in progress |
| Refresh token                        | Planned               |
| Rate limiting                        | Planned               |
| File upload security                 | Planned               |
| Service-to-service security          | Planned               |
| Audit log global                     | Planned               |
| Production secret management         | Planned               |

## Prioritas Security Terdekat

Prioritas security terdekat:

```text id="a3gygr"
1. Pastikan @EnableMethodSecurity aktif di service yang memakai @PreAuthorize.
2. Pastikan semua endpoint penting punya @PreAuthorize.
3. Test 401 dan 403 menggunakan Postman.
4. Test request.read.own vs request.read.all.
5. Test approval permission per level.
6. Test finance.disburse dan finance.settlement.verify.
7. Jangan commit secret asli.
8. Siapkan frontend permission-based menu.
```
