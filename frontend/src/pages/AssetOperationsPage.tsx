import React, { useEffect, useState } from "react";
import { useAuth } from "../auth/useAuth";
import { 
  getAllBorrowings, 
  approveBorrowing, 
  rejectBorrowing, 
  handoverAsset, 
  verifyReturnAsset 
} from "../services/assetService";
import type { Borrowing, BorrowingStatus, AssetCondition } from "../types/asset";
import { ApiError } from "../api/http";

export const AssetOperationsPage: React.FC = () => {
  const { token, hasPermission } = useAuth();
  
  const canApprove = hasPermission("asset.borrow.approve");
  const canManageOperations = hasPermission("asset.borrow.handover") || hasPermission("asset.borrow.verify_return");

  const [borrowings, setBorrowings] = useState<Borrowing[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  
  const [filterStatus, setFilterStatus] = useState<BorrowingStatus | "">("REQUESTED");
  const [search, setSearch] = useState("");

  const fetchBorrowings = async () => {
    setIsLoading(true);
    setError("");
    try {
      if (!token) return;
      const res = await getAllBorrowings(token, filterStatus || undefined, undefined, search || undefined, 0, 100);
      setBorrowings(res.content);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Gagal memuat operasional aset.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      fetchBorrowings();
    }, 500);
    return () => clearTimeout(delayDebounceFn);
  }, [filterStatus, search]);

  const handleApprove = async (id: string) => {
    if (!window.confirm("Setujui permohonan peminjaman ini?")) return;
    try {
      if (!token) return;
      await approveBorrowing(token, id);
      fetchBorrowings();
    } catch (err) {
      alert("Gagal menyetujui peminjaman.");
    }
  };

  const handleReject = async (id: string) => {
    const reason = prompt("Alasan penolakan:");
    if (!reason) return;
    try {
      if (!token) return;
      await rejectBorrowing(token, id, { reason });
      fetchBorrowings();
    } catch (err) {
      alert("Gagal menolak peminjaman.");
    }
  };

  const handleHandover = async (id: string, condition: AssetCondition) => {
    if (!window.confirm("Konfirmasi serah terima aset ke peminjam?")) return;
    try {
      if (!token) return;
      // In a real flow, a modal should ask for conditionBefore and handoverProofUrl.
      // For this implementation, we assume condition remains the same and proof is omitted for simplicity.
      await handoverAsset(token, id, { conditionBefore: condition, handoverProofUrl: "", note: "Diserahkan." });
      fetchBorrowings();
    } catch (err) {
      alert("Gagal mencatat serah terima.");
    }
  };

  const handleVerifyReturn = async (id: string, condition: AssetCondition) => {
    const newConditionStr = prompt(`Kondisi saat kembali (GOOD, MINOR_DAMAGE, DAMAGED, UNKNOWN)\nKondisi sebelum: ${condition}`, condition);
    if (!newConditionStr) return;
    
    if (!["GOOD", "MINOR_DAMAGE", "DAMAGED", "UNKNOWN"].includes(newConditionStr)) {
      alert("Input kondisi tidak valid.");
      return;
    }

    try {
      if (!token) return;
      await verifyReturnAsset(token, id, { conditionAfter: newConditionStr as AssetCondition, note: "Pengembalian diverifikasi." });
      fetchBorrowings();
    } catch (err) {
      alert("Gagal memverifikasi pengembalian.");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Operasional Aset</h1>
          <p className="text-gray-500 dark:text-gray-400">Kelola persetujuan, serah terima, dan pengembalian aset</p>
        </div>
      </div>

      <div className="division-task-filters">
        <input 
          type="text" 
          placeholder="Cari nama peminjam..." 
          value={search} 
          onChange={e => setSearch(e.target.value)} 
        />
        <select value={filterStatus} onChange={e => setFilterStatus(e.target.value as any)}>
          <option value="">Semua Status</option>
          <option value="REQUESTED">Menunggu Persetujuan</option>
          <option value="APPROVED">Disetujui (Menunggu Penyerahan)</option>
          <option value="BORROWED">Sedang Dipinjam</option>
          <option value="RETURN_REQUESTED">Menunggu Verifikasi Kembali</option>
          <option value="RETURN_VERIFIED">Selesai / Dikembalikan</option>
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
                  <div className="flex justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
                        {borrowing.borrowerName} meminjam {borrowing.asset.assetName}
                      </p>
                      <p className="mt-1 text-sm text-gray-500">
                        Tujuan: {borrowing.purpose}
                      </p>
                      <p className="mt-1 text-sm text-gray-500">
                        Tanggal: {new Date(borrowing.borrowDate).toLocaleDateString()} - {new Date(borrowing.expectedReturnDate).toLocaleDateString()}
                      </p>
                    </div>
                    <div className="flex flex-col items-end gap-2">
                      <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">
                        {borrowing.status}
                      </span>
                      <div className="flex gap-2 mt-2">
                        {borrowing.status === "REQUESTED" && canApprove && (
                          <>
                            <button className="text-sm text-blue-600 hover:text-blue-900 font-semibold" onClick={() => handleApprove(borrowing.id)}>Setujui</button>
                            <button className="text-sm text-red-600 hover:text-red-900 font-semibold" onClick={() => handleReject(borrowing.id)}>Tolak</button>
                          </>
                        )}
                        {borrowing.status === "APPROVED" && canManageOperations && (
                          <button className="text-sm text-purple-600 hover:text-purple-900 font-semibold bg-purple-50 px-2 py-1 rounded border border-purple-200" onClick={() => handleHandover(borrowing.id, borrowing.asset.currentCondition)}>Serahkan Aset</button>
                        )}
                        {borrowing.status === "RETURN_REQUESTED" && canManageOperations && (
                          <button className="text-sm text-green-600 hover:text-green-900 font-semibold bg-green-50 px-2 py-1 rounded border border-green-200" onClick={() => handleVerifyReturn(borrowing.id, borrowing.asset.currentCondition)}>Verifikasi Pengembalian</button>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </li>
            ))}
            {borrowings.length === 0 && !isLoading && (
              <li className="px-4 py-8 text-center text-gray-500">Tidak ada data operasional yang perlu ditindak.</li>
            )}
          </ul>
        </div>
      )}
    </div>
  );
};
