# 03. Microservices Architecture

## Prinsip Awal

Orchestria dirancang sebagai sistem microservices agar domain utama dapat dipisahkan dengan jelas. Setiap service bertanggung jawab atas satu area bisnis dan berkomunikasi melalui API atau event.

Dokumen ini masih berupa draft konseptual, belum menentukan framework, library, konfigurasi, atau deployment.

## Services Final Draft

### api-gateway

Menjadi pintu masuk utama API. Service ini menangani routing request, validasi token awal, rate limit dasar, dan penyatuan akses dari client ke service internal.

### auth-service

Mengelola autentikasi, user, role, permission, profil pengguna, dan token akses.

### organization-service

Mengelola data organisasi PUB, baik untuk kebutuhan internal sistem maupun kebutuhan publik web PUB.

Tanggung jawab utama:
- Periode kepengurusan organisasi.
- Divisi organisasi.
- Jabatan/position organisasi.
- Data anggota/mahasiswa PUB.
- Penugasan anggota ke periode, divisi, dan jabatan.
- Tugas operasional divisi.
- Bukti/dokumentasi tugas divisi.
- Endpoint publik untuk struktur organisasi dan daftar anggota yang boleh ditampilkan di web PUB.

Catatan desain:
- organization-service tidak menyimpan tabel users, roles, dan permissions.
- Identitas login dan authority berasal dari JWT yang dibuat oleh auth-service.
- organization-service hanya memvalidasi token dan membaca claims seperti email, roles, dan permissions.
- Endpoint internal menggunakan prefix `/api/organization/**`.
- Endpoint publik menggunakan prefix `/public/organization/**`.

### request-service

Mengelola pengajuan divisi, approval bertingkat, status pengajuan, histori keputusan, lampiran pengajuan, penerimaan dana oleh pengaju, upload struk, dan settlement dari sisi alur permintaan.

### finance-service

Mengelola pencairan dana oleh bendahara, sumber dana, transaksi pencairan, verifikasi settlement dari sisi keuangan, dana kurang, dan pengembalian dana lebih.

### notification-report-service

Mengelola email notification, reminder, template notifikasi, log pengiriman, scheduler ringan, dan laporan operasional maupun keuangan.

## Komunikasi Antar Service

Draft pola komunikasi:

- REST API untuk request-response langsung.
- Event message untuk proses asynchronous seperti notifikasi, reminder, dan pembaruan laporan.
- Setiap service memiliki data ownership masing-masing.

## Contoh Event Awal

- RequestSubmitted
- DivisionApproved
- PubApproved
- PembinaApproved
- RequestRejected
- DisbursementCompleted
- FundReceived
- ReceiptUploaded
- SettlementSubmitted
- SettlementApproved
- FundShortageCreated
- FundReturnCompleted
- NotificationRequested

## Catatan Desain

- Service boundary final draft terdiri dari enam service.
- Database per service lebih sesuai dengan prinsip microservices.
- Shared database dihindari kecuali ada alasan kuat pada tahap implementasi.
- Integrasi laporan dapat menggunakan read model atau agregasi data dari event.
- Authentication antar service menggunakan JWT stateless. Service selain auth-service tidak menyimpan session user dan tidak mengakses database auth-service secara langsung.
- Data jabatan organisasi seperti Ketua PUB, Keamanan, Sekretaris, Bendahara, Coach Instruktur, dan Koordinator Divisi disimpan sebagai Position, bukan sebagai security role.
