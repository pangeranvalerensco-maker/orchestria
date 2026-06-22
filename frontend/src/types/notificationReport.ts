export interface NotificationLog {
    id: string;
    toRecipients: string[];
    subject: string;
    body: string;
    html: boolean;
    status: 'PENDING' | 'SENT' | 'FAILED';
    attemptCount: number;
    lastError?: string;
    nextRetryAt?: string;
    createdByEmail?: string;
    createdAt?: string;
    lastAttemptAt?: string;
    sentAt?: string;
}

export interface ReportExportLog {
    id: string;
    reportType: string;
    filename: string;
    status: 'PROCESSING' | 'COMPLETED' | 'FAILED';
    recordCount?: number;
    fileSize?: number;
    errorMessage?: string;
    createdByEmail: string;
    createdAt: string;
    finishedAt?: string;
}

export interface ScheduledJobLog {
    id: string;
    jobName: string;
    triggerType: 'CRON' | 'FIXED_RATE' | 'FIXED_DELAY' | 'MANUAL';
    status: 'SUCCESS' | 'FAILED';
    message?: string;
    errorMessage?: string;
    startedAt: string;
    finishedAt?: string;
}

export interface ReportSubscriber {
    id: number;
    email: string;
    name?: string;
    reportType: 'WEEKLY' | 'MONTHLY';
    active: boolean;
    createdAt?: string;
}

export interface NotificationSendRequest {
    to: string[];
    cc?: string[];
    bcc?: string[];
    subject: string;
    body: string;
    html: boolean;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface ReportSummary {
    totalRequests: number;
    totalPending: number;
    totalApproved: number;
    totalAmount: number;
}
