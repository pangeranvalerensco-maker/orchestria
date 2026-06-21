import React, { useEffect, useState } from "react";
import { useAuth } from "../auth/useAuth";
import { getMyBorrowings, cancelBorrowing, requestReturnAsset } from "../services/assetService";
import type { Borrowing, BorrowingStatus } from "../types/asset";
import { ApiError } from "../api/http";

export const MyBorrowingsPage: React.FC = () => {
  const { token } = useAuth();
  
  const [borrowings, setBorrowings] = useState<Borrowing[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [filterStatus, setFilterStatus] = useState<BorrowingStatus | "">("");

  const fetchMyBorrowings = async () => {
    setIsLoading(true);
    setError("");
    try {
      if (!token) return;
      const res = await getMyBorrowings(token, filterStatus || undefined, 0, 100);
      setBorrowings(res.content);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Gagal memuat peminjaman saya.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchMyBorrowings();
  }, [filterStatus]);

  const handleCancel = async (id: string) => {
    const reason = prompt("Alasan pembatalan:");
    if (!reason) return;
    try {
      if (!token) return;
      await cancelBorrowing(token, id, { reason });
      fetchMyBorrowings();
    } catch (err) {
      alert("Gagal membatalkan peminjaman.");
    }
  };

  const handleReturn = async (id: string) => {
    const note = prompt("Catatan pengembalian (opsional):");
    if (note === null) return;
    try {
      if (!token) return;
      // using empty returnProofUrl for simplicity in this frontend iteration
      await requestReturnAsset(token, id, { returnProofUrl: "", note });
      fetchMyBorrowings();
    } catch (err) {
      alert("Gagal mengajukan pengembalian.");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Peminjaman Saya</h1>
          <p className="text-gray-500 dark:text-gray-400">Daftar peminjaman aset yang Anda ajukan</p>
        </div>
      </div>

      <div className="division-task-filters">
        <select value={filterStatus} onChange={e => setFilterStatus(e.target.value as any)}>
          <option value="">Semua Status</option>
          <option value="REQUESTED">Menunggu Persetujuan</option>
          <option value="APPROVED">Disetujui (Menunggu Penyerahan)</option>
          <option value="BORROWED">Sedang Dipinjam</option>
          <option value="RETURN_REQUESTED">Menunggu Verifikasi Kembali</option>
          <option value="RETURN_VERIFIED">Dikembalikan</option>
          <option value="REJECTED">Ditolak</option>
          <option value="CANCELLED">Dibatalkan</option>
        </select>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {isLoading ? (
        <div>Memuat...</div>
      ) : (
        <div className="bg-white shadow overflow-hidden sm:rounded-md dark:bg-gray-800">
          <ul className="divide-y divide-gray-200 dark:divide-gray-700">
            {borrowings.map((borrowing) => (
              <li key={borrowing.id}>
                <div className="px-4 py-4 sm:px-6">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-medium text-blue-600 truncate dark:text-blue-400">
                      {borrowing.asset.assetName} ({borrowing.asset.assetCode})
                    </p>
                    <div className="ml-2 flex-shrink-0 flex">
                      <p className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                        ${borrowing.status === 'BORROWED' ? 'bg-blue-100 text-blue-800' : ''}
                        ${borrowing.status === 'RETURN_VERIFIED' ? 'bg-green-100 text-green-800' : ''}
                        ${borrowing.status === 'REJECTED' || borrowing.status === 'CANCELLED' ? 'bg-red-100 text-red-800' : ''}
                        ${borrowing.status === 'REQUESTED' || borrowing.status === 'RETURN_REQUESTED' ? 'bg-yellow-100 text-yellow-800' : ''}
                        ${borrowing.status === 'APPROVED' ? 'bg-purple-100 text-purple-800' : ''}
                      `}>
                        {borrowing.status}
                      </p>
                    </div>
                  </div>
                  <div className="mt-2 sm:flex sm:justify-between">
                    <div className="sm:flex">
                      <p className="flex items-center text-sm text-gray-500 dark:text-gray-400">
                        Mulai: {new Date(borrowing.borrowDate).toLocaleDateString()} &bull; Ekspektasi: {new Date(borrowing.expectedReturnDate).toLocaleDateString()}
                      </p>
                    </div>
                    <div className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0 gap-2">
                      {borrowing.status === "REQUESTED" && (
                        <button className="text-red-600 hover:text-red-900" onClick={() => handleCancel(borrowing.id)}>Batalkan</button>
                      )}
                      {borrowing.status === "BORROWED" && (
                        <button className="text-blue-600 hover:text-blue-900" onClick={() => handleReturn(borrowing.id)}>Ajukan Pengembalian</button>
                      )}
                    </div>
                  </div>
                  {borrowing.rejectionReason && (
                    <div className="mt-2 text-sm text-red-600 bg-red-50 p-2 rounded">
                      <strong>Alasan penolakan:</strong> {borrowing.rejectionReason}
                    </div>
                  )}
                </div>
              </li>
            ))}
            {borrowings.length === 0 && !isLoading && (
              <li className="px-4 py-8 text-center text-gray-500">Tidak ada data peminjaman.</li>
            )}
          </ul>
        </div>
      )}
    </div>
  );
};
