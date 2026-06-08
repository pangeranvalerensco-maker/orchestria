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

### Internal Endpoints

| Method | Endpoint | Deskripsi |
| --- | --- | --- |
| GET | /api/organization/test/me | Mengecek JWT dan authority dari token |
| GET | /api/organization/divisions | Melihat daftar divisi aktif |
| GET | /api/organization/divisions/{id} | Melihat detail divisi |
| POST | /api/organization/divisions | Membuat divisi |
| PUT | /api/organization/divisions/{id} | Mengubah divisi |
| DELETE | /api/organization/divisions/{id} | Menonaktifkan divisi |
| GET | /api/organization/positions | Melihat daftar jabatan |
| GET | /api/organization/positions/{id} | Melihat detail jabatan |
| POST | /api/organization/positions | Membuat jabatan |
| PUT | /api/organization/positions/{id} | Mengubah jabatan |
| DELETE | /api/organization/positions/{id} | Menonaktifkan jabatan |
| GET | /api/organization/periods | Melihat daftar periode |
| GET | /api/organization/periods/current | Melihat periode aktif |
| GET | /api/organization/periods/{id} | Melihat detail periode |
| POST | /api/organization/periods | Membuat periode |
| PUT | /api/organization/periods/{id} | Mengubah periode |
| DELETE | /api/organization/periods/{id} | Menonaktifkan periode |
| GET | /api/organization/members | Melihat daftar anggota |
| GET | /api/organization/members/{id} | Melihat detail anggota |
| POST | /api/organization/members | Menambahkan anggota |
| PUT | /api/organization/members/{id} | Mengubah anggota |
| DELETE | /api/organization/members/{id} | Menonaktifkan anggota |
| GET | /api/organization/member-assignments | Melihat daftar penugasan anggota |
| GET | /api/organization/member-assignments/{id} | Melihat detail penugasan |
| GET | /api/organization/member-assignments/period/{periodId} | Melihat struktur berdasarkan periode |
| GET | /api/organization/member-assignments/period/{periodId}/division/{divisionId} | Melihat struktur divisi berdasarkan periode |
| POST | /api/organization/member-assignments | Membuat penugasan anggota |
| PUT | /api/organization/member-assignments/{id} | Mengubah penugasan anggota |
| DELETE | /api/organization/member-assignments/{id} | Menonaktifkan penugasan |
| GET | /api/organization/division-tasks | Melihat daftar tugas divisi |
| GET | /api/organization/division-tasks/{id} | Melihat detail tugas divisi |
| GET | /api/organization/division-tasks/division/{divisionId} | Melihat tugas berdasarkan divisi |
| GET | /api/organization/division-tasks/member/{memberId} | Melihat tugas berdasarkan anggota |
| GET | /api/organization/division-tasks/status/{status} | Melihat tugas berdasarkan status |
| POST | /api/organization/division-tasks | Membuat tugas divisi |
| PUT | /api/organization/division-tasks/{id} | Mengubah tugas divisi |
| PATCH | /api/organization/division-tasks/{id}/status/{status} | Mengubah status tugas divisi |
| DELETE | /api/organization/division-tasks/{id} | Menonaktifkan tugas divisi |
| GET | /api/organization/division-task-evidences/task/{taskId} | Melihat bukti berdasarkan tugas |
| GET | /api/organization/division-task-evidences/{id} | Melihat detail bukti tugas |
| POST | /api/organization/division-task-evidences | Menambahkan bukti tugas |
| PUT | /api/organization/division-task-evidences/{id} | Mengubah bukti tugas |
| DELETE | /api/organization/division-task-evidences/{id} | Menonaktifkan bukti tugas |

### Public Endpoints

| Method | Endpoint | Deskripsi |
| --- | --- | --- |
| GET | /public/organization/periods | Melihat daftar periode publik |
| GET | /public/organization/periods/current | Melihat periode aktif publik |
| GET | /public/organization/members?cohort={cohort} | Melihat daftar anggota publik berdasarkan angkatan |
| GET | /public/organization/structure/current | Melihat struktur organisasi periode aktif |
| GET | /public/organization/structure/period/{periodId} | Melihat struktur organisasi berdasarkan periode |

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