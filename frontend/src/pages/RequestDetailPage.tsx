import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type FormEvent,
} from "react";

import {
  Link,
  useNavigate,
  useParams,
} from "react-router";

import { ApiError } from "../api/http";
import { useAuth } from "../auth/useAuth";

import {
  addRequestItem,
  getMyRequestById,
  submitFundRequest,
} from "../services/requestService";

import type {
  FundRequest,
  FundRequestStatus,
} from "../types/request";

const statusLabels: Record<
  FundRequestStatus,
  string
> = {
  DRAFT: "Draft",
  SUBMITTED: "Diajukan",
  DIVISION_APPROVED: "Disetujui Ketua Divisi",
  PUB_APPROVED: "Disetujui Ketua PUB",
  PEMBINA_APPROVED: "Disetujui Pembina",
  REVISION_REQUESTED: "Perlu Revisi",
  REJECTED: "Ditolak",
  READY_FOR_DISBURSEMENT: "Siap Dicairkan",
  DISBURSED: "Sudah Dicairkan",
  FUND_RECEIVED: "Dana Diterima",
  SETTLEMENT_SUBMITTED: "Settlement Dikirim",
  SETTLEMENT_APPROVED: "Settlement Disetujui",
  COMPLETED: "Selesai",
  CANCELLED: "Dibatalkan",
};

function formatCurrency(value: number): string {
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    maximumFractionDigits: 0,
  }).format(value);
}

function formatDate(value?: string | null): string {
  if (!value) {
    return "-";
  }

  return new Intl.DateTimeFormat("id-ID", {
    dateStyle: "long",
  }).format(new Date(value));
}

export function RequestDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { token } = useAuth();

  const requestId = Number(id);

  const [request, setRequest] =
    useState<FundRequest | null>(null);

  const [loading, setLoading] = useState(true);
  const [submittingItem, setSubmittingItem] =
    useState(false);

  const [submittingRequest, setSubmittingRequest] =
    useState(false);

  const [errorMessage, setErrorMessage] =
    useState<string | null>(null);

  const [itemName, setItemName] = useState("");
  const [description, setDescription] = useState("");
  const [quantity, setQuantity] = useState("1");
  const [unitPrice, setUnitPrice] = useState("");

  const loadRequest = useCallback(async () => {
    if (!token || !Number.isFinite(requestId)) {
      return;
    }

    setLoading(true);
    setErrorMessage(null);

    try {
      const response =
        await getMyRequestById(
          token,
          requestId,
        );

      setRequest(response.data);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage(
          "Tidak dapat mengambil detail pengajuan.",
        );
      }
    } finally {
      setLoading(false);
    }
  }, [token, requestId]);

  useEffect(() => {
    void loadRequest();
  }, [loadRequest]);

  const itemSubtotal = useMemo(() => {
    const parsedQuantity = Number(quantity);
    const parsedUnitPrice = Number(unitPrice);

    if (
      !Number.isFinite(parsedQuantity)
      || !Number.isFinite(parsedUnitPrice)
    ) {
      return 0;
    }

    return parsedQuantity * parsedUnitPrice;
  }, [quantity, unitPrice]);

  async function handleAddItem(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    if (!token || !request) {
      return;
    }

    const parsedQuantity = Number(quantity);
    const parsedUnitPrice = Number(unitPrice);

    if (parsedQuantity < 1) {
      setErrorMessage(
        "Jumlah item minimal 1.",
      );
      return;
    }

    if (parsedUnitPrice < 0) {
      setErrorMessage(
        "Harga satuan tidak boleh negatif.",
      );
      return;
    }

    setSubmittingItem(true);
    setErrorMessage(null);

    try {
      const response = await addRequestItem(
        token,
        request.id,
        {
          itemName: itemName.trim(),
          description:
            description.trim() || undefined,
          quantity: parsedQuantity,
          unitPrice: parsedUnitPrice,
        },
      );

      setRequest(response.data);

      setItemName("");
      setDescription("");
      setQuantity("1");
      setUnitPrice("");
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage(
          "Item anggaran tidak dapat ditambahkan.",
        );
      }
    } finally {
      setSubmittingItem(false);
    }
  }

  async function handleSubmitRequest() {
    if (!token || !request) {
      return;
    }

    const confirmed = window.confirm(
      "Setelah disubmit, item pengajuan tidak dapat ditambahkan lagi. Lanjutkan?",
    );

    if (!confirmed) {
      return;
    }

    setSubmittingRequest(true);
    setErrorMessage(null);

    try {
      const response =
        await submitFundRequest(
          token,
          request.id,
        );

      setRequest(response.data);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage(
          "Pengajuan tidak dapat disubmit.",
        );
      }
    } finally {
      setSubmittingRequest(false);
    }
  }

  if (!Number.isFinite(requestId)) {
    return (
      <main className="page-content">
        <div className="alert alert-error">
          ID pengajuan tidak valid.
        </div>
      </main>
    );
  }

  if (loading) {
    return (
      <main className="page-content">
        <div className="empty-state">
          <div className="spinner" />
          <p>Memuat detail pengajuan...</p>
        </div>
      </main>
    );
  }

  if (!request) {
    return (
      <main className="page-content">
        <div className="empty-state">
          <h2>Pengajuan tidak ditemukan</h2>

          <button
            type="button"
            onClick={() =>
              navigate("/requests")
            }
          >
            Kembali
          </button>
        </div>
      </main>
    );
  }

  const isDraft = request.status === "DRAFT";

  return (
    <main className="page-content">
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            DETAIL PENGAJUAN
          </p>

          <h1>{request.title}</h1>

          <p>
            ID #{request.id} · {request.divisionName}
          </p>
        </div>

        <Link
          className="secondary-link-button"
          to="/requests"
        >
          Kembali
        </Link>
      </section>

      {errorMessage && (
        <div
          className="alert alert-error"
          role="alert"
        >
          {errorMessage}
        </div>
      )}

      <section className="request-detail-grid">
        <article className="content-card">
          <div className="card-heading">
            <div>
              <p className="eyebrow">
                INFORMASI
              </p>

              <h2>Data Pengajuan</h2>
            </div>

            <span
              className={`status-chip status-${request.status.toLowerCase()}`}
            >
              {statusLabels[request.status]}
            </span>
          </div>

          <dl className="detail-list">
            <div>
              <dt>Divisi</dt>
              <dd>{request.divisionName}</dd>
            </div>

            <div>
              <dt>Pemohon</dt>
              <dd>{request.requesterName}</dd>
            </div>

            <div>
              <dt>Prioritas</dt>
              <dd>{request.priority}</dd>
            </div>

            <div>
              <dt>Tanggal kegiatan</dt>
              <dd>
                {formatDate(
                  request.activityDate,
                )}
              </dd>
            </div>

            <div>
              <dt>Dibuat</dt>
              <dd>
                {formatDate(request.createdAt)}
              </dd>
            </div>
          </dl>

          <div className="detail-description">
            <span>Deskripsi</span>

            <p>
              {request.description
                || "Tidak ada deskripsi."}
            </p>
          </div>
        </article>

        <article className="content-card total-card">
          <p className="eyebrow">
            TOTAL PENGAJUAN
          </p>

          <strong>
            {formatCurrency(
              request.totalAmount ?? 0,
            )}
          </strong>

          <small>
            {request.items.length} item anggaran
          </small>

          {isDraft && (
            <button
              className="primary-button"
              type="button"
              disabled={
                submittingRequest
                || request.totalAmount <= 0
              }
              onClick={
                handleSubmitRequest
              }
            >
              {submittingRequest
                ? "Mengirim..."
                : "Submit Pengajuan"}
            </button>
          )}
        </article>
      </section>

      <section className="content-card budget-section">
        <div className="card-heading">
          <div>
            <p className="eyebrow">
              RINCIAN ANGGARAN
            </p>

            <h2>Item Pengajuan</h2>
          </div>
        </div>

        {request.items.length ? (
          <div className="request-table-wrapper">
            <table className="request-table">
              <thead>
                <tr>
                  <th>Item</th>
                  <th>Jumlah</th>
                  <th>Harga Satuan</th>
                  <th>Subtotal</th>
                </tr>
              </thead>

              <tbody>
                {request.items.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <strong>
                        {item.itemName}
                      </strong>

                      <small>
                        {item.description || "-"}
                      </small>
                    </td>

                    <td>{item.quantity}</td>

                    <td>
                      {formatCurrency(
                        item.unitPrice,
                      )}
                    </td>

                    <td>
                      <strong>
                        {formatCurrency(
                          item.subtotal,
                        )}
                      </strong>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-budget">
            Belum ada item anggaran.
          </div>
        )}
      </section>

      {isDraft && (
        <form
          className="content-card item-form"
          onSubmit={handleAddItem}
        >
          <div className="card-heading">
            <div>
              <p className="eyebrow">
                TAMBAH ITEM
              </p>

              <h2>Rincian Anggaran Baru</h2>
            </div>
          </div>

          <div className="form-grid">
            <label className="form-field">
              <span>Nama Item</span>

              <input
                type="text"
                value={itemName}
                onChange={(event) =>
                  setItemName(
                    event.target.value,
                  )
                }
                placeholder="Contoh: Konsumsi peserta"
                required
              />
            </label>

            <label className="form-field">
              <span>Jumlah</span>

              <input
                type="number"
                min="1"
                value={quantity}
                onChange={(event) =>
                  setQuantity(
                    event.target.value,
                  )
                }
                required
              />
            </label>
          </div>

          <label className="form-field">
            <span>Deskripsi Item</span>

            <textarea
              rows={3}
              value={description}
              onChange={(event) =>
                setDescription(
                  event.target.value,
                )
              }
              placeholder="Keterangan kebutuhan item"
            />
          </label>

          <div className="form-grid">
            <label className="form-field">
              <span>Harga Satuan</span>

              <input
                type="number"
                min="0"
                value={unitPrice}
                onChange={(event) =>
                  setUnitPrice(
                    event.target.value,
                  )
                }
                placeholder="25000"
                required
              />
            </label>

            <div className="subtotal-preview">
              <span>Perkiraan subtotal</span>

              <strong>
                {formatCurrency(itemSubtotal)}
              </strong>
            </div>
          </div>

          <div className="form-actions">
            <button
              className="primary-button form-submit-button"
              type="submit"
              disabled={submittingItem}
            >
              {submittingItem
                ? "Menambahkan..."
                : "Tambah Item"}
            </button>
          </div>
        </form>
      )}
    </main>
  );
}