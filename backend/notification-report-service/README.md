# notification-report-service

Microservice Orchestria untuk **Notifikasi** dan **Laporan**, berjalan di port **8005**.

Service ini dibangun sebagai bagian dari materi **Java Lanjutan** untuk mendemonstrasikan:
- Spring Mail (SMTP)
- Spring Scheduling (`@Scheduled`)
- Spring Events (`ApplicationEventPublisher` + `@EventListener`)
- Apache POI (laporan Excel)

---

## Daftar Isi

- [Prasyarat](#prasyarat)
- [Cara Menjalankan](#cara-menjalankan)
- [Konfigurasi Environment](#konfigurasi-environment)
- [Endpoint](#endpoint)
- [Penjelasan Konsep](#penjelasan-konsep)
- [Testing](#testing)

---

## Prasyarat

- Java 21
- Maven 3.9+
- SMTP server (Gmail / Mailtrap / lainnya) — untuk fitur email
- `request-service` berjalan di port 8003 — untuk fitur laporan Excel

---

## Cara Menjalankan

### 1. Salin konfigurasi lokal

```bash
cp src/main/resources/application-example.properties \
   src/main/resources/application-local.properties
```

Isi `application-local.properties` dengan nilai SMTP dan URL yang sesuai.

### 2. Jalankan service

```bash
./mvnw spring-boot:run
```

Service akan berjalan di: `http://localhost:8005`

### 3. Via API Gateway

Pastikan `api-gateway` berjalan di port 8000. Route sudah dikonfigurasi:
- `/api/notifications/**` → port 8005
- `/api/reports/**` → port 8005

---

## Konfigurasi Environment

Semua konfigurasi berada di `application-local.properties` (tidak dicommit ke Git).

| Property | Default | Keterangan |
|---|---|---|
| `spring.mail.host` | — | Host SMTP (misal: `smtp.gmail.com`) |
| `spring.mail.port` | — | Port SMTP (misal: `587`) |
| `spring.mail.username` | — | Username email pengirim |
| `spring.mail.password` | — | Password / App Password |
| `app.mail.from` | — | Alamat email pengirim |
| `app.mail.from-name` | `Orchestria System` | Nama pengirim |
| `services.request.base-url` | `http://localhost:8003` | URL request-service |
| `app.scheduler.enabled` | `true` | `false` untuk nonaktifkan semua scheduler |
| `app.scheduler.cron.weekly-report` | `0 0 8 * * MON` | Ekspresi cron laporan mingguan |

### Contoh konfigurasi Gmail

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.mail.from=your-email@gmail.com
```

> **Catatan:** Gunakan [App Password](https://myaccount.google.com/apppasswords) jika akun Gmail menggunakan 2FA.

---

## Endpoint

| Method | Path | Keterangan |
|---|---|---|
| `GET` | `/api/notifications/health` | Health check service |
| `GET` | `/api/reports/fund-requests.xlsx` | Download laporan Excel fund request |

### Health Check

```
GET http://localhost:8005/api/notifications/health
```

Response:
```json
{
  "success": true,
  "message": "Service berjalan normal",
  "data": {
    "service": "notification-report-service",
    "status": "UP",
    "port": 8005,
    "timestamp": "2026-06-20T10:00:00"
  }
}
```

### Laporan Excel

```
GET http://localhost:8005/api/reports/fund-requests.xlsx
```

Response: file `.xlsx` dengan kolom:
`ID | Judul | Divisi | Pemohon | Status | Total (Rp) | Tanggal Dibuat`

---

## Penjelasan Konsep

### 1. `@Scheduled(fixedRate = N)`

Dieksekusi setiap **N milidetik** dari **waktu mulai** eksekusi sebelumnya, tanpa memperhatikan berapa lama task selesai.

```java
@Scheduled(fixedRate = 300_000) // setiap 5 menit
public void healthPingScheduler() { ... }
```

**Cocok untuk:** monitoring periodik yang tidak bergantung durasi task.

---

### 2. `@Scheduled(fixedDelay = N)`

Dieksekusi **N milidetik** setelah eksekusi sebelumnya **selesai**. Menjamin tidak ada dua proses yang berjalan bersamaan.

```java
@Scheduled(fixedDelay = 600_000) // 10 menit setelah selesai
public void retryFailedEmailScheduler() { ... }
```

**Cocok untuk:** retry queue, proses yang harus selesai sebelum dijalankan ulang.

---

### 3. `@Scheduled(cron = "...")`

Format 6-field Spring Cron: `detik menit jam hari-bulan bulan hari-minggu`

```java
@Scheduled(cron = "0 0 8 * * MON") // setiap Senin jam 08:00
public void weeklyReportReminderScheduler() { ... }
```

| Field | Nilai | Contoh |
|---|---|---|
| Detik | 0-59 | `0` = detik ke-0 |
| Menit | 0-59 | `0` = menit ke-0 |
| Jam | 0-23 | `8` = pukul 08:00 |
| Hari bulan | 1-31 atau `*` | `*` = semua hari |
| Bulan | 1-12 atau `*` | `*` = semua bulan |
| Hari minggu | MON-SUN | `MON` = Senin |

---

### 4. Spring Event Publisher + `@EventListener`

Spring menyediakan mekanisme publish-subscribe internal berbasis `ApplicationEventPublisher`.

**Publikasi event:**
```java
// Di NotificationServiceImpl
eventPublisher.publishEvent(new NotificationEvent(this, "REPORT_READY", "Laporan siap"));
```

**Penanganan event:**
```java
// Di NotificationEventListener
@EventListener
public void onNotificationEvent(NotificationEvent event) {
    log.info("Event diterima: {}", event.getEventType());
    // lakukan tindakan...
}
```

**Keuntungan:**
- Decoupling antar komponen
- Mudah diperluas (tambah listener baru tanpa ubah publisher)
- Mendukung async dengan `@Async`

**Contoh alur dalam service ini:**
```
ReportService.generateFundRequestExcel()
    → publishEvent(NotificationEvent("REPORT_READY", ...))
    → NotificationEventListener.onNotificationEvent()
    → log + tindakan lanjutan
```

---

### 5. SMTP + JavaMailSender

Spring Boot mengkonfigurasi `JavaMailSender` secara otomatis dari properties `spring.mail.*`.

```java
// Kirim plain text
mailSender.send(simpleMailMessage);

// Kirim HTML dengan Cc/Bcc
MimeMessage mimeMessage = mailSender.createMimeMessage();
MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
helper.setText(htmlBody, true); // true = HTML
mailSender.send(mimeMessage);
```

Kegagalan pengiriman email **tidak melempar exception ke pemanggil** — dicatat di log dengan level `ERROR`.

---

### 6. Apache POI (Excel)

[Apache POI](https://poi.apache.org/) adalah library Java untuk membaca dan menulis file Microsoft Office.

```java
// Buat workbook baru
XSSFWorkbook workbook = new XSSFWorkbook();
Sheet sheet = workbook.createSheet("Fund Requests");

// Buat header
Row headerRow = sheet.createRow(0);
headerRow.createCell(0).setCellValue("ID");
// ...

// Buat baris data
Row dataRow = sheet.createRow(1);
dataRow.createCell(0).setCellValue(fundRequest.getId());
// ...

// Tulis ke output stream
workbook.write(outputStream);
```

Library yang digunakan: `poi-ooxml` (untuk format `.xlsx` / OOXML).

---

## Testing

### Jalankan semua test

```bash
./mvnw test
```

### Jalankan verify (test + build)

```bash
./mvnw clean verify
```

### Coverage Test

| Test Class | Yang Diuji |
|---|---|
| `NotificationReportServiceApplicationTests` | Context load |
| `HealthControllerTest` | Endpoint health (MockMvc) |
| `NotificationEventListenerTest` | Event listener semua tipe event |
| `ReportServiceImplTest` | Fetch data, generate Excel, event dipublikasikan |
| `NotificationSchedulerTest` | Scheduler enabled/disabled, tidak kirim email nyata |

Test menggunakan profile `test` (`application-test.properties`) yang terisolasi dari infrastruktur eksternal (tidak ada koneksi SMTP nyata, tidak ada database, request-service di-mock).
