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
      setSpentAmount((current) => current || String(response.data.totalAmount));
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Pengajuan tidak dapat dimuat.",
      );
    } finally {
      setLoading(false);
    }
  }, [token, requestId]);

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
    setSuccessMessage(null);
    try {
      const response = await confirmFundReceived(token, request.id);
      setRequest(response.data);
      setSuccessMessage(
        "Dana berhasil dikonfirmasi diterima. Sekarang lengkapi laporan penggunaan dana.",
      );
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Konfirmasi penerimaan dana gagal.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  async function handleSubmitReport() {
    if (!token || !request) return;

    const parsedAmount = Number(spentAmount);
    if (!Number.isFinite(parsedAmount) || parsedAmount <= 0) {
      setErrorMessage("Nominal penggunaan harus lebih dari 0.");
      return;
    }

    if (!proofUrl.trim()) {
      setErrorMessage("Bukti atau struk pembayaran wajib diisi.");
      return;
    }

    setSubmitting(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await submitSettlement(token, request.id, {
        spentAmount: parsedAmount,
        proofUrl: proofUrl.trim(),
        note: note.trim() || undefined,
      });
      setSuccessMessage(
        "Laporan penggunaan dana berhasil dikirim untuk diverifikasi Bendahara Internal.",
      );
      await loadRequest();
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Laporan penggunaan dana gagal dikirim.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  if (!Number.isFinite(requestId)) {
    return <main className="page-content"><div className="alert alert-error">ID pengajuan tidak valid.</div></main>;
  }

  if (loading) {
    return <main className="page-content"><div className="empty-state"><div className="spinner" /><p>Memuat pertanggungjawaban...</p></div></main>;
  }

  if (!request) {
    return (
      <main className="page-content">
        <div className="empty-state">
          <h2>Pengajuan tidak ditemukan</h2>
          <Link to="/requests">Kembali</Link>
        </div>
      </main>
    );
  }

  return (
    <main className="page-content">
      <section className="page-heading">
        <div>
          <p className="eyebrow">PERTANGGUNGJAWABAN DANA</p>
          <h1>{request.title}</h1>
          <p>Pengajuan #{request.id} · {request.divisionName}</p>
        </div>
        <Link className="secondary-link-button" to={`/requests/${request.id}`}>Detail Pengajuan</Link>
      </section>

      {errorMessage && <div className="alert alert-error" role="alert">{errorMessage}</div>}
      {successMessage && <div className="alert" role="status">{successMessage}</div>}

      <section className="request-detail-grid">
        <article className="content-card">
          <p className="eyebrow">STATUS</p>
          <h2>{request.status.replaceAll("_", " ")}</h2>
          <p>Pencairan belum selesai dipertanggungjawabkan sampai laporan disetujui Bendahara Internal.</p>
        </article>
        <article className="content-card total-card">
          <p className="eyebrow">DANA DISETUJUI</p>
          <strong>{formatCurrency(request.totalAmount)}</strong>
        </article>
      </section>

      {request.status === "DISBURSED" && (
        <section className="content-card">
          <div className="card-heading">
            <div><p className="eyebrow">KONFIRMASI</p><h2>Apakah Dana Sudah Diterima?</h2></div>
          </div>
          <p>
            Bendahara sudah mencatat pencairan. Klik konfirmasi hanya setelah uang benar-benar masuk atau diserahkan.
          </p>
          <div className="form-actions">
            <button
              className="primary-button"
              type="button"
              disabled={submitting}
              onClick={() => void handleConfirmReceived()}
            >
              {submitting ? "Memproses..." : "Konfirmasi Dana Diterima"}
            </button>
          </div>
        </section>
      )}

      {request.status === "FUND_RECEIVED" && (
        <section className="content-card item-form">
          <div className="card-heading">
            <div><p className="eyebrow">LAPORAN ANGGOTA</p><h2>Laporan Penggunaan Dana</h2></div>
          </div>
          <p>Isi penggunaan aktual, lampirkan bukti atau struk pembayaran, dan jelaskan sisa atau kekurangan dana.</p>
          <div className="form-grid">
            <label className="form-field">
              <span>Nominal Digunakan</span>
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={spentAmount}
                onChange={(event) => setSpentAmount(event.target.value)}
                required
              />
            </label>
            <div className="subtotal-preview">
              <span>{balance >= 0 ? "Sisa dana" : "Kekurangan dana"}</span>
              <strong>{formatCurrency(Math.abs(balance))}</strong>
            </div>
          </div>
          <label className="form-field">
            <span>Bukti/Struk Pembayaran *</span>
            <input
              type="url"
              value={proofUrl}
              onChange={(event) => setProofUrl(event.target.value)}
              placeholder="https://drive.google.com/..."
              required
            />
            <small>Gunakan tautan bukti yang dapat dibuka oleh Bendahara Internal.</small>
          </label>
          <label className="form-field">
            <span>Catatan Pertanggungjawaban</span>
            <textarea
              rows={4}
              value={note}
              onChange={(event) => setNote(event.target.value)}
              placeholder="Ringkasan penggunaan dana, sisa yang dikembalikan, atau kekurangan"
            />
          </label>
          <div className="form-actions">
            <button
              className="primary-button"
              type="button"
              disabled={submitting || !proofUrl.trim()}
              onClick={() => void handleSubmitReport()}
            >
              {submitting ? "Mengirim..." : "Kirim Laporan Penggunaan Dana"}
            </button>
          </div>
        </section>
      )}

      {request.status === "SETTLEMENT_SUBMITTED" && (
        <section className="content-card">
          <h2>Menunggu Verifikasi Bendahara</h2>
          <p>Laporan dan bukti pembayaran telah tersimpan dan menunggu pemeriksaan Bendahara Internal.</p>
        </section>
      )}

      {request.status === "COMPLETED" && (
        <section className="content-card">
          <h2>Pertanggungjawaban Selesai</h2>
          <p>Laporan telah disetujui dan pengajuan berstatus COMPLETED.</p>
        </section>
      )}
    </main>
  );
}
