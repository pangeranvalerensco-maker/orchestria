import React, { useState, useEffect, useRef } from 'react';
import './NotificationReportCenterPage.css';
import { notificationReportService } from '../../services/notificationReportService';
import type {
    NotificationLog,
    ReportExportLog,
    ScheduledJobLog,
    ReportSubscriber,
    NotificationSendRequest,
    ReportSummary,
    ImportSummary
} from '../../types/notificationReport';
import { useAuth } from '../../auth/useAuth';

export const NotificationReportCenterPage: React.FC = () => {
    const { hasPermission, token } = useAuth();
    const canOpenNotifications =
        hasPermission('notification.read') ||
        hasPermission('notification.send') ||
        hasPermission('notification.retry');

    const canOpenReports =
        hasPermission('report.read') ||
        hasPermission('report.export');

    const canOpenSubscribers =
        hasPermission('report.read') ||
        hasPermission('report.import');

    const canOpenScheduler =
        hasPermission('scheduler.log.read');

    const defaultTab = canOpenNotifications ? 'notifications' 
                     : canOpenReports ? 'reports' 
                     : canOpenSubscribers ? 'subscribers'
                     : canOpenScheduler ? 'scheduler' : 'notifications';

    const [activeTab, setActiveTab] = useState<'notifications' | 'reports' | 'scheduler' | 'subscribers'>(defaultTab);
    const fileInputRef = useRef<HTMLInputElement>(null);

    // States
    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [notificationLogs, setNotificationLogs] = useState<NotificationLog[]>([]);
    const [exportLogs, setExportLogs] = useState<ReportExportLog[]>([]);
    const [schedulerLogs, setSchedulerLogs] = useState<ScheduledJobLog[]>([]);
    const [subscribers, setSubscribers] = useState<ReportSubscriber[]>([]);
    const [summary, setSummary] = useState<ReportSummary | null>(null);
    const [importSummary, setImportSummary] = useState<ImportSummary | null>(null);
    const [importErrorMsg, setImportErrorMsg] = useState<string>('');
    const [logStatusFilter, setLogStatusFilter] = useState<string>('');

    // Form
    const [sendReq, setSendReq] = useState<NotificationSendRequest>({
        to: [],
        subject: '',
        body: '',
        html: false
    });
    const [toEmail, setToEmail] = useState('');

    useEffect(() => {
        setErrorMessage("");
        setSuccessMessage("");
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
    }, [activeTab, logStatusFilter, token, hasPermission]);

    const loadNotificationLogs = async () => {
        if (!token || !hasPermission('notification.read')) return;
        try {
            const data = await notificationReportService.getNotificationLogs(token, logStatusFilter || undefined);
            setNotificationLogs(data.content || []);
        } catch (error: unknown) {
            setErrorMessage(error instanceof Error ? error.message : "Gagal memuat log notifikasi");
        }
    };

    const loadExportLogs = async () => {
        if (!token || !hasPermission('report.read')) return;
        try {
            const data = await notificationReportService.getExportLogs(token);
            setExportLogs(data.content || []);
        } catch (error: unknown) {
            setErrorMessage(error instanceof Error ? error.message : "Gagal memuat log ekspor");
        }
    };

    const loadSchedulerLogs = async () => {
        if (!token || !hasPermission('scheduler.log.read')) return;
        try {
            const data = await notificationReportService.getSchedulerLogs(token);
            setSchedulerLogs(data.content || []);
        } catch (error: unknown) {
            setErrorMessage(error instanceof Error ? error.message : "Gagal memuat log scheduler");
        }
    };

    const loadSubscribers = async () => {
        if (!token || !hasPermission('report.read')) return;
        try {
            const data = await notificationReportService.getSubscribers(token);
            setSubscribers(data);
        } catch (error: unknown) {
            setErrorMessage(error instanceof Error ? error.message : "Gagal memuat subscribers");
        }
    };

    const loadSummary = async () => {
        if (!token || !hasPermission('report.read')) return;
        try {
            const data = await notificationReportService.getReportSummary(token);
            setSummary(data);
        } catch (error: unknown) {
            setErrorMessage(error instanceof Error ? error.message : "Gagal memuat summary");
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
            setSuccessMessage("Notifikasi berhasil dikirim.");
            loadNotificationLogs();
        } catch (error: unknown) {
            setErrorMessage(error instanceof Error ? error.message : "Gagal mengirim notifikasi");
        }
    };

    const handleRetryNotification = async (id: string) => {
        if (!token) return;
        try {
            await notificationReportService.retryNotification(token, id);
            setSuccessMessage("Notifikasi sedang dicoba ulang.");
            loadNotificationLogs();
        } catch (error: unknown) {
            setErrorMessage(error instanceof Error ? error.message : "Gagal retry notifikasi");
        }
    };

    const handleTriggerJob = async (jobName: string) => {
        if (!token) return;
        try {
            await notificationReportService.triggerScheduler(token, jobName);
            setSuccessMessage(`Job ${jobName} berhasil ditrigger.`);
            setTimeout(loadSchedulerLogs, 1000);
        } catch (error: unknown) {
            setErrorMessage(error instanceof Error ? error.message : `Gagal trigger job ${jobName}`);
        }
    };

    const handleImportSubscribers = async (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!token) return;
        setImportErrorMsg('');
        setImportSummary(null);
        
        if (e.target.files && e.target.files.length > 0) {
            try {
                const res = await notificationReportService.importSubscribers(token, e.target.files[0]);
                setImportSummary(res);
                loadSubscribers();
                if (fileInputRef.current) fileInputRef.current.value = '';
            } catch (error: unknown) {
                if (error instanceof Error) {
                    setImportErrorMsg(error.message);
                } else {
                    setImportErrorMsg('Gagal import subscribers');
                }
                if (fileInputRef.current) fileInputRef.current.value = '';
            }
        }
    };

    const handleDownloadReport = async () => {
        if (!token) return;
        try {
            await notificationReportService.downloadFundRequestsBlob(token);
            setSuccessMessage("Laporan sedang diunduh.");
            setTimeout(loadExportLogs, 2000);
        } catch (error: unknown) {
            setErrorMessage(error instanceof Error ? error.message : "Gagal mengunduh laporan");
        }
    };

    const handleDownloadTemplate = async () => {
        if (!token) return;
        try {
            await notificationReportService.downloadTemplateBlob(token);
            setSuccessMessage("Template berhasil diunduh.");
        } catch (error: unknown) {
            setErrorMessage(error instanceof Error ? error.message : "Gagal mengunduh template");
        }
    };

    return (
        <div className="nr-container">
            <div className="nr-header">
                <h1 className="nr-title">Notification & Report Center</h1>
                <p className="nr-subtitle">Pusat kendali notifikasi email, scheduler, dan laporan sistem.</p>
            </div>

            {errorMessage && (
                <div className="nr-alert nr-alert-danger">
                    {errorMessage}
                </div>
            )}
            
            {successMessage && (
                <div className="nr-alert nr-alert-success">
                    {successMessage}
                </div>
            )}

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
                            <div className="nr-card-header">
                                <h2 className="nr-card-title-no-margin">Riwayat Notifikasi</h2>
                                <select 
                                    className="nr-form-input"
                                    value={logStatusFilter} 
                                    onChange={e => setLogStatusFilter(e.target.value)}
                                >
                                    <option value="">Semua Status</option>
                                    <option value="PENDING">Pending</option>
                                    <option value="SENT">Sent</option>
                                    <option value="FAILED">Failed</option>
                                </select>
                            </div>
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
                                                    {(log.status === 'FAILED' || log.status === 'PENDING') && hasPermission('notification.retry') && (
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
                        <>
                        <div className="nr-summary-grid">
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.totalRequests}</div>
                                <div className="nr-summary-label">Total Pengajuan</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.pendingApprovalCount}</div>
                                <div className="nr-summary-label">Pending Approval</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.readyForDisbursementCount}</div>
                                <div className="nr-summary-label">Ready For Disbursement</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.disbursedCount}</div>
                                <div className="nr-summary-label">Disbursed</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.completedCount}</div>
                                <div className="nr-summary-label">Completed</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">
                                    Rp {(summary.totalRequestedAmount || 0).toLocaleString('id-ID')}
                                </div>
                                <div className="nr-summary-label">Total Dana Requested</div>
                            </div>
                        </div>
                        <div className="nr-summary-grid">
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.notificationPendingCount}</div>
                                <div className="nr-summary-label">Notification Pending</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.notificationSentCount}</div>
                                <div className="nr-summary-label">Notification Sent</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.notificationFailedCount}</div>
                                <div className="nr-summary-label">Notification Failed</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.schedulerSuccessCount}</div>
                                <div className="nr-summary-label">Scheduler Success</div>
                            </div>
                            <div className="nr-summary-item">
                                <div className="nr-summary-value">{summary.schedulerFailedCount}</div>
                                <div className="nr-summary-label">Scheduler Failed</div>
                            </div>
                        </div>
                        </>
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
                                                <span className={`nr-badge ${log.status === 'SUCCESS' ? 'success' : log.status === 'FAILED' ? 'danger' : 'warning'}`}>
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
                    
                    {importErrorMsg && (
                        <div className="nr-alert nr-alert-danger">
                            {importErrorMsg}
                        </div>
                    )}
                    
                    {importSummary && (
                        <div className="nr-alert nr-alert-success">
                            <h4>Import Berhasil</h4>
                            <ul>
                                <li>Total Baris: {importSummary.totalRows}</li>
                                <li>Berhasil Import Baru: {importSummary.importedRows}</li>
                                <li>Berhasil Update: {importSummary.updatedRows}</li>
                                <li>Gagal: {importSummary.failedRows}</li>
                            </ul>
                            {importSummary.errors.length > 0 && (
                                <div>
                                    <strong>Detail Error:</strong>
                                    <ul>
                                        {importSummary.errors.map((err, idx) => (
                                            <li key={idx}>Baris {err.rowNumber}: {err.message}</li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>
                    )}

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
