import React, { useEffect, useState } from "react";
import { useAuth } from "../auth/useAuth";
import { getAssets, deleteAsset } from "../services/assetService";
import { useNavigate } from "react-router";
import type { Asset, AssetStatus, AssetCondition } from "../types/asset";
import { ApiError } from "../api/http";
import "../assets.css";
import { AssetForm } from "../components/assets/AssetForm";
import { BorrowingForm } from "../components/assets/BorrowingForm";

export const AssetDirectoryPage: React.FC = () => {
  const { token, hasPermission } = useAuth();
  const navigate = useNavigate();
  const isManager = hasPermission("asset.manage");

  const [assets, setAssets] = useState<Asset[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingAsset, setEditingAsset] = useState<Asset | null>(null);

  const [isBorrowFormOpen, setIsBorrowFormOpen] = useState(false);
  const [borrowingAsset, setBorrowingAsset] = useState<Asset | null>(null);

  const [search, setSearch] = useState("");
  const [filterStatus, setFilterStatus] = useState<AssetStatus | "">("");
  const [filterCondition, setFilterCondition] = useState<AssetCondition | "">("");

  // Pagination simplified for now (fetching 100 to avoid pagination UI complexity in Phase 22)
  const fetchAllAssets = async () => {
    setIsLoading(true);
    setError("");
    try {
      if (!token) return;
      const res = await getAssets(token, search, filterStatus || undefined, filterCondition || undefined, 0, 100);
      setAssets(res.content);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Gagal memuat katalog aset.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      fetchAllAssets();
    }, 500);
    return () => clearTimeout(delayDebounceFn);
  }, [search, filterStatus, filterCondition]);

  const handleDelete = async (id: string) => {
    if (!window.confirm("Hapus aset ini?")) return;
    try {
      if (!token) return;
      await deleteAsset(token, id);
      fetchAllAssets();
    } catch (err: unknown) {
      alert("Gagal menghapus aset.");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Katalog Aset PUB</h1>
          <p className="text-gray-500 dark:text-gray-400">Lihat dan pinjam aset inventaris PUB</p>
        </div>
        {isManager && (
          <button className="asset-btn-primary" onClick={() => { setEditingAsset(null); setIsFormOpen(true); }}>
            + Tambah Aset
          </button>
        )}
      </div>

      <div className="division-task-filters">
        <input 
          type="text" 
          placeholder="Cari kode atau nama aset..." 
          value={search} 
          onChange={e => setSearch(e.target.value)} 
        />
        <select value={filterStatus} onChange={e => setFilterStatus(e.target.value as any)}>
          <option value="">Semua Status</option>
          <option value="AVAILABLE">Tersedia</option>
          <option value="RESERVED">Dipesan</option>
          <option value="BORROWED">Dipinjam</option>
          <option value="MAINTENANCE">Perbaikan</option>
          <option value="LOST">Hilang</option>
          <option value="INACTIVE">Tidak Aktif</option>
        </select>
        <select value={filterCondition} onChange={e => setFilterCondition(e.target.value as any)}>
          <option value="">Semua Kondisi</option>
          <option value="GOOD">Baik</option>
          <option value="MINOR_DAMAGE">Rusak Ringan</option>
          <option value="DAMAGED">Rusak Berat</option>
          <option value="UNKNOWN">Tidak Diketahui</option>
        </select>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {isLoading ? (
        <div>Memuat...</div>
      ) : (
        <div className="asset-grid">
          {assets.map((asset) => (
            <div key={asset.id} className={`asset-card ${asset.currentStatus}`}>
              <div className="asset-card-header">
                <h3>{asset.assetName}</h3>
                <span className={`asset-status ${asset.currentStatus}`}>{asset.currentStatus}</span>
              </div>
              <div className="asset-meta">
                <span><strong>Kode:</strong> {asset.assetCode}</span>
                <span><strong>Kategori:</strong> {asset.category}</span>
                <span><strong>Kondisi:</strong> {asset.currentCondition}</span>
                <span><strong>Lokasi:</strong> {asset.location}</span>
              </div>
              <p className="asset-description">{asset.description}</p>
              
              <div className="asset-actions">
                <button className="asset-btn-secondary" onClick={() => navigate(`/assets/${asset.id}`)}>Detail</button>
                {asset.currentStatus === "AVAILABLE" && hasPermission("asset.borrow.create") && (
                  <button className="asset-btn-primary" onClick={() => { setBorrowingAsset(asset); setIsBorrowFormOpen(true); }}>Pinjam</button>
                )}
                {isManager && (
                  <>
                    <button className="asset-btn-secondary" onClick={() => { setEditingAsset(asset); setIsFormOpen(true); }}>Edit</button>
                    <button className="asset-btn-secondary asset-btn-danger" onClick={() => handleDelete(asset.id)}>Hapus</button>
                  </>
                )}
              </div>
            </div>
          ))}
          {assets.length === 0 && !isLoading && (
            <div className="text-gray-500">Tidak ada aset ditemukan.</div>
          )}
        </div>
      )}

      {isFormOpen && (
        <AssetForm
          initialData={editingAsset}
          onClose={() => setIsFormOpen(false)}
          onSuccess={() => { setIsFormOpen(false); fetchAllAssets(); }}
        />
      )}

      {isBorrowFormOpen && borrowingAsset && (
        <BorrowingForm
          asset={borrowingAsset}
          onClose={() => setIsBorrowFormOpen(false)}
          onSuccess={() => { setIsBorrowFormOpen(false); fetchAllAssets(); alert("Peminjaman berhasil diajukan!"); }}
        />
      )}
    </div>
  );
};
