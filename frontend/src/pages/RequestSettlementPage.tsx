import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";
import {
  confirmFundReceived,
  getMyRequestById,
  getSettlementDetail,
  resubmitSettlement,
  submitSettlement,
} from "../services/requestService";
import type { FundRequest, RequestSettlement } from "../types/request";

function formatCurrency(value: number) {
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    maximumFractionDigits: 0,
  }).format(value);
}

function formatDateTime(value?: string | null) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("id-ID", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function shouldLoadSettlement(status: FundRequest["status"]) {
  return [
    "SETTLEMENT_SUBMITTED",
    "SETTLEMENT_REVISION_REQUIRED",
    "SETTLEMENT_APPROVED",
    "COMPLETED",
  ].includes(status);
}

export function RequestSettlementPage() {
  const { id } = useParams();
  const { token } = useAuth();
  const requestId = Number(id);

  const [request, setRequest] = useState<FundRequest | null>(null);
  const [settlement, setSettlement] = useState<RequestSettlement | null>(null);
  const [spentAmount, setSpentAmount] = useState("");
  const [proofUrl, setProofUrl] = useState("");
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    if (!token || !Number.isFinite(requestId)) return;

    setLoading(true);
    setErrorMessage(null);
    try {
      const requestResponse = await getMyRequestById(token, requestId);
      const currentRequest = requestResponse.data;
      setRequest(currentRequest);

      if (shouldLoadSettlement(currentRequest.status)) {
        const settlementResponse = await getSettlementDetail(token, requestId);
        const currentSettlement = settlementResponse.data;
        setSettlement(currentSettlement);
        setSpentAmount(String(currentSettlement.spentAmount));
        setProofUrl(currentSettlement.proofUrl ?? "");
        setNote(currentSettlement.note ?? "");
      } else {
        setSettlement(null);
        setSpentAmount(String(currentRequest.totalAmount));
        setProofUrl("");
        setNote("");
      }
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Data pertanggungjawaban tidak dapat dimuat.",
      );
    } finally {
      setLoading(false);
    }
  }, [token, requestId]);

  useEffect(() => {
    void loadData();
  }, [loadData]);

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
      await confirmFundReceived(token, request.id);
      setSuccessMessage(
        "Dana berhasil dikonfirmasi diterima. Sekarang lengkapi laporan penggunaan dana.",
      );
      await loadData();
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
      setErrorMessage("Bukti atau struk penggunaan dana wajib diisi.");
      return;
    }

    const payload = {
      spentAmount: parsedAmount,
      proofUrl: proofUrl.trim(),
      note: note.trim() || undefined,
    };

    setSubmitting(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      if (request.status === "SETTLEMENT_REVISION_REQUIRED") {
        await resubmitSettlement(token, request.id, payload);
        setSuccessMessage(
          "Laporan berhasil diperbaiki dan dikirim ulang kepada Bendahara Internal.",
        );
      } else {
        await submitSettlement(token, request.id, payload);
        setSuccessMessage(
          "Laporan penggunaan dana berhasil dikirim untuk diverifikasi Bendahara Internal.",
        );
      }
      await loadData();
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

  const isRevision = request.status === "SETTLEMENT_REVISION_REQUIRED";
  const canEditReport = request.status === "FUND_RECEIVED" || isRevision;

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
          {settlement && <small>Pengiriman laporan ke-{settlement.submissionCount}</small>}
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

      {isRevision && settlement && (
        <section className="content-card">
          <p className="eyebrow">REVISI DIPERLUKAN</p>
          <h2>Laporan Dikembalikan oleh Bendahara</h2>
          <div className="alert alert-error" role="alert">
            {settlement.lastRevisionNote || "Bendahara meminta laporan diperbaiki."}
          </div>
          <dl className="detail-list">
            <div><dt>Ditinjau oleh</dt><dd>{settlement.reviewedByEmail || "-"}</dd></div>
            <div><dt>Waktu review</dt><dd>{formatDateTime(settlement.reviewedAt)}</dd></div>
            <div><dt>Jumlah revisi</dt><dd>{settlement.revisionCount}</dd></div>
          </dl>
        </section>
      )}

      {canEditReport && (
        <section className="content-card item-form">
          <div className="card-heading">
            <div>
              <p className="eyebrow">LAPORAN PEMOHON</p>
              <h2>{isRevision ? "Perbaiki Laporan Penggunaan Dana" : "Laporan Penggunaan Dana"}</h2>
            </div>
          </div>
          <p>Isi penggunaan aktual, lampirkan bukti atau struk penggunaan dana, dan jelaskan sisa atau kekurangan dana.</p>
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
            <span>Bukti/Struk Penggunaan Dana *</span>
            <input
              type="url"
              value={proofUrl}
              onChange={(event) => setProofUrl(event.target.value)}
              placeholder="https://drive.google.com/..."
              required
            />
            <small>Gunakan tautan HTTPS yang dapat dibuka oleh Bendahara Internal.</small>
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
              {submitting
                ? "Mengirim..."
                : isRevision
                  ? "Kirim Ulang Laporan"
                  : "Kirim Laporan Penggunaan Dana"}
            </button>
          </div>
        </section>
      )}

      {request.status === "SETTLEMENT_SUBMITTED" && settlement && (
        <section className="content-card">
          <p className="eyebrow">MENUNGGU VERIFIKASI</p>
          <h2>Laporan Sudah Dikirim</h2>
          <p>Laporan bersifat read-only selama diperiksa oleh Bendahara Internal.</p>
          <dl className="detail-list">
            <div><dt>Realisasi</dt><dd>{formatCurrency(settlement.spentAmount)}</dd></div>
            <div><dt>Sisa dana</dt><dd>{formatCurrency(settlement.remainingAmount)}</dd></div>
            <div><dt>Kekurangan</dt><dd>{formatCurrency(settlement.shortageAmount)}</dd></div>
            <div><dt>Dikirim</dt><dd>{formatDateTime(settlement.submittedAt)}</dd></div>
          </dl>
          {settlement.proofUrl && (
            <a className="primary-link-button" href={settlement.proofUrl} target="_blank" rel="noreferrer">
              Lihat Bukti Penggunaan
            </a>
          )}
        </section>
      )}

      {request.status === "COMPLETED" && settlement && (
        <section className="content-card">
          <p className="eyebrow">ALUR SELESAI</p>
          <h2>Pertanggungjawaban Disetujui</h2>
          <p>Laporan telah disetujui oleh {settlement.reviewedByEmail || settlement.approvedByEmail || "Bendahara Internal"}.</p>
          <dl className="detail-list">
            <div><dt>Realisasi</dt><dd>{formatCurrency(settlement.spentAmount)}</dd></div>
            <div><dt>Disetujui</dt><dd>{formatDateTime(settlement.approvedAt)}</dd></div>
            <div><dt>Jumlah pengiriman</dt><dd>{settlement.submissionCount}</dd></div>
            <div><dt>Jumlah revisi</dt><dd>{settlement.revisionCount}</dd></div>
          </dl>
          {settlement.proofUrl && (
            <a className="primary-link-button" href={settlement.proofUrl} target="_blank" rel="noreferrer">
              Lihat Bukti Penggunaan
            </a>
          )}
        </section>
      )}
    </main>
  );
}
