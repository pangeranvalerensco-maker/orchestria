import { apiRequest } from '../api/http';
import type { SessionDemoLoginPayload, SessionDemoResponse } from '../types/sessionDemo';
import type { ApiResponse } from '../types/auth';

export const loginSessionDemo = async (payload: SessionDemoLoginPayload): Promise<ApiResponse<SessionDemoResponse>> => {
    return apiRequest<SessionDemoResponse>('/auth/session-demo/login', {
        method: 'POST',
        body: JSON.stringify(payload),
        credentials: 'include' // Wajib untuk menyimpan cookie
    });
};

export const getSessionDemoProfile = async (): Promise<ApiResponse<SessionDemoResponse>> => {
    return apiRequest<SessionDemoResponse>('/auth/session-demo/profile', {
        method: 'GET',
        credentials: 'include' // Wajib untuk mengirim cookie
    });
};

export const getSessionDemoStatus = async (): Promise<ApiResponse<SessionDemoResponse>> => {
    return apiRequest<SessionDemoResponse>('/auth/session-demo/status', {
        method: 'GET',
        credentials: 'include'
    });
};

export const logoutSessionDemo = async (): Promise<ApiResponse<SessionDemoResponse>> => {
    return apiRequest<SessionDemoResponse>('/auth/session-demo/logout', {
        method: 'POST',
        credentials: 'include'
    });
};
