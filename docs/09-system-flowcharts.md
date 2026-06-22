# 15. System Flowcharts

Dokumen ini menjadi sumber flowchart otomatis Orchestria. Diagram menggunakan Mermaid dan dapat dirender langsung oleh GitHub, VS Code, atau Mermaid Live Editor.

## 1. Arsitektur Sistem

```mermaid
flowchart LR
    U[Pengguna] --> FE[React Frontend :5173]
    FE --> GW[API Gateway :8000]
    GW --> AUTH[Auth Service :8001]
    GW --> ORG[Organization Service :8002]
    GW --> REQ[Request Service :8003]
    GW --> FIN[Finance Service :8004]
    GW --> NR[Notification Report Service :8005]

    AUTH --> ADB[(Auth DB)]
    ORG --> ODB[(Organization DB)]
    REQ --> RDB[(Request DB)]
    FIN --> FDB[(Finance DB)]
    NR --> NDB[(Notification Report DB)]

    REQ --> ORG
    FIN --> REQ
    AUTH --> NR
    NR --> REQ
    NR --> SMTP[Gmail SMTP]
```

## 2. Login JWT, OTP, MFA, dan Trusted Device

```mermaid
flowchart TD
    A[Masukkan email dan password] --> B{Kredensial valid?}
    B -- Tidak --> X[401 Unauthorized]
    B -- Ya --> C{MFA wajib/aktif dan perangkat belum dipercaya?}
    C -- Tidak --> D[Terbitkan JWT]
    C -- Ya --> E[Buat OTP challenge]
    E --> F[Kirim OTP melalui Notification Service]
    F --> G[User memasukkan OTP]
    G --> H{OTP valid dan belum kedaluwarsa?}
    H -- Tidak --> I[Tolak atau izinkan resend sesuai cooldown]
    H -- Ya --> J{Ingat perangkat?}
    J -- Ya --> K[Simpan trusted-device token HttpOnly]
    J -- Tidak --> D
    K --> D
    D --> L[Frontend mengirim Authorization Bearer JWT]
```

## 3. Stateful Session Demo

```mermaid
flowchart TD
    A[Login Session Demo] --> B[Validasi user dan password]
    B --> C[Invalidate session lama]
    C --> D[Buat HttpSession]
    D --> E[Simpan userId di memori server]
    E --> F[Browser menerima cookie ORCHESTRIA_SESSION_DEMO]
    F --> G[Request profile/status membawa cookie]
    G --> H[Server membaca data session]
    H --> I[Logout atau timeout]
    I --> J[Session invalid dan cookie dihapus]
```

## 4. Pengajuan dan Approval

```mermaid
stateDiagram-v2
    [*] --> DRAFT
    DRAFT --> SUBMITTED: pemohon submit
    SUBMITTED --> DIVISION_APPROVED: Koordinator Divisi approve
    SUBMITTED --> REVISION_REQUIRED: minta revisi
    SUBMITTED --> REJECTED: reject
    REVISION_REQUIRED --> SUBMITTED: pemohon revisi dan submit ulang
    DIVISION_APPROVED --> PUB_APPROVED: Ketua PUB approve
    DIVISION_APPROVED --> REVISION_REQUIRED: minta revisi
    DIVISION_APPROVED --> REJECTED: reject
    PUB_APPROVED --> PEMBINA_APPROVED: Pembina approve
    PUB_APPROVED --> REVISION_REQUIRED: minta revisi
    PUB_APPROVED --> REJECTED: reject
    PEMBINA_APPROVED --> READY_FOR_DISBURSEMENT
    READY_FOR_DISBURSEMENT --> DISBURSED
    DISBURSED --> FUND_RECEIVED
```

## 5. Finance dan Settlement

```mermaid
stateDiagram-v2
    FUND_RECEIVED --> SETTLEMENT_DRAFT
    SETTLEMENT_DRAFT --> SETTLEMENT_SUBMITTED
    SETTLEMENT_SUBMITTED --> SETTLEMENT_APPROVED: sesuai
    SETTLEMENT_SUBMITTED --> SETTLEMENT_REVISION_REQUIRED: perlu perbaikan
    SETTLEMENT_REVISION_REQUIRED --> SETTLEMENT_SUBMITTED
    SETTLEMENT_APPROVED --> FUND_SHORTAGE: realisasi lebih besar
    SETTLEMENT_APPROVED --> FUND_RETURN_PENDING: realisasi lebih kecil
    SETTLEMENT_APPROVED --> COMPLETED: realisasi sama
    FUND_SHORTAGE --> COMPLETED: kekurangan diselesaikan
    FUND_RETURN_PENDING --> COMPLETED: dana lebih dikembalikan
    COMPLETED --> ARCHIVED
```

## 6. Tugas Divisi

```mermaid
flowchart TD
    A[Koordinator membuat tugas] --> B[Assign anggota]
    B --> C[Anggota melihat tugas sendiri]
    C --> D[Anggota mengirim bukti]
    D --> E{Bukti diterima?}
    E -- Tidak --> F[Revision Required]
    F --> D
    E -- Ya --> G[Task Completed]
    G --> H[Masuk laporan aktivitas divisi]
```

## 7. Peminjaman Aset

```mermaid
stateDiagram-v2
    [*] --> REQUESTED
    REQUESTED --> APPROVED
    REQUESTED --> REJECTED
    REQUESTED --> CANCELLED
    APPROVED --> HANDED_OVER
    HANDED_OVER --> RETURN_REQUESTED
    RETURN_REQUESTED --> RETURNED
    RETURNED --> VERIFIED
    VERIFIED --> [*]
```

## 8. Piket dan Kebersihan

```mermaid
flowchart TD
    A[Koordinator membuat jadwal] --> B[Publish jadwal]
    B --> C[Anggota melihat jadwal]
    C --> D[Anggota mencatat pelaksanaan/presensi]
    D --> E[Koordinator memeriksa]
    E --> F[Reward atau violation point]
    F --> G[Rekap laporan kebersihan]
```

## 9. English Activity

```mermaid
stateDiagram-v2
    [*] --> ACTIVITY_DRAFT
    ACTIVITY_DRAFT --> ACTIVITY_PUBLISHED
    ACTIVITY_PUBLISHED --> DEPOSIT_SUBMITTED
    DEPOSIT_SUBMITTED --> DEPOSIT_VERIFIED
    DEPOSIT_SUBMITTED --> DEPOSIT_REJECTED
    DEPOSIT_REJECTED --> DEPOSIT_SUBMITTED
    DEPOSIT_VERIFIED --> REPORT
```

## 10. HUMAS dan Konten Publik

```mermaid
stateDiagram-v2
    [*] --> DRAFT
    DRAFT --> PUBLISHED: publish
    PUBLISHED --> ARCHIVED: archive
    ARCHIVED --> DRAFT: restore/edit
```

## 11. Notification, Scheduler, dan Report

```mermaid
flowchart TD
    A[Business event atau manual action] --> B[Notification request]
    B --> C[Simpan notification log]
    C --> D{Kirim email berhasil?}
    D -- Ya --> E[Status SENT]
    D -- Tidak --> F[Status FAILED]
    F --> G[Fixed-delay retry scheduler]
    G --> D

    H[Cron/fixed-rate scheduler] --> I[Buat scheduled job log]
    I --> J[Ambil data dari service terkait]
    J --> K[Generate summary atau Excel]
    K --> L[Simpan export log]
    L --> M[Kirim ke subscriber bila diperlukan]
```

## 12. Model Role dan Position Final

```mermaid
flowchart TD
    R[Security Role: KOORDINATOR_DIVISI] --> P[Permission pimpinan divisi]
    P --> A[request.approve.division]
    P --> B[division.task.manage]
    P --> C[permission operasional sesuai divisi]

    POS[Organization Position: KOORDINATOR_DIVISI] --> ASSIGN[Member Assignment]
    ASSIGN --> DIV[Division]
    DIV --> D1[Keasramaan]
    DIV --> D2[Bahasa Inggris]
    DIV --> D3[Kebersihan]
    DIV --> D4[Divdik dan divisi lain]

    LEG1[Legacy: KETUA_DIVISI] -. migrasi .-> R
    LEG2[Legacy: KOORDINATOR] -. migrasi .-> R
    LEG3[Label: Ketua Asrama] -. konteks Divisi Keasramaan .-> POS
```
