export type ScheduleStatus = "DRAFT" | "PUBLISHED" | "COMPLETED" | "CANCELLED";

export type AttendanceStatus = "PENDING" | "PRESENT" | "ABSENT" | "EXCUSED";

export type PointRecordType = "REWARD" | "VIOLATION";

export interface CleanlinessAssignment {
  id: string;
  memberId: number;
  memberName: string;
  memberEmail: string;
  attendanceStatus: AttendanceStatus;
  notes?: string;
  proofImageUrl?: string;
  recordedAt?: string;
}

export interface CleanlinessSchedule {
  id: string;
  title: string;
  dutyDate: string; // YYYY-MM-DD
  startTime: string; // HH:mm:ss
  endTime: string; // HH:mm:ss
  location: string;
  description?: string;
  status: ScheduleStatus;
  createdByEmail: string;
  createdAt: string;
  assignments: CleanlinessAssignment[];
}

export interface CleanlinessPointRecord {
  id: string;
  memberId: number;
  memberName: string;
  type: PointRecordType;
  pointValue: number;
  reason: string;
  scheduleId?: string;
  recordedByEmail: string;
  recordedAt: string;
}

export interface MemberPointLeaderboard {
  memberId: number;
  memberName: string;
  totalRewardPoints: number;
  totalViolationPoints: number;
  netPoints: number;
}

export interface ReportSummary {
  totalSchedules: number;
  completedSchedules: number;
  totalAttendances: number;
  presentCount: number;
  absentCount: number;
  excusedCount: number;
  totalRewardPoints: number;
  totalViolationPoints: number;
  netPoints: number;
  memberLeaderboard: MemberPointLeaderboard[];
}

export interface ScheduleRequest {
  title: string;
  dutyDate: string;
  startTime: string;
  endTime: string;
  location: string;
  description?: string;
  status: ScheduleStatus;
  memberIds: number[];
}

export interface AttendanceRequest {
  status: AttendanceStatus;
  notes?: string;
  proofImageUrl?: string;
}

export interface PointRequest {
  memberId: number;
  scheduleId?: string;
  type: PointRecordType;
  pointValue: number;
  reason: string;
}
