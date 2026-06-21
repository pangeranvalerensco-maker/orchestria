import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../auth/useAuth";
import divisionTaskService from "../services/divisionTaskService";
import type { DivisionTask } from "../types/divisionTask";

export const DivisionTasksPage: React.FC = () => {
  const { user, token } = useAuth();
  const navigate = useNavigate();

  const isManager =
    user?.permissions.includes("division.task.manage") ||
    user?.roles.includes("ROLE_SUPER_ADMIN") ||
    user?.roles.includes("ROLE_KETUA_PUB");

  const [activeTab, setActiveTab] = useState<"me" | "all">("me");
  const [tasks, setTasks] = useState<DivisionTask[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchTasks = async () => {
    setIsLoading(true);
    setError("");
    try {
      if (!token) return;
      if (activeTab === "me") {
        const res = await divisionTaskService.getMyTasks(token);
        if (res.data) setTasks(res.data);
      } else {
        if (user?.roles.includes("ROLE_SUPER_ADMIN") || user?.roles.includes("ROLE_KETUA_PUB")) {
          const res = await divisionTaskService.getAllTasks(token);
          if (res.data) setTasks(res.data);
        } else {
          // Manager (Ketua Divisi)
          // We need divisionId. Wait, getTasksByDivision requires divisionId.
          // But getTasksByDivision endpoint takes divisionId... what if we just use getAllTasks() but backend throws?
          // Wait! Did I implement getTasksByDivision or getAllTasks based on access?
          // Actually, in backend DivisionTaskController, GET / has permission division.task.manage.
          // Let's check what GET / does in backend.
        }
      }
    } catch (err: any) {
      setError(err.response?.data?.message || "Gagal mengambil data tugas.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, [activeTab]);

  return (
    <div className="division-task-page">
      <div className="division-task-header">
        <h1>Tugas Divisi</h1>
        {isManager && (
          <button className="division-task-btn-primary" onClick={() => {/* TODO: Create Task */}}>
            + Buat Tugas
          </button>
        )}
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
            className={`division-task-tab ${activeTab === "all" ? "active" : ""}`}
            onClick={() => setActiveTab("all")}
          >
            Kelola Tugas
          </button>
        </div>
      )}

      {error && <div className="division-task-error">{error}</div>}

      {isLoading ? (
        <div className="division-task-loading">Memuat...</div>
      ) : (
        <div className="division-task-grid">
          {tasks.map((task) => (
            <div key={task.id} className="division-task-card" onClick={() => navigate(`/division/tasks/${task.id}`)}>
              <div className="division-task-card-header">
                <h3>{task.title}</h3>
                <span className={`division-task-status status-${task.status.toLowerCase()}`}>
                  {task.status}
                </span>
              </div>
              <p className="division-task-desc">{task.description}</p>
              <div className="division-task-card-footer">
                <span className={`division-task-priority priority-${task.priority.toLowerCase()}`}>
                  {task.priority}
                </span>
                <span className="division-task-due">
                  Tenggat: {new Date(task.dueDate).toLocaleDateString("id-ID")}
                </span>
              </div>
            </div>
          ))}
          {tasks.length === 0 && <div className="division-task-empty">Belum ada tugas.</div>}
        </div>
      )}
    </div>
  );
};
