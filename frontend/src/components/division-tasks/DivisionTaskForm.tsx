import React, { useEffect, useState } from "react";
import { useAuth } from "../../auth/useAuth";
import {
  getDivisions,
  getCurrentPeriod,
  getMemberAssignmentsByPeriodAndDivision,
  getCurrentMemberContext,
} from "../../services/organizationService";
import divisionTaskService from "../../services/divisionTaskService";
import { ApiError } from "../../api/http";
import type { DivisionResponse, MemberAssignment } from "../../types/organization";
import type { TaskStatus, TaskPriority, DivisionTaskRequest, DivisionTask } from "../../types/divisionTask";

interface Props {
  initialData?: DivisionTask | null;
  onClose: () => void;
  onSuccess: () => void;
}

export const DivisionTaskForm: React.FC<Props> = ({ initialData, onClose, onSuccess }) => {
  const { token, hasPermission } = useAuth();
  
  const [divisions, setDivisions] = useState<DivisionResponse[]>([]);
  const [members, setMembers] = useState<MemberAssignment[]>([]);
  const [loadingContext, setLoadingContext] = useState(true);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [divisionId, setDivisionId] = useState<number | "">("");
  const [assignedMemberId, setAssignedMemberId] = useState<number | "">("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [dueDate, setDueDate] = useState("");
  const [priority, setPriority] = useState<TaskPriority>("MEDIUM");
  const [status, setStatus] = useState<TaskStatus>("TODO");

  const isGlobalManager = hasPermission("organization.manage") || hasPermission("request.read.all");

  useEffect(() => {
    async function initForm() {
      if (!token) return;
      try {
        setLoadingContext(true);
        const [divsRes, periodRes, contextRes] = await Promise.all([
          getDivisions(token),
          getCurrentPeriod(token),
          getCurrentMemberContext(token),
        ]);

        let allowedDivisions = divsRes.data || [];
        
        if (!isGlobalManager) {
           const myAssignments = contextRes.data?.activeAssignments || [];
           const managedDivIds = myAssignments
             .filter((a: MemberAssignment) => a.status === "ACTIVE" && a.periodId === periodRes.data?.id && a.positionCode === "KETUA_DIVISI")
             .map((a: MemberAssignment) => a.divisionId);
           allowedDivisions = allowedDivisions.filter(d => managedDivIds.includes(d.id));
        }
        
        setDivisions(allowedDivisions);

        if (initialData) {
          setDivisionId(initialData.divisionId);
          setAssignedMemberId(initialData.assignedMemberId || "");
          setTitle(initialData.title);
          setDescription(initialData.description || "");
          setDueDate(initialData.dueDate || "");
          setPriority(initialData.priority);
          setStatus(initialData.status);
        } else if (allowedDivisions.length === 1) {
          setDivisionId(allowedDivisions[0].id);
        }
        
      } catch (err: unknown) {
        if (err instanceof ApiError) {
           setError(err.message);
        } else {
           setError("Terjadi kesalahan memuat form.");
        }
      } finally {
        setLoadingContext(false);
      }
    }
    initForm();
  }, [token, initialData, isGlobalManager]);

  useEffect(() => {
    async function loadMembers() {
      if (!token || !divisionId) {
        setMembers([]);
        return;
      }
      try {
        const periodRes = await getCurrentPeriod(token);
        if (periodRes.data) {
          const mRes = await getMemberAssignmentsByPeriodAndDivision(token, periodRes.data.id, Number(divisionId));
          setMembers(mRes.data?.filter(m => m.status === "ACTIVE") || []);
        }
      } catch (err: unknown) {
        // fail silently or handle
      }
    }
    loadMembers();
  }, [token, divisionId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;
    setError("");

    const trimTitle = title.trim();
    const trimDesc = description.trim();

    if (!trimTitle) {
      setError("Judul wajib diisi.");
      return;
    }
    if (trimTitle.length > 150) {
      setError("Judul maksimum 150 karakter.");
      return;
    }
    if (trimDesc.length > 1000) {
      setError("Deskripsi maksimum 1000 karakter.");
      return;
    }

    if (!divisionId) {
      setError("Divisi wajib dipilih.");
      return;
    }

    const requestData: DivisionTaskRequest = {
      divisionId: Number(divisionId),
      assignedMemberId: assignedMemberId ? Number(assignedMemberId) : null,
      title: trimTitle,
      description: trimDesc || null,
      dueDate: dueDate || null,
      priority,
      status,
    };

    setIsSubmitting(true);
    try {
      if (initialData) {
        await divisionTaskService.updateTask(token, initialData.id, requestData);
      } else {
        await divisionTaskService.createTask(token, requestData);
      }
      onSuccess();
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 401) {
           setError("Sesi tidak valid. Silakan login kembali.");
        } else if (err.status === 403) {
           setError("Anda tidak memiliki akses terhadap tugas ini.");
        } else if (err.status === 404) {
           setError("Tugas atau bukti aktivitas tidak ditemukan.");
        } else if (err.status === 409) {
           setError("Perubahan tidak dapat dilakukan karena status atau data mengalami konflik.");
        } else if (err.status === 500) {
           setError("Layanan aktivitas divisi sedang bermasalah.");
        } else {
           setError(err.message);
        }
      } else {
        setError("Gagal menyimpan tugas.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="division-task-modal-overlay">
      <div className="division-task-modal-content">
        <h2>{initialData ? "Edit Tugas" : "Tambah Tugas"}</h2>
        {error && <div className="alert alert-error">{error}</div>}
        
        {loadingContext ? (
          <div>Memuat form...</div>
        ) : (
          <form onSubmit={handleSubmit} className="division-task-form">
            <div className="form-field">
              <span>Divisi</span>
              <select value={divisionId} onChange={e => setDivisionId(e.target.value ? Number(e.target.value) : "")} required>
                <option value="">-- Pilih Divisi --</option>
                {divisions.map(d => (
                  <option key={d.id} value={d.id}>{d.name}</option>
                ))}
              </select>
            </div>

            <div className="form-field">
              <span>Anggota Ditugaskan</span>
              <select value={assignedMemberId} onChange={e => setAssignedMemberId(e.target.value ? Number(e.target.value) : "")}>
                <option value="">-- Belum ditugaskan --</option>
                {members.map(m => (
                  <option key={m.id} value={m.memberId}>{m.memberName}</option>
                ))}
              </select>
            </div>

            <div className="form-field">
              <span>Judul Tugas</span>
              <input type="text" value={title} onChange={e => setTitle(e.target.value)} required maxLength={150} />
            </div>

            <div className="form-field">
              <span>Deskripsi</span>
              <textarea value={description} onChange={e => setDescription(e.target.value)} maxLength={1000} rows={4} />
            </div>

            <div className="form-field">
              <span>Tenggat Waktu</span>
              <input type="date" value={dueDate} onChange={e => setDueDate(e.target.value)} />
            </div>

            <div className="form-field">
              <span>Prioritas</span>
              <select value={priority} onChange={e => setPriority(e.target.value as TaskPriority)}>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </div>

            <div className="form-field">
              <span>Status</span>
              <select value={status} onChange={e => setStatus(e.target.value as TaskStatus)}>
                <option value="TODO">TODO</option>
                <option value="IN_PROGRESS">IN_PROGRESS</option>
                <option value="SUBMITTED">SUBMITTED</option>
                <option value="DONE">DONE</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>

            <div className="division-task-modal-actions">
              <button type="button" className="secondary-link-button" onClick={onClose} disabled={isSubmitting}>Batal</button>
              <button type="submit" className="primary-button" disabled={isSubmitting}>{isSubmitting ? "Menyimpan..." : "Simpan"}</button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
