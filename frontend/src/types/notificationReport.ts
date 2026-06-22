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
    status: 'PROCESSING' | 'SUCCESS' | 'FAILED';
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
    reportType: 'WEEKLY_REQUEST_REPORT' | 'FUND_REQUEST';
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
    totalRequestedAmount: number;
    requestCountByStatus: Record<string, number>;
    pendingApprovalCount: number;
    readyForDisbursementCount: number;
    disbursedCount: number;
    settlementPendingCount: number;
    completedCount: number;
    notificationPendingCount: number;
    notificationSentCount: number;
    notificationFailedCount: number;
    schedulerSuccessCount: number;
    schedulerFailedCount: number;
}

export interface ImportError {
    rowNumber: number;
    message: string;
}

export interface ImportSummary {
    totalRows: number;
    importedRows: number;
    updatedRows: number;
    failedRows: number;
    errors: ImportError[];
}
