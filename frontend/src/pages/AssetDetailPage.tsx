import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { useAuth } from "../auth/useAuth";
import { ApiError } from "../api/http";
import { getAssetById } from "../services/assetService";
import type { Asset } from "../types/asset";
import "../assets.css";

export const AssetDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { token } = useAuth();
  const [asset, setAsset] = useState<Asset | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchAsset = async () => {
      try {
        if (!token || !id) return;
        const res = await getAssetById(token, id);
        setAsset(res);
      } catch (err) {
        if (err instanceof ApiError) {
          setError(err.message);
        } else {
          setError("Gagal memuat detail aset.");
        }
      } finally {
        setLoading(false);
      }
    };
    fetchAsset();
  }, [id, token]);

  if (loading) return <div>Memuat detail aset...</div>;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (!asset) return <div>Aset tidak ditemukan.</div>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-4">
        <div>
          <button className="text-blue-600 hover:text-blue-800 mb-2" onClick={() => navigate(-1)}>
            &larr; Kembali
          </button>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Detail Aset: {asset.assetName}</h1>
        </div>
      </div>
      <div className="bg-white shadow overflow-hidden sm:rounded-lg dark:bg-gray-800 p-6">
        <dl className="grid grid-cols-1 gap-x-4 gap-y-8 sm:grid-cols-2">
          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500">Kode Aset</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">{asset.assetCode}</dd>
          </div>
          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500">Kategori</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">{asset.category}</dd>
          </div>
          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500">Status</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">
              <span className={`asset-status ${asset.currentStatus}`}>{asset.currentStatus}</span>
            </dd>
          </div>
          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500">Kondisi</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">{asset.currentCondition}</dd>
          </div>
          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500">Lokasi</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">{asset.location}</dd>
          </div>
          <div className="sm:col-span-2">
            <dt className="text-sm font-medium text-gray-500">Deskripsi</dt>
            <dd className="mt-1 text-sm text-gray-900 dark:text-white">{asset.description || "-"}</dd>
          </div>
        </dl>
      </div>
    </div>
  );
};
