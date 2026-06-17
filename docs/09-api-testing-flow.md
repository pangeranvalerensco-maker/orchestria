# 09. API Testing Flow

Dokumen ini menjelaskan urutan testing API Orchestria current MVP menggunakan Postman.

Dokumen ini hanya berfokus pada current MVP yang sudah/sedang diimplementasikan:

```text id="h8xg0m"
Auth
Organization
Fund Request
Approval
Finance Disbursement
Settlement
API Gateway
```

Full scope seperti Archive, Asset, Cleanliness, English Activity, Notification, dan Reporting belum masuk testing flow ini karena masih planned.

## Base URL

Semua request disarankan melalui API Gateway.

```text id="p1b74w"
http://localhost:8000
```

Service internal:

| Service              | Port |
| -------------------- | ---: |
| api-gateway          | 8000 |
| auth-service         | 8001 |
| organization-service | 8002 |
| request-service      | 8003 |
| finance-service      | 8004 |

## Postman Environment

Buat environment Postman dengan variable berikut:

| Variable         | Value                                              |
| ---------------- | -------------------------------------------------- |
| `baseUrl`        | `http://localhost:8000`                            |
| `token`          | Diisi otomatis/manual setelah login                |
| `requestId`      | Diisi setelah create request                       |
| `fundRequestId`  | Sama dengan `requestId`                            |
| `disbursementId` | Diisi setelah create disbursement                  |
| `settlementId`   | Opsional jika response mengembalikan ID settlement |

Contoh penggunaan:

```text id="06alvv"
{{baseUrl}}/api/auth/login
```

Authorization header:

```http id="kntkui"
Authorization: Bearer {{token}}
```

## Urutan Service Run

Sebelum testing, jalankan service berikut:

```text id="glz3yk"
1. auth-service
2. organization-service
3. request-service
4. finance-service
5. api-gateway
```

Pastikan semua service berjalan tanpa error dan database PostgreSQL aktif.

## Flow Utama Current MVP

Urutan testing utama:

```text id="k1vb0v"
Login
→ Create Fund Request
→ Add Request Item
→ Submit Request
→ Division Approval
→ PUB Approval
→ Pembina Approval
→ Create Finance Disbursement
→ Mark Request as Disbursed
→ Confirm Fund Received
→ Submit Settlement
→ Approve Settlement
→ Check Request Detail
→ Check Approval Timeline
→ Check Status History
```

Expected final status:

```text id="4n57xx"
COMPLETED
```

---

# 1. Login

Endpoint:

```http id="o071z3"
POST {{baseUrl}}/api/auth/login
```

Body:

```json id="cbd2i7"
{
  "email": "ISI_EMAIL_USER",
  "password": "ISI_PASSWORD_USER"
}
```

Expected status:

```text id="fbk1fn"
200 OK
```

Expected response berisi token JWT.

Contoh response:

```json id="redjy0"
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "JWT_TOKEN",
    "email": "user@example.com",
    "fullName": "User Example",
    "roles": ["SUPER_ADMIN"],
    "permissions": [
      "request.create",
      "request.read.all",
      "finance.disburse"
    ]
  }
}
```

Setelah login, copy token ke environment variable:

```text id="dxtiro"
token = JWT_TOKEN
```

Catatan:

```text id="6k5r7g"
Gunakan user dengan role berbeda untuk security testing.
Minimal siapkan token untuk SUPER_ADMIN, ANGGOTA, KETUA_DIVISI, KETUA_PUB, PEMBINA, dan BENDAHARA_INTERNAL.
```

---

# 2. Test Current User

Endpoint:

```http id="1f1wfp"
GET {{baseUrl}}/api/auth/me
```

Headers:

```http id="7sm1m4"
Authorization: Bearer {{token}}
```

Expected status:

```text id="bfgaeu"
200 OK
```

Tujuan:

```text id="2pemfb"
Memastikan token valid dan user login dapat dibaca.
```

---

# 3. Create Fund Request

Gunakan token user yang memiliki permission:

```text id="jgxe4o"
request.create
```

Endpoint:

```http id="fyfwzo"
POST {{baseUrl}}/api/requests
```

Headers:

```http id="c7hp3p"
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json id="dbhjwl"
{
  "divisionId": 1,
  "divisionName": "Divisi Kesejahteraan",
  "requesterMemberId": 1,
  "requesterName": "Pangeran Valerensco Rivaldi Hutabarat",
  "requesterAuthUserId": 1,
  "title": "Pengajuan Konsumsi Rapat Divisi",
  "description": "Konsumsi untuk rapat koordinasi divisi",
  "activityDate": "2026-06-20",
  "priority": "MEDIUM"
}
```

Expected status:

```text id="4amh40"
201 Created
```

Expected status pengajuan:

```text id="9iu4kr"
DRAFT
```

Simpan ID pengajuan ke variable:

```text id="roiehg"
requestId = response.data.id
fundRequestId = response.data.id
```

---

# 4. Add Request Item

Endpoint:

```http id="o56f6r"
POST {{baseUrl}}/api/requests/{{requestId}}/items
```

Headers:

```http id="ba0rhv"
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json id="ejp7wb"
{
  "itemName": "Nasi Box",
  "description": "Konsumsi peserta rapat",
  "quantity": 10,
  "unitPrice": 25000
}
```

Expected status:

```text id="eaty4g"
201 Created
```

Expected subtotal:

```text id="9d3rlr"
250000
```

Tambahkan item kedua jika ingin menguji total amount:

```json id="r8m34k"
{
  "itemName": "Air Mineral",
  "description": "Air mineral untuk peserta rapat",
  "quantity": 2,
  "unitPrice": 20000
}
```

Expected total jika dua item:

```text id="t1urcs"
290000
```

---

# 5. Check Request Detail

Endpoint:

```http id="5p9lan"
GET {{baseUrl}}/api/requests/{{requestId}}
```

Headers:

```http id="ujmecf"
Authorization: Bearer {{token}}
```

Expected status:

```text id="3xdq0h"
200 OK
```

Expected data:

```text id="cv1uwq"
status = DRAFT
items terisi
totalAmount sesuai subtotal item
```

---

# 6. Submit Request

Endpoint:

```http id="hu2ape"
POST {{baseUrl}}/api/requests/{{requestId}}/submit
```

Headers:

```http id="iu5rvs"
Authorization: Bearer {{token}}
```

Expected status:

```text id="y099gj"
200 OK
```

Expected status change:

```text id="eqzotd"
DRAFT → SUBMITTED
```

Setelah submit, pengajuan masuk ke tahap approval Ketua Divisi.

---

# 7. Division Approval

Gunakan token user yang memiliki permission:

```text id="nyxqph"
request.approve.division
```

Endpoint:

```http id="58fuvl"
POST {{baseUrl}}/api/requests/{{requestId}}/approvals/approve
```

Headers:

```http id="ui0g9u"
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json id="3lcsoc"
{
  "level": "DIVISION",
  "approverName": "Ketua Divisi Kesejahteraan",
  "note": "Disetujui oleh Ketua Divisi"
}
```

Expected status:

```text id="dj0v3v"
200 OK
```

Expected status change:

```text id="4x2rja"
SUBMITTED → DIVISION_APPROVED
```

---

# 8. PUB Approval

Gunakan token user yang memiliki permission:

```text id="fzd2b1"
request.approve.pub
```

Endpoint:

```http id="cjfsqm"
POST {{baseUrl}}/api/requests/{{requestId}}/approvals/approve
```

Headers:

```http id="75pgww"
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json id="eal9dj"
{
  "level": "PUB",
  "approverName": "Ketua PUB",
  "note": "Disetujui oleh Ketua PUB"
}
```

Expected status:

```text id="5u6z1i"
200 OK
```

Expected status change:

```text id="uet4s9"
DIVISION_APPROVED → PUB_APPROVED
```

---

# 9. Pembina Approval

Gunakan token user yang memiliki permission:

```text id="wjlhg1"
request.approve.pembina
```

Endpoint:

```http id="ykmdfc"
POST {{baseUrl}}/api/requests/{{requestId}}/approvals/approve
```

Headers:

```http id="spudjd"
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json id="0lzo2o"
{
  "level": "PEMBINA",
  "approverName": "Pembina PUB",
  "note": "Disetujui oleh Pembina"
}
```

Expected status:

```text id="q9ble7"
200 OK
```

Expected status change:

```text id="eb4d0b"
PUB_APPROVED → READY_FOR_DISBURSEMENT
```

---

# 10. Create Finance Disbursement

Gunakan token user yang memiliki permission:

```text id="6h9y4b"
finance.disburse
```

Endpoint:

```http id="g9rrs7"
POST {{baseUrl}}/api/finance/disbursements
```

Headers:

```http id="tonlok"
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json id="3s32kw"
{
  "fundRequestId": 1,
  "requestTitle": "Pengajuan Konsumsi Rapat Divisi",
  "divisionId": 1,
  "divisionName": "Divisi Kesejahteraan",
  "requesterName": "Pangeran Valerensco Rivaldi Hutabarat",
  "amount": 250000,
  "method": "CASH",
  "receiverName": "Pangeran Valerensco Rivaldi Hutabarat",
  "receiverNote": "Dana diterima langsung oleh pengaju",
  "proofUrl": "https://example.com/proof-disbursement.jpg",
  "note": "Pencairan dana konsumsi rapat"
}
```

Catatan:

```text id="8xps6q"
Sesuaikan fundRequestId dengan {{requestId}}.
Jika nominal totalAmount berbeda karena ada lebih dari satu item, sesuaikan amount.
```

Expected status:

```text id="fbyrce"
201 Created
```

Expected finance status:

```text id="fai08o"
DISBURSED
```

Simpan ID disbursement jika diperlukan:

```text id="cyvxz3"
disbursementId = response.data.id
```

---

# 11. Verify Request Disbursement Status

Setelah pencairan berhasil dibuat di finance-service, finance-service otomatis
memperbarui status pengajuan di request-service.

Tidak perlu memanggil endpoint `mark-disbursed` secara manual.

Verifikasi menggunakan:

```http
GET {{baseUrl}}/api/requests/{{requestId}}
```

Expected status:

DISBURSED

Hapus instruksi manual:

```http
POST /api/requests/{{requestId}}/mark-disbursed
```

---

# 12. Confirm Fund Received

Gunakan token pengaju atau user dengan permission:

```text id="19m3o7"
request.create
```

Endpoint:

```http id="sy8juk"
POST {{baseUrl}}/api/requests/{{requestId}}/confirm-received
```

Headers:

```http id="sg5gem"
Authorization: Bearer {{token}}
```

Expected status:

```text id="67zq6m"
200 OK
```

Expected status change:

```text id="r89v2f"
DISBURSED → FUND_RECEIVED
```

---

# 13. Submit Settlement

Gunakan token pengaju atau user dengan permission:

```text id="ssiotv"
request.create
```

Endpoint:

```http id="nk52x2"
POST {{baseUrl}}/api/requests/{{requestId}}/settlement
```

Headers:

```http id="eznlzw"
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body contoh dana tersisa:

```json id="qgvy6m"
{
  "spentAmount": 240000,
  "proofUrl": "https://example.com/struk-settlement.jpg",
  "note": "Dana digunakan untuk konsumsi rapat, sisa 10000"
}
```

Expected status:

```text id="lbexbh"
200 OK
```

Expected status change:

```text id="qiespn"
FUND_RECEIVED → SETTLEMENT_SUBMITTED
```

Expected calculation:

```text id="9d5wsl"
remainingAmount = totalAmount - spentAmount
shortageAmount = 0
```

Contoh dana kurang:

```json id="ivxcsk"
{
  "spentAmount": 270000,
  "proofUrl": "https://example.com/struk-settlement.jpg",
  "note": "Dana kurang karena harga konsumsi naik"
}
```

Expected calculation:

```text id="bz9eiz"
remainingAmount = 0
shortageAmount = spentAmount - totalAmount
```

---

# 14. Approve Settlement

Gunakan token user yang memiliki permission:

```text id="te5682"
finance.settlement.verify
```

Endpoint:

```http id="geazz2"
POST {{baseUrl}}/api/requests/{{requestId}}/settlement/approve
```

Headers:

```http id="opbhpy"
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json id="mv0gol"
{
  "note": "Settlement disetujui oleh bendahara"
}
```

Expected status:

```text id="qiyih9"
200 OK
```

Expected status change:

```text id="onbl3y"
SETTLEMENT_SUBMITTED → COMPLETED
```

Expected final status:

```text id="n4tsrf"
COMPLETED
```

---

# 15. Check Final Request Detail

Endpoint:

```http id="z9ggan"
GET {{baseUrl}}/api/requests/{{requestId}}
```

Headers:

```http id="fg0z3q"
Authorization: Bearer {{token}}
```

Expected status:

```text id="9pp1lc"
200 OK
```

Expected data:

```text id="4w8l2i"
status = COMPLETED
settlement terisi
items terisi
totalAmount sesuai
```

---

# 16. Check Approval Timeline

Endpoint:

```http id="u05nl6"
GET {{baseUrl}}/api/requests/{{requestId}}/approvals
```

Headers:

```http id="lc4nvb"
Authorization: Bearer {{token}}
```

Expected status:

```text id="ui1uel"
200 OK
```

Expected approval levels:

```text id="tmyspu"
DIVISION
PUB
PEMBINA
```

Expected decisions:

```text id="o4lmn7"
APPROVED
APPROVED
APPROVED
```

---

# 17. Check Status History

Endpoint:

```http id="j53vxz"
GET {{baseUrl}}/api/requests/{{requestId}}/histories
```

Headers:

```http id="gctblo"
Authorization: Bearer {{token}}
```

Expected status:

```text id="s53xa5"
200 OK
```

Expected history:

```text id="1zyqac"
DRAFT → SUBMITTED
SUBMITTED → DIVISION_APPROVED
DIVISION_APPROVED → PUB_APPROVED
PUB_APPROVED → READY_FOR_DISBURSEMENT
READY_FOR_DISBURSEMENT → DISBURSED
DISBURSED → FUND_RECEIVED
FUND_RECEIVED → SETTLEMENT_SUBMITTED
SETTLEMENT_SUBMITTED → COMPLETED
```

---

# 18. Check Finance Disbursement List

Gunakan token user yang memiliki permission:

```text id="dk0u2n"
finance.report.read
```

atau:

```text id="br7c2r"
finance.disburse
```

Endpoint:

```http id="i04ggk"
GET {{baseUrl}}/api/finance/disbursements?page=0&size=10
```

Headers:

```http id="qlfhbi"
Authorization: Bearer {{token}}
```

Expected status:

```text id="nxbqgk"
200 OK
```

---

# 19. Check Finance Disbursement by Request

Endpoint:

```http id="bjztsr"
GET {{baseUrl}}/api/finance/disbursements/by-request/{{requestId}}
```

Headers:

```http id="dfrz4s"
Authorization: Bearer {{token}}
```

Expected status:

```text id="b8iuc8"
200 OK
```

Expected data:

```text id="k4m11n"
fundRequestId = {{requestId}}
status = DISBURSED
```

---

# 20. My Requests Test

Gunakan token user dengan permission:

```text id="sytd6q"
request.read.own
```

Endpoint:

```http id="zq4wvd"
GET {{baseUrl}}/api/requests/my
```

Headers:

```http id="bk8pzf"
Authorization: Bearer {{token}}
```

Expected status:

```text id="me7wup"
200 OK
```

Expected:

```text id="r7l27r"
Hanya menampilkan pengajuan milik user login.
```

---

# 21. All Requests Test

Gunakan token user dengan permission:

```text id="gjoy81"
request.read.all
```

Endpoint:

```http id="uy3yhf"
GET {{baseUrl}}/api/requests?page=0&size=10
```

Headers:

```http id="bpe3jo"
Authorization: Bearer {{token}}
```

Expected status:

```text id="6ziprf"
200 OK
```

Expected:

```text id="i31m38"
Menampilkan semua pengajuan.
```

---

# 22. Security Test — No Token

Endpoint:

```http id="f9qd62"
GET {{baseUrl}}/api/requests
```

Tanpa header Authorization.

Expected status:

```text id="74xjnq"
401 Unauthorized
```

Tujuan:

```text id="52o3m5"
Memastikan endpoint protected tidak bisa diakses tanpa token.
```

---

# 23. Security Test — Invalid Token

Endpoint:

```http id="kq7ug8"
GET {{baseUrl}}/api/requests
```

Header:

```http id="koj2hr"
Authorization: Bearer token_salah
```

Expected status:

```text id="rrl9v2"
401 Unauthorized
```

---

# 24. Security Test — Permission Kurang

Gunakan token role `ANGGOTA`.

Endpoint:

```http id="tb1hiq"
GET {{baseUrl}}/api/requests
```

Expected status:

```text id="5ibtr0"
403 Forbidden
```

Alasan:

```text id="bbmakk"
ANGGOTA tidak memiliki request.read.all.
```

---

# 25. Security Test — Anggota Access Finance

Gunakan token role `ANGGOTA`.

Endpoint:

```http id="ag5szf"
POST {{baseUrl}}/api/finance/disbursements
```

Expected status:

```text id="f03rkl"
403 Forbidden
```

Alasan:

```text id="potuh5"
ANGGOTA tidak memiliki finance.disburse.
```

---

# 26. Security Test — Wrong Approval Level

Gunakan token `KETUA_DIVISI`, lalu coba approval level PUB.

Endpoint:

```http id="5wayua"
POST {{baseUrl}}/api/requests/{{requestId}}/approvals/approve
```

Body:

```json id="pzl4qw"
{
  "level": "PUB",
  "approverName": "Ketua Divisi Kesejahteraan",
  "note": "Mencoba approval PUB dengan role Ketua Divisi"
}
```

Expected:

```text id="b5ly91"
403 Forbidden
```

atau:

```text id="t7423b"
400/409 Business Rule Error
```

Catatan:

```text id="llw76a"
Hasil tergantung implementasi validasi permission level.
Yang penting user tidak boleh berhasil approval level yang bukan kewenangannya.
```

---

# 27. Security Test — Wrong Status Transition

Contoh: submit request yang sudah disubmit.

Endpoint:

```http id="w2q5yr"
POST {{baseUrl}}/api/requests/{{requestId}}/submit
```

Expected:

```text id="jc4ncm"
400 Bad Request
```

atau:

```text id="0i83un"
409 Conflict
```

Alasan:

```text id="qq6np6"
Request tidak lagi berada pada status DRAFT.
```

---

# 28. Reject Flow Test

Gunakan request baru agar tidak mengganggu flow utama.

Alur:

```text id="ocs8hu"
Create request
→ Add item
→ Submit
→ Reject pada level DIVISION/PUB/PEMBINA
```

Endpoint:

```http id="g6ezn8"
POST {{baseUrl}}/api/requests/{{requestId}}/approvals/reject
```

Body:

```json id="sd1sh6"
{
  "level": "DIVISION",
  "approverName": "Ketua Divisi Kesejahteraan",
  "note": "Pengajuan ditolak karena data belum sesuai"
}
```

Expected status change:

```text id="gkx8fy"
SUBMITTED → REJECTED
```

---

# 29. Revision Flow Test

Gunakan request baru agar tidak mengganggu flow utama.

Endpoint:

```http id="n6mzgq"
POST {{baseUrl}}/api/requests/{{requestId}}/approvals/revision
```

Body:

```json id="sfccn2"
{
  "level": "DIVISION",
  "approverName": "Ketua Divisi Kesejahteraan",
  "note": "Pengajuan perlu revisi pada item kebutuhan"
}
```

Expected status change:

```text id="vi1b9q"
SUBMITTED → REVISION_REQUESTED
```

---

# 30. Checklist Demo UAS

Checklist sebelum demo:

| Checklist                            | Status |
| ------------------------------------ | ------ |
| Semua service running                |        |
| Gateway port 8000 running            |        |
| Login berhasil                       |        |
| Token bisa digunakan di service lain |        |
| Create request berhasil              |        |
| Add item berhasil                    |        |
| Submit berhasil                      |        |
| Approval Division berhasil           |        |
| Approval PUB berhasil                |        |
| Approval Pembina berhasil            |        |
| Finance disbursement berhasil        |        |
| Mark disbursed berhasil              |        |
| Confirm received berhasil            |        |
| Submit settlement berhasil           |        |
| Approve settlement berhasil          |        |
| Final status `COMPLETED`             |        |
| Approval timeline muncul             |        |
| Status history muncul                |        |
| 401 test berhasil                    |        |
| 403 test berhasil                    |        |
| Wrong status transition ditolak      |        |

## Expected Final Demo Result

Pada akhir demo utama, pengajuan harus memiliki status:

```text id="vv4bti"
COMPLETED
```

Dengan histori:

```text id="nn7bir"
DRAFT
SUBMITTED
DIVISION_APPROVED
PUB_APPROVED
READY_FOR_DISBURSEMENT
DISBURSED
FUND_RECEIVED
SETTLEMENT_SUBMITTED
COMPLETED
```

## Catatan Penting

* Gunakan data dummy.
* Jangan gunakan password asli di dokumentasi.
* Jangan commit token JWT ke repository.
* Jangan commit secret database/JWT ke repository.
* Jika endpoint gagal via gateway, test langsung ke port service untuk memastikan apakah masalah ada di service atau gateway.
* Jika hasil status berbeda, cek implementasi enum dan business rule pada request-service.
