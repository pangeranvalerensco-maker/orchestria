import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { useAuth } from "../auth/useAuth";
import divisionTaskService from "../services/divisionTaskService";
import type { DivisionTask, DivisionTaskEvidence } from "../types/divisionTask";
import { TaskStatus, EvidenceType } from "../types/divisionTask";

export const DivisionTaskDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, token } = useAuth();
  
  const [task, setTask] = useState<DivisionTask | null>(null);
  const [evidences, setEvidences] = useState<DivisionTaskEvidence[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const [evidenceType, setEvidenceType] = useState<EvidenceType>("NOTE");
  const [evidenceContent, setEvidenceContent] = useState("");
  const [isSubmittingEvidence, setIsSubmittingEvidence] = useState(false);

  const isManager =
    user?.permissions.includes("division.task.manage") ||
    user?.roles.includes("ROLE_SUPER_ADMIN") ||
    user?.roles.includes("ROLE_KETUA_PUB");

  // Determine if user is assigned member by email
  const isAssigned = task?.assignedMemberEmail?.toLowerCase() === user?.email.toLowerCase();

  const fetchTaskDetails = async () => {
    if (!token) return;
    setIsLoading(true);
    setError("");
    try {
      const res = await divisionTaskService.getTaskById(token, Number(id));
      if (res.data) {
        setTask(res.data);
      }
      
      const evRes = await divisionTaskService.getEvidencesByTask(token, Number(id));
      if (evRes.data) {
        setEvidences(evRes.data);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || "Gagal memuat detail tugas.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (id && token) fetchTaskDetails();
  }, [id, token]);

  const handleUpdateMyStatus = async (status: TaskStatus) => {
    if (!token) return;
    try {
      await divisionTaskService.updateMyTaskStatus(token, Number(id), status);
      fetchTaskDetails();
    } catch (err: any) {
      alert(err.response?.data?.message || "Gagal mengubah status.");
    }
  };

  const handleUpdateTaskStatus = async (status: TaskStatus) => {
    if (!token) return;
    try {
      await divisionTaskService.updateTaskStatus(token, Number(id), status);
      fetchTaskDetails();
    } catch (err: any) {
      alert(err.response?.data?.message || "Gagal mengubah status.");
    }
  };

  const handleAddEvidence = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!evidenceContent.trim() || !token) return;
    setIsSubmittingEvidence(true);
    try {
      await divisionTaskService.createMyEvidence(token, {
        taskId: Number(id),
        type: evidenceType,
        title: `Bukti - ${new Date().toLocaleString('id-ID')}`,
        description: evidenceType === "NOTE" ? evidenceContent : undefined,
        externalLink: evidenceType === "LINK" ? evidenceContent : undefined,
      });
      setEvidenceContent("");
      fetchTaskDetails();
    } catch (err: any) {
      alert(err.response?.data?.message || "Gagal menambah bukti.");
    } finally {
      setIsSubmittingEvidence(false);
    }
  };

  if (isLoading) return <div className="division-task-loading">Memuat detail...</div>;
  if (error) return <div className="division-task-error">{error}</div>;
  if (!task) return null;

  return (
    <div className="division-task-detail-page">
      <button className="division-task-back" onClick={() => navigate("/division/tasks")}>
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
          {/* Member actions */}
          {isAssigned && task.status === "TODO" && (
            <button className="btn-start" onClick={() => handleUpdateMyStatus("IN_PROGRESS")}>
              Mulai Kerjakan
            </button>
          )}
          {isAssigned && task.status === "IN_PROGRESS" && (
            <button className="btn-submit" onClick={() => handleUpdateMyStatus("SUBMITTED")}>
              Kirim untuk Review
            </button>
          )}
          
          {/* Manager actions */}
          {isManager && task.status === "SUBMITTED" && (
            <>
              <button className="btn-done" onClick={() => handleUpdateTaskStatus("DONE")}>
                Terima (Selesai)
              </button>
              <button className="btn-reject" onClick={() => handleUpdateTaskStatus("IN_PROGRESS")}>
                Tolak (Revisi)
              </button>
            </>
          )}
          {isManager && task.status !== "DONE" && task.status !== "CANCELLED" && (
             <button className="btn-reject" onClick={() => handleUpdateTaskStatus("CANCELLED")}>
               Batalkan Tugas
             </button>
          )}
        </div>
      </div>

      <div className="division-task-content-layout">
        <div className="division-task-main">
          <div className="division-task-card">
            <h3>Deskripsi</h3>
            <p>{task.description}</p>
          </div>

          <div className="division-task-card evidence-section">
            <h3>Bukti Pengerjaan ({evidences.length})</h3>
            {evidences.map(ev => (
              <div key={ev.id} className="evidence-item">
                <div className="evidence-meta">
                  <strong>{ev.type}</strong> - {new Date(ev.createdAt).toLocaleString("id-ID")}
                </div>
                <div className="evidence-content">
                  {ev.type === "LINK" ? (
                    <a href={ev.externalLink || "#"} target="_blank" rel="noreferrer">{ev.externalLink}</a>
                  ) : (
                    <p>{ev.description}</p>
                  )}
                </div>
              </div>
            ))}
            {evidences.length === 0 && <p className="division-task-empty">Belum ada bukti.</p>}

            {/* Form to add evidence */}
            {isAssigned && task.status === "IN_PROGRESS" && (
              <form className="evidence-form" onSubmit={handleAddEvidence}>
                <h4>Tambah Bukti</h4>
                <div className="form-group">
                  <select
                    className="evidence-select"
                    value={evidenceType}
                    onChange={(e) => setEvidenceType(e.target.value as EvidenceType)}
                  >
                    <option value="NOTE">Catatan</option>
                    <option value="LINK">Tautan (Link)</option>
                    <option value="DOCUMENT">Dokumen</option>
                    <option value="PHOTO">Foto</option>
                  </select>
                </div>
                <div className="form-group">
                  <textarea
                    placeholder="Isi bukti (Teks atau Link)..."
                    value={evidenceContent}
                    onChange={(e) => setEvidenceContent(e.target.value)}
                    required
                    className="evidence-textarea"
                  />
                </div>
                <button type="submit" disabled={isSubmittingEvidence} className="btn-add-evidence">
                  {isSubmittingEvidence ? "Menyimpan..." : "Simpan Bukti"}
                </button>
              </form>
            )}
          </div>
        </div>

        <div className="division-task-sidebar">
          <div className="division-task-card">
            <h3>Informasi Tugas</h3>
            <ul className="task-info-list">
              <li><strong>Divisi:</strong> {task.divisionName}</li>
              <li><strong>Ditugaskan Ke:</strong> {task.assignedMemberName} ({task.assignedMemberEmail})</li>
              <li><strong>Prioritas:</strong> <span className={`priority-${task.priority.toLowerCase()}`}>{task.priority}</span></li>
              <li><strong>Tenggat:</strong> {new Date(task.dueDate).toLocaleDateString("id-ID")}</li>
              <li><strong>Dibuat:</strong> {new Date(task.createdAt).toLocaleDateString("id-ID")}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};
