import React, { useEffect, useState, useRef } from "react";
import { useAuth } from "../auth/useAuth";
import { listDocuments, uploadDocument, downloadDocument, softDeleteDocument } from "../services/archiveService";
import type { DocumentCategory, ArchiveDocumentResponse } from "../types/archive";
import { ApiError } from "../api/http";

export function ArchivePage() {
  const { token } = useAuth();

  const [documents, setDocuments] = useState<ArchiveDocumentResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter & Search
  const [searchQuery, setSearchQuery] = useState("");
  const [categoryFilter, setCategoryFilter] = useState<DocumentCategory | "ALL">("ALL");

  // Upload State
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  
  // Form Refs
  const fileInputRef = useRef<HTMLInputElement>(null);
  const titleInputRef = useRef<HTMLInputElement>(null);
  const descriptionInputRef = useRef<HTMLTextAreaElement>(null);
  const categorySelectRef = useRef<HTMLSelectElement>(null);

  const fetchDocuments = async () => {
    if (!token) return;
    setIsLoading(true);
    setError(null);
    try {
      const res = await listDocuments(
        token,
        searchQuery.trim() || undefined,
        categoryFilter === "ALL" ? undefined : categoryFilter
      );
      setDocuments(res.data ?? []);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 401) setError("Sesi tidak valid.");
        else if (err.status === 403) setError("Anda tidak memiliki akses.");
        else setError(err.message || "Gagal mengambil data arsip.");
      } else {
        setError("Terjadi kesalahan tak terduga.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDocuments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, categoryFilter]);

  // Handle Search dengan debounce sederhana (submit manual atau tekan enter)
  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchDocuments();
  };

  const handleUploadSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    const file = fileInputRef.current?.files?.[0];
    const title = titleInputRef.current?.value;
    const category = categorySelectRef.current?.value as DocumentCategory;
    const description = descriptionInputRef.current?.value;

    if (!file || !title || !category) {
      setUploadError("Harap lengkapi judul, kategori, dan pilih file.");
      return;
    }

    setIsUploading(true);
    setUploadError(null);

    try {
      await uploadDocument(token, title, category, file, description);
      
      // Reset form
      if (fileInputRef.current) fileInputRef.current.value = "";
      if (titleInputRef.current) titleInputRef.current.value = "";
      if (descriptionInputRef.current) descriptionInputRef.current.value = "";
      if (categorySelectRef.current) categorySelectRef.current.value = "SURAT_MASUK";
      
      // Refresh list
      fetchDocuments();
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setUploadError(err.message || "Gagal mengunggah dokumen.");
      } else {
        setUploadError("Terjadi kesalahan tak terduga saat mengunggah.");
      }
    } finally {
      setIsUploading(false);
    }
  };

  const handleDownload = async (id: number, fileName: string) => {
    if (!token) return;
    try {
      await downloadDocument(token, id, fileName);
    } catch (err) {
      alert("Gagal mengunduh dokumen.");
    }
  };

  const handleDelete = async (id: number) => {
    if (!token) return;
    if (!window.confirm("Apakah Anda yakin ingin menghapus dokumen ini?")) return;

    try {
      await softDeleteDocument(token, id);
      fetchDocuments(); // Refresh
    } catch (err) {
      alert("Gagal menghapus dokumen.");
    }
  };

  function formatBytes(bytes: number, decimals = 2) {
    if (!+bytes) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
  }

  function formatDate(isoDate: string) {
    return new Date(isoDate).toLocaleDateString("id-ID", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  if (error && !documents.length) {
    return (
      <div className="page-content">
        <div className="empty-state">
          <div className="empty-state-icon archive-error-icon">!</div>
          <h2>Akses Gagal</h2>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-content archive-page">
      <div className="page-heading archive-page-heading">
        <p className="eyebrow">ORGANIZATION</p>
        <h1>Arsip Dokumen</h1>
        <p>Kelola dan simpan dokumen penting organisasi dengan aman.</p>
      </div>

      <div className="archive-layout-grid">
        <div className="archive-main-column">
          <div className="content-card request-list-card archive-list-card">
            <div className="archive-filters">
              <form onSubmit={handleSearchSubmit} className="archive-search-form">
                <input
                  type="text"
                  className="archive-search-input"
                  placeholder="Cari berdasarkan judul atau nama file..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
                <button type="submit" className="button button-primary">Cari</button>
              </form>
              
              <div className="archive-category-filter">
                <select 
                  className="form-control" 
                  value={categoryFilter} 
                  onChange={(e) => setCategoryFilter(e.target.value as any)}
                >
                  <option value="ALL">Semua Kategori</option>
                  <option value="SURAT_MASUK">Surat Masuk</option>
                  <option value="SURAT_KELUAR">Surat Keluar</option>
                  <option value="PROPOSAL">Proposal</option>
                  <option value="LPJ">LPJ</option>
                  <option value="SK">SK</option>
                  <option value="SERTIFIKAT">Sertifikat</option>
                  <option value="LAINNYA">Lainnya</option>
                </select>
              </div>
            </div>

            {isLoading && documents.length === 0 ? (
              <div className="archive-loading">
                <div className="spinner"></div>
                <p>Memuat dokumen...</p>
              </div>
            ) : documents.length === 0 ? (
              <div className="empty-state archive-empty-state">
                <p>Tidak ada dokumen yang ditemukan.</p>
              </div>
            ) : (
              <div className="request-table-wrapper archive-table-wrapper">
                <table className="request-table">
                  <thead>
                    <tr>
                      <th>Detail Dokumen</th>
                      <th>Kategori</th>
                      <th>Ukuran</th>
                      <th>Waktu Unggah</th>
                      <th className="action-col">Aksi</th>
                    </tr>
                  </thead>
                  <tbody>
                    {documents.map((doc) => (
                      <tr key={doc.id} className="archive-row">
                        <td>
                          <div className="archive-doc-title">
                            <strong>{doc.title}</strong>
                            <small>{doc.originalFileName}</small>
                            {doc.description && <span className="archive-doc-desc">{doc.description}</span>}
                          </div>
                        </td>
                        <td>
                          <span className="archive-category-badge">{doc.category.replace('_', ' ')}</span>
                        </td>
                        <td>{formatBytes(doc.sizeBytes)}</td>
                        <td>
                          <div className="archive-date">
                            <span>{formatDate(doc.uploadedAt)}</span>
                            <small>oleh {doc.uploadedByName || doc.uploadedByEmail}</small>
                          </div>
                        </td>
                        <td className="action-col">
                          <div className="archive-actions">
                            <button 
                              className="button button-outline archive-action-btn"
                              onClick={() => handleDownload(doc.id, doc.originalFileName)}
                              title="Unduh Dokumen"
                            >
                              Unduh
                            </button>
                            <button 
                              className="button button-danger archive-action-btn"
                              onClick={() => handleDelete(doc.id)}
                              title="Hapus Dokumen"
                            >
                              Hapus
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        <div className="archive-sidebar">
          <div className="content-card archive-upload-card">
            <h3>Unggah Dokumen Baru</h3>
            <p className="upload-subtitle">Format didukung: PDF, PNG, JPG/JPEG.</p>
            
            {uploadError && <div className="archive-error-alert">{uploadError}</div>}
            
            <form className="archive-upload-form" onSubmit={handleUploadSubmit}>
              <div className="form-group">
                <label>Judul Dokumen <span className="text-danger">*</span></label>
                <input ref={titleInputRef} type="text" className="form-control" required placeholder="Contoh: Surat Undangan Rapat" />
              </div>
              
              <div className="form-group">
                <label>Kategori <span className="text-danger">*</span></label>
                <select ref={categorySelectRef} className="form-control" required defaultValue="SURAT_MASUK">
                  <option value="SURAT_MASUK">Surat Masuk</option>
                  <option value="SURAT_KELUAR">Surat Keluar</option>
                  <option value="PROPOSAL">Proposal</option>
                  <option value="LPJ">LPJ</option>
                  <option value="SK">SK</option>
                  <option value="SERTIFIKAT">Sertifikat</option>
                  <option value="LAINNYA">Lainnya</option>
                </select>
              </div>

              <div className="form-group">
                <label>File <span className="text-danger">*</span></label>
                <div className="archive-file-input-wrapper">
                  <input ref={fileInputRef} type="file" className="form-control file-input" required accept=".pdf,image/png,image/jpeg,image/jpg" />
                </div>
                <small className="form-help">Maksimal 10 MB.</small>
              </div>
              
              <div className="form-group">
                <label>Deskripsi (Opsional)</label>
                <textarea ref={descriptionInputRef} className="form-control" rows={3} placeholder="Catatan tambahan tentang dokumen..."></textarea>
              </div>

              <button 
                type="submit" 
                className="button button-primary w-100" 
                disabled={isUploading}
              >
                {isUploading ? "Mengunggah..." : "Unggah Dokumen"}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
