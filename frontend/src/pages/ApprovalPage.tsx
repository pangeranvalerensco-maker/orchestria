import {
  useCallback,
  useEffect,
  useState,
} from "react";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";

import {
  getPendingApprovals,
  processApproval,
} from "../services/requestService";

import type {
  ApprovalAction,
  ApprovalLevel,
  FundRequest,
  FundRequestStatus,
} from "../types/request";

const statusLabels: Record<
  FundRequestStatus,
  string
> = {
  DRAFT: "Draft",
  SUBMITTED: "Menunggu Ketua Divisi",
  DIVISION_APPROVED: "Menunggu Ketua PUB",
  PUB_APPROVED: "Menunggu Pembina",
  PEMBINA_APPROVED: "Disetujui Pembina",
  REVISION_REQUESTED: "Perlu Revisi",
  REJECTED: "Ditolak",
  READY_FOR_DISBURSEMENT: "Siap Dicairkan",
  DISBURSED: "Sudah Dicairkan",
  FUND_RECEIVED: "Dana Diterima",
  SETTLEMENT_SUBMITTED: "Settlement Dikirim",
  SETTLEMENT_REVISION_REQUIRED: "Laporan Perlu Diperbaiki",
  SETTLEMENT_APPROVED: "Settlement Disetujui",
  COMPLETED: "Selesai",
  CANCELLED: "Dibatalkan",
};

function determineApprovalLevel(
  status: FundRequestStatus,
): ApprovalLevel | null {
  if (status === "SUBMITTED") {
    return "DIVISION";
  }

  if (status === "DIVISION_APPROVED") {
    return "PUB";
  }

  if (status === "PUB_APPROVED") {
    return "PEMBINA";
  }

  return null;
}

function levelLabel(level: ApprovalLevel): string {
  if (level === "DIVISION") {
    return "Ketua Divisi";
  }

  if (level === "PUB") {
    return "Ketua PUB";
  }

  return "Pembina";
}

function formatCurrency(value: number): string {
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    maximumFractionDigits: 0,
  }).format(value);
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat("id-ID", {
    dateStyle: "medium",
  }).format(new Date(value));
}

export function ApprovalPage() {
  const { token } = useAuth();

  const [requests, setRequests] =
    useState<FundRequest[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [processingId, setProcessingId] =
    useState<number | null>(null);

  const [errorMessage, setErrorMessage] =
    useState<string | null>(null);

  const [successMessage, setSuccessMessage] =
    useState<string | null>(null);

  const loadApprovals = useCallback(async () => {
    if (!token) {
      return;
    }

    setLoading(true);
    setErrorMessage(null);

    try {
      const response =
        await getPendingApprovals(token);

      setRequests(response.data);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage(
          "Daftar approval tidak dapat diambil.",
        );
      }
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void loadApprovals();
  }, [loadApprovals]);

  async function handleAction(
    request: FundRequest,
    action: ApprovalAction,
  ) {
    if (!token) {
      return;
    }

    const level =
      determineApprovalLevel(request.status);

    if (!level) {
      setErrorMessage(
        "Status pengajuan tidak memiliki tahap approval.",
      );
      return;
    }

    let promptMessage =
      `Catatan approval ${levelLabel(level)}:`;

    if (action === "reject") {
      promptMessage =
        "Tuliskan alasan penolakan:";
    }

    if (action === "revision") {
      promptMessage =
        "Tuliskan bagian yang perlu direvisi:";
    }

    const note = window.prompt(promptMessage);

    if (note === null) {
      return;
    }

    if (
      (action === "reject"
        || action === "revision")
      && !note.trim()
    ) {
      setErrorMessage(
        "Catatan wajib diisi untuk penolakan atau revisi.",
      );
      return;
    }

    const confirmationText =
      action === "approve"
        ? `Setujui pengajuan pada level ${levelLabel(level)}?`
        : action === "reject"
          ? "Tolak pengajuan ini?"
          : "Kirim permintaan revisi?";

    if (!window.confirm(confirmationText)) {
      return;
    }

    setProcessingId(request.id);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const response = await processApproval(
        token,
        request.id,
        action,
        {
          level,
          note: note.trim() || undefined,
        },
      );

      setSuccessMessage(response.message);

      await loadApprovals();
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage(
          "Proses approval gagal dilakukan.",
        );
      }
    } finally {
      setProcessingId(null);
    }
  }

  return (
    <main className="page-content">
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            APPROVAL
          </p>

          <h1>Persetujuan Pengajuan</h1>

          <p>
            Proses pengajuan berdasarkan tahap
            approval yang sedang aktif.
          </p>
        </div>

        <button
          className="secondary-button"
          type="button"
          onClick={() => void loadApprovals()}
          disabled={loading}
        >
          Muat Ulang
        </button>
      </section>

      {errorMessage && (
        <div
          className="alert alert-error"
          role="alert"
        >
          {errorMessage}
        </div>
      )}

      {successMessage && (
        <div
          className="alert alert-success"
          role="status"
        >
          {successMessage}
        </div>
      )}

      {loading ? (
        <section className="content-card empty-state">
          <div className="spinner" />
          <p>Memuat antrean approval...</p>
        </section>
      ) : requests.length === 0 ? (
        <section className="content-card empty-state">
          <div className="empty-state-icon">
            ✓
          </div>

          <h2>Tidak ada approval pending</h2>

          <p>
            Seluruh pengajuan sudah diproses
            atau belum ada pengajuan yang masuk.
          </p>
        </section>
      ) : (
        <section className="approval-list">
          {requests.map((request) => {
            const level =
              determineApprovalLevel(
                request.status,
              );

            const processing =
              processingId === request.id;

            return (
              <article
                className="content-card approval-card"
                key={request.id}
              >
                <div className="approval-card-header">
                  <div>
                    <div className="approval-card-meta">
                      <span>
                        ID #{request.id}
                      </span>

                      <span>
                        {request.divisionName}
                      </span>

                      <span>
                        {formatDate(
                          request.createdAt,
                        )}
                      </span>
                    </div>

                    <h2>{request.title}</h2>

                    <p>
                      {request.description
                        || "Tidak ada deskripsi."}
                    </p>
                  </div>

                  <span
                    className={`status-chip status-${request.status.toLowerCase()}`}
                  >
                    {statusLabels[request.status]}
                  </span>
                </div>

                <div className="approval-summary">
                  <div>
                    <span>Pemohon</span>
                    <strong>
                      {request.requesterName}
                    </strong>
                  </div>

                  <div>
                    <span>Prioritas</span>
                    <strong>
                      {request.priority}
                    </strong>
                  </div>

                  <div>
                    <span>Jumlah item</span>
                    <strong>
                      {request.items.length}
                    </strong>
                  </div>

                  <div>
                    <span>Total</span>
                    <strong>
                      {formatCurrency(
                        request.totalAmount,
                      )}
                    </strong>
                  </div>

                  <div>
                    <span>Tahap</span>
                    <strong>
                      {level
                        ? levelLabel(level)
                        : "-"}
                    </strong>
                  </div>
                </div>

                {request.items.length > 0 && (
                  <details className="approval-items">
                    <summary>
                      Lihat rincian anggaran
                    </summary>

                    <div className="request-table-wrapper">
                      <table className="request-table">
                        <thead>
                          <tr>
                            <th>Item</th>
                            <th>Jumlah</th>
                            <th>Harga</th>
                            <th>Subtotal</th>
                          </tr>
                        </thead>

                        <tbody>
                          {request.items.map(
                            (item) => (
                              <tr key={item.id}>
                                <td>
                                  <strong>
                                    {item.itemName}
                                  </strong>

                                  <small>
                                    {item.description
                                      || "-"}
                                  </small>
                                </td>

                                <td>
                                  {item.quantity}
                                </td>

                                <td>
                                  {formatCurrency(
                                    item.unitPrice,
                                  )}
                                </td>

                                <td>
                                  {formatCurrency(
                                    item.subtotal,
                                  )}
                                </td>
                              </tr>
                            ),
                          )}
                        </tbody>
                      </table>
                    </div>
                  </details>
                )}

                <div className="approval-actions">
                  <button
                    className="danger-button"
                    type="button"
                    disabled={processing}
                    onClick={() =>
                      void handleAction(
                        request,
                        "reject",
                      )
                    }
                  >
                    Tolak
                  </button>

                  <button
                    className="warning-button"
                    type="button"
                    disabled={processing}
                    onClick={() =>
                      void handleAction(
                        request,
                        "revision",
                      )
                    }
                  >
                    Minta Revisi
                  </button>

                  <button
                    className="primary-action-button"
                    type="button"
                    disabled={processing}
                    onClick={() =>
                      void handleAction(
                        request,
                        "approve",
                      )
                    }
                  >
                    {processing
                      ? "Memproses..."
                      : `Setujui sebagai ${
                          level
                            ? levelLabel(level)
                            : "Approver"
                        }`}
                  </button>
                </div>
              </article>
            );
          })}
        </section>
      )}
    </main>
  );
}
