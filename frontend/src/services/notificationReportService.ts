import type { NotificationLog, ReportExportLog, ScheduledJobLog, ReportSubscriber, NotificationSendRequest, PageResponse, ReportSummary } from '../types/notificationReport';

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
        const res = await fetch(`${BASE_URL}/notifications/${id}/retry`, {
            method: 'POST',
            headers: getHeaders(token)
        });
        if (!res.ok) throw new Error('Gagal retry notifikasi');
    },

    getNotificationLogs: async (token: string, page = 0, size = 10): Promise<PageResponse<NotificationLog>> => {
        const res = await fetch(`${BASE_URL}/notifications/logs?page=${page}&size=${size}`, {
            headers: getHeaders(token)
        });
        if (!res.ok) throw new Error('Gagal mengambil log notifikasi');
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

    importSubscribers: async (token: string, file: File): Promise<void> => {
        const formData = new FormData();
        formData.append('file', file);
        
        const res = await fetch(`${BASE_URL}/reports/subscribers/import`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
                // Jangan set Content-Type untuk FormData agar browser yang urus boundary
            },
            body: formData
        });
        if (!res.ok) throw new Error('Gagal import subscribers');
    },

    downloadFundRequests: (token: string) => {
        window.open(`${BASE_URL}/reports/fund-requests.xlsx?access_token=${token}`, '_blank');
        // Catatan: implementasi asli biasanya butuh download via fetch lalu buat object URL 
        // karena butuh Auth header. Disini kita asumsikan simple fetch approach.
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
        a.download = `fund-requests.xlsx`;
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
