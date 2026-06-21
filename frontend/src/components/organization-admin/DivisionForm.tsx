import { useState, type FormEvent } from "react";
import { createDivision, updateDivision } from "../../services/organizationService";
import type { DivisionResponse, DivisionRequest } from "../../types/organization";
import { getErrorMessage } from "../../utils/apiErrorHandler";

interface DivisionFormProps {
  token: string;
  initialData?: DivisionResponse | null;
  onSuccess: () => void;
  onCancel: () => void;
}

export function DivisionForm({ token, initialData, onSuccess, onCancel }: DivisionFormProps) {
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    code: initialData?.code || "",
    name: initialData?.name || "",
    description: initialData?.description || "",
    displayOrder: initialData?.displayOrder?.toString() || "0",
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

    const payload: DivisionRequest = {
      code: formData.code.trim().toUpperCase(),
      name: formData.name.trim(),
      description: formData.description.trim() || null,
      publicVisible: formData.publicVisible,
      displayOrder: Number(formData.displayOrder) || 0,
    };

    try {
      if (initialData) {
        await updateDivision(token, initialData.id, payload);
      } else {
        await createDivision(token, payload);
      }
      onSuccess();
    } catch (err) {
      setError(getErrorMessage(err, "Gagal menyimpan data divisi."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="alert alert-error">{error}</div>}
      
      <div className="org-admin-form-group">
        <label>Kode Divisi *</label>
        <input type="text" name="code" value={formData.code} onChange={handleChange} required placeholder="Contoh: IT" />
      </div>

      <div className="org-admin-form-group">
        <label>Nama Divisi *</label>
        <input type="text" name="name" value={formData.name} onChange={handleChange} required placeholder="Contoh: Information Technology" />
      </div>

      <div className="org-admin-form-group">
        <label>Deskripsi</label>
        <textarea name="description" value={formData.description} onChange={handleChange} rows={3} />
      </div>

      <div className="org-admin-flex-between">
        <div className="org-admin-form-group org-admin-form-column">
          <label>Urutan Tampilan</label>
          <input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} required />
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
