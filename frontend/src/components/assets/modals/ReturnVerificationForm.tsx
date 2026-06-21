import React, { useState } from "react";

import type { AssetCondition, AssetReturnVerificationRequest } from "../../../types/asset";

interface ReturnVerificationFormProps {
  initialCondition: AssetCondition;
  onConfirm: (data: AssetReturnVerificationRequest) => Promise<void>;
  onCancel: () => void;
}

export const ReturnVerificationForm: React.FC<ReturnVerificationFormProps> = ({
  initialCondition,
  onConfirm,
  onCancel,
}) => {
  const [conditionAfter, setConditionAfter] = useState<AssetCondition>(initialCondition);
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      await onConfirm({
        conditionAfter,
        note: note.trim() ? note : undefined,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="asset-modal-overlay">
      <div className="asset-modal-content">
        <h2>Verifikasi Pengembalian Aset</h2>
        <form onSubmit={handleSubmit}>
          <div className="asset-form-group">
            <label>Kondisi Aset Saat Dikembalikan</label>
            <select
              value={conditionAfter}
              onChange={(e) => setConditionAfter(e.target.value as AssetCondition)}
              disabled={loading}
              className="asset-form-input"
            >
              <option value="GOOD">GOOD</option>
              <option value="MINOR_DAMAGE">MINOR_DAMAGE</option>
              <option value="DAMAGED">DAMAGED</option>
              <option value="UNKNOWN">UNKNOWN</option>
            </select>
          </div>
          <div className="asset-form-group">
            <label>Catatan Verifikasi (Opsional)</label>
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              disabled={loading}
              className="asset-form-input"
              rows={3}
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
              className="asset-btn asset-btn-primary"
              disabled={loading}
            >
              {loading ? "Menyimpan..." : "Konfirmasi Verifikasi"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
