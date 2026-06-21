import { useState, type FormEvent } from "react";
import { createPeriod, updatePeriod } from "../../services/organizationService";
import type { OrganizationPeriodResponse, OrganizationPeriodRequest } from "../../types/organization";
import { getErrorMessage } from "../../utils/apiErrorHandler";

interface PeriodFormProps {
  token: string;
  initialData?: OrganizationPeriodResponse | null;
  onSuccess: () => void;
  onCancel: () => void;
}

export function PeriodForm({ token, initialData, onSuccess, onCancel }: PeriodFormProps) {
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    name: initialData?.name || "",
    startDate: initialData?.startDate || "",
    endDate: initialData?.endDate || "",
    currentPeriod: initialData ? initialData.currentPeriod : false,
    publicVisible: initialData ? initialData.publicVisible : true,
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    
    if (formData.startDate && formData.endDate) {
      if (new Date(formData.endDate) < new Date(formData.startDate)) {
        setError("Tanggal selesai tidak boleh sebelum tanggal mulai.");
        return;
      }
    }

    setSubmitting(true);
    setError(null);

    const payload: OrganizationPeriodRequest = {
      name: formData.name.trim(),
      startDate: formData.startDate || null,
      endDate: formData.endDate || null,
      currentPeriod: formData.currentPeriod,
      publicVisible: formData.publicVisible,
    };

    try {
      if (initialData) {
        await updatePeriod(token, initialData.id, payload);
      } else {
        await createPeriod(token, payload);
      }
      onSuccess();
    } catch (err) {
      setError(getErrorMessage(err, "Gagal menyimpan data periode."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="alert alert-error">{error}</div>}
      
      <div className="org-admin-form-group">
        <label>Nama Periode *</label>
        <input type="text" name="name" value={formData.name} onChange={handleChange} required placeholder="Contoh: 2024/2025" />
      </div>

      <div className="org-admin-flex-between">
        <div className="org-admin-form-group org-admin-form-column">
          <label>Tanggal Mulai</label>
          <input type="date" name="startDate" value={formData.startDate} onChange={handleChange} />
        </div>
        <div className="org-admin-form-group org-admin-form-column">
          <label>Tanggal Selesai</label>
          <input type="date" name="endDate" value={formData.endDate} onChange={handleChange} />
        </div>
      </div>

      <div className="org-admin-form-group">
        <label className="org-admin-checkbox">
          <input type="checkbox" name="currentPeriod" checked={formData.currentPeriod} onChange={handleChange} />
          Jadikan sebagai Periode Aktif Saat Ini (Current Period)
        </label>
        {formData.currentPeriod && (
          <small className="org-admin-period-warning">
            Peringatan: Jika disimpan, periode lain akan otomatis kehilangan status current-nya sesuai aturan backend (jika dikonfigurasi demikian). Hanya boleh ada 1 periode current.
          </small>
        )}
      </div>

      <div className="org-admin-form-group">
        <label className="org-admin-checkbox">
          <input type="checkbox" name="publicVisible" checked={formData.publicVisible} onChange={handleChange} />
          Tampilkan di halaman publik
        </label>
      </div>

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
