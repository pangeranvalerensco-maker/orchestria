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

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

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
  const [attendanceEvidenceUrl, setAttendanceEvidenceUrl] = useState("");
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
      setErrorMessage("");
      if (canManageSchedule) {
        const data = await getSchedules(token);
        setSchedules(data);
      } else {
        const data = await getMySchedules(token);
        setSchedules(data);
      }
    } catch (err: unknown) {
      setErrorMessage("Gagal memuat jadwal piket.");
    } finally {
      setIsLoading(false);
    }
  }

  async function loadMembers() {
    if (!token) return;
    try {
      const res = await getMembers(token);
      setMembers(res.data);
    } catch (err: unknown) {
      setErrorMessage("Gagal memuat daftar anggota.");
    }
  }

  const handleOpenForm = (schedule?: CleanlinessSchedule) => {
    setErrorMessage("");
    setSuccessMessage("");
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
    setErrorMessage("");
    setSuccessMessage("");
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
        setSuccessMessage("Jadwal berhasil diperbarui.");
      } else {
        await createSchedule(request, token);
        setSuccessMessage("Jadwal berhasil dibuat.");
      }
      handleCloseForm();
      loadData();
    } catch (err: unknown) {
      setErrorMessage("Gagal menyimpan jadwal.");
    }
  };

  const handleSubmitAttendance = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !attendanceFormOpen) return;
    setErrorMessage("");
    setSuccessMessage("");
    try {
      const req: AttendanceRequest = {
        status: attendanceStatus,
        note: attendanceNote.trim() || undefined,
        evidenceUrl: attendanceEvidenceUrl.trim() || undefined,
      };
      await recordAttendance(attendanceFormOpen.assignmentId, req, token);
      setAttendanceFormOpen(null);
      setSuccessMessage("Presensi berhasil dicatat.");
      setAttendanceNote("");
      setAttendanceEvidenceUrl("");
      setAttendanceStatus("PRESENT");
      loadData();
    } catch (err: unknown) {
      setErrorMessage("Gagal menyimpan presensi.");
    }
  };

  const handleSubmitPoint = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !pointFormOpen) return;
    setErrorMessage("");
    setSuccessMessage("");
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
      setSuccessMessage("Poin berhasil ditambahkan.");
      loadData();
    } catch (err: unknown) {
      setErrorMessage("Gagal menambahkan poin.");
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

      {errorMessage && (
        <div className="picket-alert picket-alert-error">
          {errorMessage}
        </div>
      )}

      {successMessage && (
        <div className="picket-alert picket-alert-success">
          {successMessage}
        </div>
      )}

      {isLoading ? (
        <div className="picket-card"><p>Memuat jadwal...</p></div>
      ) : schedules.length === 0 ? (
        <div className="picket-card"><p>Tidak ada jadwal piket yang ditemukan.</p></div>
      ) : (
        <div className="picket-grid">
          {schedules.map(schedule => (
            <div key={schedule.id} className="picket-card">
              <div className="picket-card-header">
                <h3 className="picket-card-title">{schedule.title}</h3>
                {getStatusBadge(schedule.status)}
              </div>
              <p className="picket-card-meta">
                <strong>📅 Tanggal:</strong> {schedule.dutyDate} <br/>
                <strong>🕒 Waktu:</strong> {schedule.startTime} - {schedule.endTime} <br/>
                <strong>📍 Lokasi:</strong> {schedule.location}
              </p>
              {schedule.description && (
                <p className="picket-card-description">
                  "{schedule.description}"
                </p>
              )}

              <div className="picket-assignment-section">
                <h4>Petugas ({schedule.assignments.length}):</h4>
                <ul className="picket-assignment-list">
                  {schedule.assignments.map(assign => {
                    const isMyAssignment = user?.email === assign.memberEmail;
                    return (
                      <li key={assign.id} className="picket-assignment-item">
                        <span>
                          {assign.memberName} {isMyAssignment && "(Saya)"}
                        </span>
                        <div className="picket-assignment-actions">
                          {getStatusBadge(assign.attendanceStatus)}
                          
                          {canCreateAttendance && isMyAssignment && assign.attendanceStatus === "PENDING" && schedule.status === "PUBLISHED" && (
                            <button className="picket-btn picket-btn-primary picket-small-button" onClick={() => setAttendanceFormOpen({assignmentId: assign.id, scheduleId: schedule.id})}>
                              Isi Presensi
                            </button>
                          )}

                          {canManagePoints && schedule.status === "COMPLETED" && (
                            <button className="picket-btn picket-btn-secondary picket-small-button" onClick={() => setPointFormOpen({memberId: assign.memberId, scheduleId: schedule.id})}>
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
                <div className="picket-inline-actions">
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
                <div className="picket-form-grid">
                  <div className="picket-form-group">
                    <label>Tanggal</label>
                    <input type="date" className="picket-form-input" required value={formData.dutyDate} onChange={e => setFormData({...formData, dutyDate: e.target.value})} />
                  </div>
                  <div className="picket-form-group">
                    <label>Lokasi</label>
                    <input type="text" className="picket-form-input" required value={formData.location} onChange={e => setFormData({...formData, location: e.target.value})} />
                  </div>
                </div>
                <div className="picket-form-grid">
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
                  <select multiple className="picket-form-input picket-member-select" value={formData.memberIds?.map(String)} onChange={e => {
                    const ids = Array.from(e.target.selectedOptions, option => parseInt(option.value));
                    setFormData({...formData, memberIds: ids});
                  }}>
                    {members.map(m => (
                      <option key={m.id} value={m.id}>{m.fullName}</option>
                    ))}
                  </select>
                  <small className="picket-help-text">Tekan Ctrl (Windows) / Cmd (Mac) untuk memilih lebih dari satu.</small>
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
          <div className="picket-modal-content picket-modal-small">
            <div className="picket-modal-header">
              <h3>Isi Presensi</h3>
              <button className="picket-modal-close" onClick={() => setAttendanceFormOpen(null)}>✕</button>
            </div>
            <div className="picket-modal-body">
              <form id="attendance-form" onSubmit={handleSubmitAttendance}>
                <div className="picket-form-group">
                  <label>Status Kehadiran</label>
                  <select className="picket-form-input" value={attendanceStatus} onChange={e => setAttendanceStatus(e.target.value as "PRESENT" | "ABSENT" | "EXCUSED")}>
                    <option value="PRESENT">Hadir</option>
                    <option value="EXCUSED">Izin</option>
                    <option value="ABSENT">Alpa</option>
                  </select>
                </div>
                <div className="picket-form-group">
                  <label>Catatan (opsional)</label>
                  <textarea className="picket-form-input" rows={2} value={attendanceNote} onChange={e => setAttendanceNote(e.target.value)}></textarea>
                </div>
                <div className="picket-form-group">
                  <label>Bukti / Foto (URL)</label>
                  <input type="text" className="picket-form-input" value={attendanceEvidenceUrl} onChange={e => setAttendanceEvidenceUrl(e.target.value)} />
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
          <div className="picket-modal-content picket-modal-small">
            <div className="picket-modal-header">
              <h3>Beri Poin / Pelanggaran</h3>
              <button className="picket-modal-close" onClick={() => setPointFormOpen(null)}>✕</button>
            </div>
            <div className="picket-modal-body">
              <form id="point-form" onSubmit={handleSubmitPoint}>
                <div className="picket-form-group">
                  <label>Jenis</label>
                  <select className="picket-form-input" value={pointType} onChange={e => setPointType(e.target.value as "REWARD" | "VIOLATION")}>
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
