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
    <div className="page">
      <div className="page-header">
        <h1>Laporan</h1>
      </div>

      <div className="page-content">
        {message && (
          <div className={`alert alert-${message.type}`} style={{ marginBottom: "20px", padding: "12px", borderRadius: "4px", backgroundColor: message.type === 'success' ? "#e6f4ea" : "#fce8e6", color: message.type === 'success' ? "#137333" : "#c5221f" }}>
            {message.text}
          </div>
        )}

        <div className="card" style={{ padding: "24px", maxWidth: "400px" }}>
          <h2 style={{ marginTop: 0, marginBottom: "16px", fontSize: "1.25rem" }}>Laporan Pengajuan Dana</h2>
          <p style={{ marginBottom: "24px", color: "#5f6368" }}>Unduh seluruh data pengajuan dana dalam format Excel (.xlsx).</p>
          <button 
            className="button button-primary" 
            onClick={handleDownload} 
            disabled={loading}
            style={{ width: "100%", justifyContent: "center" }}
          >
            {loading ? "Mengunduh..." : "Unduh Excel"}
          </button>
        </div>
      </div>
    </div>
  );
}
