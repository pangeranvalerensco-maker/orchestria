import type { PublicContentEntry, PublicContentRequest } from '../types/publicContent';
import { PublicContentType, PublicationStatus } from '../types/publicContent';
import { apiRequest } from '../api/http';

export const publicContentService = {
  // Public Endpoint
  getPublished: async (type?: PublicContentType, category?: string): Promise<PublicContentEntry[]> => {
    const params = new URLSearchParams();
    if (type) params.append('type', type);
    if (category) params.append('category', category);
    const queryString = params.toString();
    const url = `/api/organization/public/content${queryString ? `?${queryString}` : ''}`;
    
    // No wrapper! apiRequest already returns the unwrapped data, but wait, the instruction says:
    // "Semua memakai: apiRequest<PublicContentEntry> apiRequest<PublicContentEntry[]> Jangan membungkus generic dengan ApiResponse lagi."
    const response = await apiRequest<PublicContentEntry[]>(url, { method: 'GET' });
    return response.data;
  },

  getPublishedDetail: async (id: string): Promise<PublicContentEntry> => {
    const response = await apiRequest<PublicContentEntry>(`/api/organization/public/content/${id}`, { method: 'GET' });
    return response.data;
  },

  // Admin Endpoints
  getAllContents: async (token: string, type?: PublicContentType, status?: PublicationStatus, active?: boolean): Promise<PublicContentEntry[]> => {
    const params = new URLSearchParams();
    if (type) params.append('type', type);
    if (status) params.append('status', status);
    if (active !== undefined) params.append('active', String(active));
    const queryString = params.toString();
    const url = `/api/organization/public-content${queryString ? `?${queryString}` : ''}`;
    const response = await apiRequest<PublicContentEntry[]>(url, { method: 'GET' }, token);
    return response.data;
  },

  getContent: async (id: string, token: string): Promise<PublicContentEntry> => {
    const response = await apiRequest<PublicContentEntry>(`/api/organization/public-content/${id}`, { method: 'GET' }, token);
    return response.data;
  },

  createContent: async (request: PublicContentRequest, token: string): Promise<PublicContentEntry> => {
    const response = await apiRequest<PublicContentEntry>(
      '/api/organization/public-content',
      {
        method: 'POST',
        body: JSON.stringify(request)
      },
      token
    );
    return response.data;
  },

  updateContent: async (id: string, request: PublicContentRequest, token: string): Promise<PublicContentEntry> => {
    const response = await apiRequest<PublicContentEntry>(
      `/api/organization/public-content/${id}`,
      {
        method: 'PUT',
        body: JSON.stringify(request)
      },
      token
    );
    return response.data;
  },

  publishContent: async (id: string, token: string): Promise<PublicContentEntry> => {
    const response = await apiRequest<PublicContentEntry>(
      `/api/organization/public-content/${id}/publish`,
      { method: 'POST' },
      token
    );
    return response.data;
  },

  archiveContent: async (id: string, token: string): Promise<PublicContentEntry> => {
    const response = await apiRequest<PublicContentEntry>(
      `/api/organization/public-content/${id}/archive`,
      { method: 'POST' },
      token
    );
    return response.data;
  },

  restoreDraftContent: async (id: string, token: string): Promise<PublicContentEntry> => {
    const response = await apiRequest<PublicContentEntry>(
      `/api/organization/public-content/${id}/restore-draft`,
      { method: 'POST' },
      token
    );
    return response.data;
  },

  deleteContent: async (id: string, token: string): Promise<void> => {
    await apiRequest<void>(
      `/api/organization/public-content/${id}`,
      { method: 'DELETE' },
      token
    );
  }
};
