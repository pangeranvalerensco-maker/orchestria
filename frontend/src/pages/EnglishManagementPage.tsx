import React, { useState, useEffect } from 'react';
import { englishService } from '../services/englishService';
import type { 
  EnglishActivity, 
  EnglishDeposit, 
  EnglishReportSummary,
  EnglishActivityRequest,
  EnglishDepositVerificationRequest,
  EnglishActivityStatus,
  EnglishDepositStatus
} from '../types/english';
import { useAuth } from '../auth/useAuth';
import { ApiError } from '../api/http';

export const EnglishManagementPage: React.FC = () => {
  const { token, hasPermission } = useAuth();
  
  const hasManageAuth = hasPermission('english.activity.manage');
  const hasVerifyAuth = hasPermission('english.deposit.verify');
  const hasReportAuth = hasPermission('english.report.read');
  const hasReadAllDeposit = hasPermission('english.deposit.read.all');

  const showActivitiesTab = hasManageAuth || hasReadAllDeposit || hasReportAuth;
  const showDepositsTab = hasReadAllDeposit || hasVerifyAuth;
  const showReportTab = hasReportAuth;

  const [activeTab, setActiveTab] = useState<'ACTIVITIES' | 'DEPOSITS' | 'REPORT'>(showActivitiesTab ? 'ACTIVITIES' : (showDepositsTab ? 'DEPOSITS' : 'REPORT'));
  
  // States
  const [activities, setActivities] = useState<EnglishActivity[]>([]);
  const [deposits, setDeposits] = useState<EnglishDeposit[]>([]);
  const [report, setReport] = useState<EnglishReportSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Modals
  const [showActivityModal, setShowActivityModal] = useState(false);
  const [showVerifyModal, setShowVerifyModal] = useState(false);
  
  // Selected Data
  const [selectedActivity, setSelectedActivity] = useState<EnglishActivity | null>(null);
  const [selectedDeposit, setSelectedDeposit] = useState<EnglishDeposit | null>(null);
  
  // Deposit Filter
  const [depositFilter, setDepositFilter] = useState<'ALL' | EnglishDepositStatus>('ALL');

  // Forms
  const [activityForm, setActivityForm] = useState<EnglishActivityRequest>({
    title: '',
    activityDate: '',
    startTime: '',
    endTime: '',
    topic: '',
    description: '',
    status: 'DRAFT'
  });
  
  const [verifyForm, setVerifyForm] = useState<EnglishDepositVerificationRequest>({
    decision: 'VERIFIED',
    score: 100,
    verificationNote: ''
  });

  useEffect(() => {
    loadData();
  }, [activeTab, token]);

  const loadData = async () => {
    if (!token) return;
    setLoading(true);
    setError('');
    setSuccessMessage('');
    try {
      if (activeTab === 'ACTIVITIES') {
        const data = await englishService.getAllActivities(token);
        setActivities(data);
      } else if (activeTab === 'DEPOSITS' && hasReadAllDeposit) {
        const data = await englishService.getAllDeposits(token);
        setDeposits(data);
      } else if (activeTab === 'REPORT' && hasReportAuth) {
        const data = await englishService.getReportSummary(token);
        setReport(data);
      }
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'Gagal memuat data');
    } finally {
      setLoading(false);
    }
  };

  const handleActivitySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;
    try {
      if (selectedActivity) {
        await englishService.updateActivity(token, selectedActivity.id, activityForm);
        setSuccessMessage('Aktivitas berhasil diperbarui!');
      } else {
        await englishService.createActivity(token, activityForm);
        setSuccessMessage('Aktivitas berhasil dibuat!');
      }
      setShowActivityModal(false);
      loadData();
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'Gagal menyimpan activity');
    }
  };

  const handleVerifySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedDeposit || !token) return;
    try {
      await englishService.verifyDeposit(token, selectedDeposit.id, verifyForm);
      setSuccessMessage(verifyForm.decision === 'VERIFIED' ? 'Setoran berhasil diverifikasi!' : 'Setoran ditolak.');
      setShowVerifyModal(false);
      loadData();
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'Gagal memverifikasi deposit');
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch(status) {
      case 'PUBLISHED':
      case 'VERIFIED':
      case 'COMPLETED': return 'english-badge-success';
      case 'SUBMITTED': return 'english-badge-info';
      case 'REJECTED': 
      case 'MISSED':
      case 'CANCELLED': return 'english-badge-error';
      default: return 'english-badge-draft';
    }
  };

  const filteredDeposits = deposits.filter(d => depositFilter === 'ALL' || d.status === depositFilter);

  return (
    <div className="english-page">
      <div className="english-header">
        <div>
          <h1 className="english-title">English Management</h1>
          <p className="english-subtitle">Kelola aktivitas, setoran, dan laporan bahasa Inggris.</p>
        </div>
      </div>

      <div className="english-card english-tab-bar">
        {showActivitiesTab && (
          <button 
            className={`english-btn ${activeTab === 'ACTIVITIES' ? 'english-btn-primary' : 'english-btn-secondary'}`}
            onClick={() => setActiveTab('ACTIVITIES')}
          >
            Aktivitas
          </button>
        )}
        {showDepositsTab && (
          <button 
            className={`english-btn ${activeTab === 'DEPOSITS' ? 'english-btn-primary' : 'english-btn-secondary'}`}
            onClick={() => setActiveTab('DEPOSITS')}
          >
            Setoran
          </button>
        )}
        {showReportTab && (
          <button 
            className={`english-btn ${activeTab === 'REPORT' ? 'english-btn-primary' : 'english-btn-secondary'}`}
            onClick={() => setActiveTab('REPORT')}
          >
            Laporan
          </button>
        )}
      </div>

      {error && <div className="english-alert english-alert-error">{error}</div>}
      {successMessage && <div className="english-alert english-success-message">{successMessage}</div>}

      {/* ACTIVITIES TAB */}
      {activeTab === 'ACTIVITIES' && (
        <div className="english-card">
          <div className="english-section-header">
            <h2>Daftar Aktivitas</h2>
            {hasManageAuth && (
              <button 
                className="english-btn english-btn-primary"
                onClick={() => {
                  setSelectedActivity(null);
                  setActivityForm({
                    title: '', activityDate: '', startTime: '', endTime: '', topic: '', description: '', status: 'DRAFT'
                  });
                  setShowActivityModal(true);
                }}
              >
                Buat Aktivitas
              </button>
            )}
          </div>
          
          {loading ? <div className="english-empty-state">Memuat...</div> : (
            <table className="english-table">
              <thead>
                <tr>
                  <th>Tanggal</th>
                  <th>Waktu</th>
                  <th>Judul</th>
                  <th>Topik</th>
                  <th>Status</th>
                  <th>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {activities.length === 0 ? (
                  <tr><td colSpan={6} className="english-empty-state">Tidak ada aktivitas.</td></tr>
                ) : activities.map(act => (
                  <tr key={act.id}>
                    <td>{act.activityDate}</td>
                    <td>{act.startTime} - {act.endTime}</td>
                    <td>{act.title}</td>
                    <td>{act.topic}</td>
                    <td><span className={`english-badge ${getStatusBadgeClass(act.status)}`}>{act.status}</span></td>
                    <td>
                      {hasManageAuth && (
                        <button 
                          className="english-btn english-btn-secondary"
                          onClick={() => {
                            setSelectedActivity(act);
                            setActivityForm({
                              title: act.title,
                              activityDate: act.activityDate,
                              startTime: act.startTime,
                              endTime: act.endTime,
                              topic: act.topic,
                              description: act.description || '',
                              status: act.status
                            });
                            setShowActivityModal(true);
                          }}
                        >
                          Edit
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* DEPOSITS TAB */}
      {activeTab === 'DEPOSITS' && (
        <div className="english-card">
          <div className="english-section-header">
            <h2>Daftar Setoran</h2>
            <select 
              className="english-form-input" 
              value={depositFilter} 
              onChange={(e) => setDepositFilter(e.target.value as 'ALL' | EnglishDepositStatus)}
            >
              <option value="ALL">Semua Status</option>
              <option value="SUBMITTED">SUBMITTED</option>
              <option value="VERIFIED">VERIFIED</option>
              <option value="REJECTED">REJECTED</option>
              <option value="MISSED">MISSED</option>
            </select>
          </div>
          {loading ? <div className="english-empty-state">Memuat...</div> : (
            <table className="english-table english-table-spaced">
              <thead>
                <tr>
                  <th>Waktu</th>
                  <th>Anggota</th>
                  <th>Aktivitas</th>
                  <th>Topik</th>
                  <th>Status</th>
                  <th>Skor</th>
                  <th>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {filteredDeposits.length === 0 ? (
                  <tr><td colSpan={7} className="english-empty-state">Tidak ada setoran.</td></tr>
                ) : filteredDeposits.map(dep => (
                  <tr key={dep.id}>
                    <td>{new Date(dep.submittedAt).toLocaleString('id-ID')}</td>
                    <td>{dep.memberName}</td>
                    <td>{dep.activityTitle}</td>
                    <td>{dep.topic}</td>
                    <td><span className={`english-badge ${getStatusBadgeClass(dep.status)}`}>{dep.status}</span></td>
                    <td>{dep.score ?? '-'}</td>
                    <td className="english-inline-gap">
                      <a href={dep.evidenceUrl} target="_blank" rel="noreferrer" className="english-btn english-btn-secondary">
                        Buka Bukti
                      </a>
                      {hasVerifyAuth && dep.status === 'SUBMITTED' && (
                        <button 
                          className="english-btn english-btn-primary"
                          onClick={() => {
                            setSelectedDeposit(dep);
                            setVerifyForm({ decision: 'VERIFIED', score: 100, verificationNote: '' });
                            setShowVerifyModal(true);
                          }}
                        >
                          Verifikasi
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* REPORT TAB */}
      {activeTab === 'REPORT' && report && (
        <div className="english-grid">
          <div className="english-card">
            <h3>Ringkasan Aktivitas</h3>
            <ul className="english-report-list">
              <li>Total: {report.totalActivities}</li>
              <li>Published: {report.publishedActivities}</li>
              <li>Selesai: {report.completedActivities}</li>
            </ul>
          </div>
          <div className="english-card">
            <h3>Ringkasan Setoran</h3>
            <ul className="english-report-list">
              <li>Total: {report.totalDeposits}</li>
              <li>Submitted: {report.submittedDeposits}</li>
              <li>Verified: {report.verifiedDeposits}</li>
              <li>Rejected: {report.rejectedDeposits}</li>
              <li>Rata-rata Skor: {report.averageScore}</li>
            </ul>
          </div>
          <div className="english-card english-report-wide">
            <h3>Peringkat Anggota</h3>
            <table className="english-table english-table-spaced">
              <thead>
                <tr>
                  <th>Anggota</th>
                  <th>Terkumpul</th>
                  <th>Diverifikasi</th>
                  <th>Ditolak</th>
                  <th>Rata-rata Skor</th>
                </tr>
              </thead>
              <tbody>
                {report.memberSummary.map(m => (
                  <tr key={m.memberId}>
                    <td>{m.memberName}</td>
                    <td>{m.submittedCount}</td>
                    <td>{m.verifiedCount}</td>
                    <td>{m.rejectedCount}</td>
                    <td>{m.averageScore}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* MODAL ACTIVITY */}
      {showActivityModal && (
        <div className="english-modal-overlay">
          <div className="english-modal-content">
            <div className="english-modal-header">
              <h3>{selectedActivity ? 'Edit Aktivitas' : 'Buat Aktivitas'}</h3>
              <button className="english-btn english-btn-secondary" onClick={() => setShowActivityModal(false)}>X</button>
            </div>
            <form onSubmit={handleActivitySubmit}>
              <div className="english-modal-body">
                <div className="english-form-group">
                  <label className="english-form-label">Judul</label>
                  <input required className="english-form-input" value={activityForm.title} onChange={e => setActivityForm({...activityForm, title: e.target.value})} />
                </div>
                <div className="english-form-group">
                  <label className="english-form-label">Tanggal</label>
                  <input required type="date" className="english-form-input" value={activityForm.activityDate} onChange={e => setActivityForm({...activityForm, activityDate: e.target.value})} />
                </div>
                <div className="english-form-row">
                  <div className="english-form-column">
                    <label className="english-form-label">Mulai</label>
                    <input required type="time" className="english-form-input" value={activityForm.startTime} onChange={e => setActivityForm({...activityForm, startTime: e.target.value})} />
                  </div>
                  <div className="english-form-column">
                    <label className="english-form-label">Selesai</label>
                    <input required type="time" className="english-form-input" value={activityForm.endTime} onChange={e => setActivityForm({...activityForm, endTime: e.target.value})} />
                  </div>
                </div>
                <div className="english-form-group">
                  <label className="english-form-label">Topik</label>
                  <input required className="english-form-input" value={activityForm.topic} onChange={e => setActivityForm({...activityForm, topic: e.target.value})} />
                </div>
                <div className="english-form-group">
                  <label className="english-form-label">Deskripsi</label>
                  <textarea className="english-form-input" value={activityForm.description} onChange={e => setActivityForm({...activityForm, description: e.target.value})} rows={3}></textarea>
                </div>
                <div className="english-form-group">
                  <label className="english-form-label">Status</label>
                  <select required className="english-form-input" value={activityForm.status} onChange={e => setActivityForm({...activityForm, status: e.target.value as EnglishActivityStatus})}>
                    <option value="DRAFT">DRAFT</option>
                    <option value="PUBLISHED">PUBLISHED</option>
                    <option value="COMPLETED">COMPLETED</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                </div>
              </div>
              <div className="english-modal-footer">
                <button type="button" className="english-btn english-btn-secondary" onClick={() => setShowActivityModal(false)}>Batal</button>
                <button type="submit" className="english-btn english-btn-primary">Simpan</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL VERIFY */}
      {showVerifyModal && selectedDeposit && (
        <div className="english-modal-overlay">
          <div className="english-modal-content">
            <div className="english-modal-header">
              <h3>Verifikasi Setoran</h3>
              <button className="english-btn english-btn-secondary" onClick={() => setShowVerifyModal(false)}>X</button>
            </div>
            <form onSubmit={handleVerifySubmit}>
              <div className="english-modal-body">
                <p className="english-verification-summary">
                  <strong>Anggota:</strong> {selectedDeposit.memberName}<br/>
                  <strong>Topik:</strong> {selectedDeposit.topic}
                </p>
                <div className="english-form-group">
                  <label className="english-form-label">Keputusan</label>
                  <select required className="english-form-input" value={verifyForm.decision} onChange={e => setVerifyForm({...verifyForm, decision: e.target.value as 'VERIFIED' | 'REJECTED'})}>
                    <option value="VERIFIED">TERIMA (VERIFIED)</option>
                    <option value="REJECTED">TOLAK (REJECTED)</option>
                  </select>
                </div>
                {verifyForm.decision === 'VERIFIED' && (
                  <div className="english-form-group">
                    <label className="english-form-label">Skor (0-100)</label>
                    <input required type="number" min="0" max="100" className="english-form-input" value={verifyForm.score} onChange={e => setVerifyForm({...verifyForm, score: Number(e.target.value)})} />
                  </div>
                )}
                <div className="english-form-group">
                  <label className="english-form-label">Catatan</label>
                  <textarea required={verifyForm.decision === 'REJECTED'} className="english-form-input" value={verifyForm.verificationNote} onChange={e => setVerifyForm({...verifyForm, verificationNote: e.target.value})} rows={3}></textarea>
                </div>
              </div>
              <div className="english-modal-footer">
                <button type="button" className="english-btn english-btn-secondary" onClick={() => setShowVerifyModal(false)}>Batal</button>
                <button type="submit" className="english-btn english-btn-primary">Simpan Verifikasi</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
