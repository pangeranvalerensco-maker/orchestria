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
  
  const [formData, setFormData] = useState<BorrowingCreateRequest>({
    assetId: asset.id,
    purpose: "",
    borrowDate: new Date().toISOString().split('T')[0],
    expectedReturnDate: new Date(Date.now() + 86400000).toISOString().split('T')[0] // default tomorrow
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
    
    // Convert dates to ISO strings with time if backend expects LocalDateTime.
    // Assuming backend receives 'yyyy-MM-dd' or we should format as start/end of day.
    const requestData = {
      ...formData,
      borrowDate: new Date(formData.borrowDate).toISOString(),
      expectedReturnDate: new Date(formData.expectedReturnDate).toISOString()
    };
    
    try {
      if (!token) return;
      await createBorrowing(token, requestData as any); // using any for simplicity, as we overwrite dates
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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-lg flex flex-col max-h-[90vh]">
        <div className="flex justify-between items-center p-4 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
            Pengajuan Peminjaman
          </h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-500">
            <span className="sr-only">Tutup</span>
            <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <div className="p-4 overflow-y-auto">
          {error && <div className="mb-4 alert alert-error">{error}</div>}
          
          <div className="mb-4 p-3 bg-blue-50 text-blue-900 rounded-md">
            Anda akan meminjam: <strong>{asset.assetName}</strong> ({asset.assetCode})
          </div>

          <form id="borrowing-form" onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Tanggal Mulai Pinjam *</label>
              <input
                type="date"
                name="borrowDate"
                required
                value={formData.borrowDate}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Ekspektasi Tanggal Kembali *</label>
              <input
                type="date"
                name="expectedReturnDate"
                required
                value={formData.expectedReturnDate}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Tujuan Peminjaman *</label>
              <textarea
                name="purpose"
                required
                rows={3}
                value={formData.purpose}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                placeholder="Jelaskan untuk kegiatan apa aset ini digunakan"
              ></textarea>
            </div>
          </form>
        </div>
        
        <div className="p-4 border-t border-gray-200 dark:border-gray-700 flex justify-end gap-3 bg-gray-50 dark:bg-gray-800 rounded-b-lg">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200 dark:hover:bg-gray-600"
            disabled={isSubmitting}
          >
            Batal
          </button>
          <button
            type="submit"
            form="borrowing-form"
            className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Mengajukan..." : "Ajukan"}
          </button>
        </div>
      </div>
    </div>
  );
};
