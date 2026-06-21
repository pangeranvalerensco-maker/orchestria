import { apiRequest } from "../api/http";
import type {
  EnglishActivity,
  EnglishActivityRequest,
  EnglishDeposit,
  EnglishDepositRequest,
  EnglishDepositVerificationRequest,
  EnglishReportSummary
} from "../types/english";

export const englishService = {
  // Activity
  createActivity: async (token: string, data: EnglishActivityRequest) => {
    const response = await apiRequest<EnglishActivity>("/api/organization/english/activities", { method: "POST", body: JSON.stringify(data) }, token);
    return response.data;
  },

  getAllActivities: async (token: string) => {
    const response = await apiRequest<EnglishActivity[]>("/api/organization/english/activities", { method: "GET" }, token);
    return response.data;
  },

  getActivity: async (token: string, id: string) => {
    const response = await apiRequest<EnglishActivity>(`/api/organization/english/activities/${id}`, { method: "GET" }, token);
    return response.data;
  },

  updateActivity: async (token: string, id: string, data: EnglishActivityRequest) => {
    const response = await apiRequest<EnglishActivity>(`/api/organization/english/activities/${id}`, { method: "PUT", body: JSON.stringify(data) }, token);
    return response.data;
  },

  deleteActivity: async (token: string, id: string) => {
    const response = await apiRequest<void>(`/api/organization/english/activities/${id}`, { method: "DELETE" }, token);
    return response.data;
  },

  // Deposit
  createDeposit: async (token: string, data: EnglishDepositRequest) => {
    const response = await apiRequest<EnglishDeposit>("/api/organization/english/deposits", { method: "POST", body: JSON.stringify(data) }, token);
    return response.data;
  },

  getAllDeposits: async (token: string) => {
    const response = await apiRequest<EnglishDeposit[]>("/api/organization/english/deposits", { method: "GET" }, token);
    return response.data;
  },

  getMyDeposits: async (token: string) => {
    const response = await apiRequest<EnglishDeposit[]>("/api/organization/english/deposits/my", { method: "GET" }, token);
    return response.data;
  },

  getDeposit: async (token: string, id: string) => {
    const response = await apiRequest<EnglishDeposit>(`/api/organization/english/deposits/${id}`, { method: "GET" }, token);
    return response.data;
  },

  verifyDeposit: async (token: string, id: string, data: EnglishDepositVerificationRequest) => {
    const response = await apiRequest<EnglishDeposit>(`/api/organization/english/deposits/${id}/verify`, { method: "POST", body: JSON.stringify(data) }, token);
    return response.data;
  },

  // Report
  getReportSummary: async (token: string) => {
    const response = await apiRequest<EnglishReportSummary>("/api/organization/english/reports/summary", { method: "GET" }, token);
    return response.data;
  }
};
