import React, { useState } from "react";


interface BorrowingDecisionFormProps {
  actionLabel: string;
  onConfirm: (reason: string) => Promise<void>;
  onCancel: () => void;
}

export const BorrowingDecisionForm: React.FC<BorrowingDecisionFormProps> = ({
  actionLabel,
  onConfirm,
  onCancel,
}) => {
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reason.trim()) return;
    try {
      setLoading(true);
      await onConfirm(reason);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="asset-modal-overlay">
      <div className="asset-modal-content">
        <h2>{actionLabel}</h2>
        <form onSubmit={handleSubmit}>
          <div className="asset-form-group">
            <label>Alasan</label>
            <textarea
              required
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              disabled={loading}
              className="asset-form-input"
              rows={4}
            />
          </div>
          <div className="asset-modal-actions">
            <button
              type="button"
              className="asset-btn asset-btn-secondary"
              onClick={onCancel}
              disabled={loading}
            >
              Batal
            </button>
            <button
              type="submit"
              className="asset-btn asset-btn-danger"
              disabled={loading || !reason.trim()}
            >
              {loading ? "Menyimpan..." : "Konfirmasi"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
