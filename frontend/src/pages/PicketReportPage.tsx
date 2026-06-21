import { useEffect, useState } from "react";
import { useAuth } from "../auth/useAuth";
import { getReportSummary } from "../services/cleanlinessService";
import type { ReportSummary } from "../types/cleanliness";

export function PicketReportPage() {
  const { token } = useAuth();
  const [summary, setSummary] = useState<ReportSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    async function loadData() {
      if (!token) return;
      try {
        setIsLoading(true);
        setErrorMessage("");
        const data = await getReportSummary(token);
        setSummary(data);
      } catch (err: unknown) {
        setErrorMessage("Gagal memuat laporan.");
      } finally {
        setIsLoading(false);
      }
    }
    loadData();
  }, [token]);

  if (isLoading) {
    return (
      <div className="picket-page">
        <div className="picket-card"><p>Memuat laporan...</p></div>
      </div>
    );
  }

  if (errorMessage || !summary) {
    return (
      <div className="picket-page">
        <div className="picket-card"><p className="picket-text-error">{errorMessage || "Gagal memuat laporan."}</p></div>
      </div>
    );
  }

  return (
    <div className="picket-page">
      <div className="picket-page-header">
        <div>
          <h2>Laporan & Leaderboard Kebersihan</h2>
          <p>Ringkasan performa tugas piket anggota organisasi.</p>
        </div>
      </div>

      <div className="picket-grid picket-grid-3">
        <div className="picket-stat-card">
          <span className="picket-stat-title">Total Jadwal (Completed)</span>
          <span className="picket-stat-value">{summary.completedSchedules} / {summary.totalSchedules}</span>
        </div>
        <div className="picket-stat-card">
          <span className="picket-stat-title">Kehadiran (Hadir / Alpa / Izin)</span>
          <span className="picket-stat-value">{summary.presentCount} / {summary.absentCount} / {summary.excusedCount}</span>
        </div>
        <div className="picket-stat-card">
          <span className="picket-stat-title">Total Poin Keseluruhan</span>
          <span className={`picket-stat-value ${summary.netPoints >= 0 ? "picket-text-success" : "picket-text-error"}`}>
            {summary.netPoints > 0 ? "+" : ""}{summary.netPoints}
          </span>
        </div>
      </div>

      <div className="picket-card picket-mt-4">
        <h3 className="picket-card-title picket-mb-4">Leaderboard Poin</h3>
        <div className="picket-table-wrapper">
          <table className="picket-table">
            <thead>
              <tr>
                <th>Peringkat</th>
                <th>Nama Anggota</th>
                <th>Reward Poin</th>
                <th>Violation Poin</th>
                <th>Net Poin</th>
              </tr>
            </thead>
            <tbody>
              {summary.memberLeaderboard.length === 0 ? (
                <tr>
                  <td colSpan={5} className="picket-empty picket-text-center picket-p-8">
                    Belum ada data poin
                  </td>
                </tr>
              ) : (
                summary.memberLeaderboard.map((m, index) => (
                  <tr key={m.memberId}>
                    <td>
                      {index === 0 && "🥇 "}
                      {index === 1 && "🥈 "}
                      {index === 2 && "🥉 "}
                      {index > 2 && `${index + 1}`}
                    </td>
                    <td className="picket-font-medium">{m.memberName}</td>
                    <td className="picket-text-success">+{m.totalRewardPoints}</td>
                    <td className="picket-text-error">-{m.totalViolationPoints}</td>
                    <td>
                      <span className={`picket-badge ${m.netPoints > 0 ? "picket-badge-green" : m.netPoints < 0 ? "picket-badge-red" : "picket-badge-gray"}`}>
                        {m.netPoints > 0 ? "+" : ""}{m.netPoints}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
