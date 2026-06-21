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
      } catch (err: unknown) {
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

  if (loading) return <div className="asset-loading">Memuat detail aset...</div>;
  if (error) return <div className="asset-alert asset-alert-error">{error}</div>;
  if (!asset) return <div className="asset-empty">Aset tidak ditemukan.</div>;

  return (
    <div className="asset-page">
      <div className="asset-page-header">
        <div>
          <button className="asset-back-button" onClick={() => navigate(-1)}>
            &larr; Kembali
          </button>
          <h2>Detail Aset: {asset.assetName}</h2>
        </div>
      </div>
      
      <div className="asset-detail-card">
        <div className="asset-detail-grid">
          <div>
            <strong>Kode Aset</strong>
            <div>{asset.assetCode}</div>
          </div>
          <div>
            <strong>Kategori</strong>
            <div>{asset.category}</div>
          </div>
          <div>
            <strong>Status</strong>
            <div>
              <span className={`asset-status-badge ${asset.currentStatus}`}>
                {asset.currentStatus}
              </span>
            </div>
          </div>
          <div>
            <strong>Kondisi</strong>
            <div>{asset.currentCondition}</div>
          </div>
          <div>
            <strong>Lokasi</strong>
            <div>{asset.location}</div>
          </div>
          <div>
            <strong>Deskripsi</strong>
            <div>{asset.description || "-"}</div>
          </div>
          {asset.imageUrl && (
            <div>
              <strong>Gambar</strong>
              <div>
                <img src={asset.imageUrl} alt={asset.assetName} className="asset-image" />
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
