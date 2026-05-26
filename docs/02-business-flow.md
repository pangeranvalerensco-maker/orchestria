# 02. Business Flow

## Alur Utama Operasional PUB

1. Anggota atau pengurus divisi membuat pengajuan divisi.
2. Pengajuan berisi kebutuhan, estimasi dana, tanggal kegiatan, deskripsi, dan lampiran pendukung.
3. Ketua Divisi melakukan approval awal.
4. Jika disetujui Ketua Divisi, pengajuan masuk ke Ketua PUB.
5. Ketua PUB melakukan approval tingkat organisasi PUB.
6. Jika disetujui Ketua PUB, pengajuan masuk ke Pembina.
7. Pembina memberi approval akhir.
8. Bendahara Internal atau Bendahara Eksternal melakukan pencairan dana sesuai sumber dan kebijakan dana.
9. Pengaju mengonfirmasi penerimaan dana.
10. Pengaju menjalankan kegiatan dan mengunggah struk atau bukti transaksi.
11. Pengaju membuat settlement penggunaan dana.
12. Bendahara memeriksa settlement.
13. Jika realisasi lebih besar dari dana diterima, sistem mencatat dana kurang untuk ditindaklanjuti.
14. Jika realisasi lebih kecil dari dana diterima, pengaju mengembalikan dana lebih.
15. Setelah dana kurang atau dana lebih diselesaikan, pengajuan diarsipkan.
16. Data masuk ke laporan operasional dan keuangan.

## Status Pengajuan

- Draft
- Submitted
- Division Approved
- PUB Approved
- Pembina Approved
- Rejected
- Need Revision
- Ready for Disbursement
- Disbursed
- Fund Received
- Receipt Upload Pending
- Settlement Submitted
- Settlement Approved
- Fund Shortage
- Fund Return Pending
- Settled
- Archived

## Approval Bertingkat

Approval dasar berjalan berurutan:

1. Ketua Divisi
2. Ketua PUB
3. Pembina

Bendahara tidak menjadi approver utama pengajuan, tetapi memproses pencairan setelah approval selesai. Pada MVP awal, seluruh pengajuan mengikuti alur approval yang sama: Ketua Divisi, Ketua PUB, lalu Pembina. Aturan tambahan berdasarkan jenis kegiatan atau sumber dana dapat dikembangkan pada fase berikutnya.

## Pencairan Dana

Pencairan dilakukan setelah pengajuan disetujui Pembina. Data pencairan minimal memuat:

- Pengajuan terkait.
- Nominal disetujui.
- Tanggal pencairan.
- Metode pencairan.
- Penerima dana.
- Bukti pencairan.

## Penerimaan Dana

Setelah dana dicairkan, pengaju wajib mengonfirmasi penerimaan dana. Konfirmasi ini menjadi dasar bahwa kegiatan dapat berjalan dengan dana yang sudah diterima.

## Upload Struk dan Settlement

Settlement dibuat setelah kegiatan berjalan dan bukti transaksi tersedia. Data minimal:

- Realisasi penggunaan dana.
- Struk atau bukti transaksi.
- Catatan pelaksanaan kegiatan.
- Status verifikasi.

## Dana Kurang

Jika realisasi lebih besar dari dana yang diterima, sistem mencatat dana kurang. Bendahara dan pihak terkait menentukan apakah selisih disetujui untuk dibayarkan atau perlu revisi.

## Pengembalian Dana Lebih

Jika realisasi lebih kecil dari dana yang diterima, pengaju wajib mengembalikan dana lebih. Bendahara mencatat penerimaan pengembalian sebelum settlement ditutup.

## Notifikasi dan Reminder

Sistem mengirim email untuk:

- Pengajuan baru.
- Approval yang menunggu tindakan.
- Pengajuan ditolak atau perlu revisi.
- Dana telah dicairkan.
- Dana diterima pengaju.
- Reminder upload struk.
- Settlement diterima atau ditolak.
- Dana kurang perlu tindak lanjut.
- Pengembalian dana lebih perlu diselesaikan.
