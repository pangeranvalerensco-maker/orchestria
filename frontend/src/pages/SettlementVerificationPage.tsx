import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";
import {
  approveSettlement,
  getPendingSettlements,
  requestSettlementRevision,
} from "../services/financeService";
import { getSettlementDetail } from "../services/requestService";
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

export function SettlementVerificationPage() {
  const { token } = useAuth();
  const [requests, setRequests] = useState<FundRequest[]>([]);
  const [selectedRequest, setSelectedRequest] = useState<FundRequest | null>(null);
  const [settlement, setSettlement] = useState<RequestSettlement | null>(null);
  const [revisionNote, setRevisionNote] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [processingAction, setProcessingAction] = useState<"approve" | "revision" | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadRequests = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      const response = await getPendingSettlements(token);
      setRequests(response.data.content);
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Laporan penggunaan dana yang menunggu verifikasi tidak dapat dimuat.",
      );
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void loadRequests();
  }, [loadRequests]);

  async function handleInspect(request: FundRequest) {
    if (!token) return;

    setSelectedRequest(request);
    setSettlement(null);
    setRevisionNote("");
    setLoadingDetail(true);
    setErrorMessage(null);
    try {
      const response = await getSettlementDetail(token, request.id);
      setSettlement(response.data);
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Detail laporan penggunaan dana tidak dapat dimuat.",
      );
    } finally {
      setLoadingDetail(false);
    }
  }

  async function handleApprove(request: FundRequest) {
    if (!token || !settlement?.proofUrl) return;

    const confirmed = window.confirm(
      `Setujui laporan penggunaan dana pengajuan #${request.id} dan ubah status menjadi COMPLETED?`,
    );
    if (!confirmed) return;

    setProcessingAction("approve");
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await approveSettlement(token, request.id);
      setSuccessMessage(
        `Laporan pengajuan #${request.id} disetujui. Alur selesai pada status COMPLETED.`,
      );
      setSelectedRequest(null);
      setSettlement(null);
      setRevisionNote("");
      await loadRequests();
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Laporan penggunaan dana gagal disetujui.",
      );
    } finally {
      setProcessingAction(null);
    }
  }

  async function handleRequestRevision(request: FundRequest) {
    if (!token) return;

    const normalizedNote = revisionNote.trim();
    if (!normalizedNote) {
      setErrorMessage("Catatan revisi wajib diisi.");
      return;
    }

    const confirmed = window.confirm(
      `Kembalikan laporan pengajuan #${request.id} kepada pemohon untuk diperbaiki?`,
    );
    if (!confirmed) return;

    setProcessingAction("revision");
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await requestSettlementRevision(token, request.id, normalizedNote);
      setSuccessMessage(
        `Revisi laporan pengajuan #${request.id} berhasil diminta kepada pemohon.`,
      );
      setSelectedRequest(null);
      setSettlement(null);
      setRevisionNote("");
      await loadRequests();
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Permintaan revisi laporan gagal disimpan.",
      );
    } finally {
      setProcessingAction(null);
    }
  }

  return (
    <main className="page-content">
      <section className="page-heading">
        <div>
          <p className="eyebrow">FINANCE · PERTANGGUNGJAWABAN</p>
          <h1>Verifikasi Laporan Dana</h1>
          <p>Bendahara wajib memeriksa nominal realisasi, catatan, dan bukti penggunaan sebelum mengambil keputusan.</p>
        </div>
        <Link className="secondary-link-button" to="/finance/disbursements">Pencairan Dana</Link>
      </section>

      {errorMessage && <div className="alert alert-error" role="alert">{errorMessage}</div>}
      {successMessage && <div className="alert" role="status">{successMessage}</div>}

      <section className="content-card request-list-card">
        <div className="card-heading">
          <div><p className="eyebrow">MENUNGGU VERIFIKASI</p><h2>Laporan dari Pemohon</h2></div>
        </div>

        {loading ? (
          <div className="empty-state"><div className="spinner" /><p>Memuat laporan...</p></div>
        ) : requests.length ? (
          <div className="request-table-wrapper">
            <table className="request-table">
              <thead><tr><th>Pengajuan</th><th>Pemohon</th><th>Total Pengajuan</th><th>Status</th><th>Aksi</th></tr></thead>
              <tbody>
                {requests.map((request) => (
                  <tr key={request.id}>
                    <td><strong>#{request.id} {request.title}</strong><small>{request.divisionName}</small></td>
                    <td>{request.requesterName}</td>
                    <td>{formatCurrency(request.totalAmount)}</td>
                    <td><span className="status-chip status-settlement_submitted">Laporan Dikirim</span></td>
                    <td>
                      <button
                        className="secondary-button"
                        type="button"
                        onClick={() => void handleInspect(request)}
                      >
                        Periksa Laporan
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <h2>Tidak ada laporan pending</h2>
            <p>Laporan penggunaan dana yang dikirim pemohon akan tampil di sini.</p>
          </div>
        )}
      </section>

      {selectedRequest && (
        <section className="content-card item-form">
          <div className="card-heading">
            <div>
              <p className="eyebrow">DETAIL LAPORAN</p>
              <h2>#{selectedRequest.id} {selectedRequest.title}</h2>
            </div>
            <button
              type="button"
              disabled={processingAction !== null}
              onClick={() => {
                setSelectedRequest(null);
                setSettlement(null);
                setRevisionNote("");
              }}
            >
              Tutup
            </button>
          </div>

          {loadingDetail ? (
            <div className="empty-state"><div className="spinner" /><p>Memuat detail laporan...</p></div>
          ) : settlement ? (
            <>
              <dl className="detail-list">
                <div><dt>Status laporan</dt><dd>{settlement.status.replaceAll("_", " ")}</dd></div>
                <div><dt>Dana Disetujui</dt><dd>{formatCurrency(settlement.requestedAmount)}</dd></div>
                <div><dt>Realisasi</dt><dd>{formatCurrency(settlement.spentAmount)}</dd></div>
                <div><dt>Sisa Dana</dt><dd>{formatCurrency(settlement.remainingAmount)}</dd></div>
                <div><dt>Kekurangan</dt><dd>{formatCurrency(settlement.shortageAmount)}</dd></div>
                <div><dt>Dikirim Oleh</dt><dd>{settlement.submittedByEmail || "-"}</dd></div>
                <div><dt>Waktu Kirim</dt><dd>{formatDateTime(settlement.submittedAt)}</dd></div>
                <div><dt>Pengiriman Ke</dt><dd>{settlement.submissionCount}</dd></div>
                <div><dt>Jumlah Revisi</dt><dd>{settlement.revisionCount}</dd></div>
              </dl>

              <div className="detail-description">
                <span>Catatan Pertanggungjawaban</span>
                <p>{settlement.note || "Tidak ada catatan tambahan."}</p>
              </div>

              <div className="form-field">
                <span>Bukti/Struk Penggunaan Dana</span>
                {settlement.proofUrl ? (
                  <a
                    className="primary-link-button"
                    href={settlement.proofUrl}
                    target="_blank"
                    rel="noreferrer"
                  >
                    Buka Bukti Penggunaan
                  </a>
                ) : (
                  <div className="alert alert-error" role="alert">Bukti penggunaan belum tersedia.</div>
                )}
              </div>

              <label className="form-field">
                <span>Catatan Revisi</span>
                <textarea
                  rows={4}
                  maxLength={2000}
                  value={revisionNote}
                  onChange={(event) => setRevisionNote(event.target.value)}
                  placeholder="Jelaskan bagian laporan atau bukti yang harus diperbaiki"
                />
                <small>Wajib diisi apabila laporan dikembalikan kepada pemohon.</small>
              </label>

              <div className="form-actions">
                <button
                  className="primary-button"
                  type="button"
                  disabled={processingAction !== null || !settlement.proofUrl}
                  onClick={() => void handleApprove(selectedRequest)}
                >
                  {processingAction === "approve" ? "Memproses..." : "Setujui & Selesaikan"}
                </button>
                <button
                  type="button"
                  disabled={processingAction !== null || !revisionNote.trim()}
                  onClick={() => void handleRequestRevision(selectedRequest)}
                >
                  {processingAction === "revision" ? "Menyimpan..." : "Minta Revisi"}
                </button>
              </div>
            </>
          ) : null}
        </section>
      )}
    </main>
  );
}
