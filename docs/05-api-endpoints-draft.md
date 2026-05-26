# 05. API Endpoints Draft

Dokumen ini berisi draft endpoint konseptual. Belum ada implementasi backend.

## api-gateway

| Method | Endpoint | Deskripsi |
| --- | --- | --- |
| GET | /health | Cek status gateway |
| ANY | /api/** | Routing request ke service internal |

## auth-service

| Method | Endpoint | Deskripsi |
| --- | --- | --- |
| POST | /auth/login | Login pengguna |
| POST | /auth/refresh | Refresh token |
| GET | /users/me | Melihat profil pengguna aktif |
| GET | /users | Melihat daftar user |
| POST | /users | Membuat user |
| PATCH | /users/{id} | Mengubah user |
| GET | /roles | Melihat daftar role |

## organization-service

| Method | Endpoint | Deskripsi |
| --- | --- | --- |
| GET | /organizations/pub | Melihat profil organisasi PUB |
| GET | /divisions | Melihat daftar divisi |
| POST | /divisions | Membuat divisi |
| PATCH | /divisions/{id} | Mengubah divisi |
| GET | /members | Melihat daftar anggota |
| POST | /members | Menambahkan anggota |
| PATCH | /members/{id} | Mengubah data anggota |
| GET | /positions | Melihat daftar jabatan |

## request-service

| Method | Endpoint | Deskripsi |
| --- | --- | --- |
| POST | /requests | Membuat pengajuan |
| GET | /requests | Melihat daftar pengajuan |
| GET | /requests/{id} | Melihat detail pengajuan |
| PATCH | /requests/{id} | Mengubah pengajuan |
| POST | /requests/{id}/submit | Mengirim pengajuan |
| POST | /requests/{id}/approve/ | Approval Ketua Divisi |
| POST | /requests/{id}/reject | Menolak pengajuan |
| POST | /requests/{id}/request-revision | Meminta revisi |
| GET | /requests/{id}/history | Melihat histori pengajuan |
| POST | /requests/{id}/confirm-fund-received | Konfirmasi dana diterima |
| POST | /requests/{id}/receipts | Upload struk atau bukti transaksi |
| POST | /requests/{id}/settlements | Mengirim settlement |
| GET | /requests/{id}/settlements | Melihat settlement pengajuan |

## finance-service

| Method | Endpoint | Deskripsi |
| --- | --- | --- |
| GET | /disbursements | Melihat daftar pencairan |
| POST | /disbursements | Membuat data pencairan |
| GET | /disbursements/{id} | Melihat detail pencairan |
| POST | /disbursements/{id}/complete | Menandai pencairan selesai |
| GET | /settlements | Melihat daftar settlement |
| POST | /settlements/{id}/verify | Memverifikasi settlement |
| POST | /settlements/{id}/reject | Menolak settlement |
| GET | /fund-shortages | Melihat daftar dana kurang |
| POST | /fund-shortages/{id}/resolve | Menyelesaikan dana kurang |
| GET | /fund-returns | Melihat daftar pengembalian dana lebih |
| POST | /fund-returns/{id}/confirm | Konfirmasi pengembalian dana lebih |

## notification-report-service

| Method | Endpoint | Deskripsi |
| --- | --- | --- |
| GET | /reports/submissions | Laporan pengajuan |
| GET | /reports/approvals | Laporan approval |
| GET | /reports/disbursements | Laporan pencairan |
| GET | /reports/settlements | Laporan settlement |
| GET | /reports/fund-shortages | Laporan dana kurang |
| GET | /reports/fund-returns | Laporan pengembalian dana lebih |
| GET | /notifications/logs | Melihat log notifikasi |
| POST | /notifications/email/test | Mengirim email test |
| GET | /notifications/templates | Melihat template notifikasi |
| PATCH | /notifications/templates/{id} | Mengubah template notifikasi |
| POST | /scheduler/reminders/run | Menjalankan reminder manual |

## archive / document feature

| Method | Endpoint | Deskripsi |
| --- | --- | --- |
| POST | /archives | Mengunggah dokumen arsip |
| GET | /archives | Melihat daftar arsip |
| GET | /archives/{id} | Melihat detail arsip |
| GET | /archives/{id}/download | Mengunduh arsip |
| PATCH | /archives/{id} | Mengubah metadata arsip |
| DELETE | /archives/{id} | Menghapus arsip jika diizinkan |