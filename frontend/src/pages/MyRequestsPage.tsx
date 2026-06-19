import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";
import { getMyRequests } from "../services/requestService";
import type { FundRequest, FundRequestStatus, PageResponse } from "../types/request";

const statusLabels: Record<FundRequestStatus, string> = {
  DRAFT: "Draft",
  SUBMITTED: "Diajukan",
  DIVISION_APPROVED: "Disetujui Ketua Divisi",
  PUB_APPROVED: "Disetujui Ketua PUB",
  PEMBINA_APPROVED: "Disetujui Pembina",
  REVISION_REQUESTED: "Perlu Revisi",
  REJECTED: "Ditolak",
  READY_FOR_DISBURSEMENT: "Siap Dicairkan",
  DISBURSED: "Dicairkan · Belum Dikonfirmasi",
  FUND_RECEIVED: "Dana Diterima · Laporan Belum Dikirim",
  SETTLEMENT_SUBMITTED: "Laporan Dikirim",
  SETTLEMENT_APPROVED: "Laporan Disetujui",
  COMPLETED: "Selesai",
  CANCELLED: "Dibatalkan",
};

function formatCurrency(value: number) {
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    maximumFractionDigits: 0,
  }).format(value);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("id-ID", { dateStyle: "medium" })
    .format(new Date(value));
}

function hasReportFlow(request: FundRequest) {
  return [
    "DISBURSED",
    "FUND_RECEIVED",
    "SETTLEMENT_SUBMITTED",
    "COMPLETED",
  ].includes(request.status);
}

function reportActionLabel(status: FundRequestStatus) {
  if (status === "DISBURSED") return "Konfirmasi Dana";
  if (status === "FUND_RECEIVED") return "Isi Laporan Dana";
  return "Lihat Pertanggungjawaban";
}

export function MyRequestsPage() {
  const { token } = useAuth();
  const [result, setResult] = useState<PageResponse<FundRequest> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadRequests = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      const response = await getMyRequests(token, page, 10);
      setResult(response.data);
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Tidak dapat mengambil data pengajuan.",
      );
    } finally {
      setLoading(false);
    }
  }, [token, page]);

  useEffect(() => {
    void loadRequests();
  }, [loadRequests]);

  return (
    <main className="page-content">
      <section className="page-heading">
        <div>
          <p className="eyebrow">PENGAJUAN DANA</p>
          <h1>Pengajuan Saya</h1>
          <p>Lihat dan kelola seluruh pengajuan dana yang kamu buat.</p>
        </div>
        <Link className="primary-link-button" to="/requests/new">Buat Pengajuan</Link>
      </section>

      {errorMessage && (
        <div className="alert alert-error" role="alert">
          {errorMessage}
          <button type="button" onClick={() => void loadRequests()}>Coba lagi</button>
        </div>
      )}

      <section className="content-card request-list-card">
        {loading ? (
          <div className="empty-state"><div className="spinner" /><p>Memuat pengajuan...</p></div>
        ) : result?.content.length ? (
          <>
            <div className="request-table-wrapper">
              <table className="request-table">
                <thead>
                  <tr>
                    <th>Pengajuan</th>
                    <th>Divisi</th>
                    <th>Prioritas</th>
                    <th>Status</th>
                    <th>Total</th>
                    <th>Dibuat</th>
                    <th>Aksi</th>
                  </tr>
                </thead>
                <tbody>
                  {result.content.map((request) => (
                    <tr key={request.id}>
                      <td><strong>{request.title}</strong><small>ID #{request.id}</small></td>
                      <td>{request.divisionName}</td>
                      <td><span className="table-chip">{request.priority}</span></td>
                      <td>
                        <span className={`status-chip status-${request.status.toLowerCase()}`}>
                          {statusLabels[request.status]}
                        </span>
                      </td>
                      <td>{formatCurrency(request.totalAmount ?? 0)}</td>
                      <td>{formatDate(request.createdAt)}</td>
                      <td>
                        <Link className="table-action" to={`/requests/${request.id}`}>Detail</Link>
                        {hasReportFlow(request) && (
                          <Link className="table-action" to={`/requests/${request.id}/settlement`}>
                            {reportActionLabel(request.status)}
                          </Link>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="pagination">
              <span>Menampilkan {result.content.length} dari {result.totalElements} pengajuan</span>
              <div>
                <button type="button" disabled={result.first} onClick={() => setPage((current) => Math.max(0, current - 1))}>Sebelumnya</button>
                <span>Halaman {result.page + 1} dari {Math.max(result.totalPages, 1)}</span>
                <button type="button" disabled={result.last} onClick={() => setPage((current) => current + 1)}>Berikutnya</button>
              </div>
            </div>
          </>
        ) : (
          <div className="empty-state">
            <div className="empty-state-icon">+</div>
            <h2>Belum ada pengajuan</h2>
            <p>Pengajuan yang kamu buat akan tampil di halaman ini.</p>
            <Link className="primary-link-button" to="/requests/new">Buat Pengajuan Pertama</Link>
          </div>
        )}
      </section>
    </main>
  );
}
