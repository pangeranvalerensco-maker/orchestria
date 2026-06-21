import { useEffect, useState } from "react";
import { useAuth } from "../auth/useAuth";
import {
  getSchedules,
  getMySchedules,
  createSchedule,
  updateSchedule,
  recordAttendance,
  createPointRecord,
} from "../services/cleanlinessService";
import { getMembers } from "../services/organizationService";
import type {
  CleanlinessSchedule,
  ScheduleRequest,
  AttendanceRequest,
  PointRequest,
  ScheduleStatus,
} from "../types/cleanliness";
import type { MemberResponse } from "../types/organization";

export function PicketSchedulesPage() {
  const { token, hasPermission, user } = useAuth();
  const [schedules, setSchedules] = useState<CleanlinessSchedule[]>([]);
  const [members, setMembers] = useState<MemberResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Permissions
  const canManageSchedule = hasPermission("cleanliness.schedule.manage");
  const canCreateAttendance = hasPermission("cleanliness.attendance.create");
  const canManagePoints = hasPermission("cleanliness.point.manage") || hasPermission("cleanliness.violation.manage");

  const [selectedSchedule, setSelectedSchedule] = useState<CleanlinessSchedule | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);

  // Form State
  const [formData, setFormData] = useState<Partial<ScheduleRequest>>({
    title: "",
    dutyDate: "",
    startTime: "",
    endTime: "",
    location: "",
    description: "",
    status: "DRAFT",
    memberIds: [],
  });
  
  // Attendance & Points State
  const [attendanceFormOpen, setAttendanceFormOpen] = useState<{assignmentId: string, scheduleId: string} | null>(null);
  const [attendanceNote, setAttendanceNote] = useState("");
  const [attendanceStatus, setAttendanceStatus] = useState<"PRESENT" | "ABSENT" | "EXCUSED">("PRESENT");

  const [pointFormOpen, setPointFormOpen] = useState<{memberId: number, scheduleId: string} | null>(null);
  const [pointType, setPointType] = useState<"REWARD" | "VIOLATION">("REWARD");
  const [pointValue, setPointValue] = useState(5);
  const [pointReason, setPointReason] = useState("");

  useEffect(() => {
    loadData();
    if (canManageSchedule) {
      loadMembers();
    }
  }, [token, canManageSchedule]);

  async function loadData() {
    if (!token) return;
    try {
      setIsLoading(true);
      if (canManageSchedule) {
        const data = await getSchedules(token);
        setSchedules(data);
      } else {
        const data = await getMySchedules(token);
        setSchedules(data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  }

  async function loadMembers() {
    if (!token) return;
    try {
      const res = await getMembers(token);
      setMembers(res.data);
    } catch (err) {
      console.error(err);
    }
  }

  const handleOpenForm = (schedule?: CleanlinessSchedule) => {
    if (schedule) {
      setFormData({
        title: schedule.title,
        dutyDate: schedule.dutyDate,
        startTime: schedule.startTime,
        endTime: schedule.endTime,
        location: schedule.location,
        description: schedule.description || "",
        status: schedule.status,
        memberIds: schedule.assignments.map(a => a.memberId),
      });
      setSelectedSchedule(schedule);
    } else {
      setFormData({
        title: "",
        dutyDate: new Date().toISOString().split("T")[0],
        startTime: "08:00:00",
        endTime: "10:00:00",
        location: "",
        description: "",
        status: "DRAFT",
        memberIds: [],
      });
      setSelectedSchedule(null);
    }
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setIsFormOpen(false);
    setSelectedSchedule(null);
  };

  const handleSubmitSchedule = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;
    try {
      const request: ScheduleRequest = {
        title: formData.title || "",
        dutyDate: formData.dutyDate || "",
        startTime: formData.startTime || "",
        endTime: formData.endTime || "",
        location: formData.location || "",
        description: formData.description,
        status: formData.status as ScheduleStatus,
        memberIds: formData.memberIds || [],
      };

      if (selectedSchedule) {
        await updateSchedule(selectedSchedule.id, request, token);
      } else {
        await createSchedule(request, token);
      }
      handleCloseForm();
      loadData();
    } catch (err) {
      console.error("Gagal menyimpan jadwal", err);
    }
  };

  const handleSubmitAttendance = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !attendanceFormOpen) return;
    try {
      const req: AttendanceRequest = {
        status: attendanceStatus,
        notes: attendanceNote,
      };
      await recordAttendance(attendanceFormOpen.assignmentId, req, token);
      setAttendanceFormOpen(null);
      loadData();
    } catch (err) {
      console.error("Gagal presensi", err);
    }
  };

  const handleSubmitPoint = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !pointFormOpen) return;
    try {
      const req: PointRequest = {
        memberId: pointFormOpen.memberId,
        scheduleId: pointFormOpen.scheduleId,
        type: pointType,
        pointValue: pointValue,
        reason: pointReason,
      };
      await createPointRecord(req, token);
      setPointFormOpen(null);
      loadData();
      alert("Poin berhasil ditambahkan!");
    } catch (err) {
      console.error("Gagal tambah poin", err);
    }
  };

  const getStatusBadge = (status: string) => {
    switch(status) {
      case "DRAFT": return <span className="picket-badge picket-badge-gray">Draft</span>;
      case "PUBLISHED": return <span className="picket-badge picket-badge-blue">Published</span>;
      case "COMPLETED": return <span className="picket-badge picket-badge-green">Completed</span>;
      case "CANCELLED": return <span className="picket-badge picket-badge-red">Cancelled</span>;
      case "PRESENT": return <span className="picket-badge picket-badge-green">Hadir</span>;
      case "ABSENT": return <span className="picket-badge picket-badge-red">Alpa</span>;
      case "EXCUSED": return <span className="picket-badge picket-badge-yellow">Izin</span>;
      case "PENDING": return <span className="picket-badge picket-badge-gray">Pending</span>;
      default: return <span className="picket-badge">{status}</span>;
    }
  };

  return (
    <div className="picket-page">
      <div className="picket-page-header">
        <div>
          <h2>Jadwal Piket & Kebersihan</h2>
          <p>Kelola jadwal dan tugas kebersihan organisasi.</p>
        </div>
        {canManageSchedule && (
          <button className="picket-btn picket-btn-primary" onClick={() => handleOpenForm()}>
            + Buat Jadwal
          </button>
        )}
      </div>

      {isLoading ? (
        <div className="picket-card"><p>Memuat jadwal...</p></div>
      ) : schedules.length === 0 ? (
        <div className="picket-card"><p>Tidak ada jadwal piket yang ditemukan.</p></div>
      ) : (
        <div className="picket-grid">
          {schedules.map(schedule => (
            <div key={schedule.id} className="picket-card">
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "1rem" }}>
                <h3 style={{ margin: 0, fontSize: "1.125rem", color: "#111827" }}>{schedule.title}</h3>
                {getStatusBadge(schedule.status)}
              </div>
              <p style={{ fontSize: "0.875rem", color: "#4B5563", marginBottom: "0.5rem" }}>
                <strong>📅 Tanggal:</strong> {schedule.dutyDate} <br/>
                <strong>🕒 Waktu:</strong> {schedule.startTime} - {schedule.endTime} <br/>
                <strong>📍 Lokasi:</strong> {schedule.location}
              </p>
              {schedule.description && (
                <p style={{ fontSize: "0.875rem", color: "#6B7280", fontStyle: "italic", marginBottom: "1rem" }}>
                  "{schedule.description}"
                </p>
              )}

              <div style={{ marginTop: "1rem" }}>
                <h4 style={{ fontSize: "0.875rem", fontWeight: 600, color: "#111827", marginBottom: "0.5rem" }}>Petugas ({schedule.assignments.length}):</h4>
                <ul style={{ listStyleType: "none", padding: 0, margin: 0 }}>
                  {schedule.assignments.map(assign => {
                    const isMyAssignment = user?.email === assign.memberEmail;
                    return (
                      <li key={assign.id} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "0.5rem 0", borderBottom: "1px solid #E5E7EB" }}>
                        <span style={{ fontSize: "0.875rem" }}>
                          {assign.memberName} {isMyAssignment && "(Saya)"}
                        </span>
                        <div style={{ display: "flex", gap: "0.5rem", alignItems: "center" }}>
                          {getStatusBadge(assign.attendanceStatus)}
                          
                          {/* Presensi Action */}
                          {canCreateAttendance && isMyAssignment && assign.attendanceStatus === "PENDING" && schedule.status === "PUBLISHED" && (
                            <button className="picket-btn picket-btn-primary" style={{ padding: "0.25rem 0.5rem", fontSize: "0.75rem" }} onClick={() => setAttendanceFormOpen({assignmentId: assign.id, scheduleId: schedule.id})}>
                              Isi Presensi
                            </button>
                          )}

                          {/* Beri Poin Action */}
                          {canManagePoints && schedule.status === "COMPLETED" && (
                            <button className="picket-btn picket-btn-secondary" style={{ padding: "0.25rem 0.5rem", fontSize: "0.75rem" }} onClick={() => setPointFormOpen({memberId: assign.memberId, scheduleId: schedule.id})}>
                              Beri Poin
                            </button>
                          )}
                        </div>
                      </li>
                    );
                  })}
                </ul>
              </div>

              {canManageSchedule && (
                <div style={{ marginTop: "1.5rem", display: "flex", justifyContent: "flex-end", gap: "0.5rem" }}>
                  <button className="picket-btn picket-btn-secondary" onClick={() => handleOpenForm(schedule)}>
                    Edit Jadwal
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* SCHEDULE MODAL */}
      {isFormOpen && (
        <div className="picket-modal-overlay">
          <div className="picket-modal-content">
            <div className="picket-modal-header">
              <h3>{selectedSchedule ? "Edit Jadwal" : "Buat Jadwal Baru"}</h3>
              <button className="picket-modal-close" onClick={handleCloseForm}>✕</button>
            </div>
            <div className="picket-modal-body">
              <form id="schedule-form" onSubmit={handleSubmitSchedule}>
                <div className="picket-form-group">
                  <label>Judul Piket</label>
                  <input type="text" className="picket-form-input" required value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})} />
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem" }}>
                  <div className="picket-form-group">
                    <label>Tanggal</label>
                    <input type="date" className="picket-form-input" required value={formData.dutyDate} onChange={e => setFormData({...formData, dutyDate: e.target.value})} />
                  </div>
                  <div className="picket-form-group">
                    <label>Lokasi</label>
                    <input type="text" className="picket-form-input" required value={formData.location} onChange={e => setFormData({...formData, location: e.target.value})} />
                  </div>
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem" }}>
                  <div className="picket-form-group">
                    <label>Jam Mulai (HH:mm:ss)</label>
                    <input type="time" step="1" className="picket-form-input" required value={formData.startTime} onChange={e => setFormData({...formData, startTime: e.target.value})} />
                  </div>
                  <div className="picket-form-group">
                    <label>Jam Selesai (HH:mm:ss)</label>
                    <input type="time" step="1" className="picket-form-input" required value={formData.endTime} onChange={e => setFormData({...formData, endTime: e.target.value})} />
                  </div>
                </div>
                <div className="picket-form-group">
                  <label>Status</label>
                  <select className="picket-form-input" value={formData.status} onChange={e => setFormData({...formData, status: e.target.value as ScheduleStatus})}>
                    <option value="DRAFT">DRAFT</option>
                    <option value="PUBLISHED">PUBLISHED</option>
                    <option value="COMPLETED">COMPLETED</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                </div>
                <div className="picket-form-group">
                  <label>Anggota Bertugas</label>
                  <select multiple className="picket-form-input" style={{ height: "150px" }} value={formData.memberIds?.map(String)} onChange={e => {
                    const ids = Array.from(e.target.selectedOptions, option => parseInt(option.value));
                    setFormData({...formData, memberIds: ids});
                  }}>
                    {members.map(m => (
                      <option key={m.id} value={m.id}>{m.fullName}</option>
                    ))}
                  </select>
                  <small style={{ color: "#6B7280" }}>Tekan Ctrl (Windows) / Cmd (Mac) untuk memilih lebih dari satu.</small>
                </div>
                <div className="picket-form-group">
                  <label>Deskripsi / Instruksi</label>
                  <textarea className="picket-form-input" rows={3} value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})}></textarea>
                </div>
              </form>
            </div>
            <div className="picket-modal-footer">
              <button className="picket-btn picket-btn-secondary" onClick={handleCloseForm}>Batal</button>
              <button type="submit" form="schedule-form" className="picket-btn picket-btn-primary">Simpan</button>
            </div>
          </div>
        </div>
      )}

      {/* ATTENDANCE MODAL */}
      {attendanceFormOpen && (
        <div className="picket-modal-overlay">
          <div className="picket-modal-content" style={{ maxWidth: "400px" }}>
            <div className="picket-modal-header">
              <h3>Isi Presensi</h3>
              <button className="picket-modal-close" onClick={() => setAttendanceFormOpen(null)}>✕</button>
            </div>
            <div className="picket-modal-body">
              <form id="attendance-form" onSubmit={handleSubmitAttendance}>
                <div className="picket-form-group">
                  <label>Status Kehadiran</label>
                  <select className="picket-form-input" value={attendanceStatus} onChange={e => setAttendanceStatus(e.target.value as any)}>
                    <option value="PRESENT">Hadir</option>
                    <option value="EXCUSED">Izin</option>
                    <option value="ABSENT">Alpa</option>
                  </select>
                </div>
                <div className="picket-form-group">
                  <label>Catatan (opsional)</label>
                  <textarea className="picket-form-input" rows={3} value={attendanceNote} onChange={e => setAttendanceNote(e.target.value)}></textarea>
                </div>
              </form>
            </div>
            <div className="picket-modal-footer">
              <button className="picket-btn picket-btn-secondary" onClick={() => setAttendanceFormOpen(null)}>Batal</button>
              <button type="submit" form="attendance-form" className="picket-btn picket-btn-primary">Kirim</button>
            </div>
          </div>
        </div>
      )}

      {/* POINT MODAL */}
      {pointFormOpen && (
        <div className="picket-modal-overlay">
          <div className="picket-modal-content" style={{ maxWidth: "400px" }}>
            <div className="picket-modal-header">
              <h3>Beri Poin / Pelanggaran</h3>
              <button className="picket-modal-close" onClick={() => setPointFormOpen(null)}>✕</button>
            </div>
            <div className="picket-modal-body">
              <form id="point-form" onSubmit={handleSubmitPoint}>
                <div className="picket-form-group">
                  <label>Jenis</label>
                  <select className="picket-form-input" value={pointType} onChange={e => setPointType(e.target.value as any)}>
                    <option value="REWARD">Reward (Poin Positif)</option>
                    <option value="VIOLATION">Violation (Pelanggaran)</option>
                  </select>
                </div>
                <div className="picket-form-group">
                  <label>Nilai Poin (harus positif)</label>
                  <input type="number" min="1" className="picket-form-input" required value={pointValue} onChange={e => setPointValue(Number(e.target.value))} />
                </div>
                <div className="picket-form-group">
                  <label>Alasan</label>
                  <textarea className="picket-form-input" rows={3} required value={pointReason} onChange={e => setPointReason(e.target.value)}></textarea>
                </div>
              </form>
            </div>
            <div className="picket-modal-footer">
              <button className="picket-btn picket-btn-secondary" onClick={() => setPointFormOpen(null)}>Batal</button>
              <button type="submit" form="point-form" className="picket-btn picket-btn-primary">Kirim</button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
