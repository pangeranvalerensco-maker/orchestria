import React, { useState } from "react";
import { cancelBorrowing } from "../../../services/assetService";
import { useAuth } from "../../../auth/useAuth";

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
    } catch (err: any) {
      setError(err.message || "Terjadi kesalahan saat membatalkan peminjaman.");
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6 dark:bg-gray-800">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Batalkan Peminjaman</h2>
        
        {error && <div className="alert alert-error mb-4">{error}</div>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">
              Alasan Pembatalan
            </label>
            <textarea
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              required
            />
          </div>

          <div className="flex justify-end space-x-3 mt-6">
            <button
              type="button"
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Kembali
            </button>
            <button
              type="submit"
              className="px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:bg-red-400"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Menyimpan..." : "Batalkan Peminjaman"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
