import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { useAuth } from "../auth/useAuth";
import { ApiError } from "../api/http";
import { getBorrowingById } from "../services/assetService";
import type { Borrowing } from "../types/asset";
import "../assets.css";

export const AssetBorrowingDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { token, hasPermission } = useAuth();
  const [borrowing, setBorrowing] = useState<Borrowing | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const canManageOperations = hasPermission("asset.borrow.handover") || hasPermission("asset.return.verify");

  useEffect(() => {
    const fetchBorrowing = async () => {
      try {
        if (!token || !id) return;
        const res = await getBorrowingById(token, id);
        setBorrowing(res);
      } catch (err: unknown) {
        if (err instanceof ApiError) {
          setError(err.message);
        } else {
          setError("Gagal memuat detail peminjaman.");
        }
      } finally {
        setLoading(false);
      }
    };
    fetchBorrowing();
  }, [id, token, canManageOperations]);

  if (loading) return <div className="asset-loading">Memuat detail peminjaman...</div>;
  if (error) return <div className="asset-alert asset-alert-error">{error}</div>;
  if (!borrowing) return <div className="asset-empty">Data peminjaman tidak ditemukan.</div>;

  return (
    <div className="asset-page">
      <div className="asset-page-header">
        <div>
          <button className="asset-back-button" onClick={() => navigate(-1)}>
            &larr; Kembali
          </button>
          <h2>Detail Peminjaman: {borrowing.asset.assetName}</h2>
        </div>
      </div>
      
      <div className="asset-detail-card">
        <div className="asset-detail-grid">
          <div>
            <strong>Peminjam</strong>
            <div>{borrowing.borrowerName} ({borrowing.borrowerEmail})</div>
          </div>
          <div>
            <strong>Status Peminjaman</strong>
            <div>
              <span className={`asset-status-badge ${borrowing.status}`}>
                {borrowing.status}
              </span>
              {borrowing.overdue && <span style={{ marginLeft: "0.5rem", color: "#dc2626", fontWeight: "bold" }}>(Overdue)</span>}
            </div>
          </div>
          <div>
            <strong>Tujuan Peminjaman</strong>
            <div>{borrowing.purpose}</div>
          </div>
          <div>
            <strong>Catatan (Note)</strong>
            <div>{borrowing.note || "-"}</div>
          </div>
          
          <div>
            <strong>Tanggal Mengajukan</strong>
            <div>{borrowing.createdAt ? new Date(borrowing.createdAt).toLocaleString() : "-"}</div>
          </div>
          <div>
            <strong>Tanggal Mulai Pinjam</strong>
            <div>{new Date(borrowing.borrowDate).toLocaleDateString()}</div>
          </div>
          <div>
            <strong>Ekspektasi Pengembalian</strong>
            <div>{new Date(borrowing.expectedReturnDate).toLocaleDateString()}</div>
          </div>
          <div>
            <strong>Tanggal Kembali Aktual</strong>
            <div>{borrowing.actualReturnDate ? new Date(borrowing.actualReturnDate).toLocaleDateString() : "-"}</div>
          </div>

          {borrowing.approvedByEmail && (
            <div>
              <strong>Disetujui Oleh</strong>
              <div>{borrowing.approvedByEmail} pada {borrowing.approvedAt ? new Date(borrowing.approvedAt).toLocaleString() : "-"}</div>
            </div>
          )}

          {borrowing.handedOverByEmail && (
            <div>
              <strong>Diserahkan Oleh</strong>
              <div>{borrowing.handedOverByEmail} pada {borrowing.handedOverAt ? new Date(borrowing.handedOverAt).toLocaleString() : "-"}</div>
            </div>
          )}

          {borrowing.returnVerifiedByEmail && (
            <div>
              <strong>Verifikasi Pengembalian Oleh</strong>
              <div>{borrowing.returnVerifiedByEmail} pada {borrowing.returnVerifiedAt ? new Date(borrowing.returnVerifiedAt).toLocaleString() : "-"}</div>
            </div>
          )}

          {(borrowing.conditionBefore || borrowing.conditionAfter) && (
            <div>
              <strong>Perubahan Kondisi Aset</strong>
              <div>
                Sebelum: {borrowing.conditionBefore || "-"} <br/>
                Sesudah: {borrowing.conditionAfter || "-"}
              </div>
            </div>
          )}

          {borrowing.handoverProofUrl && (
            <div>
              <strong>Bukti Penyerahan</strong>
              <div><a href={borrowing.handoverProofUrl} target="_blank" rel="noreferrer" style={{ color: "#3b82f6" }}>Lihat Bukti</a></div>
            </div>
          )}

          {borrowing.returnProofUrl && (
            <div>
              <strong>Bukti Pengembalian</strong>
              <div><a href={borrowing.returnProofUrl} target="_blank" rel="noreferrer" style={{ color: "#3b82f6" }}>Lihat Bukti</a></div>
            </div>
          )}

          {(borrowing.rejectionReason || borrowing.cancellationReason) && (
            <div>
              <strong style={{ color: "#dc2626" }}>Alasan Penolakan / Pembatalan</strong>
              <div>{borrowing.rejectionReason || borrowing.cancellationReason}</div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
