import React, { useEffect, useState } from "react";
import { useAuth } from "../auth/useAuth";
import { 
  getAllBorrowings, 
  approveBorrowing, 
  rejectBorrowing, 
  handoverAsset, 
  verifyReturnAsset 
} from "../services/assetService";
import type { Borrowing, BorrowingStatus, AssetCondition, AssetHandoverRequest, AssetReturnVerificationRequest } from "../types/asset";
import { ApiError } from "../api/http";
import { BorrowingDecisionForm } from "../components/assets/modals/BorrowingDecisionForm";
import { AssetHandoverForm } from "../components/assets/modals/AssetHandoverForm";
import { ReturnVerificationForm } from "../components/assets/modals/ReturnVerificationForm";
import { useNavigate } from "react-router";

export const AssetOperationsPage: React.FC = () => {
  const { token, hasPermission } = useAuth();
  const navigate = useNavigate();
  
  const canApprove = hasPermission("asset.borrow.approve");
  const canManageOperations = hasPermission("asset.borrow.handover") || hasPermission("asset.return.verify");

  const [borrowings, setBorrowings] = useState<Borrowing[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  
  const [filterStatus, setFilterStatus] = useState<BorrowingStatus | "">("REQUESTED");
  const [search, setSearch] = useState("");

  const [rejectingId, setRejectingId] = useState<string | null>(null);
  const [handoverData, setHandoverData] = useState<{id: string, condition: AssetCondition} | null>(null);
  const [verifyReturnData, setVerifyReturnData] = useState<{id: string, condition: AssetCondition} | null>(null);

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
    } catch (err: unknown) {
      setError("Gagal menyetujui peminjaman.");
    }
  };

  const handleReject = async (reason: string) => {
    if (!rejectingId) return;
    try {
      if (!token) return;
      await rejectBorrowing(token, rejectingId, { reason });
      setRejectingId(null);
      fetchBorrowings();
    } catch (err: unknown) {
      setError("Gagal menolak peminjaman.");
    }
  };

  const handleHandover = async (data: unknown) => {
    if (!handoverData) return;
    try {
      if (!token) return;
      await handoverAsset(token, handoverData.id, data as AssetHandoverRequest);
      setHandoverData(null);
      fetchBorrowings();
    } catch (err: unknown) {
      setError("Gagal mencatat serah terima.");
    }
  };

  const handleVerifyReturn = async (data: unknown) => {
    if (!verifyReturnData) return;
    try {
      if (!token) return;
      await verifyReturnAsset(token, verifyReturnData.id, data as AssetReturnVerificationRequest);
      setVerifyReturnData(null);
      fetchBorrowings();
    } catch (err: unknown) {
      setError("Gagal memverifikasi pengembalian.");
    }
  };

  return (
    <div className="asset-directory-page">
      <div className="asset-header-row">
        <div>
          <h2>Operasional Aset</h2>
          <p>Kelola persetujuan, serah terima, dan pengembalian aset</p>
        </div>
      </div>

      <div className="division-task-filters">
        <input 
          type="text" 
          placeholder="Cari nama peminjam..." 
          value={search} 
          onChange={e => setSearch(e.target.value)} 
        />
        <select value={filterStatus} onChange={e => setFilterStatus(e.target.value as BorrowingStatus)}>
          <option value="">Semua Status</option>
          <option value="REQUESTED">Menunggu Persetujuan</option>
          <option value="APPROVED">Disetujui (Menunggu Penyerahan)</option>
          <option value="BORROWED">Sedang Dipinjam</option>
          <option value="RETURN_REQUESTED">Menunggu Verifikasi Kembali</option>
          <option value="RETURN_VERIFIED">Selesai / Dikembalikan</option>
        </select>
      </div>

      {error && <div className="asset-alert asset-alert-error">{error}</div>}

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
                            <button className="text-sm text-red-600 hover:text-red-900 font-semibold" onClick={() => setRejectingId(borrowing.id)}>Tolak</button>
                          </>
                        )}
                        {borrowing.status === "APPROVED" && canManageOperations && (
                          <button className="text-sm text-purple-600 hover:text-purple-900 font-semibold bg-purple-50 px-2 py-1 rounded border border-purple-200" onClick={() => setHandoverData({ id: borrowing.id, condition: borrowing.asset.currentCondition })}>Serahkan Aset</button>
                        )}
                        {borrowing.status === "RETURN_REQUESTED" && canManageOperations && (
                          <button className="text-sm text-green-600 hover:text-green-900 font-semibold bg-green-50 px-2 py-1 rounded border border-green-200" onClick={() => setVerifyReturnData({ id: borrowing.id, condition: borrowing.asset.currentCondition })}>Verifikasi Pengembalian</button>
                        )}
                        <button className="text-sm text-blue-600 hover:text-blue-900 font-semibold" onClick={() => navigate(`/asset-borrowings/${borrowing.id}`)}>Detail</button>
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

      {rejectingId && (
        <BorrowingDecisionForm
          actionLabel="Tolak Permohonan"
          onConfirm={handleReject}
          onCancel={() => setRejectingId(null)}
        />
      )}

      {handoverData && (
        <AssetHandoverForm
          initialCondition={handoverData.condition}
          onConfirm={handleHandover}
          onCancel={() => setHandoverData(null)}
        />
      )}

      {verifyReturnData && (
        <ReturnVerificationForm
          initialCondition={verifyReturnData.condition}
          onConfirm={handleVerifyReturn}
          onCancel={() => setVerifyReturnData(null)}
        />
      )}
    </div>
  );
};
