import { apiRequest } from '../api/http';
import type { DivisionTask, DivisionTaskRequest, DivisionTaskEvidence, DivisionTaskEvidenceRequest, TaskStatus } from '../types/divisionTask';

const API_URL = '/api/organization/division-tasks';
const EVIDENCE_API_URL = '/api/organization/division-task-evidences';

const divisionTaskService = {
  // --- Tasks (Manager) ---
  getAllTasks: async (token: string) => {
    return await apiRequest<DivisionTask[]>(API_URL, { method: 'GET' }, token);
  },

  getTasksByDivision: async (token: string, divisionId: number) => {
    return await apiRequest<DivisionTask[]>(`${API_URL}/division/${divisionId}`, { method: 'GET' }, token);
  },

  createTask: async (token: string, data: DivisionTaskRequest) => {
    return await apiRequest<DivisionTask>(API_URL, { method: 'POST', body: JSON.stringify(data) }, token);
  },

  updateTask: async (token: string, id: number, data: DivisionTaskRequest) => {
    return await apiRequest<DivisionTask>(`${API_URL}/${id}`, { method: 'PUT', body: JSON.stringify(data) }, token);
  },

  updateTaskStatus: async (token: string, id: number, status: TaskStatus) => {
    return await apiRequest<DivisionTask>(`${API_URL}/${id}/status/${status}`, { method: 'PATCH' }, token);
  },

  deleteTask: async (token: string, id: number) => {
    return await apiRequest<void>(`${API_URL}/${id}`, { method: 'DELETE' }, token);
  },

  getTaskById: async (token: string, id: number) => {
    return await apiRequest<DivisionTask>(`${API_URL}/${id}`, { method: 'GET' }, token);
  },

  // --- Tasks (Member / Me) ---
  getMyTasks: async (token: string) => {
    return await apiRequest<DivisionTask[]>(`${API_URL}/me`, { method: 'GET' }, token);
  },

  updateMyTaskStatus: async (token: string, id: number, status: TaskStatus) => {
    return await apiRequest<DivisionTask>(`${API_URL}/${id}/my-status/${status}`, { method: 'PATCH' }, token);
  },

  // --- Evidences ---
  getEvidencesByTask: async (token: string, taskId: number) => {
    return await apiRequest<DivisionTaskEvidence[]>(`${EVIDENCE_API_URL}/task/${taskId}`, { method: 'GET' }, token);
  },

  createEvidence: async (token: string, data: DivisionTaskEvidenceRequest) => {
    return await apiRequest<DivisionTaskEvidence>(EVIDENCE_API_URL, { method: 'POST', body: JSON.stringify(data) }, token);
  },

  updateEvidence: async (token: string, id: number, data: DivisionTaskEvidenceRequest) => {
    return await apiRequest<DivisionTaskEvidence>(`${EVIDENCE_API_URL}/${id}`, { method: 'PUT', body: JSON.stringify(data) }, token);
  },

  deleteEvidence: async (token: string, id: number) => {
    return await apiRequest<void>(`${EVIDENCE_API_URL}/${id}`, { method: 'DELETE' }, token);
  },

  createMyEvidence: async (token: string, data: DivisionTaskEvidenceRequest) => {
    return await apiRequest<DivisionTaskEvidence>(`${EVIDENCE_API_URL}/my`, { method: 'POST', body: JSON.stringify(data) }, token);
  },

  updateMyEvidence: async (token: string, id: number, data: DivisionTaskEvidenceRequest) => {
    return await apiRequest<DivisionTaskEvidence>(`${EVIDENCE_API_URL}/my/${id}`, { method: 'PUT', body: JSON.stringify(data) }, token);
  },

  deleteMyEvidence: async (token: string, id: number) => {
    return await apiRequest<void>(`${EVIDENCE_API_URL}/my/${id}`, { method: 'DELETE' }, token);
  }
};

export default divisionTaskService;
