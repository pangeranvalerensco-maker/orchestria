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

Entitas awal:

- organizations
- divisions
- members
- positions
- member_positions
- organization_periods

Data utama:

- Profil organisasi PUB.
- Daftar divisi.
- Data anggota.
- Jabatan dan periode kepengurusan.
- Relasi anggota dengan divisi dan jabatan.

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
