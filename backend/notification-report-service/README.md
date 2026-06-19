# notification-report-service

Service notifikasi dan laporan Orchestria pada port `8005`.

## Fitur

- Spring Mail: plain text, HTML, To, Cc, dan Bcc
- Scheduler: `fixedRate`, `fixedDelay`, dan `cron`
- Spring Events: publisher dan listener
- Apache POI: laporan Excel
- JWT authentication dan permission `request.read.all`

## Menjalankan

Salin `src/main/resources/application-example.properties` menjadi `application-local.properties`, lalu isi konfigurasi environment yang sama dengan service lain.

```bash
mvn spring-boot:run
```

## Endpoint

```http
GET /api/notifications/health
```

Health check bersifat publik.

```http
GET /api/reports/fund-requests.xlsx
```

Endpoint laporan memerlukan JWT dengan permission `request.read.all`. Header Authorization diteruskan ke request-service. Kegagalan upstream dipetakan menjadi `502 Bad Gateway`, bukan file Excel kosong.

Kolom laporan:

```text
ID | Judul | Divisi | Pemohon | Status | Total (Rp) | Tanggal Dibuat
```

## Scheduler

- `fixedRate`: health ping setiap 5 menit
- `fixedDelay`: pemeriksaan retry email setiap 10 menit
- `cron`: pengingat laporan setiap Senin pukul 08.00

Scheduler dapat dimatikan melalui:

```properties
app.scheduler.enabled=false
```

## Event

`NotificationService` memublikasikan `NotificationEvent`, lalu `NotificationEventListener` memproses event melalui `@EventListener`.

## Testing

```bash
mvn clean verify
```

Test mencakup context load, health endpoint, security laporan, penerusan Authorization, kegagalan upstream, Excel, event listener, dan scheduler.
