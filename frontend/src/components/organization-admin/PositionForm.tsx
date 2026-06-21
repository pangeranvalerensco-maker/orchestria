import { useState, type FormEvent } from "react";
import { createPosition, updatePosition } from "../../services/organizationService";
import type { PositionResponse, PositionRequest } from "../../types/organization";
import { getErrorMessage } from "../../utils/apiErrorHandler";

interface PositionFormProps {
  token: string;
  initialData?: PositionResponse | null;
  onSuccess: () => void;
  onCancel: () => void;
}

export function PositionForm({ token, initialData, onSuccess, onCancel }: PositionFormProps) {
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    code: initialData?.code || "",
    name: initialData?.name || "",
    description: initialData?.description || "",
    levelOrder: initialData?.levelOrder?.toString() || "0",
    publicVisible: initialData ? initialData.publicVisible : true,
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target as HTMLInputElement;
    const checked = (e.target as HTMLInputElement).checked;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : name === "code" ? value.toUpperCase() : value,
    }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    const payload: PositionRequest = {
      code: formData.code.trim().toUpperCase(),
      name: formData.name.trim(),
      description: formData.description.trim() || null,
      publicVisible: formData.publicVisible,
      levelOrder: Number(formData.levelOrder) || 0,
    };

    try {
      if (initialData) {
        await updatePosition(token, initialData.id, payload);
      } else {
        await createPosition(token, payload);
      }
      onSuccess();
    } catch (err) {
      setError(getErrorMessage(err, "Gagal menyimpan data jabatan."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="alert alert-error">{error}</div>}
      
      <div className="org-admin-form-group">
        <label>Kode Jabatan *</label>
        <input type="text" name="code" value={formData.code} onChange={handleChange} required placeholder="Contoh: CHR" />
      </div>

      <div className="org-admin-form-group">
        <label>Nama Jabatan *</label>
        <input type="text" name="name" value={formData.name} onChange={handleChange} required placeholder="Contoh: Ketua" />
      </div>

      <div className="org-admin-form-group">
        <label>Deskripsi</label>
        <textarea name="description" value={formData.description} onChange={handleChange} rows={3} />
      </div>

      <div className="org-admin-flex-between">
        <div className="org-admin-form-group org-admin-form-column">
          <label>Level Jabatan</label>
          <input type="number" name="levelOrder" value={formData.levelOrder} onChange={handleChange} required />
          <small>Level yang lebih kecil berarti jabatan lebih tinggi (misal: 1 = Ketua)</small>
        </div>
      </div>

      <label className="org-admin-checkbox">
        <input type="checkbox" name="publicVisible" checked={formData.publicVisible} onChange={handleChange} />
        Tampilkan di halaman publik
      </label>

      <div className="org-admin-modal-footer org-admin-modal-footer-spacing">
        <button type="button" className="secondary-link-button" onClick={onCancel} disabled={submitting}>
          Batal
        </button>
        <button type="submit" className="primary-button org-admin-primary-button-auto" disabled={submitting}>
          {submitting ? "Menyimpan..." : "Simpan"}
        </button>
      </div>
    </form>
  );
}
