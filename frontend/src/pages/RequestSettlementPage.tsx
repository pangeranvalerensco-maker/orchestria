import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";
import {
  confirmFundReceived,
  getMyRequestById,
  submitSettlement,
} from "../services/requestService";
import type { FundRequest } from "../types/request";

function formatCurrency(value: number) {
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    maximumFractionDigits: 0,
  }).format(value);
}

export function RequestSettlementPage() {
  const { id } = useParams();
  const { token } = useAuth();
  const requestId = Number(id);

  const [request, setRequest] = useState<FundRequest | null>(null);
  const [spentAmount, setSpentAmount] = useState("");
  const [proofUrl, setProofUrl] = useState("");
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadRequest = useCallback(async () => {
    if (!token || !Number.isFinite(requestId)) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      const response = await getMyRequestById(token, requestId);
      setRequest(response.data);
      if (!spentAmount) setSpentAmount(String(response.data.totalAmount));
    } catch (error) {
      setErrorMessage(error instanceof ApiError ? error.message : "Pengajuan tidak dapat dimuat.");
    } finally {
      setLoading(false);
    }
  }, [token, requestId, spentAmount]);

  useEffect(() => {
    void loadRequest();
  }, [loadRequest]);

  const balance = useMemo(() => {
    if (!request) return 0;
    return request.totalAmount - Number(spentAmount || 0);
  }, [request, spentAmount]);

  async function handleConfirmReceived() {
    if (!token || !request) return;
    setSubmitting(true);
    setErrorMessage(null);
    try {
      const response = await confirmFundReceived(token, request.id);
      setRequest(response.data);
      setSuccessMessage("Penerimaan dana berhasil dikonfirmasi. Settlement sekarang dapat dikirim.");
    } catch (error) {
      setErrorMessage(error instanceof ApiError ? error.message : "Konfirmasi penerimaan dana gagal.");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleSubmitSettlement() {
    if (!token || !request) return;
    const parsedAmount = Number(spentAmount);
    if (!Number.isFinite(parsedAmount) || parsedAmount <= 0) {
      setErrorMessage("Nominal penggunaan harus lebih dari 0.");
      return;
    }

    setSubmitting(true);
    setErrorMessage(null);
    try {
      await submitSettlement(token, request.id, {
        spentAmount: parsedAmount,
        proofUrl: proofUrl.trim() || undefined,
        note: note.trim() || undefined,
      });
      setSuccessMessage("Settlement berhasil dikirim untuk diverifikasi Bendahara.");
      await loadRequest();
    } catch (error) {
      setErrorMessage(error instanceof ApiError ? error.message : "Settlement gagal dikirim.");
    } finally {
      setSubmitting(false);
    }
  }

  if (!Number.isFinite(requestId)) {
    return <main className="page-content"><div className="alert alert-error">ID pengajuan tidak valid.</div></main>;
  }

  if (loading) {
    return <main className="page-content"><div className="empty-state"><div className="spinner" /><p>Memuat settlement...</p></div></main>;
  }

  if (!request) {
    return <main className="page-content"><div className="empty-state"><h2>Pengajuan tidak ditemukan</h2><Link to="/requests">Kembali</Link></div></main>;
  }

  return (
    <main className="page-content">
      <section className="page-heading">
        <div><p className="eyebrow">SETTLEMENT</p><h1>{request.title}</h1><p>Pengajuan #{request.id} · {request.divisionName}</p></div>
        <Link className="secondary-link-button" to={`/requests/${request.id}`}>Detail Pengajuan</Link>
      </section>

      {errorMessage && <div className="alert alert-error" role="alert">{errorMessage}</div>}
      {successMessage && <div className="alert" role="status">{successMessage}</div>}

      <section className="request-detail-grid">
        <article className="content-card">
          <p className="eyebrow">STATUS</p>
          <h2>{request.status.replaceAll("_", " ")}</h2>
          <p>Pencairan dan settlement hanya dapat diproses sesuai urutan status backend.</p>
        </article>
        <article className="content-card total-card">
          <p className="eyebrow">DANA DISETUJUI</p>
          <strong>{formatCurrency(request.totalAmount)}</strong>
        </article>
      </section>

      {request.status === "DISBURSED" && (
        <section className="content-card">
          <div className="card-heading"><div><p className="eyebrow">KONFIRMASI</p><h2>Dana Sudah Diterima?</h2></div></div>
          <p>Konfirmasi ini mengubah status pengajuan menjadi FUND_RECEIVED dan membuka form settlement.</p>
          <div className="form-actions"><button className="primary-button" type="button" disabled={submitting} onClick={() => void handleConfirmReceived()}>{submitting ? "Memproses..." : "Konfirmasi Dana Diterima"}</button></div>
        </section>
      )}

      {request.status === "FUND_RECEIVED" && (
        <section className="content-card item-form">
          <div className="card-heading"><div><p className="eyebrow">PERTANGGUNGJAWABAN</p><h2>Kirim Settlement</h2></div></div>
          <div className="form-grid">
            <label className="form-field"><span>Nominal Digunakan</span><input type="number" min="0.01" step="0.01" value={spentAmount} onChange={(event) => setSpentAmount(event.target.value)} required /></label>
            <div className="subtotal-preview"><span>{balance >= 0 ? "Sisa dana" : "Kekurangan dana"}</span><strong>{formatCurrency(Math.abs(balance))}</strong></div>
          </div>
          <label className="form-field"><span>URL Bukti</span><input value={proofUrl} onChange={(event) => setProofUrl(event.target.value)} placeholder="https://drive.google.com/..." /></label>
          <label className="form-field"><span>Catatan</span><textarea rows={4} value={note} onChange={(event) => setNote(event.target.value)} placeholder="Ringkasan penggunaan dana dan pengembalian/kekurangan" /></label>
          <div className="form-actions"><button className="primary-button" type="button" disabled={submitting} onClick={() => void handleSubmitSettlement()}>{submitting ? "Mengirim..." : "Kirim Settlement"}</button></div>
        </section>
      )}

      {request.status === "SETTLEMENT_SUBMITTED" && <section className="content-card"><h2>Menunggu Verifikasi Bendahara</h2><p>Settlement telah tersimpan dan menunggu persetujuan.</p></section>}
      {request.status === "COMPLETED" && <section className="content-card"><h2>Alur Selesai</h2><p>Settlement telah disetujui dan pengajuan berstatus COMPLETED.</p></section>}
    </main>
  );
}
