export type DocumentCategory = 
    | 'SURAT_MASUK'
    | 'SURAT_KELUAR'
    | 'PROPOSAL'
    | 'LPJ'
    | 'SK'
    | 'SERTIFIKAT'
    | 'LAINNYA';

export interface ArchiveDocumentResponse {
    id: number;
    title: string;
    description?: string;
    category: DocumentCategory;
    originalFileName: string;
    contentType: string;
    sizeBytes: number;
    uploadedByEmail: string;
    uploadedByName?: string;
    uploadedAt: string;
    deleted: boolean;
}
