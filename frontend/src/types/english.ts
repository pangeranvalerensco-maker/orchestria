export type EnglishActivityStatus = 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'CANCELLED';
export type EnglishDepositStatus = 'SUBMITTED' | 'VERIFIED' | 'REJECTED' | 'MISSED';

export interface EnglishDeposit {
  id: string;
  activityId: string;
  activityTitle: string;
  memberId: number;
  memberName: string;
  memberEmail: string;
  topic: string;
  evidenceUrl: string;
  submissionNote: string;
  status: EnglishDepositStatus;
  score?: number;
  verificationNote?: string;
  submittedAt: string;
  verifiedByEmail?: string;
  verifiedAt?: string;
}

export interface EnglishActivity {
  id: string;
  title: string;
  activityDate: string;
  startTime: string;
  endTime: string;
  topic: string;
  description: string;
  status: EnglishActivityStatus;
  createdByEmail: string;
  createdAt: string;
  updatedAt: string;
  deposits?: EnglishDeposit[];
}

export interface EnglishActivityRequest {
  title: string;
  activityDate: string;
  startTime: string;
  endTime: string;
  topic: string;
  description?: string;
  status: EnglishActivityStatus;
}

export interface EnglishDepositRequest {
  activityId: string;
  topic: string;
  evidenceUrl: string;
  submissionNote?: string;
}

export interface EnglishDepositVerificationRequest {
  decision: 'VERIFIED' | 'REJECTED';
  score?: number;
  verificationNote: string;
}

export interface EnglishMemberSummary {
  memberId: number;
  memberName: string;
  submittedCount: number;
  verifiedCount: number;
  rejectedCount: number;
  averageScore: number;
}

export interface EnglishReportSummary {
  totalActivities: number;
  publishedActivities: number;
  completedActivities: number;
  totalDeposits: number;
  submittedDeposits: number;
  verifiedDeposits: number;
  rejectedDeposits: number;
  missedDeposits: number;
  averageScore: number;
  memberSummary: EnglishMemberSummary[];
}
