import React, { useEffect, useState, useRef, useMemo } from "react";
import { useAuth } from "../auth/useAuth";
import { 
  listDocuments, 
  uploadDocument, 
  downloadDocument, 
  softDeleteDocument,
  getDocumentCategories
} from "../services/archiveService";
import type { DocumentCategory, ArchiveDocumentResponse } from "../types/archive";
import { ApiError } from "../api/http";

const categoryLabelMapper: Record<DocumentCategory | string, string> = {
  SURAT_MASUK: "Surat Masuk",
  SURAT_KELUAR: "Surat Keluar",
  PROPOSAL: "Proposal",
  LAPORAN: "Laporan",
  NOTULEN: "Notulen",
  DOKUMENTASI: "Dokumentasi",
  LAINNYA: "Lainnya"
};

const ALLOWED_EXTENSIONS = ['.pdf', '.docx', '.xlsx', '.png', '.jpg', '.jpeg'];
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

export function ArchivePage() {
  const { token } = useAuth();

  const [documents, setDocuments] = useState<ArchiveDocumentResponse[]>([]);
  const [categories, setCategories] = useState<DocumentCategory[]>([]);
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

  const fetchCategories = async () => {
    if (!token) return;
    try {
      const data = await getDocumentCategories(token);
      setCategories(data.data ?? []);
    } catch {
      // Abaikan error saat mengambil kategori, biarkan list dokumen yang menampilkan pesan error utama
    }
  };

  const fetchDocuments = async () => {
    if (!token) {
      setIsLoading(false);
      setError("Sesi tidak valid.");
      return;
    }
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
    if (token) {
      fetchCategories();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  useEffect(() => {
    fetchDocuments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, categoryFilter]);

  const handleSearchSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await fetchDocuments();
  };

  const handleResetFilter = async () => {
    setSearchQuery("");
    setCategoryFilter("ALL");
    // Karena setState bersifat asinkron, pemanggilan fetchDocuments akan ditangani
    // oleh useEffect yang bereaksi terhadap perubahan categoryFilter jika categoryFilter berubah.
    // Jika hanya search query yang berubah, useEffect tidak akan jalan.
    // Solusi terbaik: kita setState, dan setelah dirender useEffect menangkapnya.
    // Jika tidak ada perubahan filter (sudah "ALL" dan query kosong), fetchDocuments dipanggil langsung.
    if (categoryFilter === "ALL") {
      // Kita panggil fetch langsung dengan nilai baru karena useEffect tidak akan ter-trigger
      try {
        setIsLoading(true);
        const res = await listDocuments(token!, undefined, undefined);
        setDocuments(res.data ?? []);
      } catch (err) {
        // error handling handled in normal fetch
      } finally {
        setIsLoading(false);
      }
    }
  };

  const validateFile = (file: File): string | null => {
    if (file.size > MAX_FILE_SIZE) {
      return "Ukuran file melebihi 10 MB.";
    }
    const fileName = file.name.toLowerCase();
    const isExtensionValid = ALLOWED_EXTENSIONS.some(ext => fileName.endsWith(ext));
    if (!isExtensionValid) {
      return "Format file tidak didukung. Harap gunakan PDF, DOCX, XLSX, PNG, atau JPG/JPEG.";
    }
    return null;
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

    const validationError = validateFile(file);
    if (validationError) {
      setUploadError(validationError);
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
      if (categorySelectRef.current && categories.length > 0) {
        categorySelectRef.current.value = categories[0];
      }
      
      await fetchDocuments();
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 401) setUploadError("Sesi tidak valid.");
        else if (err.status === 403) setUploadError("Anda tidak memiliki akses.");
        else if (err.status === 413) setUploadError("Ukuran file melebihi 10 MB.");
        else if (err.status === 415) setUploadError("Format file tidak didukung.");
        else setUploadError(err.message || "Gagal mengunggah dokumen.");
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
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        alert(`Gagal mengunduh dokumen: ${err.message}`);
      } else {
        alert("Gagal mengunduh dokumen karena kesalahan sistem.");
      }
    }
  };

  const handleDelete = async (id: number) => {
    if (!token) return;
    if (!window.confirm("Apakah Anda yakin ingin menghapus dokumen ini?")) return;

    try {
      await softDeleteDocument(token, id);
      await fetchDocuments();
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        alert(`Gagal menghapus dokumen: ${err.message}`);
      } else {
        alert("Gagal menghapus dokumen karena kesalahan sistem.");
      }
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

  const getCategoryLabel = (categoryValue: string) => {
    return categoryLabelMapper[categoryValue] || categoryValue;
  };

  // Hitung kategori terpakai
  const usedCategoriesCount = useMemo(() => {
    const uniqueCategories = new Set(documents.map(d => d.category));
    return uniqueCategories.size;
  }, [documents]);

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
        <div>
          <p className="eyebrow">ADMINISTRATION</p>
          <h1>Arsip Dokumen</h1>
          <p>Kelola dan simpan dokumen penting administrasi organisasi secara terpusat.</p>
        </div>
      </div>

      <div className="summary-grid">
        <div className="summary-card">
          <span>Total Dokumen</span>
          <strong>{documents.length}</strong>
        </div>
        <div className="summary-card">
          <span>Kategori Terpakai</span>
          <strong>{usedCategoriesCount}</strong>
        </div>
      </div>

      <div className="archive-layout-grid archive-layout-spacing">
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
                  onChange={(e) => {
                    const val = e.target.value;
                    if (val === "ALL") {
                      setCategoryFilter("ALL");
                    } else {
                      setCategoryFilter(val as DocumentCategory);
                    }
                  }}
                >
                  <option value="ALL">Semua Kategori</option>
                  {categories.map((cat) => (
                    <option key={cat} value={cat}>{getCategoryLabel(cat)}</option>
                  ))}
                </select>
              </div>

              <button 
                type="button" 
                className="button button-outline" 
                onClick={handleResetFilter}
              >
                Reset Filter
              </button>
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
                            <div style={{ marginTop: "8px", display: "flex", gap: "8px" }}>
                              <button 
                                type="button"
                                className="button button-outline archive-action-btn"
                                onClick={() => handleDownload(doc.id, doc.originalFileName)}
                                title="Unduh Dokumen"
                                style={{ padding: "4px 8px", fontSize: "12px" }}
                              >
                                Unduh
                              </button>
                              <button 
                                type="button"
                                className="button button-danger archive-action-btn"
                                onClick={() => handleDelete(doc.id)}
                                title="Hapus Dokumen"
                                style={{ padding: "4px 8px", fontSize: "12px" }}
                              >
                                Hapus
                              </button>
                            </div>
                          </div>
                        </td>
                        <td>
                          <span className="archive-category-badge">{getCategoryLabel(doc.category)}</span>
                        </td>
                        <td>{formatBytes(doc.sizeBytes)}</td>
                        <td>
                          <div className="archive-date">
                            <span>{formatDate(doc.uploadedAt)}</span>
                            <small>oleh {doc.uploadedByName || doc.uploadedByEmail}</small>
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
            <p className="upload-subtitle">Maks 10MB (.pdf, .docx, .xlsx, .png, .jpg).</p>
            
            {uploadError && <div className="archive-error-alert">{uploadError}</div>}
            
            <form className="archive-upload-form" onSubmit={handleUploadSubmit}>
              <div className="form-group">
                <label>Judul Dokumen <span className="text-danger">*</span></label>
                <input ref={titleInputRef} type="text" className="form-control" required placeholder="Contoh: Surat Undangan Rapat" />
              </div>
              
              <div className="form-group">
                <label>Kategori <span className="text-danger">*</span></label>
                <select ref={categorySelectRef} className="form-control" required>
                  {categories.map((cat) => (
                    <option key={cat} value={cat}>{getCategoryLabel(cat)}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>File <span className="text-danger">*</span></label>
                <div className="archive-file-input-wrapper">
                  <input 
                    ref={fileInputRef} 
                    type="file" 
                    className="form-control file-input" 
                    required 
                    accept=".pdf,.docx,.xlsx,.png,.jpg,.jpeg" 
                  />
                </div>
                <small className="form-help">Pilih dokumen untuk diunggah.</small>
              </div>
              
              <div className="form-group">
                <label>Deskripsi (Opsional)</label>
                <textarea ref={descriptionInputRef} className="form-control" rows={3} placeholder="Catatan tambahan tentang dokumen..."></textarea>
              </div>

              <button 
                type="submit" 
                className="button button-primary w-100" 
                disabled={isUploading || categories.length === 0}
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
