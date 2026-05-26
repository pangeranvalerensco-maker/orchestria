# 01. Project Overview

## Ringkasan

Orchestria adalah Sistem Manajemen Alur Operasional Organisasi. Sistem ini membantu organisasi mengelola proses operasional lintas divisi secara terdokumentasi, terukur, dan dapat diaudit.

Studi kasus awal menggunakan operasional divisi PUB, terutama proses pengajuan kebutuhan, approval ketua divisi, approval ketua PUB, approval pembina, pencairan oleh bendahara, penerimaan dana oleh pengaju, upload struk, settlement, dana kurang, pengembalian dana lebih, arsip dokumen, laporan, dan notifikasi.

## Masalah yang Ingin Diselesaikan

- Pengajuan operasional masih tersebar di chat, spreadsheet, atau dokumen manual.
- Approval sulit dilacak dan tidak selalu memiliki histori yang jelas.
- Pencairan dana, penerimaan dana, dan settlement sering tidak terhubung dalam satu alur.
- Arsip dokumen tidak terpusat.
- Laporan membutuhkan rekap manual.
- Reminder approval, upload struk, dan settlement belum otomatis.

## Tujuan Sistem

- Menyediakan alur operasional yang rapi dari pengajuan sampai arsip.
- Memastikan setiap proses memiliki status, histori, dan penanggung jawab.
- Memisahkan domain sistem ke beberapa microservices.
- Menyediakan dasar untuk otomasi notifikasi dan scheduler.
- Mendukung laporan operasional dan keuangan.

## Aktor Utama

- SUPER_ADMIN
- PEMBINA
- KETUA_PUB
- KETUA_DIVISI
- SEKRETARIS
- BENDAHARA_INTERNAL
- BENDAHARA_EKSTERNAL
- ANGGOTA

## Modul Awal

- Auth dan role management
- Struktur organisasi
- Pengajuan divisi
- Approval workflow
- Pencairan dana
- Penerimaan dana
- Upload struk dan settlement
- Dana kurang dan pengembalian dana lebih
- Arsip dokumen
- Reporting dan notification

## Microservices Awal

- api-gateway
- auth-service
- organization-service
- request-service
- finance-service
- notification-report-service

## Batasan Draft

Dokumen ini belum membahas detail implementasi Java Spring Boot, infrastruktur, deployment, Docker, maupun struktur kode.
