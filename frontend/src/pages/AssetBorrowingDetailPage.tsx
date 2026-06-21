import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { useAuth } from "../auth/useAuth";
import { ApiError } from "../api/http";
import { getBorrowingById } from "../services/assetService";
import type { Borrowing } from "../types/asset";
import "../assets.css";

export const AssetBorrowingDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { token, hasPermission } = useAuth();
  const [borrowing, setBorrowing] = useState<Borrowing | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const canManageOperations = hasPermission("asset.borrow.handover") || hasPermission("asset.borrow.verify_return");

  useEffect(() => {
    const fetchBorrowing = async () => {
      try {
        if (!token || !id) return;
        const res = await getBorrowingById(token, id);
        setBorrowing(res);
      } catch (err) {
        if (err instanceof ApiError) {
          setError(err.message);
        } else {
          setError("Gagal memuat detail peminjaman.");
        }
      } finally {
        setLoading(false);
      }
    };
    fetchBorrowing();
  }, [id, token, canManageOperations]);

  if (loading) return <div>Memuat detail peminjaman...</div>;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (!borrowing) return <div>Data peminjaman tidak ditemukan.</div>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-4">
        <div>
          <button className="text-blue-600 hover:text-blue-800 mb-2" onClick={() => navigate(-1)}>
            &larr; Kembali
          </button>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Detail Peminjaman: {borrowing.asset.assetName}</h1>
        </div>
      </div>
      <div className="bg-white shadow overflow-hidden sm:rounded-lg dark:bg-gray-800 p-6">
        <dl className="grid grid-cols-1 gap-x-4 gap-y-8 sm:grid-cols-2">
          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500">Peminjam</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">{borrowing.borrowerName}</dd>
          </div>
          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500">Status Peminjaman</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">
              <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">
                {borrowing.status}
              </span>
            </dd>
          </div>
          <div className="sm:col-span-2">
            <dt className="text-sm font-medium text-gray-500">Tujuan Peminjaman</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">{borrowing.purpose}</dd>
          </div>
          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500">Tanggal Mulai</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">
              {new Date(borrowing.borrowDate).toLocaleDateString()}
            </dd>
          </div>
          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500">Ekspektasi Pengembalian</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">
              {new Date(borrowing.expectedReturnDate).toLocaleDateString()}
            </dd>
          </div>
          {borrowing.actualReturnDate && (
            <div className="sm:col-span-1">
              <dt className="text-sm font-medium text-gray-500">Tanggal Kembali Aktual</dt>
              <dd className="mt-1 text-sm text-gray-900 dark:text-white">
                {new Date(borrowing.actualReturnDate).toLocaleDateString()}
              </dd>
            </div>
          )}
          {borrowing.rejectionReason && (
            <div className="sm:col-span-2">
              <dt className="text-sm font-medium text-gray-500 text-red-600">Alasan Penolakan / Pembatalan</dt>
              <dd className="mt-1 text-sm text-gray-900 dark:text-white">{borrowing.rejectionReason}</dd>
            </div>
          )}
        </dl>
      </div>
    </div>
  );
};
