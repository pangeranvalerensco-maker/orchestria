import type { 
    NotificationLog, 
    ReportExportLog, 
    ScheduledJobLog, 
    ReportSubscriber, 
    NotificationSendRequest, 
    PageResponse, 
    ReportSummary,
    ImportSummary
} from '../types/notificationReport';

const BASE_URL = 'http://localhost:8000/api';

const getHeaders = (token: string) => {
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };
};

export const notificationReportService = {
    // Notifications
    sendNotification: async (token: string, request: NotificationSendRequest): Promise<void> => {
        const res = await fetch(`${BASE_URL}/notifications/email`, {
            method: 'POST',
            headers: getHeaders(token),
            body: JSON.stringify(request)
        });
        if (!res.ok) throw new Error('Gagal mengirim notifikasi');
    },

    retryNotification: async (token: string, id: string): Promise<void> => {
        const res = await fetch(`${BASE_URL}/notifications/logs/${id}/retry`, {
            method: 'POST',
            headers: getHeaders(token)
        });
        if (!res.ok) throw new Error('Gagal retry notifikasi');
    },

    getNotificationLogs: async (token: string, status?: string, page = 0, size = 10): Promise<PageResponse<NotificationLog>> => {
        const query = new URLSearchParams({ page: page.toString(), size: size.toString() });
        if (status) query.append('status', status);
        
        const res = await fetch(`${BASE_URL}/notifications/logs?${query.toString()}`, {
            headers: getHeaders(token)
        });
        if (!res.ok) throw new Error('Gagal mengambil log notifikasi');
        return res.json();
    },

    getNotificationLogDetail: async (token: string, id: string): Promise<NotificationLog> => {
        const res = await fetch(`${BASE_URL}/notifications/logs/${id}`, {
            headers: getHeaders(token)
        });
        if (!res.ok) throw new Error('Gagal mengambil detail log notifikasi');
        return res.json();
    },

    // Scheduler
    getSchedulerLogs: async (token: string, page = 0, size = 10): Promise<PageResponse<ScheduledJobLog>> => {
        const res = await fetch(`${BASE_URL}/scheduler/logs?page=${page}&size=${size}`, {
            headers: getHeaders(token)
        });
        if (!res.ok) throw new Error('Gagal mengambil log scheduler');
        return res.json();
    },

    triggerScheduler: async (token: string, jobName: string): Promise<void> => {
        const res = await fetch(`${BASE_URL}/scheduler/${jobName}/trigger`, {
            method: 'POST',
            headers: getHeaders(token)
        });
        if (!res.ok) throw new Error('Gagal trigger scheduler');
    },

    // Reports
    getReportSummary: async (token: string): Promise<ReportSummary> => {
        const res = await fetch(`${BASE_URL}/reports/summary`, {
            headers: getHeaders(token)
        });
        if (!res.ok) throw new Error('Gagal mengambil summary laporan');
        return res.json();
    },

    getSubscribers: async (token: string): Promise<ReportSubscriber[]> => {
        const res = await fetch(`${BASE_URL}/reports/subscribers`, {
            headers: getHeaders(token)
        });
        if (!res.ok) throw new Error('Gagal mengambil subscribers');
        return res.json();
    },

    getExportLogs: async (token: string, page = 0, size = 10): Promise<PageResponse<ReportExportLog>> => {
        const res = await fetch(`${BASE_URL}/reports/exports?page=${page}&size=${size}`, {
            headers: getHeaders(token)
        });
        if (!res.ok) throw new Error('Gagal mengambil log export');
        return res.json();
    },

    importSubscribers: async (token: string, file: File): Promise<ImportSummary> => {
        const formData = new FormData();
        formData.append('file', file);
        
        const res = await fetch(`${BASE_URL}/reports/subscribers/import`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });
        if (!res.ok) {
            const err = await res.text();
            throw new Error(err || 'Gagal import subscribers');
        }
        return res.json();
    },
    
    downloadFundRequestsBlob: async (token: string): Promise<void> => {
        const res = await fetch(`${BASE_URL}/reports/fund-requests.xlsx`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (!res.ok) throw new Error('Gagal download laporan');
        
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const disposition = res.headers.get('Content-Disposition');
        let filename = 'fund-requests.xlsx';
        if (disposition && disposition.indexOf('attachment') !== -1) {
            const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = filenameRegex.exec(disposition);
            if (matches != null && matches[1]) { 
                filename = matches[1].replace(/['"]/g, '');
            }
        }
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
    },
    
    downloadTemplateBlob: async (token: string): Promise<void> => {
        const res = await fetch(`${BASE_URL}/reports/subscribers/template.xlsx`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (!res.ok) throw new Error('Gagal download template');
        
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `subscribers-template.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
    }
};
