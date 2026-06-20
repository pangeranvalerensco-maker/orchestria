import { useState } from "react";
import { useAuth } from "../auth/useAuth";

export function ReportsPage() {
  const { token, logout } = useAuth();
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);

  const handleDownload = async () => {
    setLoading(true);
    setMessage(null);
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8000"}/api/reports/fund-requests.xlsx`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (!response.ok) {
        if (response.status === 401) {
          setMessage({ type: 'error', text: "Sesi tidak valid. Silakan login kembali." });
          logout();
          return;
        } else if (response.status === 403) {
          setMessage({ type: 'error', text: "Anda tidak mempunyai izin untuk mengunduh laporan ini." });
          return;
        } else if (response.status === 502 || response.status >= 500) {
          setMessage({ type: 'error', text: "Layanan laporan saat ini bermasalah. Coba lagi nanti." });
          return;
        } else {
          setMessage({ type: 'error', text: `Gagal mengunduh laporan (Status: ${response.status}).` });
          return;
        }
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      // Get filename from header if possible, else default
      let filename = "fund-requests.xlsx";
      const disposition = response.headers.get("Content-Disposition");
      if (disposition && disposition.indexOf("filename=") !== -1) {
        const matches = /filename="([^"]+)"/.exec(disposition);
        if (matches != null && matches[1]) {
          filename = matches[1];
        }
      }
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

      setMessage({ type: 'success', text: "Laporan berhasil diunduh." });
    } catch (error) {
      setMessage({ type: 'error', text: "Terjadi kesalahan jaringan atau layanan tidak tersedia." });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-content">
      <div className="report-header">
        <span className="eyebrow">REPORTING</span>
        <h1>Laporan Organisasi</h1>
        <p>Pusat kontrol untuk mengunduh seluruh laporan operasional dan keuangan. Data terekspor dalam format standar untuk kemudahan rekonsiliasi.</p>
      </div>

      {message && (
        <div className={`alert ${message.type === 'success' ? 'alert-success' : 'alert-error'}`}>
          {message.type === 'success' ? '✓ ' : '⚠ '}
          {message.text}
        </div>
      )}

      <div className="report-cards-grid">
        <div className="report-card">
          <div className="report-card-header">
            <span className="report-label">Laporan Keuangan</span>
            <span className="report-badge">XLSX</span>
          </div>
          <h2>Laporan Pengajuan Dana</h2>
          <p>Unduh seluruh data histori pengajuan dana, termasuk status approval, detail nilai nominal, divisi, dan waktu pengajuan untuk kebutuhan audit.</p>
          <div className="report-meta">
            <span>🗄️ Sumber: Finance Service</span>
          </div>
          <button 
            className="primary-button" 
            onClick={handleDownload} 
            disabled={loading}
          >
            {loading ? "⏳ Mengunduh..." : "⬇️ Unduh Excel"}
          </button>
        </div>
      </div>
    </div>
  );
}
