# Finance Request Synchronization Schema

`finance-service` menyimpan metadata sinkronisasi ke `request-service` pada tabel `fund_disbursements`. Empat kolom berikut wajib tersedia pada setiap environment:

| Kolom | Tipe yang diharapkan | Nullability | Default |
| --- | --- | --- | --- |
| `request_sync_status` | `VARCHAR(30)` | `NOT NULL` | `'PENDING'` |
| `request_sync_attempts` | `INTEGER` | `NOT NULL` | `0` |
| `request_sync_error` | `VARCHAR(500)` | nullable | tidak ada |
| `request_synced_at` | `TIMESTAMP` | nullable | tidak ada |

## Kebijakan Inisialisasi Schema

`finance-service` sengaja tidak menggunakan `schema.sql` otomatis untuk membuat atau mengubah kolom tersebut. Jangan mengaktifkan `spring.sql.init.mode=always` untuk menjalankan DDL atau backfill pada startup aplikasi.

Environment baru harus menyediakan schema melalui salah satu mekanisme yang dikendalikan secara eksplisit:

- migration versioned, misalnya Flyway atau Liquibase; atau
- provisioning SQL manual yang telah direview dan dijalankan oleh operator database.

Entity `FundDisbursement` dan logic sinkronisasi mengandalkan keempat kolom tersebut. Karena itu deployment ke database baru harus memverifikasi keberadaan, tipe, nullability, dan default kolom sebelum service dijalankan.

## Data Legacy

Jangan mengubah record legacy menjadi `SYNCED` secara otomatis hanya karena nilai metadata sinkronisasi belum tersedia. Status harus mencerminkan hasil sinkronisasi yang benar-benar dapat dibuktikan.

Record dengan kombinasi berikut harus diaudit secara manual:

```text
request_sync_status = SYNCED
request_sync_attempts = 0
request_synced_at IS NULL
```

Kombinasi tersebut dapat menunjukkan metadata legacy yang tidak lengkap. Audit harus membandingkan status pencairan di `finance-service` dengan status request terkait di `request-service` sebelum melakukan remediation apa pun.
