# Orchestria

Orchestria adalah sistem manajemen alur operasional organisasi berbasis microservices. Studi kasus awal berfokus pada operasional PUB UNAS PASIM, mulai dari manajemen user, struktur organisasi, pengajuan dana divisi, approval bertingkat, pencairan dana, penerimaan dana, settlement, hingga histori proses.

Project ini dibuat sebagai implementasi Java Spring Boot microservices untuk kebutuhan UAS Java Lanjutan sekaligus portofolio backend.

## Tujuan

- Menyediakan alur operasional organisasi yang terdokumentasi dari pengajuan sampai penyelesaian.
- Memisahkan domain sistem ke beberapa microservices.
- Menggunakan autentikasi JWT dan authorization berbasis role/permission.
- Menyediakan audit trail dasar melalui histori status dan approval.
- Menjadi fondasi untuk pengembangan frontend React, notifikasi, reporting, dan deployment.

## Service yang Digunakan

| Service | Port | Database | Status |
| --- | ---: | --- | --- |
| api-gateway | 8000 | - | Routing gateway berjalan |
| auth-service | 8001 | orchestria_auth_db | Login, JWT, role, permission, admin auth |
| organization-service | 8002 | orchestria_organization_db | Struktur organisasi dan public API |
| request-service | 8003 | orchestria_request_db | Pengajuan, item, approval, timeline, settlement |
| finance-service | 8004 | orchestria_finance_db | Pencairan dana |
| notification-report-service | - | - | Belum diimplementasikan |

## Alur Utama MVP

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

## Teknologi

- Java 21
- Spring Boot 4.x
- Spring Security
- JWT dengan JJWT
- Spring Data JPA
- PostgreSQL
- Spring Cloud Gateway
- Maven
- Lombok
- Postman untuk API testing

## Routing via API Gateway

Frontend/Postman cukup mengakses gateway di port `8000`.

| Gateway Path | Target Service |
| --- | --- |
| `/api/auth/**` | `http://localhost:8001` |
| `/api/organization/**` | `http://localhost:8002` |
| `/api/requests/**` | `http://localhost:8003` |
| `/api/finance/**` | `http://localhost:8004` |

## Struktur Dokumentasi

- [Project Overview](docs/01-project-overview.md)
- [Business Flow](docs/02-business-flow.md)
- [Microservices Architecture](docs/03-microservices-architecture.md)
- [Role and Permission](docs/04-role-and-permission.md)
- [API Endpoints Draft](docs/05-api-endpoints-draft.md)
- [Database Design Draft](docs/06-database-design-draft.md)
- [Security Design](docs/07-security-design.md)
- [Development Roadmap](docs/08-development-roadmap.md)
- [Postman Testing Flow](docs/09-api-testing-flow.md)

## Status Project

Status saat ini: backend MVP berjalan untuk auth-service, organization-service, request-service, finance-service, dan api-gateway. Frontend React, deployment, notification-report-service, Docker, dan reporting masih tahap berikutnya.