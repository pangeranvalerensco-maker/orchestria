# Orchestria

Orchestria adalah sistem manajemen alur operasional organisasi berbasis microservices. Studi kasus awal berfokus pada operasional PUB Universitas Nasional PASIM, tetapi full scope project tidak terbatas pada pengajuan dana.

Project ini dibuat sebagai implementasi Java Spring Boot microservices untuk kebutuhan UAS Java Lanjutan sekaligus portofolio backend/full-stack.

## Target Project

Orchestria mempunyai dua target yang harus dipenuhi bersamaan:

1. seluruh materi Java Lanjutan yang sudah diajarkan memiliki implementasi nyata dan dapat didemokan;
2. seluruh domain full scope mempunyai minimal satu alur fungsional.

Pengajuan dana adalah vertical slice pertama, bukan keseluruhan scope project.

## Full Scope

- Auth dan Access Control
- Organization Management
- Fund Request dan Approval
- Finance dan Settlement
- Notification dan Scheduler
- Reporting dan Export
- Archive dan Document
- Asset Management
- Cleanliness/Picket
- Division Activity
- English Activity
- HUMAS/Public Web

## Service

| Service | Port | Database | Status |
| --- | ---: | --- | --- |
| api-gateway | 8000 | - | Implemented |
| auth-service | 8001 | `orchestria_auth_db` | Implemented |
| organization-service | 8002 | `orchestria_organization_db` | Implemented, perlu perluasan full scope |
| request-service | 8003 | `orchestria_request_db` | Implemented MVP |
| finance-service | 8004 | `orchestria_finance_db` | Implemented MVP |
| notification-report-service | 8005 planned | `orchestria_notification_report_db` | Not implemented |

## Current Core Flow

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

Backend core flow sudah tersedia secara mayoritas. Frontend saat ini sudah mencakup login, dashboard, My Requests, Create Request, Request Detail, item pengajuan, submit, dan approval queue. Frontend Finance dan Settlement masih harus diselesaikan.

## Frontend Route Saat Ini

```text
/login
/dashboard
/requests
/requests/new
/requests/:id
/approvals
```

## Materi Java Lanjutan

Sudah terimplementasi:

- MVC/layered architecture;
- REST API dan JSON;
- microservices;
- PostgreSQL;
- API Gateway;
- service-to-service communication;
- exception handling;
- CRUD;
- Bean/IoC;
- authentication dan authorization;
- role dan permission;
- `@PreAuthorize`;
- JWT stateless;
- frontend integration;
- Git workflow.

Belum lengkap dan menjadi target wajib:

- Java/Spring email;
- scheduler fixed rate, fixed delay, dan cron;
- event publisher dan listener;
- authentication stateful berbasis session sebagai demo terisolasi;
- Excel import/export/template menggunakan Apache POI;
- notification log;
- reporting dashboard;
- full scope module operasional.

Lihat matriks lengkap pada [Java Lanjutan Course Coverage Matrix](docs/11-course-coverage-matrix.md).

## Teknologi

### Backend

- Java 21
- Spring Boot 4.x
- Spring Security
- Spring Data JPA
- JWT dengan JJWT
- PostgreSQL
- Spring Cloud Gateway
- Maven
- Lombok

### Frontend

- React
- TypeScript
- Vite
- React Router

### Development Tools

- VS Code
- DBeaver
- Postman
- Git dan GitHub

## Routing API Gateway

Frontend dan Postman mengakses backend melalui port `8000`.

| Gateway Path | Target Service |
| --- | --- |
| `/api/auth/**` | auth-service |
| `/api/organization/**` | organization-service |
| `/api/requests/**` | request-service |
| `/api/finance/**` | finance-service |
| `/api/notifications/**` | notification-report-service, planned |
| `/api/reports/**` | notification-report-service, planned |

## Prinsip Arsitektur

- database per service;
- tidak ada shared database;
- tidak ada foreign key lintas database;
- relasi lintas service menggunakan ID referensi;
- integrasi saat ini menggunakan REST;
- JWT divalidasi pada service yang menerima request;
- API Gateway menjadi single entry point client;
- business logic utama berada di service layer Java.

## Dokumentasi

- [Project Overview](docs/01-project-overview.md)
- [Business Flow](docs/02-business-flow.md)
- [Microservices Architecture](docs/03-microservices-architecture.md)
- [Role and Permission](docs/04-role-and-permission.md)
- [API Endpoints Draft](docs/05-api-endpoints-draft.md)
- [Database Design Draft](docs/06-database-design-draft.md)
- [Security Design](docs/07-security-design.md)
- [Development Roadmap](docs/08-development-roadmap.md)
- [Postman Testing Flow](docs/09-api-testing-flow.md)
- [Current Status Handoff](docs/10-current-status-handoff.md)
- [Java Lanjutan Course Coverage Matrix](docs/11-course-coverage-matrix.md)
- [Full Scope Acceptance Criteria](docs/12-full-scope-acceptance-criteria.md)
- [48-Hour Execution Plan](docs/13-48-hour-execution-plan.md)

## Aturan Handoff

Sebelum melanjutkan project pada sesi baru:

1. periksa commit terbaru branch `main`;
2. baca dokumen `10` sampai `13`;
3. jangan menyederhanakan Orchestria hanya menjadi aplikasi pengajuan dana;
4. jangan membuat ulang struktur yang sudah ada;
5. kerjakan berdasarkan source code aktual;
6. gunakan commit kecil dan build setelah setiap slice.

## Status Saat Ini

```text
Backend foundation dan vertical slice Request–Approval–Finance tersedia.
Frontend Request dan Approval tersedia.
Frontend Finance/Settlement, notification-report-service, course gaps,
dan module full scope operasional masih harus diselesaikan.
```
