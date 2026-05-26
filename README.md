# Orchestria

Orchestria adalah draft sistem manajemen alur operasional organisasi berbasis microservices. Studi kasus awal berfokus pada operasional divisi PUB, mulai dari pengajuan kebutuhan divisi, approval bertingkat, pencairan dana, penerimaan dana, upload struk, settlement, pengembalian dana lebih, penanganan dana kurang, arsip dokumen, laporan, dan notifikasi.

Dokumentasi ini masih tahap awal dan digunakan sebagai fondasi konsep sebelum implementasi teknis dibuat.

## Tujuan

- Menyediakan gambaran awal kebutuhan bisnis Orchestria.
- Menentukan batas awal layanan microservices.
- Mendokumentasikan alur kerja operasional divisi PUB.
- Menjadi acuan sebelum membuat project Java Spring Boot.

## Ruang Lingkup Awal

- Pengajuan operasional divisi.
- Approval bertingkat berdasarkan role dan urutan workflow organisasi.
- Pencairan dana setelah approval selesai.
- Penerimaan dana oleh pengaju.
- Upload struk dan settlement penggunaan dana.
- Penanganan dana kurang dan pengembalian dana lebih.
- Arsip dokumen dan histori aktivitas.
- Laporan operasional dan keuangan.
- Notifikasi email.
- Reminder dan proses berkala.

## Microservices Awal

- api-gateway
- auth-service
- organization-service
- request-service
- finance-service
- notification-report-service

## Struktur Dokumentasi

- [Project Overview](docs/01-project-overview.md)
- [Business Flow](docs/02-business-flow.md)
- [Microservices Architecture](docs/03-microservices-architecture.md)
- [Role and Permission](docs/04-role-and-permission.md)
- [API Endpoints Draft](docs/05-api-endpoints-draft.md)
- [Database Design Draft](docs/06-database-design-draft.md)
- [Security Design](docs/07-security-design.md)
- [Development Roadmap](docs/08-development-roadmap.md)

## Status

Status project: dokumentasi konsep awal.

Belum ada backend, frontend, Docker, konfigurasi, maupun implementasi Spring Boot.
