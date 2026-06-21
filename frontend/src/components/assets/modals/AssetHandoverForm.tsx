import React, { useState } from "react";

import type { AssetCondition, AssetHandoverRequest } from "../../../types/asset";

interface AssetHandoverFormProps {
  initialCondition: AssetCondition;
  onConfirm: (data: AssetHandoverRequest) => Promise<void>;
  onCancel: () => void;
}

export const AssetHandoverForm: React.FC<AssetHandoverFormProps> = ({
  initialCondition,
  onConfirm,
  onCancel,
}) => {
  const [conditionBefore, setConditionBefore] = useState<AssetCondition>(initialCondition);
  const [handoverProofUrl, setHandoverProofUrl] = useState("");
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!handoverProofUrl.trim()) return;
    try {
      setLoading(true);
      await onConfirm({
        conditionBefore,
        handoverProofUrl,
        note: note.trim() ? note : undefined,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="asset-modal-overlay">
      <div className="asset-modal-content">
        <h2>Serahkan Aset</h2>
        <form onSubmit={handleSubmit}>
          <div className="asset-form-group">
            <label>Kondisi Aset Saat Penyerahan</label>
            <select
              value={conditionBefore}
              onChange={(e) => setConditionBefore(e.target.value as AssetCondition)}
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
            <label>URL Bukti Penyerahan (Foto)</label>
            <input
              type="url"
              required
              value={handoverProofUrl}
              onChange={(e) => setHandoverProofUrl(e.target.value)}
              disabled={loading}
              className="asset-form-input"
              placeholder="https://..."
            />
          </div>
          <div className="asset-form-group">
            <label>Catatan Penyerahan (Opsional)</label>
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
              disabled={loading || !handoverProofUrl.trim()}
            >
              {loading ? "Menyimpan..." : "Konfirmasi Penyerahan"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
