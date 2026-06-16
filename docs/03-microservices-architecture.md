# 03. Microservices Architecture

Dokumen ini menjelaskan arsitektur microservices Orchestria berdasarkan full scope project dan implementasi backend MVP saat ini.

## Prinsip Arsitektur

Orchestria dirancang menggunakan pendekatan microservices agar setiap domain bisnis memiliki batas tanggung jawab yang jelas.

Prinsip utama:

* Setiap service memiliki tanggung jawab domain sendiri.
* Setiap service memiliki database sendiri.
* Relasi antar service menggunakan ID referensi, bukan foreign key lintas database.
* Authentication menggunakan JWT stateless.
* Authorization menggunakan role dan permission dari token.
* API Gateway menjadi satu pintu akses dari client.
* Integrasi awal antar service menggunakan REST API.
* Event/message broker dapat ditambahkan pada fase berikutnya.

## High Level Architecture

```text
Client / Frontend / Postman
        ↓
API Gateway
        ↓
+----------------------+----------------------+----------------------+
| auth-service         | organization-service | request-service      |
| user, role, JWT      | struktur organisasi  | pengajuan, approval  |
+----------------------+----------------------+----------------------+
        ↓                      ↓                      ↓
 auth database        organization database     request database

+----------------------+------------------------------+
| finance-service      | notification-report-service   |
| pencairan dana       | notification, scheduler, report|
+----------------------+------------------------------+
        ↓                              ↓
 finance database              planned database
```

## Service List

| Service                     | Port | Database                     | Status          |
| --------------------------- | ---: | ---------------------------- | --------------- |
| api-gateway                 | 8000 | -                            | Implemented     |
| auth-service                | 8001 | `orchestria_auth_db`         | Implemented     |
| organization-service        | 8002 | `orchestria_organization_db` | Implemented     |
| request-service             | 8003 | `orchestria_request_db`      | Implemented MVP |
| finance-service             | 8004 | `orchestria_finance_db`      | Implemented MVP |
| notification-report-service |    - | -                            | Planned         |

## API Gateway

API Gateway menjadi satu pintu akses backend.

Tanggung jawab utama:

* Routing request ke service internal.
* Menyatukan base URL client.
* CORS untuk local frontend development.
* Request logging.
* Menyederhanakan akses dari frontend atau Postman.

Base URL gateway:

```text
http://localhost:8000
```

Routing utama:

| Gateway Path           | Target Service          |
| ---------------------- | ----------------------- |
| `/api/auth/**`         | `http://localhost:8001` |
| `/api/organization/**` | `http://localhost:8002` |
| `/api/requests/**`     | `http://localhost:8003` |
| `/api/finance/**`      | `http://localhost:8004` |

Catatan:

```text
Pada MVP saat ini, validasi JWT tetap dilakukan oleh masing-masing service.
Gateway belum menjadi satu-satunya authentication layer.
```

## Auth Service

Port:

```text
8001
```

Database:

```text
orchestria_auth_db
```

Tanggung jawab:

* Login.
* Register.
* User management.
* Role management.
* Permission management.
* JWT generation.
* JWT validation.
* Current user endpoint.
* Auth admin endpoints.

Data ownership:

```text
users
roles
permissions
user_roles
role_permissions
```

Auth-service adalah pemilik data user, role, dan permission.

Service lain tidak boleh langsung mengakses database auth-service.

## Organization Service

Port:

```text
8002
```

Database:

```text
orchestria_organization_db
```

Tanggung jawab:

* Periode kepengurusan.
* Divisi organisasi.
* Jabatan atau position organisasi.
* Data anggota.
* Assignment anggota ke periode, divisi, dan jabatan.
* Tugas divisi.
* Bukti tugas divisi.
* Public organization API untuk web PUB.

Data ownership:

```text
organization_periods
divisions
positions
members
member_assignments
division_tasks
division_task_evidences
```

Catatan penting:

```text
Organization-service tidak menyimpan data role dan permission security.
Role dan permission tetap milik auth-service.
```

Position organisasi seperti Ketua PUB, Sekretaris, Bendahara, Koordinator Divisi, dan Anggota disimpan sebagai data organisasi, bukan security role.

## Request Service

Port:

```text
8003
```

Database:

```text
orchestria_request_db
```

Tanggung jawab MVP:

* Membuat pengajuan dana.
* Menambahkan item pengajuan.
* Submit pengajuan.
* Approval bertingkat.
* Reject pengajuan.
* Request revision.
* Status history.
* Approval timeline.
* Menandai dana sudah dicairkan.
* Konfirmasi dana diterima.
* Submit settlement.
* Approve settlement.
* My requests endpoint.

Data ownership:

```text
fund_requests
request_items
request_approvals
request_status_histories
request_settlements
```

Flow utama:

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

Request-service tidak menyimpan data user secara penuh. Identitas user login diambil dari JWT.

## Finance Service

Port:

```text
8004
```

Database:

```text
orchestria_finance_db
```

Tanggung jawab MVP:

* Mencatat pencairan dana.
* Melihat daftar pencairan dana.
* Melihat detail pencairan dana.
* Melihat pencairan berdasarkan ID pengajuan.
* Membatasi akses berdasarkan permission finance.

Data ownership:

```text
fund_disbursements
```

Finance-service menyimpan referensi ke pengajuan melalui:

```text
fund_request_id
```

Catatan:

```text
fund_request_id bukan foreign key lintas database.
Itu hanya ID referensi ke request-service.
```

## Notification Report Service

Status:

```text
Planned
```

Tanggung jawab rencana:

* Email notification.
* Reminder approval.
* Reminder settlement.
* Reminder tugas divisi.
* Reminder pengembalian aset.
* Scheduler berkala.
* Log pengiriman notifikasi.
* Reporting dashboard.
* Export PDF.
* Export Excel.

Kemungkinan data ownership:

```text
notification_templates
notification_requests
notification_logs
scheduled_jobs
job_execution_logs
report_snapshots
report_export_logs
```

Service ini belum diimplementasikan pada MVP saat ini.

## Planned Service / Module Expansion

Selain service utama yang sudah berjalan, Orchestria memiliki beberapa module/domain lanjutan.

### Archive & Document Module

Tanggung jawab:

* Arsip surat.
* Arsip dokumen organisasi.
* Arsip bukti approval.
* Arsip bukti pencairan.
* Arsip bukti settlement.
* Metadata dokumen.
* Hak akses dokumen.
* Download dokumen.

Kemungkinan implementasi:

```text
archive-service
```

atau sebagai module awal di service yang sudah ada, tergantung kompleksitas.

### Asset Management Module

Tanggung jawab:

* Data aset/laptop.
* Status aset.
* Peminjaman aset.
* Pengembalian aset.
* Checker aset.
* Kondisi aset sebelum dipinjam.
* Kondisi aset setelah dikembalikan.
* Histori peminjaman.

Kemungkinan implementasi:

```text
asset-service
```

atau module dalam service operasional baru.

### Cleanliness / Picket Module

Tanggung jawab:

* Jadwal piket.
* Kehadiran piket.
* Poin kebersihan.
* Pelanggaran.
* Rekap anggota.
* Rekap divisi.
* Laporan kebersihan.

Kemungkinan implementasi:

```text
cleanliness-service
```

atau module dalam service operasional.

### English Activity Module

Tanggung jawab:

* Setoran Bahasa Inggris.
* Jadwal setoran.
* Progress anggota.
* Rekap aktivitas.
* Catatan pembinaan.

Kemungkinan implementasi:

```text
activity-service
```

atau module dalam organization-service jika scope masih sederhana.

### Reporting Dashboard

Tanggung jawab:

* Laporan pengajuan.
* Laporan approval.
* Laporan pencairan.
* Laporan settlement.
* Laporan aset.
* Laporan kebersihan.
* Laporan aktivitas divisi.

Kemungkinan pendekatan:

```text
1. Query agregasi via API antar service.
2. Read model khusus reporting.
3. Event-driven projection pada fase lanjut.
```

## Database per Service

Setiap service memiliki database sendiri.

| Service                     | Database                     |
| --------------------------- | ---------------------------- |
| auth-service                | `orchestria_auth_db`         |
| organization-service        | `orchestria_organization_db` |
| request-service             | `orchestria_request_db`      |
| finance-service             | `orchestria_finance_db`      |
| notification-report-service | Planned                      |

Prinsip:

```text
Tidak ada shared database.
Tidak ada foreign key lintas database.
Service lain hanya menyimpan ID referensi.
```

Contoh referensi lintas service:

| Field                                  | Referensi                           |
| -------------------------------------- | ----------------------------------- |
| `members.auth_user_id`                 | ID user dari auth-service           |
| `fund_requests.division_id`            | ID divisi dari organization-service |
| `fund_requests.requester_member_id`    | ID member dari organization-service |
| `fund_requests.requester_auth_user_id` | ID user dari auth-service           |
| `fund_disbursements.fund_request_id`   | ID pengajuan dari request-service   |

## Authentication Architecture

Authentication berpusat pada auth-service.

Alur:

```text
1. User login ke auth-service.
2. Auth-service memvalidasi email dan password.
3. Auth-service membuat JWT.
4. Client mengirim JWT pada request berikutnya.
5. Service lain memvalidasi JWT menggunakan secret yang sama.
6. Service lain mengambil email, roles, dan permissions dari JWT.
```

Header:

```http
Authorization: Bearer <TOKEN>
```

## Authorization Architecture

Authorization menggunakan permission dari JWT.

Role dikonversi menjadi authority dengan prefix:

```text
ROLE_SUPER_ADMIN
ROLE_KETUA_PUB
ROLE_ANGGOTA
```

Permission digunakan sebagai authority langsung:

```text
request.create
request.read.own
request.read.all
request.approve.division
request.approve.pub
request.approve.pembina
finance.disburse
finance.settlement.verify
finance.report.read
```

Contoh endpoint protection:

```java
@PreAuthorize("hasAuthority('request.create')")
```

## Communication Pattern

### Current MVP

Pada MVP saat ini, komunikasi antar service menggunakan REST API dan flow manual melalui client/Postman/frontend.

Contoh:

```text
1. Finance-service mencatat pencairan dana.
2. Request-service kemudian dipanggil untuk mark request as disbursed.
```

Catatan:

```text
Belum ada event/message broker pada MVP saat ini.
```

### Planned Communication

Pada fase berikutnya, beberapa proses dapat menggunakan event-driven communication.

Contoh event:

```text
RequestSubmitted
DivisionApproved
PubApproved
PembinaApproved
RequestRejected
RequestRevisionRequested
DisbursementCreated
FundReceived
SettlementSubmitted
SettlementApproved
AssetBorrowed
AssetReturned
PicketCompleted
NotificationRequested
ReportSnapshotRequested
```

Event-driven communication cocok untuk:

* notifikasi;
* reporting;
* audit log global;
* sinkronisasi status lanjutan;
* reminder scheduler.

## Security Boundary

Setiap service bertanggung jawab mengamankan endpoint-nya sendiri.

| Service                     | Security Responsibility                                 |
| --------------------------- | ------------------------------------------------------- |
| auth-service                | Membuat JWT, mengelola role dan permission              |
| organization-service        | Validasi JWT untuk endpoint organisasi                  |
| request-service             | Validasi JWT dan permission request/approval/settlement |
| finance-service             | Validasi JWT dan permission finance                     |
| api-gateway                 | Routing, CORS, logging                                  |
| notification-report-service | Planned security                                        |

Catatan:

```text
Meskipun request masuk melalui API Gateway, setiap service tetap harus memvalidasi JWT.
```

## API Gateway vs Service Security

API Gateway:

* menyatukan akses;
* melakukan routing;
* mengatur CORS;
* mencatat request log.

Service:

* memvalidasi JWT;
* membaca user dari token;
* membaca roles dan permissions dari token;
* menjalankan business validation;
* menjalankan permission validation.

Dengan pola ini, jika service diakses langsung saat development, security tetap berjalan.

## Deployment View Saat Ini

Local development:

```text
api-gateway          : localhost:8000
auth-service         : localhost:8001
organization-service : localhost:8002
request-service      : localhost:8003
finance-service      : localhost:8004
```

Database local:

```text
PostgreSQL
├── orchestria_auth_db
├── orchestria_organization_db
├── orchestria_request_db
└── orchestria_finance_db
```

## Deployment View Rencana

Pada fase deployment, service dapat dijalankan menggunakan Docker Compose.

Rencana komponen:

```text
api-gateway
auth-service
organization-service
request-service
finance-service
notification-report-service
frontend
postgres-auth
postgres-organization
postgres-request
postgres-finance
redis
message-broker
```

Komponen opsional:

```text
Redis untuk rate limiting atau cache.
RabbitMQ/Kafka untuk event-driven communication.
Object storage untuk file upload.
```

## Frontend Integration

Frontend React akan mengakses backend melalui API Gateway.

Base URL frontend:

```text
VITE_API_BASE_URL=http://localhost:8000
```

Frontend tidak perlu mengetahui port masing-masing service.

Contoh:

```text
Frontend login:
POST /api/auth/login

Frontend request list:
GET /api/requests/my

Frontend finance:
GET /api/finance/disbursements
```

## Architecture Decision

### Mengapa Microservices?

Alasan:

* Domain PUB cukup banyak dan beragam.
* Auth, organization, request, finance, asset, archive, dan reporting memiliki tanggung jawab berbeda.
* Setiap service dapat dikembangkan bertahap.
* Cocok untuk memenuhi kebutuhan UAS Java Lanjutan yang mensyaratkan minimal beberapa microservices.
* Cocok untuk portofolio backend.

### Risiko Microservices

Risiko:

* Setup lebih kompleks.
* Database lebih banyak.
* Integrasi antar service perlu desain yang jelas.
* Debugging lebih sulit dibanding monolith.
* Perlu dokumentasi dan testing flow yang rapi.

Mitigasi:

* Mulai dari vertical slice kecil.
* Gunakan API Gateway.
* Gunakan Postman flow.
* Gunakan dokumentasi service boundary.
* Jangan membuat semua module sekaligus.
* Tambahkan event-driven communication setelah MVP stabil.

## Current Architecture Status

| Area                           | Status          |
| ------------------------------ | --------------- |
| Multi-service backend          | Implemented     |
| Database per service           | Implemented     |
| API Gateway                    | Implemented     |
| JWT stateless                  | Implemented     |
| Permission-based authorization | In progress     |
| Request/finance vertical slice | Implemented MVP |
| Frontend                       | Planned         |
| Notification service           | Planned         |
| Reporting                      | Planned         |
| Event/message broker           | Planned         |
| Docker/deployment              | Planned         |

## Next Architecture Priorities

Prioritas arsitektur berikutnya:

1. Menstabilkan permission-based authorization.
2. Menyelesaikan Postman testing flow.
3. Menentukan module lanjutan: Archive atau Asset.
4. Membuat frontend MVP untuk vertical slice pertama.
5. Menambahkan file/document strategy.
6. Menambahkan notification-report-service.
7. Menyiapkan deployment dengan Docker Compose.
