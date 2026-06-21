import React, { useState, useEffect } from "react";
import { useAuth } from "../../auth/useAuth";
import { getMembers } from "../../services/organizationService";
import { createAsset, updateAsset } from "../../services/assetService";
import type { Asset, AssetRequest } from "../../types/asset";
import { ApiError } from "../../api/http";

interface AssetFormProps {
  initialData?: Asset | null;
  onClose: () => void;
  onSuccess: () => void;
}

export const AssetForm: React.FC<AssetFormProps> = ({ initialData, onClose, onSuccess }) => {
  const { token } = useAuth();
  const [members, setMembers] = useState<{ id: string; fullName: string }[]>([]);
  
  const [formData, setFormData] = useState<AssetRequest>({
    assetCode: "",
    assetName: "",
    category: "",
    description: "",
    currentCondition: "GOOD",
    location: "",
    responsibleMemberId: undefined,
    imageUrl: ""
  });
  
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [memberError, setMemberError] = useState("");

  useEffect(() => {
    if (initialData) {
      setFormData({
        assetCode: initialData.assetCode,
        assetName: initialData.assetName,
        category: initialData.category,
        description: initialData.description,
        currentCondition: initialData.currentCondition,
        location: initialData.location || "",
        responsibleMemberId: initialData.responsibleMemberId,
        imageUrl: initialData.imageUrl || ""
      });
    }
  }, [initialData]);

  useEffect(() => {
    const fetchMembers = async () => {
      try {
        if (!token) return;
        const res = await getMembers(token);
        if (res.data) {
          setMembers(res.data.map(m => ({ id: m.id.toString(), fullName: m.fullName })));
        }
      } catch (err: unknown) {
        setMemberError("Gagal memuat anggota.");
      }
    };
    fetchMembers();
  }, [token]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleResponsibleMemberChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    setFormData(current => ({
      ...current,
      responsibleMemberId: value ? Number(value) : undefined,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);
    try {
      if (!token) return;
      if (initialData?.id) {
        await updateAsset(token, initialData.id, formData);
      } else {
        await createAsset(token, formData);
      }
      onSuccess();
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Gagal menyimpan data aset.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="asset-modal-overlay">
      <div className="asset-modal-content" style={{ maxWidth: "800px" }}>
        <div className="asset-modal-header">
          <h2>
            {initialData ? "Edit Aset" : "Tambah Aset Baru"}
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
          {memberError && <div className="asset-alert asset-alert-warning">{memberError}</div>}
          
          <form id="asset-form" onSubmit={handleSubmit}>
            <div className="asset-grid-2">
              <div className="asset-form-group">
                <label>Kode Aset *</label>
                <input
                  type="text"
                  name="assetCode"
                  required
                  value={formData.assetCode}
                  onChange={handleChange}
                  className="asset-form-input"
                  placeholder="Mis. PUB-LT-001"
                />
              </div>
              
              <div className="asset-form-group">
                <label>Nama Aset *</label>
                <input
                  type="text"
                  name="assetName"
                  required
                  value={formData.assetName}
                  onChange={handleChange}
                  className="asset-form-input"
                />
              </div>
            </div>

            <div className="asset-grid-2">
              <div className="asset-form-group">
                <label>Kategori *</label>
                <input
                  type="text"
                  name="category"
                  required
                  value={formData.category}
                  onChange={handleChange}
                  className="asset-form-input"
                  placeholder="Mis. Elektronik, Perabotan"
                />
              </div>

              <div className="asset-form-group">
                <label>Lokasi *</label>
                <input
                  type="text"
                  name="location"
                  required
                  value={formData.location}
                  onChange={handleChange}
                  className="asset-form-input"
                  placeholder="Mis. Sekretariat PUB"
                />
              </div>
            </div>

            <div className="asset-grid-2">
              <div className="asset-form-group">
                <label>Kondisi Saat Ini *</label>
                <select
                  name="currentCondition"
                  required
                  value={formData.currentCondition}
                  onChange={handleChange}
                  className="asset-form-input"
                >
                  <option value="GOOD">Baik</option>
                  <option value="MINOR_DAMAGE">Rusak Ringan</option>
                  <option value="DAMAGED">Rusak Berat</option>
                  <option value="UNKNOWN">Tidak Diketahui</option>
                </select>
              </div>

              <div className="asset-form-group">
                <label>Penanggung Jawab (Opsional)</label>
                <select
                  name="responsibleMemberId"
                  value={formData.responsibleMemberId || ""}
                  onChange={handleResponsibleMemberChange}
                  className="asset-form-input"
                >
                  <option value="">-- Pilih Anggota --</option>
                  {members.map(m => (
                    <option key={m.id} value={m.id.toString()}>{m.fullName}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="asset-form-group">
              <label>Deskripsi *</label>
              <textarea
                name="description"
                required
                rows={3}
                value={formData.description}
                onChange={handleChange}
                className="asset-form-input"
              ></textarea>
            </div>

            <div className="asset-form-group">
              <label>URL Gambar (Opsional)</label>
              <input
                type="url"
                name="imageUrl"
                value={formData.imageUrl}
                onChange={handleChange}
                className="asset-form-input"
              />
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
            form="asset-form"
            className="asset-btn asset-btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Menyimpan..." : "Simpan"}
          </button>
        </div>
      </div>
    </div>
  );
};
