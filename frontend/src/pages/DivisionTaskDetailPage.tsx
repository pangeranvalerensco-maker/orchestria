import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { useAuth } from "../auth/useAuth";
import divisionTaskService from "../services/divisionTaskService";
import type { DivisionTask, DivisionTaskEvidence } from "../types/divisionTask";
import { TaskStatus, EvidenceType } from "../types/divisionTask";
import { ApiError } from "../api/http";
import { DivisionTaskForm } from "../components/division-tasks/DivisionTaskForm";

export const DivisionTaskDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, token, hasPermission } = useAuth();
  
  const [task, setTask] = useState<DivisionTask | null>(null);
  const [evidences, setEvidences] = useState<DivisionTaskEvidence[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const [isEditingTask, setIsEditingTask] = useState(false);

  // Evidence Form State
  const [showEvidenceForm, setShowEvidenceForm] = useState(false);
  const [editingEvidence, setEditingEvidence] = useState<DivisionTaskEvidence | null>(null);
  const [evidenceType, setEvidenceType] = useState<EvidenceType>("NOTE");
  const [evidenceTitle, setEvidenceTitle] = useState("");
  const [evidenceDesc, setEvidenceDesc] = useState("");
  const [evidenceFileUrl, setEvidenceFileUrl] = useState("");
  const [evidenceExternalLink, setEvidenceExternalLink] = useState("");
  const [isSubmittingEvidence, setIsSubmittingEvidence] = useState(false);

  const isManager = hasPermission("division.task.manage");

  // The assignment member ID is in task.assignedMemberId.
  // Wait, user object has what properties? Let's assume we match user.email with task.assignedMemberEmail, 
  // or maybe better if the backend tells us. The backend updateMyTaskStatus will fail if we are not the assigned member.
  const isAssigned = task?.assignedMemberEmail?.toLowerCase() === user?.email.toLowerCase();

  const fetchTaskDetails = async () => {
    if (!token || !id) return;
    setIsLoading(true);
    setError("");
    try {
      const res = await divisionTaskService.getTaskById(token, Number(id));
      if (res.data) setTask(res.data);
      
      const evRes = await divisionTaskService.getEvidencesByTask(token, Number(id));
      if (evRes.data) setEvidences(evRes.data);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 401) {
           setError("Sesi tidak valid. Silakan login kembali.");
        } else if (err.status === 403) {
           setError("Anda tidak memiliki akses terhadap tugas ini.");
        } else if (err.status === 404) {
           setError("Tugas atau bukti aktivitas tidak ditemukan.");
        } else if (err.status === 500) {
           setError("Layanan aktivitas divisi sedang bermasalah.");
        } else {
           setError(err.message);
        }
      } else {
        setError("Gagal memuat detail tugas.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTaskDetails();
  }, [id, token]);

  const handleUpdateMyStatus = async (status: TaskStatus) => {
    if (!token || !id) return;
    try {
      await divisionTaskService.updateMyTaskStatus(token, Number(id), status);
      fetchTaskDetails();
    } catch (err: unknown) {
      if (err instanceof ApiError) alert(err.message);
    }
  };

  const handleUpdateTaskStatus = async (status: TaskStatus) => {
    if (!token || !id) return;
    try {
      await divisionTaskService.updateTaskStatus(token, Number(id), status);
      fetchTaskDetails();
    } catch (err: unknown) {
      if (err instanceof ApiError) alert(err.message);
    }
  };

  const openAddEvidenceForm = () => {
    setEditingEvidence(null);
    setEvidenceType("NOTE");
    setEvidenceTitle("");
    setEvidenceDesc("");
    setEvidenceFileUrl("");
    setEvidenceExternalLink("");
    setShowEvidenceForm(true);
  };

  const openEditEvidenceForm = (ev: DivisionTaskEvidence) => {
    setEditingEvidence(ev);
    setEvidenceType(ev.type);
    setEvidenceTitle(ev.title);
    setEvidenceDesc(ev.description || "");
    setEvidenceFileUrl(ev.fileUrl || "");
    setEvidenceExternalLink(ev.externalLink || "");
    setShowEvidenceForm(true);
  };

  const handleDeleteEvidence = async (ev: DivisionTaskEvidence) => {
    if (!token) return;
    if (window.confirm("Hapus bukti ini?")) {
      try {
        if (isManager && !isAssigned) {
          // If manager and not assigned (manager flow)
          // Wait, manager can edit their own and other's if managed.
          // Let's just try deleteEvidence or deleteMyEvidence.
          // Actually, manager uses deleteEvidence. Member uses deleteMyEvidence.
          if (ev.submittedByMemberId === task?.assignedMemberId && !isManager) {
             await divisionTaskService.deleteMyEvidence(token, ev.id);
          } else {
             await divisionTaskService.deleteEvidence(token, ev.id);
          }
        } else {
          // If user is just member, or manager trying to delete own.
          // Let's just use deleteMyEvidence for member, deleteEvidence for manager.
          if (isManager) {
            await divisionTaskService.deleteEvidence(token, ev.id);
          } else {
            await divisionTaskService.deleteMyEvidence(token, ev.id);
          }
        }
        fetchTaskDetails();
      } catch (err: unknown) {
        if (err instanceof ApiError) alert(err.message);
      }
    }
  };

  const handleSaveEvidence = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !id) return;
    setIsSubmittingEvidence(true);
    try {
      const data = {
        taskId: Number(id),
        type: evidenceType,
        title: evidenceTitle.trim(),
        description: evidenceDesc.trim() || null,
        fileUrl: evidenceFileUrl.trim() || null,
        externalLink: evidenceExternalLink.trim() || null,
      };

      if (editingEvidence) {
        if (isManager) {
          await divisionTaskService.updateEvidence(token, editingEvidence.id, data);
        } else {
          await divisionTaskService.updateMyEvidence(token, editingEvidence.id, data);
        }
      } else {
        if (isManager && !isAssigned) {
          await divisionTaskService.createEvidence(token, data);
        } else if (isAssigned) {
          await divisionTaskService.createMyEvidence(token, data);
        } else if (isManager) {
          await divisionTaskService.createEvidence(token, data);
        }
      }
      setShowEvidenceForm(false);
      fetchTaskDetails();
    } catch (err: unknown) {
      if (err instanceof ApiError) alert(err.message);
    } finally {
      setIsSubmittingEvidence(false);
    }
  };

  const canMutateTask = task && task.status !== "DONE" && task.status !== "CANCELLED";
  const canMutateEvidenceMember = isAssigned && canMutateTask && (task.status === "TODO" || task.status === "IN_PROGRESS" || task.status === "SUBMITTED");

  if (isLoading && !task) return <div className="division-task-loading">Memuat detail...</div>;
  if (error && !task) return <div className="division-task-error">{error}</div>;
  if (!task) return null;

  return (
    <div className="division-task-detail-page">
      <button type="button" className="division-task-back" onClick={() => navigate("/division-tasks")}>
        &larr; Kembali
      </button>

      <div className="division-task-header-detail">
        <div>
          <h1>{task.title}</h1>
          <span className={`division-task-status status-${task.status.toLowerCase()}`}>
            {task.status}
          </span>
        </div>
        <div className="division-task-actions">
          {isAssigned && task.status === "TODO" && (
            <button type="button" className="btn-start" onClick={() => handleUpdateMyStatus("IN_PROGRESS")}>
              Mulai Kerjakan
            </button>
          )}
          {isAssigned && task.status === "IN_PROGRESS" && (
            <button type="button" className="btn-submit" onClick={() => handleUpdateMyStatus("SUBMITTED")}>
              Kirim untuk Review
            </button>
          )}
          
          {isManager && (
            <>
              {task.status === "SUBMITTED" && (
                <>
                  <button type="button" className="btn-done" onClick={() => handleUpdateTaskStatus("DONE")}>
                    Tandai Selesai
                  </button>
                  <button type="button" className="btn-reject" onClick={() => handleUpdateTaskStatus("IN_PROGRESS")}>
                    Kembalikan ke IN_PROGRESS
                  </button>
                </>
              )}
              {canMutateTask && (
                 <>
                   <button type="button" className="secondary-link-button" onClick={() => setIsEditingTask(true)}>Edit Tugas</button>
                   <button type="button" className="btn-reject" onClick={() => handleUpdateTaskStatus("CANCELLED")}>Batalkan Tugas</button>
                 </>
              )}
            </>
          )}
        </div>
      </div>

      <div className="division-task-content-layout">
        <div className="division-task-main">
          <div className="division-task-card">
            <h3>Deskripsi</h3>
            <p className="division-task-desc-detail">{task.description || "Tidak ada deskripsi"}</p>
          </div>

          <div className="division-task-card evidence-section">
            <div className="division-task-evidence-header">
              <h3>Bukti Pengerjaan ({evidences.length})</h3>
              {(canMutateEvidenceMember || (isManager && canMutateTask)) && (
                <button type="button" className="secondary-link-button" onClick={openAddEvidenceForm}>+ Tambah Bukti</button>
              )}
            </div>
            
            {evidences.map(ev => {
              const canEditEv = isManager ? true : (ev.submittedByMemberId === task.assignedMemberId && canMutateEvidenceMember);
              return (
                <div key={ev.id} className="evidence-item division-task-evidence-item">
                  <div>
                    <div className="evidence-meta division-task-evidence-meta">
                      <strong className="division-task-evidence-badge">{ev.type}</strong> - {new Date(ev.createdAt).toLocaleString("id-ID")}
                    </div>
                    <div className="evidence-content">
                      <h4 className="division-task-evidence-title">{ev.title}</h4>
                      {ev.description && <p className="division-task-evidence-desc">{ev.description}</p>}
                      {ev.externalLink && (
                        <div className="division-task-evidence-link">
                          <a href={ev.externalLink} target="_blank" rel="noreferrer">Tautan Eksternal</a>
                        </div>
                      )}
                      {ev.fileUrl && (
                        <div>
                          <a href={ev.fileUrl} target="_blank" rel="noreferrer">Lihat File/Foto</a>
                        </div>
                      )}
                    </div>
                  </div>
                  {canEditEv && (
                    <div className="division-task-evidence-actions">
                      <button type="button" className="secondary-link-button" onClick={() => openEditEvidenceForm(ev)}>Edit</button>
                      <button type="button" className="secondary-link-button danger" onClick={() => handleDeleteEvidence(ev)}>Hapus</button>
                    </div>
                  )}
                </div>
              );
            })}
            {evidences.length === 0 && <p className="division-task-empty">Belum ada bukti.</p>}
          </div>
        </div>

        <div className="division-task-sidebar">
          <div className="division-task-card">
            <h3>Informasi Tugas</h3>
            <ul className="task-info-list">
              <li><strong>Divisi:</strong> {task.divisionName}</li>
              <li><strong>Ditugaskan Ke:</strong> {task.assignedMemberName || "Belum ditugaskan"}</li>
              <li><strong>Prioritas:</strong> <span className={`priority-${task.priority.toLowerCase()}`}>{task.priority}</span></li>
              <li><strong>Tenggat:</strong> {task.dueDate ? new Date(task.dueDate).toLocaleDateString("id-ID") : "Tanpa tenggat"}</li>
              <li><strong>Dibuat:</strong> {new Date(task.createdAt).toLocaleString("id-ID")}</li>
              <li><strong>Terakhir Diperbarui:</strong> {new Date(task.updatedAt).toLocaleString("id-ID")}</li>
            </ul>
          </div>
        </div>
      </div>

      {isEditingTask && (
        <DivisionTaskForm
          initialData={task}
          onClose={() => setIsEditingTask(false)}
          onSuccess={() => { setIsEditingTask(false); fetchTaskDetails(); }}
        />
      )}

      {showEvidenceForm && (
        <div className="division-task-modal-overlay">
          <div className="division-task-modal-content">
            <h2>{editingEvidence ? "Edit Bukti" : "Tambah Bukti"}</h2>
            <form onSubmit={handleSaveEvidence} className="division-task-form">
              <div className="division-task-evidence-form-group">
                <label>Tipe Bukti</label>
                <select value={evidenceType} onChange={(e) => setEvidenceType(e.target.value as EvidenceType)}>
                  <option value="NOTE">Catatan</option>
                  <option value="LINK">Tautan (Link)</option>
                  <option value="DOCUMENT">Dokumen</option>
                  <option value="PHOTO">Foto</option>
                </select>
              </div>
              <div className="division-task-evidence-form-group">
                <label>Judul</label>
                <input type="text" value={evidenceTitle} onChange={(e) => setEvidenceTitle(e.target.value)} required maxLength={150} />
              </div>
              
              <div className="division-task-evidence-form-group">
                <label>Deskripsi</label>
                <textarea value={evidenceDesc} onChange={(e) => setEvidenceDesc(e.target.value)} rows={3} />
              </div>

              {evidenceType === "LINK" && (
                <div className="division-task-evidence-form-group">
                  <label>External Link</label>
                  <input type="url" value={evidenceExternalLink} onChange={(e) => setEvidenceExternalLink(e.target.value)} required />
                </div>
              )}

              {evidenceType === "PHOTO" && (
                <div className="division-task-evidence-form-group">
                  <label>URL Foto / Referensi File</label>
                  <input type="url" value={evidenceFileUrl} onChange={(e) => setEvidenceFileUrl(e.target.value)} required />
                </div>
              )}

              {evidenceType === "DOCUMENT" && (
                <div className="division-task-evidence-form-group">
                  <label>URL Dokumen / Referensi File</label>
                  <input type="url" value={evidenceFileUrl} onChange={(e) => setEvidenceFileUrl(e.target.value)} required />
                </div>
              )}

              <div className="division-task-modal-actions">
                <button type="button" className="secondary-link-button" onClick={() => setShowEvidenceForm(false)} disabled={isSubmittingEvidence}>Batal</button>
                <button type="submit" className="primary-button" disabled={isSubmittingEvidence}>{isSubmittingEvidence ? "Menyimpan..." : "Simpan"}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
