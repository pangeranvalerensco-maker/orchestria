import { useState, type FormEvent } from "react";
import { createMemberAssignment, updateMemberAssignment } from "../../services/organizationService";
import type { 
  MemberAssignment, 
  MemberAssignmentRequest,
  MemberResponse,
  DivisionResponse,
  PositionResponse,
  OrganizationPeriodResponse
} from "../../types/organization";
import { ApiError } from "../../api/http";

interface AssignmentFormProps {
  token: string;
  initialData?: MemberAssignment | null;
  members: MemberResponse[];
  divisions: DivisionResponse[];
  positions: PositionResponse[];
  periods: OrganizationPeriodResponse[];
  onSuccess: () => void;
  onCancel: () => void;
}

export function AssignmentForm({ 
  token, initialData, members, divisions, positions, periods, onSuccess, onCancel 
}: AssignmentFormProps) {
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const currentPeriod = periods.find(p => p.currentPeriod);

  const [formData, setFormData] = useState({
    memberId: initialData?.memberId?.toString() || "",
    periodId: initialData?.periodId?.toString() || currentPeriod?.id?.toString() || "",
    divisionId: initialData?.divisionId?.toString() || "",
    positionId: initialData?.positionId?.toString() || "",
    status: initialData?.status || "ACTIVE",
  });

  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    const payload: MemberAssignmentRequest = {
      memberId: Number(formData.memberId),
      periodId: Number(formData.periodId),
      divisionId: Number(formData.divisionId),
      positionId: Number(formData.positionId),
      status: formData.status as any,
    };

    try {
      if (initialData) {
        await updateMemberAssignment(token, initialData.id, payload);
      } else {
        await createMemberAssignment(token, payload);
      }
      onSuccess();
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Gagal menyimpan data penempatan.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="alert alert-error">{error}</div>}
      
      <div className="org-admin-form-group">
        <label>Anggota *</label>
        <select name="memberId" value={formData.memberId} onChange={handleChange} required disabled={!!initialData}>
          <option value="">-- Pilih Anggota --</option>
          {members.map(m => (
            <option key={m.id} value={m.id}>{m.fullName} {m.email ? `(${m.email})` : ''}</option>
          ))}
        </select>
        {!!initialData && <small>Anggota tidak dapat diubah pada mode edit.</small>}
      </div>

      <div className="org-admin-form-group">
        <label>Periode Kepengurusan *</label>
        <select name="periodId" value={formData.periodId} onChange={handleChange} required>
          <option value="">-- Pilih Periode --</option>
          {periods.map(p => (
            <option key={p.id} value={p.id}>{p.name} {p.currentPeriod ? "(Current)" : ""}</option>
          ))}
        </select>
      </div>

      <div className="org-admin-form-group">
        <label>Divisi *</label>
        <select name="divisionId" value={formData.divisionId} onChange={handleChange} required>
          <option value="">-- Pilih Divisi --</option>
          {divisions.map(d => (
            <option key={d.id} value={d.id}>{d.name} ({d.code})</option>
          ))}
        </select>
      </div>

      <div className="org-admin-flex-between">
        <div className="org-admin-form-group" style={{ flex: 1 }}>
          <label>Jabatan *</label>
          <select name="positionId" value={formData.positionId} onChange={handleChange} required>
            <option value="">-- Pilih Jabatan --</option>
            {positions.map(p => (
              <option key={p.id} value={p.id}>{p.name} ({p.code})</option>
            ))}
          </select>
        </div>
        <div className="org-admin-form-group" style={{ flex: 1 }}>
          <label>Status *</label>
          <select name="status" value={formData.status} onChange={handleChange} required>
            <option value="ACTIVE">Aktif</option>
            <option value="INACTIVE">Tidak Aktif</option>
          </select>
        </div>
      </div>

      <div className="org-admin-modal-footer" style={{ marginTop: "24px", margin: "0 -24px -24px" }}>
        <button type="button" className="secondary-link-button" onClick={onCancel} disabled={submitting}>
          Batal
        </button>
        <button type="submit" className="primary-button" style={{ width: "auto" }} disabled={submitting}>
          {submitting ? "Menyimpan..." : "Simpan"}
        </button>
      </div>
    </form>
  );
}
