# 11. Java Lanjutan Course Coverage Matrix

Dokumen ini memetakan materi pelatihan Java Lanjutan PUB 2026 terhadap implementasi Orchestria.

Tujuannya bukan sekadar menyebut teknologi, tetapi memastikan setiap materi mempunyai bukti implementasi yang dapat ditunjukkan melalui source code, endpoint, database, frontend, log, atau file hasil export.

## Status

| Status | Arti |
| --- | --- |
| `IMPLEMENTED` | Sudah ada di repository dan dapat digunakan |
| `PARTIAL` | Sudah ada sebagian, tetapi belum memenuhi bukti demo penuh |
| `NOT IMPLEMENTED` | Belum tersedia |
| `DEMO TARGET` | Wajib dibuat sebelum presentasi |
| `OPTIONAL ENHANCEMENT` | Disebut pada overview, tetapi tidak ditemukan sebagai materi pertemuan khusus |

## Pertemuan 1 — Overview Java Lanjutan

Materi:

- review MVC;
- REST API;
- JSON;
- microservices backend;
- frontend;
- PostgreSQL;
- Git;
- reporting Excel dengan Apache POI;
- reporting PDF dengan Jasper;
- tools development.

| Materi | Bukti di Orchestria | Status | Tindakan |
| --- | --- | --- | --- |
| MVC/layered architecture | Controller, service, repository, entity, payload | `IMPLEMENTED` | Pertahankan boundary package |
| REST API | Endpoint Auth, Organization, Request, Finance | `IMPLEMENTED` | Tambah endpoint domain tersisa |
| JSON | Request/response standar dan payload frontend | `IMPLEMENTED` | Tidak ada gap utama |
| Microservices | Auth, Organization, Request, Finance, Gateway | `IMPLEMENTED` | Tambah Notification Report Service |
| Frontend | React + TypeScript menggantikan contoh Angular | `IMPLEMENTED` | Lengkapi seluruh module UI |
| PostgreSQL | Database per service | `IMPLEMENTED` | Tambah DB notification/report |
| Git | Commit history dan remote GitHub | `IMPLEMENTED` | Gunakan commit kecil |
| Excel Apache POI | Belum ada import/export/template | `NOT IMPLEMENTED` | Wajib di notification-report-service |
| PDF Jasper | Belum ada | `OPTIONAL ENHANCEMENT` | Implementasi minimal jika waktu memungkinkan |

Catatan: React dipakai sebagai frontend karena ketentuan UAS membebaskan teknologi frontend. Konsep FE–BE tetap sama.

## Pertemuan 2 — API, JSON, Postman, dan Microservices

Materi:

- pengujian API melalui Postman;
- struktur JSON;
- komunikasi antarmicroservice;
- perbedaan microservices dan monolith.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| Postman testing | `docs/09-api-testing-flow.md` dan pengujian endpoint | `PARTIAL` | Perbarui collection/flow final |
| JSON object dan array | Payload request/response pada seluruh service | `IMPLEMENTED` | Tambah contoh import/export |
| Microservice independence | Database per service | `IMPLEMENTED` | Jangan membuat shared database |
| Inter-service communication | OrganizationClient dan RequestClient | `IMPLEMENTED` | Tambah integrasi notification/report |

## Pertemuan 3 — FE ke BE dan API Gateway

Materi:

- alur frontend memanggil backend;
- request params;
- authorization header;
- HTTP headers;
- request body;
- API Gateway sebagai single entry point.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| Frontend memanggil backend | `frontend/src/api/http.ts` | `IMPLEMENTED` | Gunakan gateway untuk semua module |
| Query params | Pagination My Requests | `IMPLEMENTED` | Gunakan filter reporting |
| Authorization header | Bearer JWT pada API client | `IMPLEMENTED` | Pertahankan |
| Request body | Login, request, item, approval | `IMPLEMENTED` | Tambah payload module baru |
| API Gateway | Port 8000 dan routing service | `IMPLEMENTED` | Tambah route `/api/notifications/**` dan `/api/reports/**` |

## Pertemuan 4 — Exception Handling dan Praktik Dua Microservice

Materi:

- `try`, `catch`, dan `finally`;
- graceful error handling;
- dua microservice Java;
- service-to-service request;
- PostgreSQL;
- annotation dan dependency injection;
- `@RequestParam` dan `@RequestBody`.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| Try-catch | API client, service integration, frontend API error handling | `IMPLEMENTED` | Pastikan error tidak ditelan tanpa log |
| Global exception handling | `GlobalExceptionHandler` pada service utama | `IMPLEMENTED` | Tambah pada service baru |
| Service-to-service call | Request → Organization, Finance → Request | `IMPLEMENTED` | Tambah timeout/error mapping jika sempat |
| PostgreSQL/JPA | Entity dan repository per service | `IMPLEMENTED` | Tambah entity module baru |
| Dependency injection | Constructor injection/Lombok | `IMPLEMENTED` | Pertahankan |
| RequestParam/RequestBody | Pagination dan command endpoint | `IMPLEMENTED` | Tidak ada gap utama |

## Pertemuan 5 — Object, CRUD, ArrayList, dan Perulangan

Materi:

- class `Object`;
- casting/parsing;
- GET, POST, PUT, DELETE;
- `ArrayList`;
- perulangan;
- CRUD data.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| Java Object dan DTO mapping | Payload dan generic `ApiResponse<T>` | `IMPLEMENTED` | Jelaskan saat presentasi |
| GET | Banyak endpoint read | `IMPLEMENTED` | - |
| POST | Login, create request, approval, disbursement | `IMPLEMENTED` | - |
| PUT | CRUD Organization/Auth | `IMPLEMENTED` | Pastikan satu contoh dapat didemokan |
| DELETE | CRUD Organization/Auth | `IMPLEMENTED` | Pastikan satu contoh dapat didemokan |
| List/ArrayList | Collection pada role, permission, items, approval | `IMPLEMENTED` | - |
| Loop/stream | Mapping entity, authority, item, report | `IMPLEMENTED` | Tambah pada Excel/report |

## Pertemuan 6 — JSON Object dan Email Java

Materi:

- parsing JSON Object dan array of objects;
- pengiriman email melalui Java;
- SMTP configuration;
- HTML email body;
- To, Cc, dan Bcc.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| JSON Object parsing | Jackson otomatis pada Spring MVC dan frontend JSON parsing | `IMPLEMENTED` | Tambah dokumentasi contoh |
| Java Mail/Spring Mail | Belum ada | `NOT IMPLEMENTED` | Buat `EmailService` |
| SMTP properties | Belum ada | `NOT IMPLEMENTED` | Gunakan environment variables, jangan commit password |
| HTML email | Belum ada | `NOT IMPLEMENTED` | Buat template approval/reminder |
| To/Cc/Bcc | Belum ada | `NOT IMPLEMENTED` | Sediakan request DTO dan demo aman |

Acceptance demo:

```text
POST /api/notifications/email
→ menyimpan notification log
→ mencoba mengirim email
→ mengembalikan status berhasil/gagal secara terkontrol
```

## Pertemuan 7 — Scheduler

Materi:

- `@EnableScheduling`;
- `@Scheduled`;
- fixed rate;
- fixed delay;
- cron;
- scheduler execution history;
- scheduler pengiriman email.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| Scheduler foundation | Belum ada | `NOT IMPLEMENTED` | Aktifkan di notification-report-service |
| Fixed rate | Belum ada | `NOT IMPLEMENTED` | Demo heartbeat/report snapshot |
| Fixed delay | Belum ada | `NOT IMPLEMENTED` | Demo retry notification queue |
| Cron | Belum ada | `NOT IMPLEMENTED` | Demo daily reminder, cron configurable |
| Scheduler history | Belum ada | `NOT IMPLEMENTED` | Buat `scheduled_job_logs` |
| Scheduler email | Belum ada | `NOT IMPLEMENTED` | Kirim reminder melalui EmailService |

Scheduler produksi harus configurable agar demo tidak menunggu terlalu lama.

## Materi Tambahan — Event, Listener, Function, dan Trigger

Deck Pertemuan 8 tidak tersedia pada file yang diunggah. Berdasarkan pembahasan kelas dan catatan project, materi tambahan yang perlu dibuktikan adalah:

- application event;
- event publisher;
- event listener;
- perbedaan business logic Java dan database trigger/function.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| ApplicationEventPublisher | Belum ada | `NOT IMPLEMENTED` | Publish event saat request disubmit/diapprove |
| Event listener | Belum ada | `NOT IMPLEMENTED` | Listener membuat notification request/log |
| Database function/trigger | Belum menjadi fitur utama | `PARTIAL` | Tambah trigger audit sederhana atau dokumentasikan alasan logic utama di Java |
| Business logic Java | Workflow request berada di service layer | `IMPLEMENTED` | Pertahankan sebagai source of truth |

Implementasi yang disarankan:

```text
RequestApprovedEvent
→ listener
→ notification log
→ email notification
```

## Pertemuan 9 — Authentication, Authorization, Stateful, Stateless

Materi:

- authentication;
- authorization;
- role dan permission;
- stateful;
- stateless;
- REST API security sederhana.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| Authentication | Login auth-service | `IMPLEMENTED` | - |
| Authorization | Role, permission, `@PreAuthorize` | `IMPLEMENTED` | Lengkapi module baru |
| Stateless | JWT pada service | `IMPLEMENTED` | - |
| Stateful | Belum ada demonstrasi session | `NOT IMPLEMENTED` | Buat endpoint demo session terisolasi |
| Ownership authorization | My Requests dan validation | `IMPLEMENTED` | Test 403/404 |

Stateful demo tidak boleh mengganti arsitektur utama JWT. Gunakan endpoint khusus untuk membuktikan konsep session.

## Pertemuan 10 — Spring Bean dan Authentication Stateful

Materi:

- IoC container;
- `@Configuration`;
- `@Bean`;
- dependency injection;
- session timeout;
- login/profile/logout stateful.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| Spring Bean | `SecurityFilterChain`, `PasswordEncoder`, CORS, client config | `IMPLEMENTED` | Siapkan satu endpoint/demo penjelasan |
| IoC/DI | Constructor injection | `IMPLEMENTED` | - |
| Stateful login | Belum ada | `NOT IMPLEMENTED` | Buat session demo |
| Session timeout | Belum ada | `NOT IMPLEMENTED` | Configurable property |
| Session logout | Belum ada | `NOT IMPLEMENTED` | Endpoint khusus |

## Pertemuan 11 — Authorization dan Method Security

Materi:

- tabel user, role, menu/permission;
- entity dan repository;
- SecurityConfig;
- method authorization;
- `@PreAuthorize`;
- custom permission evaluation;
- testing forbidden access.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| User/role/permission tables | Auth database | `IMPLEMENTED` | - |
| Entity/repository | Auth service | `IMPLEMENTED` | - |
| SecurityConfig | Service utama | `IMPLEMENTED` | Tambah pada service baru |
| `@PreAuthorize` | Auth, Organization, Request, Finance | `IMPLEMENTED` | Tambah module permissions |
| Forbidden test | Test/security flow | `PARTIAL` | Dokumentasikan matrix 401/403 final |
| Custom evaluator | Tidak dipakai; authority langsung dipakai | `IMPLEMENTED` secara ekuivalen | Jelaskan keputusan arsitektur |

Penggunaan authority langsung dari JWT adalah implementasi sah dari tujuan authorization, walaupun struktur class tidak harus identik dengan contoh materi.

## Pertemuan 12 — Authentication Stateless dan JWT

Materi:

- dependency JWT;
- JWT utility/service;
- JWT filter;
- stateless SecurityConfig;
- login token generator;
- token valid dan invalid.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| JJWT dependency | POM service terkait | `IMPLEMENTED` | - |
| JwtService/JwtUtil | Auth dan service lain | `IMPLEMENTED` | Kurangi duplikasi setelah UAS, bukan sekarang |
| JwtAuthenticationFilter | Service utama | `IMPLEMENTED` | Tambah pada service baru |
| Stateless session | SecurityConfig | `IMPLEMENTED` | - |
| Token generation | Auth login | `IMPLEMENTED` | - |
| Invalid token handling | Security entry point/filter | `IMPLEMENTED` | Tambah test final |

## Pertemuan 13 — Excel Import, Export, dan Template

Materi:

- Apache POI;
- multipart upload;
- import data Excel ke database;
- export data database ke Excel;
- download template Excel;
- auto-increment ID;
- testing file melalui Postman.

| Materi | Bukti | Status | Tindakan |
| --- | --- | --- | --- |
| Apache POI dependency | Belum ada | `NOT IMPLEMENTED` | Tambah di notification-report-service |
| Import Excel | Belum ada | `NOT IMPLEMENTED` | Pilih data member/activity sebagai target |
| Export Excel | Belum ada | `NOT IMPLEMENTED` | Export laporan operasional |
| Template Excel | Belum ada | `NOT IMPLEMENTED` | Download template dengan header |
| Multipart endpoint | Belum ada | `NOT IMPLEMENTED` | Tambah controller upload |
| Error per row | Belum ada | `NOT IMPLEMENTED` | Return imported/failed row summary |

## Cakupan Tambahan Full Scope

Materi berikut bukan satu pertemuan khusus, tetapi dibutuhkan agar project utuh:

| Fitur | Status |
| --- | --- |
| Finance frontend | `NOT IMPLEMENTED` |
| Settlement frontend | `NOT IMPLEMENTED` |
| Notification log | `NOT IMPLEMENTED` |
| Reporting dashboard | `NOT IMPLEMENTED` |
| Archive module | `NOT IMPLEMENTED` |
| Asset module | `NOT IMPLEMENTED` |
| Cleanliness/Picket module | `NOT IMPLEMENTED` |
| English Activity module | `NOT IMPLEMENTED` |
| Division Activity frontend | `PARTIAL` |
| HUMAS/Public frontend | `PARTIAL` |

## Course Definition of Done

Materi dianggap benar-benar terimplementasi apabila memenuhi seluruh kondisi yang relevan:

- ada source code nyata;
- code dapat di-build;
- endpoint atau scheduler dapat dijalankan;
- data tersimpan atau hasil dapat diverifikasi;
- error path ditangani;
- permission diterapkan jika endpoint protected;
- tersedia langkah demo;
- dokumentasi menunjukkan file dan endpoint bukti;
- fitur tidak hanya berupa placeholder UI.

## Urutan Penyelesaian Gap

1. Core Finance dan Settlement melalui frontend.
2. Notification Report Service foundation.
3. Email.
4. Scheduler fixed rate, fixed delay, dan cron.
5. Application event dan listener.
6. Excel import/export/template.
7. Stateful session demo.
8. Reporting dashboard dan export.
9. Domain full scope lainnya.
10. Final security test dan dokumentasi.
