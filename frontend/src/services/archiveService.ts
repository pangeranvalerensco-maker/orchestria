import { apiRequest, API_BASE_URL, ApiError } from "../api/http";
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

export function getDocumentCategories(token: string) {
    return apiRequest<DocumentCategory[]>(
        `/api/organization/archive/documents/categories`,
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
    const response = await fetch(`${API_BASE_URL}/api/organization/archive/documents/${id}/download`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!response.ok) {
        let errorMessage = "Gagal mengunduh dokumen";
        let errors = undefined;
        try {
            const errorJson = await response.json();
            errorMessage = errorJson.message || errorMessage;
            errors = errorJson.errors;
        } catch {
            // Abaikan jika tidak ada JSON
        }
        throw new ApiError(errorMessage, response.status, errors);
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
    return apiRequest<void>(
        `/api/organization/archive/documents/${id}`,
        { method: "DELETE" },
        token
    );
}
