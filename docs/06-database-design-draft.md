# 06. Database Design Draft

Dokumen ini berisi rancangan data konseptual. Detail tabel, tipe data, index, migration, dan relasi final belum ditentukan.

## Prinsip Awal

- Setiap microservice idealnya memiliki database sendiri.
- Data antar service dihubungkan melalui identifier, bukan foreign key lintas database.
- Histori perubahan penting untuk audit.
- Dokumen file disimpan oleh service pemilik proses atau storage dokumen yang ditentukan kemudian, sedangkan database menyimpan metadata dan referensi dokumen.

## api-gateway

Pada tahap draft, api-gateway tidak wajib memiliki database. Jika diperlukan, data yang mungkin disimpan:

- gateway_routes
- gateway_access_logs
- rate_limit_rules

## auth-service

Entitas awal:

- users
- roles
- permissions
- user_roles
- role_permissions

Data utama:

- Identitas pengguna.
- Role pengguna.
- Permission yang melekat pada role.
- Status aktif pengguna.

## organization-service

Entitas implementasi saat ini:

- organization_periods
- divisions
- positions
- members
- member_assignments
- division_tasks
- division_task_evidences

Data utama:

- Periode kepengurusan PUB.
- Divisi organisasi.
- Jabatan atau position organisasi.
- Data anggota/mahasiswa PUB.
- Relasi anggota dengan periode, divisi, dan jabatan.
- Tugas operasional divisi.
- Bukti atau dokumentasi tugas divisi.
- Data yang boleh ditampilkan untuk web publik PUB.

Catatan desain:

- `members.auth_user_id` menyimpan referensi ID user dari auth-service, tetapi tidak menggunakan foreign key lintas database.
- `member_assignments` menghubungkan `members`, `organization_periods`, `divisions`, dan `positions`.
- `positions` digunakan untuk jabatan organisasi seperti Ketua PUB, Keamanan, Sekretaris, Bendahara, Coach Instruktur, Koordinator Divisi, Instruktur, dan Anggota.
- `divisions` digunakan untuk unit kerja seperti Divdik, Humas, Kesejahteraan, Kebersihan, Bahasa Inggris, Kerohanian, Keasramaan, Kesehatan, PPMB, dan Aset.
- Endpoint publik hanya mengembalikan data yang aman untuk web, sedangkan data internal seperti nomor telepon dan auth reference tidak wajib ditampilkan ke publik.

## request-service

Entitas awal:

- requests
- request_items
- request_approvals
- request_status_history
- request_notes
- request_documents
- fund_receipts
- spending_receipts
- settlements

Data utama:

- Nomor pengajuan.
- Divisi pemohon.
- Pemohon.
- Deskripsi kegiatan.
- Estimasi dana.
- Status pengajuan.
- Tanggal kegiatan.
- Approval Ketua Divisi, Ketua PUB, dan Pembina.
- Konfirmasi penerimaan dana.
- Struk atau bukti transaksi.
- Settlement penggunaan dana.
- Referensi dokumen pendukung.

## finance-service

Entitas awal:

- disbursements
- finance_transactions
- settlement_verifications
- fund_shortages
- fund_returns
- finance_documents

Data utama:

- Pengajuan terkait.
- Nominal disetujui.
- Nominal dicairkan.
- Sumber dana.
- Bendahara pemroses.
- Tanggal pencairan.
- Metode pencairan.
- Verifikasi settlement.
- Dana kurang.
- Pengembalian dana lebih.
- Bukti transaksi keuangan.

## notification-report-service

Entitas awal:

- notification_templates
- notification_requests
- notification_logs
- scheduled_jobs
- job_execution_logs
- report_snapshots
- report_export_logs

Data utama:

- Template email.
- Request pengiriman notifikasi.
- Status pengiriman.
- Error message jika gagal.
- Jadwal reminder.
- Log eksekusi job.
- Snapshot atau cache laporan.
- Log export laporan.

Reporting dapat menggunakan:

- Query agregasi antar service melalui API.
- Read model khusus laporan.
- Event-driven projection.

Pendekatan final ditentukan setelah kebutuhan performa dan audit lebih jelas.
