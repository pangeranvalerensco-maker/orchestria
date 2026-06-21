import { useState, type FormEvent } from "react";
import { createMember, updateMember } from "../../services/organizationService";
import type { MemberResponse, MemberRequest } from "../../types/organization";
import { getErrorMessage } from "../../utils/apiErrorHandler";

interface MemberFormProps {
  token: string;
  initialData?: MemberResponse | null;
  onSuccess: () => void;
  onCancel: () => void;
}

export function MemberForm({ token, initialData, onSuccess, onCancel }: MemberFormProps) {
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    authUserId: initialData?.authUserId?.toString() || "",
    fullName: initialData?.fullName || "",
    email: initialData?.email || "",
    studentNumber: initialData?.studentNumber || "",
    phoneNumber: initialData?.phoneNumber || "",
    cohort: initialData?.cohort || "",
    profilePhotoUrl: initialData?.profilePhotoUrl || "",
    major: initialData?.major || "",
    campusClass: initialData?.campusClass || "",
    displayOrder: initialData?.displayOrder?.toString() || "0",
    publicVisible: initialData ? initialData.publicVisible : true,
    status: initialData?.status || "ACTIVE",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target as HTMLInputElement;
    const checked = (e.target as HTMLInputElement).checked;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    const payload: MemberRequest = {
      authUserId: formData.authUserId ? Number(formData.authUserId) : null,
      fullName: formData.fullName.trim(),
      email: formData.email.trim(),
      studentNumber: formData.studentNumber.trim() || null,
      phoneNumber: formData.phoneNumber.trim() || null,
      cohort: formData.cohort.trim() || null,
      profilePhotoUrl: formData.profilePhotoUrl.trim() || null,
      major: formData.major.trim() || null,
      campusClass: formData.campusClass.trim() || null,
      publicVisible: formData.publicVisible,
      displayOrder: Number(formData.displayOrder) || 0,
      status: formData.status as MemberRequest["status"],
    };

    try {
      if (initialData) {
        await updateMember(token, initialData.id, payload);
      } else {
        await createMember(token, payload);
      }
      onSuccess();
    } catch (err) {
      setError(getErrorMessage(err, "Gagal menyimpan data anggota."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="alert alert-error">{error}</div>}
      
      <div className="org-admin-form-group">
        <label>Nama Lengkap *</label>
        <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} required />
      </div>

      <div className="org-admin-form-group">
        <label>Email *</label>
        <input type="email" name="email" value={formData.email} onChange={handleChange} required />
      </div>

      <div className="org-admin-form-group">
        <label>NIM</label>
        <input type="text" name="studentNumber" value={formData.studentNumber} onChange={handleChange} />
      </div>

      <div className="org-admin-form-group">
        <label>Nomor Telepon</label>
        <input type="text" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} />
      </div>

      <div className="org-admin-flex-between">
        <div className="org-admin-form-group org-admin-form-column">
          <label>Angkatan</label>
          <input type="text" name="cohort" value={formData.cohort} onChange={handleChange} placeholder="Contoh: 2023" />
        </div>
        <div className="org-admin-form-group org-admin-form-column">
          <label>Jurusan</label>
          <input type="text" name="major" value={formData.major} onChange={handleChange} />
        </div>
        <div className="org-admin-form-group org-admin-form-column">
          <label>Kelas</label>
          <input type="text" name="campusClass" value={formData.campusClass} onChange={handleChange} />
        </div>
      </div>

      <div className="org-admin-form-group">
        <label>URL Foto Profil</label>
        <input type="url" name="profilePhotoUrl" value={formData.profilePhotoUrl} onChange={handleChange} />
      </div>

      <div className="org-admin-flex-between">
        <div className="org-admin-form-group org-admin-form-column">
          <label>Auth User ID</label>
          <input type="number" name="authUserId" value={formData.authUserId} onChange={handleChange} min="1" />
          <small>Kosongkan jika belum terhubung dengan akun login</small>
        </div>
        <div className="org-admin-form-group org-admin-form-column">
          <label>Urutan Tampilan</label>
          <input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} required />
        </div>
        <div className="org-admin-form-group org-admin-form-column">
          <label>Status</label>
          <select name="status" value={formData.status} onChange={handleChange}>
            <option value="ACTIVE">Aktif</option>
            <option value="INACTIVE">Tidak Aktif</option>
            <option value="ALUMNI">Alumni</option>
          </select>
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
