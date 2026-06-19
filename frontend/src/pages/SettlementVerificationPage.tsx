import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";
import {
  approveSettlement,
  getPendingSettlements,
} from "../services/financeService";
import type { FundRequest } from "../types/request";

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
  const [loading, setLoading] = useState(true);
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
      setErrorMessage(error instanceof ApiError ? error.message : "Settlement pending tidak dapat dimuat.");
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void loadRequests();
  }, [loadRequests]);

  async function handleApprove(request: FundRequest) {
    if (!token) return;
    const confirmed = window.confirm(
      `Setujui settlement pengajuan #${request.id} dan ubah status menjadi COMPLETED?`,
    );
    if (!confirmed) return;

    setProcessingId(request.id);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await approveSettlement(token, request.id);
      setSuccessMessage(`Settlement pengajuan #${request.id} disetujui. Core flow selesai pada COMPLETED.`);
      await loadRequests();
    } catch (error) {
      setErrorMessage(error instanceof ApiError ? error.message : "Settlement gagal disetujui.");
    } finally {
      setProcessingId(null);
    }
  }

  return (
    <main className="page-content">
      <section className="page-heading">
        <div>
          <p className="eyebrow">FINANCE · SETTLEMENT</p>
          <h1>Verifikasi Settlement</h1>
          <p>Persetujuan mengakhiri core flow dan mengubah pengajuan menjadi COMPLETED.</p>
        </div>
        <Link className="secondary-link-button" to="/finance/disbursements">Pencairan Dana</Link>
      </section>

      {errorMessage && <div className="alert alert-error" role="alert">{errorMessage}</div>}
      {successMessage && <div className="alert" role="status">{successMessage}</div>}

      <section className="content-card request-list-card">
        <div className="card-heading">
          <div><p className="eyebrow">MENUNGGU VERIFIKASI</p><h2>Settlement Dikirim</h2></div>
        </div>

        {loading ? (
          <div className="empty-state"><div className="spinner" /><p>Memuat settlement...</p></div>
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
                    <td><span className="status-chip status-settlement_submitted">Settlement Dikirim</span></td>
                    <td>
                      <button
                        className="primary-button"
                        type="button"
                        disabled={processingId === request.id}
                        onClick={() => void handleApprove(request)}
                      >
                        {processingId === request.id ? "Memproses..." : "Setujui & Selesaikan"}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state"><h2>Tidak ada settlement pending</h2><p>Settlement yang dikirim pemohon akan tampil di sini.</p></div>
        )}
      </section>
    </main>
  );
}
