import React, { useState } from "react";
import { useAuth } from "../../auth/useAuth";
import { createBorrowing } from "../../services/assetService";
import type { Asset, BorrowingCreateRequest } from "../../types/asset";
import { ApiError } from "../../api/http";

interface BorrowingFormProps {
  asset: Asset;
  onClose: () => void;
  onSuccess: () => void;
}

export const BorrowingForm: React.FC<BorrowingFormProps> = ({ asset, onClose, onSuccess }) => {
  const { token } = useAuth();
  
  const today = new Date();
  const tomorrow = new Date(today.getTime() + 86400000);
  const formatDate = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

  const [formData, setFormData] = useState<BorrowingCreateRequest>({
    assetId: asset.id,
    purpose: "",
    borrowDate: formatDate(today),
    expectedReturnDate: formatDate(tomorrow)
  });
  
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);
    
    const trimmedPurpose = formData.purpose.trim();
    if (!trimmedPurpose) {
      setError("Tujuan peminjaman tidak boleh kosong.");
      setIsSubmitting(false);
      return;
    }

    const borrowDateObj = new Date(formData.borrowDate);
    const expectedReturnDateObj = new Date(formData.expectedReturnDate);
    
    borrowDateObj.setHours(0, 0, 0, 0);
    const todayObj = new Date();
    todayObj.setHours(0, 0, 0, 0);

    if (borrowDateObj < todayObj) {
      setError("Tanggal peminjaman tidak boleh di masa lalu.");
      setIsSubmitting(false);
      return;
    }

    if (expectedReturnDateObj < borrowDateObj) {
      setError("Tanggal pengembalian tidak valid.");
      setIsSubmitting(false);
      return;
    }

    const requestData = {
      ...formData,
      purpose: trimmedPurpose
    };
    
    try {
      if (!token) return;
      await createBorrowing(token, requestData);
      onSuccess();
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Gagal mengajukan peminjaman aset.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="asset-modal-overlay">
      <div className="asset-modal-content">
        <div className="asset-modal-header">
          <h2>
            Pengajuan Peminjaman
          </h2>
          <button onClick={onClose} className="asset-btn-danger" style={{ background: "none", border: "none" }}>
            <span className="sr-only">Tutup</span>
            <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <div className="asset-modal-body">
          {error && <div className="asset-alert asset-alert-error">{error}</div>}
          
          <div className="asset-alert" style={{ backgroundColor: "#eff6ff", color: "#1e3a8a", border: "1px solid #bfdbfe" }}>
            Anda akan meminjam: <strong>{asset.assetName}</strong> ({asset.assetCode})
          </div>

          <form id="borrowing-form" onSubmit={handleSubmit} style={{ marginTop: "1rem" }}>
            <div className="asset-form-group">
              <label>Tanggal Mulai Pinjam *</label>
              <input
                type="date"
                name="borrowDate"
                required
                value={formData.borrowDate}
                onChange={handleChange}
                className="asset-form-input"
              />
            </div>
            
            <div className="asset-form-group">
              <label>Ekspektasi Tanggal Kembali *</label>
              <input
                type="date"
                name="expectedReturnDate"
                required
                value={formData.expectedReturnDate}
                onChange={handleChange}
                className="asset-form-input"
              />
            </div>

            <div className="asset-form-group">
              <label>Tujuan Peminjaman *</label>
              <textarea
                name="purpose"
                required
                rows={3}
                value={formData.purpose}
                onChange={handleChange}
                className="asset-form-input"
                placeholder="Jelaskan untuk kegiatan apa aset ini digunakan"
              ></textarea>
            </div>
          </form>
        </div>
        
        <div className="asset-modal-footer">
          <button
            type="button"
            onClick={onClose}
            className="asset-btn asset-btn-secondary"
            disabled={isSubmitting}
          >
            Batal
          </button>
          <button
            type="submit"
            form="borrowing-form"
            className="asset-btn asset-btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Mengajukan..." : "Ajukan"}
          </button>
        </div>
      </div>
    </div>
  );
};
