# Panduan Akun dan Password Orchestria

Berikut adalah daftar role dan kredensial default yang bisa digunakan untuk keperluan *testing* di sistem Orchestria. 

> [!NOTE]
> Semua akun di bawah ini menggunakan **password yang sama**, yaitu:
> `AdminOrchestria123!` (Perhatikan huruf besar/kecil dan tanda seru di akhir).
> Fitur verifikasi OTP juga telah **dimatikan**, sehingga Anda bisa langsung *login*.

## Daftar Akun Penting (Berdasarkan Role)

### 1. Super Admin (Akses Penuh Keseluruhan)
- **Email:** `admin@mailinator.com` atau `superadmin@mailinator.com`
- **Fungsi:** Mengakses semua modul, mengelola notifikasi, scheduler, kebersihan, dan aktivitas bahasa Inggris secara global.

### 2. Ketua PUB (Approval Tahap Akhir)
- **Email:** `pangeran.valerensco.rivaldi.hutabarat@mailinator.com`
- **Fungsi:** Menyetujui pencairan dana setelah direview oleh Sekretaris dan Pembina.

### 3. Pembina (Approval Tahap 2)
- **Email:** `sri.muthia.ningrum@mailinator.com`
- **Fungsi:** Meninjau pengajuan setelah Sekretaris menyetujui, sebelum masuk ke Ketua PUB.

### 4. Sekretaris (Approval Tahap 1 & Pengelola Arsip)
- **Email:** `khalisha.ulfa.marsha@mailinator.com`
- **Fungsi:** *Reviewer* pertama pada pengajuan dana. Memiliki wewenang untuk mengelola Arsip Dokumen.

### 5. Ketua Divisi (Contoh)
- **Email:** `miftahul.jannah.harahap@mailinator.com` (Ketua Divisi Pendidikan dan Pelatihan)
- **Fungsi:** Memberikan konfirmasi level divisi atas pengajuan dana yang dibuat oleh anggotanya.

### 6. Bendahara Internal
- **Email:** `dea.afrilia@mailinator.com`
- **Fungsi:** Menyalurkan (mencairkan) dana setelah semua *approval* selesai.

### 7. Checker (Pengelola Peminjaman Aset)
- **Email:** `rickhy.ramadhan@mailinator.com`
- **Fungsi:** Memproses persetujuan, serah terima, dan pengembalian peminjaman aset.

### 8. Anggota Biasa (Contoh)
- **Email:** `izhar.harahap@mailinator.com`
- **Fungsi:** Mengajukan proposal dana, meminjam aset.

---

> [!IMPORTANT]
> **Terkait Pengelola Kebersihan dan Bahasa Inggris:**
> Meskipun di dalam sistem terdapat *role* **KOORDINATOR** yang didedikasikan untuk membuat jadwal kebersihan dan English, saat ini di *database* belum ada *user* yang di-*assign* dengan *role* tersebut. 
> Untuk saat ini, **Super Admin** (`admin@mailinator.com`) bisa membuat jadwal tersebut. 
> Jika Anda menghendaki ada *user* spesifik (misalnya koordinator divisi kebersihan) untuk mengelolanya, hal tersebut bisa saya tambahkan nanti ke *database* atau bisa diatur mandiri oleh Super Admin.
