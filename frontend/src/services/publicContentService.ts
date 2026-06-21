import type { PublicContentEntry, PublicContentRequest } from '../types/publicContent';
import { PublicContentType } from '../types/publicContent';
import { apiRequest } from '../api/http';

export const publicContentService = {
  // Public endpoints
  getAllPublished: async (token?: string): Promise<PublicContentEntry[]> => {
    const res = await apiRequest<PublicContentEntry[]>('/api/organization/public/content', { method: 'GET' }, token);
    return res.data;
  },

  getPublishedByType: async (type: PublicContentType, token?: string): Promise<PublicContentEntry[]> => {
    const res = await apiRequest<PublicContentEntry[]>(`/api/organization/public/content/type/${type}`, { method: 'GET' }, token);
    return res.data;
  },

  getPublishedHero: async (token?: string): Promise<PublicContentEntry> => {
    const res = await apiRequest<PublicContentEntry>('/api/organization/public/content/hero', { method: 'GET' }, token);
    return res.data;
  },

  // Admin endpoints
  getAllContentsAdmin: async (token: string): Promise<PublicContentEntry[]> => {
    const res = await apiRequest<PublicContentEntry[]>('/api/organization/admin/public-content', { method: 'GET' }, token);
    return res.data;
  },

  createContent: async (data: PublicContentRequest, token: string): Promise<PublicContentEntry> => {
    const res = await apiRequest<PublicContentEntry>('/api/organization/admin/public-content', {
      method: 'POST',
      body: JSON.stringify(data)
    }, token);
    return res.data;
  },

  updateContent: async (id: string, data: PublicContentRequest, token: string): Promise<PublicContentEntry> => {
    const res = await apiRequest<PublicContentEntry>(`/api/organization/admin/public-content/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    }, token);
    return res.data;
  },

  publishContent: async (id: string, token: string): Promise<PublicContentEntry> => {
    const res = await apiRequest<PublicContentEntry>(`/api/organization/admin/public-content/${id}/publish`, { method: 'PATCH' }, token);
    return res.data;
  },

  archiveContent: async (id: string, token: string): Promise<PublicContentEntry> => {
    const res = await apiRequest<PublicContentEntry>(`/api/organization/admin/public-content/${id}/archive`, { method: 'PATCH' }, token);
    return res.data;
  },

  restoreContent: async (id: string, token: string): Promise<PublicContentEntry> => {
    const res = await apiRequest<PublicContentEntry>(`/api/organization/admin/public-content/${id}/restore`, { method: 'PATCH' }, token);
    return res.data;
  },

  deleteContent: async (id: string, token: string): Promise<void> => {
    await apiRequest<void>(`/api/organization/admin/public-content/${id}`, { method: 'DELETE' }, token);
  }
};
