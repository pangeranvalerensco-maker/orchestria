import { apiRequest } from "../api/http";
import type {
  AttendanceRequest,
  CleanlinessAssignment,
  CleanlinessPointRecord,
  CleanlinessSchedule,
  PointRequest,
  ReportSummary,
  ScheduleRequest,
} from "../types/cleanliness";

// --- SCHEDULES ---

export const getSchedules = async (token: string): Promise<CleanlinessSchedule[]> => {
  const response = await apiRequest<CleanlinessSchedule[]>("/api/organization/cleanliness/schedules", { method: "GET" }, token);
  return response.data;
};

export const getMySchedules = async (token: string): Promise<CleanlinessSchedule[]> => {
  const response = await apiRequest<CleanlinessSchedule[]>("/api/organization/cleanliness/schedules/my", { method: "GET" }, token);
  return response.data;
};

export const getSchedule = async (id: string, token: string): Promise<CleanlinessSchedule> => {
  const response = await apiRequest<CleanlinessSchedule>(`/api/organization/cleanliness/schedules/${id}`, { method: "GET" }, token);
  return response.data;
};

export const createSchedule = async (request: ScheduleRequest, token: string): Promise<CleanlinessSchedule> => {
  const response = await apiRequest<CleanlinessSchedule>("/api/organization/cleanliness/schedules", {
    method: "POST",
    body: JSON.stringify(request),
  }, token);
  return response.data;
};

export const updateSchedule = async (id: string, request: ScheduleRequest, token: string): Promise<CleanlinessSchedule> => {
  const response = await apiRequest<CleanlinessSchedule>(`/api/organization/cleanliness/schedules/${id}`, {
    method: "PUT",
    body: JSON.stringify(request),
  }, token);
  return response.data;
};

export const deleteSchedule = async (id: string, token: string): Promise<void> => {
  await apiRequest<void>(`/api/organization/cleanliness/schedules/${id}`, { method: "DELETE" }, token);
};

// --- ATTENDANCE ---

export const recordAttendance = async (
  assignmentId: string,
  request: AttendanceRequest,
  token: string
): Promise<CleanlinessAssignment> => {
  const response = await apiRequest<CleanlinessAssignment>(`/api/organization/cleanliness/assignments/${assignmentId}/attendance`, {
    method: "POST",
    body: JSON.stringify(request),
  }, token);
  return response.data;
};

export const getAllAttendances = async (
  token: string,
  scheduleId?: string,
  memberId?: number,
  status?: string
): Promise<CleanlinessAssignment[]> => {
  const params = new URLSearchParams();
  if (scheduleId) params.append("scheduleId", scheduleId);
  if (memberId) params.append("memberId", memberId.toString());
  if (status) params.append("status", status);

  const response = await apiRequest<CleanlinessAssignment[]>(`/api/organization/cleanliness/attendances?${params.toString()}`, { method: "GET" }, token);
  return response.data;
};

// --- POINTS ---

export const createPointRecord = async (request: PointRequest, token: string): Promise<CleanlinessPointRecord> => {
  const response = await apiRequest<CleanlinessPointRecord>("/api/organization/cleanliness/points", {
    method: "POST",
    body: JSON.stringify(request),
  }, token);
  return response.data;
};

export const getAllPoints = async (token: string): Promise<CleanlinessPointRecord[]> => {
  const response = await apiRequest<CleanlinessPointRecord[]>("/api/organization/cleanliness/points", { method: "GET" }, token);
  return response.data;
};

export const getMyPoints = async (token: string): Promise<CleanlinessPointRecord[]> => {
  const response = await apiRequest<CleanlinessPointRecord[]>("/api/organization/cleanliness/points/my", { method: "GET" }, token);
  return response.data;
};

// --- REPORTS ---

export const getReportSummary = async (token: string): Promise<ReportSummary> => {
  const response = await apiRequest<ReportSummary>("/api/organization/cleanliness/reports/summary", { method: "GET" }, token);
  return response.data;
};
