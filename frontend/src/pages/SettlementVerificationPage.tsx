import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";
import {
  approveSettlement,
  getPendingSettlements,
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

export function SettlementVerificationPage() {
  const { token } = useAuth();
  const [requests, setRequests] = useState<FundRequest[]>([]);
  const [selectedRequest, setSelectedRequest] = useState<FundRequest | null>(null);
  const [settlement, setSettlement] = useState<RequestSettlement | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [processingId, setProcessingId] = useState<number | null>(null);
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

    setProcessingId(request.id);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await approveSettlement(token, request.id);
      setSuccessMessage(
        `Laporan pengajuan #${request.id} disetujui. Alur selesai pada status COMPLETED.`,
      );
      setSelectedRequest(null);
      setSettlement(null);
      await loadRequests();
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Laporan penggunaan dana gagal disetujui.",
      );
    } finally {
      setProcessingId(null);
    }
  }

  return (
    <main className="page-content">
      <section className="page-heading">
        <div>
          <p className="eyebrow">FINANCE · PERTANGGUNGJAWABAN</p>
          <h1>Verifikasi Laporan Dana</h1>
          <p>Bendahara wajib memeriksa nominal realisasi, catatan, dan bukti pembayaran sebelum menyelesaikan pengajuan.</p>
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
              onClick={() => {
                setSelectedRequest(null);
                setSettlement(null);
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
                <div><dt>Dana Disetujui</dt><dd>{formatCurrency(settlement.requestedAmount)}</dd></div>
                <div><dt>Realisasi</dt><dd>{formatCurrency(settlement.spentAmount)}</dd></div>
                <div><dt>Sisa Dana</dt><dd>{formatCurrency(settlement.remainingAmount)}</dd></div>
                <div><dt>Kekurangan</dt><dd>{formatCurrency(settlement.shortageAmount)}</dd></div>
                <div><dt>Dikirim Oleh</dt><dd>{settlement.submittedByEmail}</dd></div>
              </dl>

              <div className="detail-description">
                <span>Catatan Pertanggungjawaban</span>
                <p>{settlement.note || "Tidak ada catatan tambahan."}</p>
              </div>

              <div className="form-field">
                <span>Bukti/Struk Pembayaran</span>
                <a
                  className="primary-link-button"
                  href={settlement.proofUrl}
                  target="_blank"
                  rel="noreferrer"
                >
                  Buka Bukti Pembayaran
                </a>
              </div>

              <div className="form-actions">
                <button
                  className="primary-button"
                  type="button"
                  disabled={processingId === selectedRequest.id || !settlement.proofUrl}
                  onClick={() => void handleApprove(selectedRequest)}
                >
                  {processingId === selectedRequest.id ? "Memproses..." : "Setujui & Selesaikan"}
                </button>
              </div>
            </>
          ) : null}
        </section>
      )}
    </main>
  );
}
