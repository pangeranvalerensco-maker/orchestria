# 07. Security Design

## Tujuan Keamanan

- Memastikan hanya pengguna terotorisasi yang dapat mengakses data.
- Melindungi dokumen dan data keuangan.
- Menyediakan audit trail untuk proses penting.
- Mengurangi risiko manipulasi approval, pencairan, settlement, dana kurang, dan pengembalian dana lebih.

## Autentikasi

Draft pendekatan:

- Login menggunakan username atau email dan password.
- Token digunakan untuk akses API.
- Refresh token dipertimbangkan untuk sesi yang lebih panjang.
- Password disimpan dalam bentuk hash.

## Otorisasi

Otorisasi berbasis role dan permission.

Contoh:

- ANGGOTA dapat membuat dan melihat pengajuan sendiri.
- KETUA_DIVISI dapat memberi approval awal untuk pengajuan divisinya.
- KETUA_PUB dapat memberi approval tingkat PUB.
- PEMBINA dapat memberi approval akhir.
- BENDAHARA_INTERNAL dan BENDAHARA_EKSTERNAL dapat memproses pencairan, settlement, dana kurang, dan pengembalian dana lebih sesuai sumber dana.
- SEKRETARIS dapat membantu administrasi dokumen dan laporan sesuai hak akses.
- SUPER_ADMIN dapat mengelola konfigurasi, user, role, dan struktur organisasi.

## Proteksi Dokumen

- File tidak boleh diakses langsung tanpa otorisasi.
- Download dokumen harus melewati service pemilik proses dan validasi otorisasi.
- Akses dokumen penting dicatat dalam log.
- Dokumen keuangan dan bukti transaksi dibatasi untuk role terkait.

## Audit Trail

Aktivitas yang perlu dicatat:

- Login dan perubahan user penting.
- Pembuatan dan perubahan pengajuan.
- Keputusan approval.
- Pencairan dana.
- Konfirmasi penerimaan dana.
- Upload struk.
- Pengajuan dan verifikasi settlement.
- Pencatatan dana kurang.
- Pencatatan pengembalian dana lebih.
- Upload, download, dan penghapusan dokumen.

## Validasi Bisnis

Contoh aturan:

- Pengajuan tidak dapat dicairkan sebelum approval Ketua Divisi, Ketua PUB, dan Pembina selesai.
- User tidak dapat melakukan approval untuk pengajuan yang tidak ditugaskan kepadanya.
- Pengaju tidak dapat mengonfirmasi penerimaan dana sebelum pencairan selesai.
- Settlement tidak dapat dikirim sebelum dana diterima.
- Pengajuan tidak dapat ditutup jika dana kurang atau dana lebih belum diselesaikan.
- Dokumen wajib harus tersedia sebelum proses tertentu dilanjutkan.

## Risiko Awal

- Penyalahgunaan akun approver.
- Akses dokumen oleh pihak tidak berwenang.
- Perubahan data keuangan tanpa histori.
- Approval ganda atau approval di luar urutan.
- Pengakuan penerimaan dana yang tidak sesuai pencairan.
- Settlement tanpa bukti transaksi memadai.
- Email notification gagal terkirim tanpa retry.

## Mitigasi Draft

- Role dan permission ketat.
- Audit log untuk proses kritikal.
- Status transition yang tervalidasi.
- Log notifikasi dan mekanisme retry.
- Pembatasan akses dokumen berdasarkan role dan relasi data.

## Perlindungan dari IDOR

Sistem tidak boleh menerima userId, ketuaId, bendaharaId, atau pembinaId dari request sebagai bukti identitas.

Identitas user login harus selalu diambil dari JWT/SecurityContext.

Contoh endpoint yang dihindari:

POST /requests/{id}/approve?ketuaId=1

Contoh endpoint yang benar:

POST /requests/{id}/approve

Backend akan mengecek:
- siapa user login dari token
- role dan permission user
- apakah user adalah approver aktif
- apakah status pengajuan sesuai