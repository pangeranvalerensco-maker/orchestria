import { apiRequest } from "../api/http";
import type { ArchiveDocumentResponse, DocumentCategory } from "../types/archive";

export function listDocuments(token: string, keyword?: string, category?: DocumentCategory) {
    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    if (category) params.append('category', category);
    
    return apiRequest<ArchiveDocumentResponse[]>(
        `/api/organization/archive/documents?${params.toString()}`,
        { method: "GET" },
        token
    );
}

export function getDocument(token: string, id: number) {
    return apiRequest<ArchiveDocumentResponse>(
        `/api/organization/archive/documents/${id}`,
        { method: "GET" },
        token
    );
}

export function uploadDocument(
    token: string,
    title: string,
    category: DocumentCategory,
    file: File,
    description?: string
) {
    const formData = new FormData();
    formData.append('title', title);
    formData.append('category', category);
    formData.append('file', file);
    if (description) formData.append('description', description);

    return apiRequest<ArchiveDocumentResponse>(
        `/api/organization/archive/documents`,
        {
            method: "POST",
            body: formData,
        },
        token
    );
}

export async function downloadDocument(token: string, id: number, originalFileName: string) {
    // Gunakan fetch langsung karena kita butuh Blob, bukan JSON yang di-parse oleh apiRequest
    const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8000";
    
    const response = await fetch(`${API_BASE_URL}/api/organization/archive/documents/${id}/download`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!response.ok) {
        throw new Error(`Gagal mengunduh dokumen dengan status ${response.status}`);
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    
    let fileName = originalFileName || 'document';
    const contentDisposition = response.headers.get('content-disposition');
    if (contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename="?([^"]+)"?/);
        if (fileNameMatch && fileNameMatch.length >= 2) {
            fileName = fileNameMatch[1];
        }
    }
    
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    
    link.remove();
    window.URL.revokeObjectURL(url);
}

export function softDeleteDocument(token: string, id: number) {
    return apiRequest<{success: boolean, message: string}>(
        `/api/organization/archive/documents/${id}`,
        { method: "DELETE" },
        token
    );
}
