import React, { useState } from "react";

import type { AssetCondition, AssetStatus, AssetConditionUpdateRequest } from "../../../types/asset";

interface AssetConditionFormProps {
  initialCondition: AssetCondition;
  initialStatus: AssetStatus;
  onConfirm: (data: AssetConditionUpdateRequest) => Promise<void>;
  onCancel: () => void;
}

export const AssetConditionForm: React.FC<AssetConditionFormProps> = ({
  initialCondition,
  initialStatus,
  onConfirm,
  onCancel,
}) => {
  const [newCondition, setNewCondition] = useState<AssetCondition>(initialCondition);
  const [newStatus, setNewStatus] = useState<AssetStatus>(initialStatus);
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      await onConfirm({
        newCondition,
        newStatus,
        note: note.trim() ? note : undefined,
      });
    } finally {
      setLoading(false);
    }
  };

  if (initialStatus === "RESERVED" || initialStatus === "BORROWED") {
    return (
      <div className="asset-modal-overlay">
        <div className="asset-modal-content">
          <h2>Perbarui Kondisi Aset</h2>
          <div className="asset-alert asset-alert-warning">
            Status hanya dapat berubah melalui alur peminjaman.
          </div>
          <div className="asset-modal-actions">
            <button
              type="button"
              className="asset-btn asset-btn-secondary"
              onClick={onCancel}
            >
              Kembali
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="asset-modal-overlay">
      <div className="asset-modal-content">
        <h2>Perbarui Kondisi Aset</h2>
        <form onSubmit={handleSubmit}>
          <div className="asset-form-group">
            <label>Kondisi Baru</label>
            <select
              value={newCondition}
              onChange={(e) => setNewCondition(e.target.value as AssetCondition)}
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
            <label>Status Baru</label>
            <select
              value={newStatus}
              onChange={(e) => setNewStatus(e.target.value as AssetStatus)}
              disabled={loading}
              className="asset-form-input"
            >
              <option value="AVAILABLE">AVAILABLE</option>
              <option value="MAINTENANCE">MAINTENANCE</option>
              <option value="LOST">LOST</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
          </div>
          <div className="asset-form-group">
            <label>Catatan (Opsional)</label>
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
              {loading ? "Menyimpan..." : "Perbarui Kondisi"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
