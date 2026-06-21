import React, { useState } from "react";
import { cancelBorrowing } from "../../../services/assetService";
import { useAuth } from "../../../auth/useAuth";
import { ApiError } from "../../../api/http";

interface BorrowingCancelFormProps {
  borrowingId: string;
  onSuccess: () => void;
  onClose: () => void;
}

export const BorrowingCancelForm: React.FC<BorrowingCancelFormProps> = ({ borrowingId, onSuccess, onClose }) => {
  const { token } = useAuth();
  const [reason, setReason] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    if (!reason.trim()) {
      setError("Alasan pembatalan wajib diisi");
      return;
    }

    setIsSubmitting(true);
    setError("");

    try {
      await cancelBorrowing(token, borrowingId, { reason });
      onSuccess();
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Terjadi kesalahan saat membatalkan peminjaman.");
      }
      setIsSubmitting(false);
    }
  };

  return (
    <div className="asset-modal-overlay">
      <div className="asset-modal-content">
        <div className="asset-modal-header">
          <h2>Batalkan Peminjaman</h2>
        </div>
        
        <div className="asset-modal-body">
          {error && <div className="asset-alert asset-alert-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="asset-form-group">
              <label>
                Alasan Pembatalan
              </label>
              <textarea
                className="asset-form-input"
                rows={3}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                required
              />
            </div>
          </form>
        </div>

        <div className="asset-modal-footer">
            <button
              type="button"
              className="asset-btn asset-btn-secondary"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Kembali
            </button>
            <button
              type="submit"
              onClick={handleSubmit}
              className="asset-btn asset-btn-danger"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Menyimpan..." : "Batalkan Peminjaman"}
            </button>
        </div>
      </div>
    </div>
  );
};
