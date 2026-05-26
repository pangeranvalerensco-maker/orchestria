# 04. Role and Permission

## Role Awal

### SUPER_ADMIN

Mengelola sistem, user, role, permission, struktur organisasi, dan konfigurasi utama.

### PEMBINA

Memberikan approval akhir atas pengajuan yang sudah disetujui Ketua Divisi dan Ketua PUB. Dapat melihat laporan dan histori proses.

### KETUA_PUB

Mengelola proses organisasi PUB, memberi approval setelah Ketua Divisi, dan memantau seluruh pengajuan divisi.

### KETUA_DIVISI

Melakukan approval awal atas pengajuan dari anggota atau pengurus divisinya.

### SEKRETARIS

Membantu administrasi pengajuan, dokumen, arsip, dan laporan kegiatan sesuai hak akses.

### BENDAHARA_INTERNAL

Mengelola pencairan, settlement, dana kurang, dan pengembalian dana lebih untuk sumber dana internal.

### BENDAHARA_EKSTERNAL

Mengelola pencairan, settlement, dana kurang, dan pengembalian dana lebih untuk sumber dana eksternal.

### ANGGOTA

Membuat pengajuan, melihat status pengajuan sendiri, mengonfirmasi penerimaan dana, mengunggah struk, dan membuat settlement.

## Draft Permission

| Permission | Deskripsi |
| --- | --- |
| user.manage | Mengelola user dan role |
| organization.manage | Mengelola struktur organisasi |
| request.create | Membuat pengajuan |
| request.read.own | Melihat pengajuan milik sendiri |
| request.read.division | Melihat pengajuan satu divisi |
| request.read.all | Melihat semua pengajuan PUB |
| request.approve.division | Approval sebagai Ketua Divisi |
| request.approve.pub | Approval sebagai Ketua PUB |
| request.approve.pembina | Approval sebagai Pembina |
| request.reject | Menolak pengajuan |
| request.confirm_fund_received | Mengonfirmasi penerimaan dana |
| request.upload_receipt | Mengunggah struk atau bukti transaksi |
| request.submit_settlement | Mengirim settlement |
| finance.disburse | Melakukan pencairan dana |
| finance.verify_settlement | Memverifikasi settlement |
| finance.handle_shortage | Menangani dana kurang |
| finance.record_return | Mencatat pengembalian dana lebih |
| document.upload | Mengunggah dokumen |
| document.read | Melihat dokumen |
| report.read | Melihat laporan |
| notification.manage | Mengelola template dan log notifikasi |

## Aturan Akses Awal

- Pengaju hanya dapat mengubah pengajuan selama masih Draft atau Need Revision.
- Approval hanya dapat dilakukan oleh role yang sedang menjadi approver aktif.
- Urutan approval dasar adalah Ketua Divisi, Ketua PUB, lalu Pembina.
- Bendahara hanya dapat mencairkan dana setelah approval Pembina selesai.
- Pengaju hanya dapat upload struk dan settlement setelah dana diterima.
- Dana lebih harus dikembalikan sebelum pengajuan dinyatakan settled.
- Dana kurang harus ditindaklanjuti sebelum pengajuan diarsipkan.
- Dokumen sensitif hanya dapat dilihat oleh role terkait.

## Catatan

Model permission masih draft dan akan disesuaikan setelah workflow approval final.
