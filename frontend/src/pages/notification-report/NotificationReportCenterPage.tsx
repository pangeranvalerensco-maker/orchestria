import React, { useState, useEffect, useRef } from 'react';
import './NotificationReportCenterPage.css';
import { notificationReportService } from '../../services/notificationReportService';
import type {
    NotificationLog,
    ReportExportLog,
    ScheduledJobLog,
    ReportSubscriber,
    NotificationSendRequest,
    ReportSummary
} from '../../types/notificationReport';
import { useAuth } from '../../auth/useAuth';

export const NotificationReportCenterPage: React.FC = () => {
    const { hasPermission, token } = useAuth();
    const [activeTab, setActiveTab] = useState<'notifications' | 'reports' | 'scheduler' | 'subscribers'>('notifications');
    const fileInputRef = useRef<HTMLInputElement>(null);

    // States
    const [notificationLogs, setNotificationLogs] = useState<NotificationLog[]>([]);
    const [exportLogs, setExportLogs] = useState<ReportExportLog[]>([]);
    const [schedulerLogs, setSchedulerLogs] = useState<ScheduledJobLog[]>([]);
    const [subscribers, setSubscribers] = useState<ReportSubscriber[]>([]);
    const [summary, setSummary] = useState<ReportSummary | null>(null);

    // Form
    const [sendReq, setSendReq] = useState<NotificationSendRequest>({
        to: [],
        subject: '',
        body: '',
        html: false
    });
    const [toEmail, setToEmail] = useState('');

    useEffect(() => {
        if (activeTab === 'notifications' && hasPermission('notification.read')) {
            loadNotificationLogs();
        } else if (activeTab === 'reports' && hasPermission('report.read')) {
            loadExportLogs();
            loadSummary();
        } else if (activeTab === 'scheduler' && hasPermission('scheduler.log.read')) {
            loadSchedulerLogs();
        } else if (activeTab === 'subscribers' && hasPermission('report.read')) {
            loadSubscribers();
        }
    }, [activeTab]);

    const loadNotificationLogs = async () => {
        if (!token) return;
        try {
            const data = await notificationReportService.getNotificationLogs(token);
            setNotificationLogs(data.content || []);
        } catch (error) {
            // Error handling ignored per requirements, but we shouldn't use console.log
        }
    };

    const loadExportLogs = async () => {
        if (!token) return;
        try {
            const data = await notificationReportService.getExportLogs(token);
            setExportLogs(data.content || []);
        } catch (error) {
            //
        }
    };

    const loadSchedulerLogs = async () => {
        if (!token) return;
        try {
            const data = await notificationReportService.getSchedulerLogs(token);
            setSchedulerLogs(data.content || []);
        } catch (error) {
            //
        }
    };

    const loadSubscribers = async () => {
        if (!token) return;
        try {
            const data = await notificationReportService.getSubscribers(token);
            setSubscribers(data);
        } catch (error) {
            //
        }
    };

    const loadSummary = async () => {
        if (!token) return;
        try {
            const data = await notificationReportService.getReportSummary(token);
            setSummary(data);
        } catch (error) {
            //
        }
    };

    // Actions
    const handleSendNotification = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!token) return;
        try {
            const request = { ...sendReq, to: [toEmail] };
            await notificationReportService.sendNotification(token, request);
            setSendReq({ to: [], subject: '', body: '', html: false });
            setToEmail('');
            loadNotificationLogs();
        } catch (error) {
            //
        }
    };

    const handleRetryNotification = async (id: string) => {
        if (!token) return;
        try {
            await notificationReportService.retryNotification(token, id);
            loadNotificationLogs();
        } catch (error) {
            //
        }
    };

    const handleTriggerJob = async (jobName: string) => {
        if (!token) return;
        try {
            await notificationReportService.triggerScheduler(token, jobName);
            setTimeout(loadSchedulerLogs, 1000);
        } catch (error) {
            //
        }
    };

    const handleImportSubscribers = async (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!token) return;
        if (e.target.files && e.target.files.length > 0) {
            try {
                await notificationReportService.importSubscribers(token, e.target.files[0]);
                loadSubscribers();
                if (fileInputRef.current) fileInputRef.current.value = '';
            } catch (error) {
                //
            }
        }
    };

    const handleDownloadReport = async () => {
        if (!token) return;
        try {
            await notificationReportService.downloadFundRequestsBlob(token);
            setTimeout(loadExportLogs, 2000);
        } catch (error) {
            //
        }
    };

    const handleDownloadTemplate = async () => {
        if (!token) return;
        try {
            await notificationReportService.downloadTemplateBlob(token);
        } catch (error) {
            //
        }
    };

    return (
        <div className="nr-container">
            <div className="nr-header">
                <h1 className="nr-title">Notification & Report Center</h1>
                <p className="nr-subtitle">Pusat kendali notifikasi email, scheduler, dan laporan sistem.</p>
            </div>

            <div className="nr-tabs">
                {hasPermission('notification.read') && (
                    <button
                        className={`nr-tab ${activeTab === 'notifications' ? 'active' : ''}`}
                        onClick={() => setActiveTab('notifications')}
                    >
                        Notifikasi
                    </button>
                )}
                {hasPermission('report.read') && (
                    <button
                        className={`nr-tab ${activeTab === 'reports' ? 'active' : ''}`}
                        onClick={() => setActiveTab('reports')}
                    >
                        Laporan
                    </button>
                )}
                {hasPermission('report.read') && (
                    <button
                        className={`nr-tab ${activeTab === 'subscribers' ? 'active' : ''}`}
                        onClick={() => setActiveTab('subscribers')}
                    >
                        Subscribers
                    </button>
                )}
                {hasPermission('scheduler.log.read') && (
                    <button
                        className={`nr-tab ${activeTab === 'scheduler' ? 'active' : ''}`}
                        onClick={() => setActiveTab('scheduler')}
                    >
                        Scheduler
                    </button>
                )}
            </div>

            {/* TAB: NOTIFICATIONS */}
            {activeTab === 'notifications' && (
                <div>
                    {hasPermission('notification.send') && (
                        <div className="nr-card">
                            <h2 className="nr-card-title">Kirim Notifikasi Manual</h2>
                            <form onSubmit={handleSendNotification}>
                                <div className="nr-form-group">
                                    <label className="nr-form-label">Email Penerima</label>
                                    <input
                                        type="email"
                                        className="nr-form-input"
                                        value={toEmail}
                                        onChange={(e) => setToEmail(e.target.value)}
                                        required
                                    />
                                </div>
                                <div className="nr-form-group">
                                    <label className="nr-form-label">Subjek</label>
                                    <input
                                        type="text"
                                        className="nr-form-input"
                                        value={sendReq.subject}
                                        onChange={(e) => setSendReq({ ...sendReq, subject: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="nr-form-group">
                                    <label className="nr-form-label">Pesan</label>
                                    <textarea
                                        className="nr-form-input"
                                        rows={4}
                                        value={sendReq.body}
                                        onChange={(e) => setSendReq({ ...sendReq, body: e.target.value })}
                                        required
                                    />
                                </div>
                                <button type="submit" className="nr-btn nr-btn-primary">
                                    Kirim Notifikasi
                                </button>
                            </form>
                        </div>
                    )}

                    {hasPermission('notification.read') && (
                        <div className="nr-card">
                            <h2 className="nr-card-title">Riwayat Notifikasi</h2>
                            <div className="nr-table-container">
                                <table className="nr-table">
                                    <thead>
                                        <tr>
                                            <th>Waktu</th>
                                            <th>Penerima</th>
                                            <th>Subjek</th>
                                            <th>Status</th>
                                            <th>Aksi</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {notificationLogs.map(log => (
                                            <tr key={log.id}>
                                                <td>{log.createdAt ? new Date(log.createdAt).toLocaleString() : '-'}</td>
                                                <td>{log.toRecipients.join(', ')}</td>
                                                <td>{log.subject}</td>
                                                <td>
                                                    <span className={`nr-badge ${log.status === 'SENT' ? 'success' : log.status === 'FAILED' ? 'danger' : 'warning'}`}>
                                                        {log.status}
                                                    </span>
                                                </td>
                                                <td>
                                                    {log.status === 'FAILED' && hasPermission('notification.retry') && (
                                                        <button
                                                            className="nr-btn nr-btn-secondary"
                                                            onClick={() => handleRetryNotification(log.id)}
                                                        >
                                                            Retry
                                                        </button>
                                                    )}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    )}
                </div>
            )}

            {/* TAB: REPORTS */}
            {activeTab === 'reports' && hasPermission('report.read') && (
                <div>
                    {summary && (
                        <div className="nr-summary-grid">
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.totalRequests}</div>
                                <div className="nr-summary-label">Total Pengajuan</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.totalPending}</div>
                                <div className="nr-summary-label">Pending</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.totalApproved}</div>
                                <div className="nr-summary-label">Approved</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">
                                    Rp {(summary.totalAmount || 0).toLocaleString('id-ID')}
                                </div>
                                <div className="nr-summary-label">Total Dana Disetujui</div>
                            </div>
                        </div>
                    )}

                    <div className="nr-card">
                        <div className="nr-card-header">
                            <h2 className="nr-card-title-no-margin">Laporan Operasional</h2>
                            {hasPermission('report.export') && (
                                <button className="nr-btn nr-btn-primary" onClick={handleDownloadReport}>
                                    Download Fund Requests
                                </button>
                            )}
                        </div>

                        <div className="nr-table-container">
                            <table className="nr-table">
                                <thead>
                                    <tr>
                                        <th>Waktu Export</th>
                                        <th>File</th>
                                        <th>Status</th>
                                        <th>Records</th>
                                        <th>Size (Bytes)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {exportLogs.map(log => (
                                        <tr key={log.id}>
                                            <td>{log.createdAt ? new Date(log.createdAt).toLocaleString() : '-'}</td>
                                            <td>{log.filename}</td>
                                            <td>
                                                <span className={`nr-badge ${log.status === 'COMPLETED' ? 'success' : log.status === 'FAILED' ? 'danger' : 'warning'}`}>
                                                    {log.status}
                                                </span>
                                            </td>
                                            <td>{log.recordCount ?? '-'}</td>
                                            <td>{log.fileSize ?? '-'}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}

            {/* TAB: SUBSCRIBERS */}
            {activeTab === 'subscribers' && hasPermission('report.read') && (
                <div className="nr-card">
                    <div className="nr-card-header">
                        <h2 className="nr-card-title-no-margin">Daftar Subscriber Laporan</h2>
                        
                        {hasPermission('report.import') && (
                            <div className="nr-action-group">
                                <button className="nr-btn nr-btn-secondary" onClick={handleDownloadTemplate}>
                                    Unduh Template
                                </button>
                                <input
                                    type="file"
                                    accept=".xlsx"
                                    className="nr-hidden-input"
                                    ref={fileInputRef}
                                    onChange={handleImportSubscribers}
                                />
                                <button className="nr-btn nr-btn-primary" onClick={() => fileInputRef.current?.click()}>
                                    Import Excel
                                </button>
                            </div>
                        )}
                    </div>

                    <div className="nr-table-container">
                        <table className="nr-table">
                            <thead>
                                <tr>
                                    <th>Email</th>
                                    <th>Nama</th>
                                    <th>Tipe Laporan</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {subscribers.map(sub => (
                                    <tr key={sub.id}>
                                        <td>{sub.email}</td>
                                        <td>{sub.name || '-'}</td>
                                        <td>{sub.reportType}</td>
                                        <td>
                                            <span className={`nr-badge ${sub.active ? 'success' : 'danger'}`}>
                                                {sub.active ? 'Aktif' : 'Non-Aktif'}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* TAB: SCHEDULER */}
            {activeTab === 'scheduler' && hasPermission('scheduler.log.read') && (
                <div className="nr-card">
                    <div className="nr-card-header">
                        <h2 className="nr-card-title-no-margin">Scheduler Logs</h2>
                        <div className="nr-action-group">
                            <button className="nr-btn nr-btn-secondary" onClick={() => handleTriggerJob('Health Ping')}>
                                Ping
                            </button>
                            <button className="nr-btn nr-btn-secondary" onClick={() => handleTriggerJob('Retry Failed Email')}>
                                Retry Emails
                            </button>
                            <button className="nr-btn nr-btn-primary" onClick={() => handleTriggerJob('Weekly Report Reminder')}>
                                Run Weekly Report
                            </button>
                        </div>
                    </div>

                    <div className="nr-table-container">
                        <table className="nr-table">
                            <thead>
                                <tr>
                                    <th>Waktu Eksekusi</th>
                                    <th>Job Name</th>
                                    <th>Trigger</th>
                                    <th>Status</th>
                                    <th>Pesan</th>
                                </tr>
                            </thead>
                            <tbody>
                                {schedulerLogs.map(log => (
                                    <tr key={log.id}>
                                        <td>{log.startedAt ? new Date(log.startedAt).toLocaleString() : '-'}</td>
                                        <td>{log.jobName}</td>
                                        <td>{log.triggerType}</td>
                                        <td>
                                            <span className={`nr-badge ${log.status === 'SUCCESS' ? 'success' : 'danger'}`}>
                                                {log.status}
                                            </span>
                                        </td>
                                        <td>{log.message || log.errorMessage || '-'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
};
