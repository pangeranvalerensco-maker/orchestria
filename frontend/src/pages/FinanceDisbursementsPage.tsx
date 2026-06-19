import { useCallback, useEffect, useState } from "react";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";
import {
  createDisbursement,
  getDisbursements,
  getReadyForDisbursement,
} from "../services/financeService";
import type { FundDisbursement } from "../types/finance";
import type { FundRequest } from "../types/request";

function formatCurrency(value: number) {
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    maximumFractionDigits: 0,
  }).format(value);
}

export function FinanceDisbursementsPage() {
  const { token } = useAuth();
  const [readyRequests, setReadyRequests] = useState<FundRequest[]>([]);
  const [disbursements, setDisbursements] = useState<FundDisbursement[]>([]);
  const [selectedRequest, setSelectedRequest] = useState<FundRequest | null>(null);
  const [method, setMethod] = useState<"CASH" | "BANK_TRANSFER" | "E_WALLET">("BANK_TRANSFER");
  const [receiverName, setReceiverName] = useState("");
  const [receiverNote, setReceiverNote] = useState("");
  const [proofUrl, setProofUrl] = useState("");
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      const [readyResponse, disbursementResponse] = await Promise.all([
        getReadyForDisbursement(token),
        getDisbursements(token),
      ]);
      setReadyRequests(readyResponse.data.content);
      setDisbursements(disbursementResponse.data.content);
    } catch (error) {
      setErrorMessage(error instanceof ApiError ? error.message : "Data keuangan tidak dapat dimuat.");
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  async function handleDisburse() {
    if (!token || !selectedRequest || !receiverName.trim()) return;
    setSubmitting(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await createDisbursement(token, {
        fundRequestId: selectedRequest.id,
        method,
        receiverName: receiverName.trim(),
        receiverNote: receiverNote.trim() || undefined,
        proofUrl: proofUrl.trim() || undefined,
        note: note.trim() || undefined,
      });
      setSuccessMessage(`Pencairan pengajuan #${selectedRequest.id} berhasil dicatat.`);
      setSelectedRequest(null);
      setReceiverName("");
      setReceiverNote("");
      setProofUrl("");
      setNote("");
      await loadData();
    } catch (error) {
      setErrorMessage(error instanceof ApiError ? error.message : "Pencairan gagal dicatat.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="page-content">
      <section className="page-heading">
        <div><p className="eyebrow">FINANCE</p><h1>Pencairan Dana</h1><p>Catat pencairan untuk pengajuan yang telah lolos seluruh approval.</p></div>
      </section>

      {errorMessage && <div className="alert alert-error" role="alert">{errorMessage}</div>}
      {successMessage && <div className="alert" role="status">{successMessage}</div>}

      <section className="content-card">
        <div className="card-heading"><div><p className="eyebrow">SIAP DICAIRKAN</p><h2>Pengajuan Pending</h2></div></div>
        {loading ? <div className="empty-state"><div className="spinner" /><p>Memuat data...</p></div> : readyRequests.length ? (
          <div className="request-table-wrapper"><table className="request-table"><thead><tr><th>Pengajuan</th><th>Pemohon</th><th>Total</th><th>Aksi</th></tr></thead><tbody>
            {readyRequests.map((request) => <tr key={request.id}><td><strong>#{request.id} {request.title}</strong><small>{request.divisionName}</small></td><td>{request.requesterName}</td><td>{formatCurrency(request.totalAmount)}</td><td><button className="primary-button" type="button" onClick={() => { setSelectedRequest(request); setReceiverName(request.requesterName); }}>Cairkan</button></td></tr>)}
          </tbody></table></div>
        ) : <div className="empty-state"><p>Tidak ada pengajuan yang siap dicairkan.</p></div>}
      </section>

      {selectedRequest && <section className="content-card item-form">
        <div className="card-heading"><div><p className="eyebrow">CATAT PENCAIRAN</p><h2>#{selectedRequest.id} {selectedRequest.title}</h2></div></div>
        <div className="form-grid">
          <label className="form-field"><span>Metode</span><select value={method} onChange={(event) => setMethod(event.target.value as typeof method)}><option value="BANK_TRANSFER">Transfer Bank</option><option value="CASH">Tunai</option><option value="E_WALLET">E-Wallet</option></select></label>
          <label className="form-field"><span>Nama Penerima</span><input value={receiverName} onChange={(event) => setReceiverName(event.target.value)} required /></label>
        </div>
        <label className="form-field"><span>Informasi Penerima</span><input value={receiverNote} onChange={(event) => setReceiverNote(event.target.value)} placeholder="Nomor rekening atau keterangan penyerahan" /></label>
        <label className="form-field"><span>URL Bukti Pencairan</span><input value={proofUrl} onChange={(event) => setProofUrl(event.target.value)} placeholder="https://..." /></label>
        <label className="form-field"><span>Catatan</span><textarea rows={3} value={note} onChange={(event) => setNote(event.target.value)} /></label>
        <div className="form-actions"><button className="primary-button" type="button" disabled={submitting || !receiverName.trim()} onClick={() => void handleDisburse()}>{submitting ? "Menyimpan..." : `Cairkan ${formatCurrency(selectedRequest.totalAmount)}`}</button><button type="button" onClick={() => setSelectedRequest(null)}>Batal</button></div>
      </section>}

      <section className="content-card">
        <div className="card-heading"><div><p className="eyebrow">RIWAYAT</p><h2>Pencairan Terbaru</h2></div></div>
        {disbursements.length ? <div className="request-table-wrapper"><table className="request-table"><thead><tr><th>Pengajuan</th><th>Penerima</th><th>Nominal</th><th>Sinkronisasi</th></tr></thead><tbody>{disbursements.map((item) => <tr key={item.id}><td><strong>#{item.fundRequestId} {item.requestTitle}</strong><small>{item.divisionName}</small></td><td>{item.receiverName}</td><td>{formatCurrency(item.amount)}</td><td>{item.requestSyncStatus}</td></tr>)}</tbody></table></div> : <div className="empty-state"><p>Belum ada pencairan.</p></div>}
      </section>
    </main>
  );
}
