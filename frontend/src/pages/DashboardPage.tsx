import { useEffect, useState } from "react";
import { Link } from "react-router";
import { useAuth } from "../auth/useAuth";
import { getMyRequests, getPendingApprovals } from "../services/requestService";
import { getReadyForDisbursement } from "../services/financeService";
import type { FundRequest } from "../types/request";

export function DashboardPage() {
  const { user, token, hasPermission } = useAuth();

  const [myRequests, setMyRequests] = useState<FundRequest[]>([]);
  const [myRequestsCount, setMyRequestsCount] = useState<number | null>(null);
  
  const [pendingApprovalsCount, setPendingApprovalsCount] = useState<number | null>(null);
  const [readyDisburseCount, setReadyDisburseCount] = useState<number | null>(null);

  const [isLoading, setIsLoading] = useState(true);

  // Permissions checks
  const canReadOwnRequest = hasPermission("request.read.own") || hasPermission("request.create");
  const canApprove = hasPermission("request.approve.division") || hasPermission("request.approve.pub") || hasPermission("request.approve.pembina");
  const canDisburse = hasPermission("finance.disburse");
  const canManageArchive = hasPermission("archive.manage");
  const canReadOrganization = hasPermission("organization.read");
  const canReadReports = hasPermission("request.read.all");
  const canVerifySettlement = hasPermission("finance.settlement.verify");

  useEffect(() => {
    async function loadDashboardData() {
      if (!user || !token) return;
      setIsLoading(true);

      const promises: Promise<void>[] = [];

      if (canReadOwnRequest) {
        promises.push(
          getMyRequests(token, 0, 5).then((res) => {
            if (res.data) {
              setMyRequests(res.data.content);
              setMyRequestsCount(res.data.totalElements);
            }
          }).catch(() => {})
        );
      }

      if (canApprove) {
        promises.push(
          getPendingApprovals(token).then((res) => {
            if (res.data) setPendingApprovalsCount(res.data.length);
          }).catch(() => {})
        );
      }

      if (canDisburse) {
        promises.push(
          getReadyForDisbursement(token).then((res) => {
            if (res.data) setReadyDisburseCount(res.data.content.length);
          }).catch(() => {})
        );
      }

      await Promise.all(promises);
      setIsLoading(false);
    }

    loadDashboardData();
  }, [user, canReadOwnRequest, canApprove, canDisburse]);

  const todayDate = new Date().toLocaleDateString("id-ID", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });

  const primaryRole = user?.roles[0] || "Anggota";

  if (isLoading && !myRequests.length) {
    return (
      <main className="page-content dashboard-page">
        <div className="dashboard-loading">
          <div className="spinner"></div>
          <p>Memuat workspace...</p>
        </div>
      </main>
    );
  }

  return (
    <main className="page-content dashboard-page">
      {/* HEADER */}
      <section className="dashboard-header">
        <div>
          <p className="eyebrow">{todayDate.toUpperCase()}</p>
          <h1>Selamat Datang, {user?.fullName}</h1>
          <p>Role utama Anda saat ini adalah <strong>{primaryRole}</strong>.</p>
        </div>
        <div className="dashboard-role-badge">
          {primaryRole.charAt(0)}
        </div>
      </section>

      {/* SUMMARY CARDS */}
      <section className="dashboard-summary-grid">
        {canReadOwnRequest && (
          <div className="dashboard-card stat-card">
            <span className="stat-label">Pengajuan Saya</span>
            <strong className="stat-value">{myRequestsCount !== null ? myRequestsCount : "..."}</strong>
            <Link to="/requests" className="stat-link">Lihat semua &rarr;</Link>
          </div>
        )}

        {canApprove && (
          <div className="dashboard-card stat-card stat-warning">
            <span className="stat-label">Approval Menunggu</span>
            <strong className="stat-value">{pendingApprovalsCount !== null ? pendingApprovalsCount : "..."}</strong>
            <Link to="/approvals" className="stat-link">Proses sekarang &rarr;</Link>
          </div>
        )}

        {canDisburse && (
          <div className="dashboard-card stat-card stat-success">
            <span className="stat-label">Siap Cair</span>
            <strong className="stat-value">{readyDisburseCount !== null ? readyDisburseCount : "..."}</strong>
            <Link to="/finance/disbursements" className="stat-link">Menu Pencairan &rarr;</Link>
          </div>
        )}

        {canManageArchive && (
          <div className="dashboard-card stat-card stat-neutral">
            <span className="stat-label">Arsip Dokumen</span>
            <strong className="stat-value">🗂️</strong>
            <Link to="/archive" className="stat-link">Buka Modul &rarr;</Link>
          </div>
        )}

        {canReadOrganization && (
          <div className="dashboard-card stat-card stat-neutral">
            <span className="stat-label">Struktur Organisasi</span>
            <strong className="stat-value">👥</strong>
            <Link to="/organization" className="stat-link">Buka Direktori &rarr;</Link>
          </div>
        )}

        {canReadReports && (
          <div className="dashboard-card stat-card stat-neutral">
            <span className="stat-label">Laporan Global</span>
            <strong className="stat-value">📊</strong>
            <Link to="/reports" className="stat-link">Buka Laporan &rarr;</Link>
          </div>
        )}
      </section>

      <div className="dashboard-main-grid">
        {/* AKTIVITAS / PEKERJAAN */}
        {canReadOwnRequest && (
          <section className="dashboard-card">
            <div className="dashboard-card-header">
              <h2>Aktivitas Pengajuan Saya</h2>
              <Link to="/requests/new" className="dashboard-btn-small">Buat Baru</Link>
            </div>
            
            {myRequests.length === 0 ? (
              <div className="dashboard-empty">
                <p>Belum ada pengajuan terbaru.</p>
              </div>
            ) : (
              <div className="dashboard-activity-list">
                {myRequests.map((req) => (
                  <Link to={`/requests/${req.id}`} key={req.id} className="dashboard-activity-item">
                    <div className="activity-info">
                      <strong>{req.title}</strong>
                      <small>{req.activityDate || "Tanpa tanggal"}</small>
                    </div>
                    <span className={`status-badge status-${req.status.toLowerCase()}`}>
                      {req.status}
                    </span>
                  </Link>
                ))}
              </div>
            )}
          </section>
        )}

        {/* QUICK ACTIONS & AKSES SAYA */}
        <div className="dashboard-side-col">
          <section className="dashboard-card">
            <div className="dashboard-card-header">
              <h2>Quick Actions</h2>
            </div>
            <div className="dashboard-quick-actions">
              {canReadOwnRequest && <Link to="/requests/new" className="quick-action-btn">📝 Buat Pengajuan</Link>}
              {canReadOwnRequest && <Link to="/requests" className="quick-action-btn">📋 Lihat Pengajuan Saya</Link>}
              {canApprove && <Link to="/approvals" className="quick-action-btn">✅ Proses Approval</Link>}
              {canDisburse && <Link to="/finance/disbursements" className="quick-action-btn">💰 Pencairan Dana</Link>}
              {canVerifySettlement && <Link to="/finance/settlements" className="quick-action-btn">🧾 Verifikasi Settlement</Link>}
              {canManageArchive && <Link to="/archive" className="quick-action-btn">🗂️ Kelola Arsip</Link>}
              {canReadOrganization && <Link to="/organization" className="quick-action-btn">👥 Direktori Organisasi</Link>}
              {canReadReports && <Link to="/reports" className="quick-action-btn">📊 Buka Laporan</Link>}
            </div>
          </section>

          <section className="dashboard-card dashboard-access-card">
            <div className="dashboard-card-header">
              <h2>Akses Saya</h2>
            </div>
            <div className="access-info">
              <p>Anda login dengan <strong>{user?.roles.length} role</strong> dan memiliki <strong>{user?.permissions.length} permission</strong> aktif.</p>
              <div className="access-roles">
                {user?.roles.map(role => (
                  <span key={role} className="role-chip">{role}</span>
                ))}
              </div>
              <details className="access-details">
                <summary>Lihat detail permission</summary>
                <ul className="permission-list">
                  {user?.permissions.map(perm => (
                    <li key={perm}>{perm}</li>
                  ))}
                </ul>
              </details>
            </div>
          </section>
        </div>
      </div>
    </main>
  );
}