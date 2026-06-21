import React, { useEffect, useState, useMemo } from "react";
import { Link } from "react-router";
import { useAuth } from "../auth/useAuth";
import divisionTaskService from "../services/divisionTaskService";
import { getDivisions } from "../services/organizationService";
import type { DivisionTask } from "../types/divisionTask";
import type { DivisionResponse } from "../types/organization";
import { ApiError } from "../api/http";
import { DivisionTaskForm } from "../components/division-tasks/DivisionTaskForm";

export const DivisionTasksPage: React.FC = () => {
  const { token, hasPermission } = useAuth();

  const isManager = hasPermission("division.task.manage");

  const [activeTab, setActiveTab] = useState<"me" | "manage">(isManager ? "manage" : "me");
  const [tasks, setTasks] = useState<DivisionTask[]>([]);
  const [divisions, setDivisions] = useState<DivisionResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [dataWarning, setDataWarning] = useState("");
  
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<DivisionTask | null>(null);

  // Filters
  const [search, setSearch] = useState("");
  const [filterDivision, setFilterDivision] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [filterPriority, setFilterPriority] = useState("");
  const [filterOverdue, setFilterOverdue] = useState(false);

  const fetchTasks = async () => {
    setIsLoading(true);
    setError("");
    try {
      if (!token) return;
      if (activeTab === "me" || !isManager) {
        const res = await divisionTaskService.getMyTasks(token);
        if (res.data) setTasks(res.data);
      } else {
        const res = await divisionTaskService.getAllTasks(token);
        if (res.data) setTasks(res.data);
      }
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
        setError("Gagal mengambil data tugas.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const fetchDivisions = async () => {
    try {
      if (!token) return;
      const res = await getDivisions(token);
      if (res.data) setDivisions(res.data);
    } catch (err: unknown) {
      setDataWarning("Daftar divisi belum dapat dimuat.");
    }
  };

  useEffect(() => {
    fetchTasks();
    if (isManager) fetchDivisions();
  }, [activeTab, isManager]);

  const handleDelete = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation();
    if (!token) return;
    if (window.confirm("Apakah Anda yakin ingin menghapus tugas ini?")) {
      try {
        await divisionTaskService.deleteTask(token, id);
        fetchTasks();
      } catch (err: unknown) {
        if (err instanceof ApiError) {
          alert(err.message);
        } else {
          alert("Gagal menghapus tugas.");
        }
      }
    }
  };

  const filteredTasks = useMemo(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    let filtered = [...tasks];

    if (search) {
      const s = search.toLowerCase();
      filtered = filtered.filter(
        (task) => task.title.toLowerCase().includes(s) || (task.assignedMemberName && task.assignedMemberName.toLowerCase().includes(s))
      );
    }
    if (filterDivision) {
      filtered = filtered.filter((task) => task.divisionId.toString() === filterDivision);
    }
    if (filterStatus) {
      filtered = filtered.filter((task) => task.status === filterStatus);
    }
    if (filterPriority) {
      filtered = filtered.filter((task) => task.priority === filterPriority);
    }
    if (filterOverdue) {
      filtered = filtered.filter((task) => {
        if (!task.dueDate || task.status === "DONE" || task.status === "CANCELLED") return false;
        const dDate = new Date(task.dueDate);
        return dDate < today;
      });
    }

    filtered.sort((a, b) => {
      if (!a.dueDate) return 1;
      if (!b.dueDate) return -1;
      return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
    });

    return filtered;
  }, [tasks, search, filterDivision, filterStatus, filterPriority, filterOverdue]);

  const summary = useMemo(() => {
    const total = tasks.length;
    const todo = tasks.filter(t => t.status === "TODO").length;
    const inProgress = tasks.filter(t => t.status === "IN_PROGRESS").length;
    const submitted = tasks.filter(t => t.status === "SUBMITTED").length;
    const done = tasks.filter(t => t.status === "DONE").length;
    
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const overdue = tasks.filter(t => {
      if (!t.dueDate || t.status === "DONE" || t.status === "CANCELLED") return false;
      return new Date(t.dueDate) < today;
    }).length;

    return { total, todo, inProgress, submitted, done, overdue };
  }, [tasks]);

  const isOverdue = (dueDate: string | null) => {
    if (!dueDate) return false;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return new Date(dueDate) < today;
  };

  return (
    <div className="division-task-page">
      <div className="division-task-header">
        <div>
          <p className="eyebrow">DIVISION ACTIVITY</p>
          <h1>Tugas dan Aktivitas Divisi</h1>
          <p>Pantau tugas dan bukti aktivitas secara terpusat dan transparan.</p>
        </div>
        {isManager && (
          <button className="division-task-btn-primary" onClick={() => { setEditingTask(null); setIsFormOpen(true); }}>
            + Tambah Tugas
          </button>
        )}
      </div>

      <div className="summary-grid">
        <div className="summary-card"><span>Total Tugas</span><strong>{summary.total}</strong></div>
        <div className="summary-card"><span>Belum Dimulai</span><strong>{summary.todo}</strong></div>
        <div className="summary-card"><span>Sedang Berjalan</span><strong>{summary.inProgress}</strong></div>
        <div className="summary-card"><span>Menunggu Review</span><strong>{summary.submitted}</strong></div>
        <div className="summary-card"><span>Selesai</span><strong>{summary.done}</strong></div>
        <div className="summary-card"><span className="overdue-text">Terlambat</span><strong className="overdue-text">{summary.overdue}</strong></div>
      </div>

      {isManager && (
        <div className="division-task-tabs">
          <button
            className={`division-task-tab ${activeTab === "me" ? "active" : ""}`}
            onClick={() => setActiveTab("me")}
          >
            Tugas Saya
          </button>
          <button
            className={`division-task-tab ${activeTab === "manage" ? "active" : ""}`}
            onClick={() => setActiveTab("manage")}
          >
            Kelola Tugas
          </button>
        </div>
      )}

      <div className="division-task-filters">
        <input type="text" placeholder="Cari judul atau anggota..." value={search} onChange={e => setSearch(e.target.value)} />
        {isManager && activeTab === "manage" && (
          <select value={filterDivision} onChange={e => setFilterDivision(e.target.value)}>
            <option value="">Semua Divisi</option>
            {divisions.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
          </select>
        )}
        <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
          <option value="">Semua Status</option>
          <option value="TODO">TODO</option>
          <option value="IN_PROGRESS">IN_PROGRESS</option>
          <option value="SUBMITTED">SUBMITTED</option>
          <option value="DONE">DONE</option>
          <option value="CANCELLED">CANCELLED</option>
        </select>
        <select value={filterPriority} onChange={e => setFilterPriority(e.target.value)}>
          <option value="">Semua Prioritas</option>
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
        </select>
        <label>
          <input type="checkbox" checked={filterOverdue} onChange={e => setFilterOverdue(e.target.checked)} />
          Hanya Terlambat
        </label>
      </div>

      {dataWarning && <div className="alert alert-warning" style={{ background: '#fef3c7', color: '#92400e' }}>{dataWarning}</div>}
      {error && <div className="alert alert-error">{error}</div>}

      {isLoading ? (
        <div className="division-task-loading">Memuat...</div>
      ) : (
        <div className="division-task-grid">
          {filteredTasks.map((task) => (
            <div key={task.id} className="division-task-card">
              <div className="division-task-card-header">
                <h3>{task.title}</h3>
                <span className={`division-task-status division-task-status-${task.status.toLowerCase()}`}>
                  {task.status}
                </span>
              </div>
              <div className="division-task-meta">
                {task.divisionName} &bull; {task.assignedMemberName || "Belum ditugaskan"}
              </div>
              <div className="division-task-card-footer">
                <span className={`division-task-priority division-task-priority-${task.priority.toLowerCase()}`}>
                  {task.priority}
                </span>
                <span className={`division-task-due ${isOverdue(task.dueDate) ? "division-task-due-overdue" : ""}`}>
                  {task.dueDate ? new Date(task.dueDate).toLocaleDateString("id-ID") : "Tanpa tenggat"}
                </span>
              </div>
              
              <div className="division-task-actions-row">
                <Link to={`/division-tasks/${task.id}`} className="secondary-link-button">Detail</Link>
                {isManager && activeTab === "manage" && (
                  <>
                    <button type="button" className="secondary-link-button" onClick={() => { setEditingTask(task); setIsFormOpen(true); }}>Edit</button>
                    <button type="button" className="secondary-link-button danger" onClick={(e) => handleDelete(e, task.id)}>Hapus</button>
                  </>
                )}
              </div>
            </div>
          ))}
          {filteredTasks.length === 0 && <div className="division-task-empty">Tidak ada tugas yang sesuai.</div>}
        </div>
      )}

      {isFormOpen && (
        <DivisionTaskForm
          initialData={editingTask}
          onClose={() => setIsFormOpen(false)}
          onSuccess={() => { setIsFormOpen(false); fetchTasks(); }}
        />
      )}
    </div>
  );
};
