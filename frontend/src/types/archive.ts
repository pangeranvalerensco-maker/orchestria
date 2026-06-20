export type DocumentCategory = 
    | 'SURAT_MASUK'
    | 'SURAT_KELUAR'
    | 'PROPOSAL'
    | 'LAPORAN'
    | 'NOTULEN'
    | 'DOKUMENTASI'
    | 'LAINNYA';

export interface ArchiveDocumentResponse {
    id: number;
    title: string;
    description: string | null;
    category: DocumentCategory;
    originalFileName: string;
    contentType: string;
    sizeBytes: number;
    uploadedByEmail: string;
    uploadedByName: string | null;
    uploadedAt: string;
    deleted: boolean;
}
