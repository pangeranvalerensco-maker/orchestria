import React, { useState, useEffect } from 'react';
import { englishService } from '../services/englishService';
import type { EnglishActivity, EnglishDepositRequest, EnglishDeposit } from '../types/english';
import { useAuth } from '../auth/useAuth';
import { ApiError } from '../api/http';

export const EnglishActivityPage: React.FC = () => {
  const { token, hasPermission } = useAuth();
  const [activities, setActivities] = useState<EnglishActivity[]>([]);
  const [myDeposits, setMyDeposits] = useState<EnglishDeposit[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const [showDepositModal, setShowDepositModal] = useState(false);
  const [selectedActivity, setSelectedActivity] = useState<EnglishActivity | null>(null);
  const [depositForm, setDepositForm] = useState<EnglishDepositRequest>({
    activityId: '',
    topic: '',
    evidenceUrl: '',
    submissionNote: ''
  });

  const hasDepositAuth = hasPermission('english.deposit.create');

  useEffect(() => {
    loadData();
  }, [token]);

  const loadData = async () => {
    if (!token) return;
    setLoading(true);
    setError('');
    try {
      const acts = await englishService.getAllActivities(token);
      setActivities(acts);
      if (hasDepositAuth) {
        const deps = await englishService.getMyDeposits(token);
        setMyDeposits(deps);
      }
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'Gagal memuat data');
    } finally {
      setLoading(false);
    }
  };

  const handleDepositSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;
    try {
      await englishService.createDeposit(token, depositForm);
      setSuccessMessage('Setoran berhasil dikirim!');
      setDepositForm({ activityId: '', topic: '', evidenceUrl: '', submissionNote: '' });
      setShowDepositModal(false);
      loadData();
    } catch (err: unknown) {
      setError(err instanceof ApiError ? err.message : 'Gagal mengirim setoran');
    }
  };

  const getMyDepositForActivity = (activityId: string) => {
    return myDeposits.find(d => d.activityId === activityId);
  };

  return (
    <div className="english-page">
      <div className="english-header">
        <div>
          <h1 className="english-title">English Activity Portal</h1>
          <p className="english-subtitle">Lihat jadwal aktivitas dan kirim setoran English Anda.</p>
        </div>
      </div>

      {error && <div className="english-alert english-alert-error">{error}</div>}
      {successMessage && <div className="english-alert english-success-message">{successMessage}</div>}

      <div className="english-grid">
        {loading ? (
          <div className="english-empty-state">Memuat aktivitas...</div>
        ) : activities.length === 0 ? (
          <div className="english-empty-state">Belum ada aktivitas yang tersedia.</div>
        ) : activities.map(act => {
          const myDep = getMyDepositForActivity(act.id);
          return (
            <div key={act.id} className="english-card english-card-column">
              <div className="english-card-header">
                <h3 className="english-title">{act.title}</h3>
                {myDep && <span className="english-badge english-badge-success">{myDep.status}</span>}
              </div>
              <div className="english-card-meta">
                <div><strong>Tanggal:</strong> {act.activityDate} ({act.startTime} - {act.endTime})</div>
                <div><strong>Topik:</strong> {act.topic}</div>
              </div>
              {act.description && <p className="english-card-description">{act.description}</p>}
              
              <div className="english-card-footer">
                {!myDep && hasDepositAuth && act.status === 'PUBLISHED' && (
                  <button 
                    className="english-btn english-btn-primary english-full-width" 
                    onClick={() => {
                      setSelectedActivity(act);
                      setDepositForm({
                        activityId: act.id,
                        topic: '',
                        evidenceUrl: '',
                        submissionNote: ''
                      });
                      setShowDepositModal(true);
                    }}
                  >
                    Kirim Setoran
                  </button>
                )}
                {myDep && (
                  <div className="english-deposit-summary">
                    <div className="english-deposit-topic"><strong>Topik Anda:</strong> {myDep.topic}</div>
                    {myDep.score != null && <div><strong>Skor:</strong> {myDep.score}</div>}
                    {myDep.verificationNote && <div><strong>Catatan:</strong> {myDep.verificationNote}</div>}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* DEPOSIT MODAL */}
      {showDepositModal && selectedActivity && (
        <div className="english-modal-overlay">
          <div className="english-modal-content">
            <div className="english-modal-header">
              <h3>Kirim Setoran: {selectedActivity.title}</h3>
              <button className="english-btn english-btn-secondary" onClick={() => setShowDepositModal(false)}>X</button>
            </div>
            <form onSubmit={handleDepositSubmit}>
              <div className="english-modal-body">
                <div className="english-form-group">
                  <label className="english-form-label">Topik Setoran</label>
                  <input required className="english-form-input" value={depositForm.topic} onChange={e => setDepositForm({...depositForm, topic: e.target.value})} placeholder="Contoh: My Daily Routine" />
                </div>
                <div className="english-form-group">
                  <label className="english-form-label">Link Bukti (Video/Audio)</label>
                  <input required type="url" className="english-form-input" value={depositForm.evidenceUrl} onChange={e => setDepositForm({...depositForm, evidenceUrl: e.target.value})} placeholder="https://..." />
                </div>
                <div className="english-form-group">
                  <label className="english-form-label">Catatan Tambahan (Opsional)</label>
                  <textarea className="english-form-input" value={depositForm.submissionNote || ''} onChange={e => setDepositForm({...depositForm, submissionNote: e.target.value})} rows={3}></textarea>
                </div>
              </div>
              <div className="english-modal-footer">
                <button type="button" className="english-btn english-btn-secondary" onClick={() => setShowDepositModal(false)}>Batal</button>
                <button type="submit" className="english-btn english-btn-primary">Kirim</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
