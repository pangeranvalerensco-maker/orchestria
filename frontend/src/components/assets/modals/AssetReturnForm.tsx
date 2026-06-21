import React, { useState } from "react";

import type { AssetReturnRequest } from "../../../types/asset";

interface AssetReturnFormProps {
  onConfirm: (data: AssetReturnRequest) => Promise<void>;
  onCancel: () => void;
}

export const AssetReturnForm: React.FC<AssetReturnFormProps> = ({
  onConfirm,
  onCancel,
}) => {
  const [returnProofUrl, setReturnProofUrl] = useState("");
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!returnProofUrl.trim()) return;
    try {
      setLoading(true);
      await onConfirm({
        returnProofUrl,
        note: note.trim() ? note : undefined,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="asset-modal-overlay">
      <div className="asset-modal-content">
        <h2>Kembalikan Aset</h2>
        <form onSubmit={handleSubmit}>
          <div className="asset-form-group">
            <label>URL Bukti Pengembalian (Foto)</label>
            <input
              type="url"
              required
              value={returnProofUrl}
              onChange={(e) => setReturnProofUrl(e.target.value)}
              disabled={loading}
              className="asset-form-input"
              placeholder="https://..."
            />
          </div>
          <div className="asset-form-group">
            <label>Catatan Pengembalian (Opsional)</label>
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
              disabled={loading || !returnProofUrl.trim()}
            >
              {loading ? "Menyimpan..." : "Ajukan Pengembalian"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
