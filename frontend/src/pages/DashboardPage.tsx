import { useEffect, useState } from "react";
import { Link } from "react-router";
import { useAuth } from "../auth/useAuth";
import { getMyRequests, getPendingApprovals } from "../services/requestService";
import { getReadyForDisbursement } from "../services/financeService";
import divisionTaskService from "../services/divisionTaskService";
import { getMyBorrowings, getAllBorrowings } from "../services/assetService";
import { getMySchedules, getMyPoints } from "../services/cleanlinessService";
import { englishService } from "../services/englishService";
import type { FundRequest } from "../types/request";

export function DashboardPage() {
  const { user, token, hasPermission } = useAuth();

  const [myRequests, setMyRequests] = useState<FundRequest[]>([]);
  const [myRequestsCount, setMyRequestsCount] = useState<number | null>(null);
  
  const [pendingApprovalsCount, setPendingApprovalsCount] = useState<number | null>(null);
  const [readyDisburseCount, setReadyDisburseCount] = useState<number | null>(null);
  const [activeTasksCount, setActiveTasksCount] = useState<number | null>(null);
  
  const [activeBorrowingsCount, setActiveBorrowingsCount] = useState<number | null>(null);
  const [requestedBorrowingsCount, setRequestedBorrowingsCount] = useState<number | null>(null);
  const [activePicketCount, setActivePicketCount] = useState<number | null>(null);
  const [myCleanlinessPoints, setMyCleanlinessPoints] = useState<number | null>(null);
  
  const [activeEnglishActivitiesCount, setActiveEnglishActivitiesCount] = useState<number | null>(null);
  const [pendingEnglishDepositsCount, setPendingEnglishDepositsCount] = useState<number | null>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [dashboardWarning, setDashboardWarning] = useState<string | null>(null);

  // Permissions checks
  const canReadOwnRequest = hasPermission("request.read.own") || hasPermission("request.create");
  const canApprove = hasPermission("request.approve.division") || hasPermission("request.approve.pub") || hasPermission("request.approve.pembina");
  const canDisburse = hasPermission("finance.disburse");
  const canManageArchive = hasPermission("archive.manage");
  const canReadOrganization = hasPermission("organization.read");
  const canManageOrganization = hasPermission("organization.manage");
  const canReadReports = hasPermission("request.read.all");
  const canVerifySettlement = hasPermission("finance.settlement.verify");
  const canReadTasks = hasPermission("division.task.read") || hasPermission("division.task.manage");
  const canReadAssets = hasPermission("asset.read");
  const canReadOwnBorrowing = hasPermission("asset.borrow.read.own");
  const canManageBorrowing = hasPermission("asset.borrow.read.all");
  const canReadCleanliness = hasPermission("cleanliness.schedule.read");
  const canReadEnglish = hasPermission("english.activity.read");
  const canReadOwnEnglishDeposit = hasPermission("english.deposit.read.own");
  const canReadAllEnglishDeposit = hasPermission("english.deposit.read.all");
  const canVerifyEnglishDeposit = hasPermission("english.deposit.verify");

  useEffect(() => {
    async function loadDashboardData() {
      if (!user || !token) {
        setIsLoading(false);
        return;
      }
      setIsLoading(true);
      setDashboardWarning(null);

      const promises: Promise<void>[] = [];

      if (canReadOwnRequest) {
        promises.push(
          getMyRequests(token, 0, 5).then((res) => {
            if (res.data) {
              setMyRequests(res.data.content);
              setMyRequestsCount(res.data.totalElements);
            }
          })
        );
      }

      if (canApprove) {
        promises.push(
          getPendingApprovals(token).then((res) => {
            if (res.data) setPendingApprovalsCount(res.data.length);
          })
        );
      }

      if (canDisburse) {
        promises.push(
          getReadyForDisbursement(token).then((res) => {
            if (res.data) setReadyDisburseCount(res.data.content.length);
          })
        );
      }

      if (canReadTasks && token) {
        promises.push(
          divisionTaskService.getMyTasks(token).then((res) => {
            if (res.data) {
              const activeTasks = res.data.filter(
                (task) =>
                  task.status !== "DONE" &&
                  task.status !== "CANCELLED"
              );
              setActiveTasksCount(activeTasks.length);
            }
          })
        );
      }

      if (canReadOwnBorrowing && token) {
        promises.push(
          getMyBorrowings(token, undefined, 0, 1000).then((res) => {
            if (res && res.content) {
              const active = res.content.filter(b => 
                b.status === "REQUESTED" || 
                b.status === "APPROVED" || 
                b.status === "BORROWED" || 
                b.status === "RETURN_REQUESTED"
              );
              setActiveBorrowingsCount(active.length);
            }
          })
        );
      }

      if (canManageBorrowing && token) {
        promises.push(
          getAllBorrowings(token, "REQUESTED", undefined, undefined, 0, 1).then((res) => {
            if (res) {
              setRequestedBorrowingsCount(res.totalElements);
            }
          })
        );
      }

      if (canReadCleanliness && token) {
        promises.push(
          getMySchedules(token).then((res) => {
            if (res) {
              const activeSchedules = res.filter(s => s.status === "PUBLISHED");
              setActivePicketCount(activeSchedules.length);
            }
          })
        );
        promises.push(
          getMyPoints(token).then((res) => {
            if (res) {
              const totalReward = res.filter(p => p.type === "REWARD").reduce((sum, p) => sum + p.pointValue, 0);
              const totalViolation = res.filter(p => p.type === "VIOLATION").reduce((sum, p) => sum + p.pointValue, 0);
              setMyCleanlinessPoints(totalReward - totalViolation);
            }
          })
        );
      }

      if (canReadEnglish && token) {
        promises.push(
          englishService.getAllActivities(token).then((res) => {
            if (res) {
              const active = res.filter((a) => a.status === "PUBLISHED");
              setActiveEnglishActivitiesCount(active.length);
            }
          })
        );
      }

      if ((canReadAllEnglishDeposit || canVerifyEnglishDeposit) && token) {
        if (canReadAllEnglishDeposit) {
          promises.push(
            englishService.getAllDeposits(token).then((res) => {
              if (res) {
                const submitted = res.filter((d) => d.status === "SUBMITTED");
                setPendingEnglishDepositsCount(submitted.length);
              }
            })
          );
        }
      } else if (canReadOwnEnglishDeposit && token) {
        promises.push(
          englishService.getMyDeposits(token).then((res) => {
            if (res) {
              const submitted = res.filter((d) => d.status === "SUBMITTED");
              setPendingEnglishDepositsCount(submitted.length);
            }
          })
        );
      }

      const results = await Promise.allSettled(promises);
      const hasError = results.some(r => r.status === "rejected");
      if (hasError) {
        setDashboardWarning("Sebagian data dashboard belum dapat dimuat.");
      }

      setIsLoading(false);
    }

    loadDashboardData();
  }, [user, token, canReadOwnRequest, canApprove, canDisburse, canReadTasks, canReadOwnBorrowing, canManageBorrowing, canReadCleanliness, canReadEnglish, canReadOwnEnglishDeposit, canReadAllEnglishDeposit, canVerifyEnglishDeposit]);

  const renderCount = (count: number | null) => {
    if (isLoading) return "...";
    return count !== null ? count : "—";
  };

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

      {dashboardWarning && (
        <div className="dashboard-warning-wrapper">
          <div className="dashboard-warning" role="status">
            ⚠️ {dashboardWarning}
          </div>
        </div>
      )}

      {/* SUMMARY CARDS */}
      <section className="dashboard-summary-grid">
        {canReadOwnRequest && (
          <div className="dashboard-card stat-card">
            <span className="stat-label">Pengajuan Saya</span>
            <strong className="stat-value">{renderCount(myRequestsCount)}</strong>
            <Link to="/requests" className="stat-link">Lihat semua &rarr;</Link>
          </div>
        )}

        {canApprove && (
          <div className="dashboard-card stat-card stat-warning">
            <span className="stat-label">Approval Menunggu</span>
            <strong className="stat-value">{renderCount(pendingApprovalsCount)}</strong>
            <Link to="/approvals" className="stat-link">Proses sekarang &rarr;</Link>
          </div>
        )}

        {canDisburse && (
          <div className="dashboard-card stat-card stat-success">
            <span className="stat-label">Siap Cair</span>
            <strong className="stat-value">{renderCount(readyDisburseCount)}</strong>
            <Link to="/finance/disbursements" className="stat-link">Menu Pencairan &rarr;</Link>
          </div>
        )}

        {canReadTasks && (
          <div className="dashboard-card stat-card stat-info">
            <span className="stat-label">Tugas Aktif Saya</span>
            <strong className="stat-value">{renderCount(activeTasksCount)}</strong>
            <Link to="/division-tasks" className="stat-link">Lihat Tugas &rarr;</Link>
          </div>
        )}

        {canReadOwnBorrowing && (
          <div className="dashboard-card stat-card stat-info">
            <span className="stat-label">Peminjaman Aktif Saya</span>
            <strong className="stat-value">{renderCount(activeBorrowingsCount)}</strong>
            <Link to="/my-borrowings" className="stat-link">Lihat Detail &rarr;</Link>
          </div>
        )}

        {canManageBorrowing && (
          <div className="dashboard-card stat-card stat-warning">
            <span className="stat-label">Request Aset Baru</span>
            <strong className="stat-value">{renderCount(requestedBorrowingsCount)}</strong>
            <Link to="/asset-operations" className="stat-link">Proses Request &rarr;</Link>
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

        {canReadAssets && (
          <div className="dashboard-card stat-card stat-info">
            <span className="stat-label">Katalog Aset</span>
            <strong className="stat-value">📦</strong>
            <Link to="/assets" className="stat-link">Lihat Aset &rarr;</Link>
          </div>
        )}

        {canReadCleanliness && (
          <div className="dashboard-card stat-card stat-neutral">
            <span className="stat-label">Jadwal Piket Aktif</span>
            <strong className="stat-value">{renderCount(activePicketCount)}</strong>
            <Link to="/picket-schedules" className="stat-link">Lihat Piket &rarr;</Link>
          </div>
        )}

        {canReadCleanliness && (
          <div className="dashboard-card stat-card stat-success">
            <span className="stat-label">Poin Kebersihan Saya</span>
            <strong className="stat-value">{renderCount(myCleanlinessPoints)}</strong>
            <Link to="/picket-schedules" className="stat-link">Detail Point &rarr;</Link>
          </div>
        )}

        {canReadEnglish && (
          <div className="dashboard-card stat-card stat-neutral">
            <span className="stat-label">Aktivitas English</span>
            <strong className="stat-value">{renderCount(activeEnglishActivitiesCount)}</strong>
            <Link to="/english-activities" className="stat-link">Lihat Portal &rarr;</Link>
          </div>
        )}

        {(canReadAllEnglishDeposit || canVerifyEnglishDeposit) ? (
          <div className="dashboard-card stat-card stat-warning">
            <span className="stat-label">Setoran Menunggu Verifikasi</span>
            <strong className="stat-value">{renderCount(pendingEnglishDepositsCount)}</strong>
            <Link to="/english-management" className="stat-link">Proses Sekarang &rarr;</Link>
          </div>
        ) : canReadOwnEnglishDeposit ? (
          <div className="dashboard-card stat-card stat-info">
            <span className="stat-label">Setoran Bahasa Inggris Saya</span>
            <strong className="stat-value">{renderCount(pendingEnglishDepositsCount)}</strong>
            <Link to="/english-activities" className="stat-link">Lihat Status &rarr;</Link>
          </div>
        ) : null}
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
              {canManageOrganization && <Link to="/admin/organization" className="quick-action-btn">⚙️ Kelola Organisasi</Link>}
              {canReadReports && <Link to="/reports" className="quick-action-btn">📊 Buka Laporan</Link>}
              {canReadAssets && <Link to="/assets" className="quick-action-btn">📦 Katalog Aset</Link>}
              {canReadOwnBorrowing && <Link to="/my-borrowings" className="quick-action-btn">📚 Peminjaman Saya</Link>}
              {canManageBorrowing && <Link to="/asset-operations" className="quick-action-btn">🛠️ Operasional Aset</Link>}
              {canReadCleanliness && <Link to="/picket-schedules" className="quick-action-btn">🧹 Jadwal Piket</Link>}
              {canReadEnglish && <Link to="/english-activities" className="quick-action-btn">🗣️ English Portal</Link>}
              {hasPermission("english.activity.manage") && <Link to="/english-management" className="quick-action-btn">📋 Kelola English</Link>}
              {hasPermission("public.content.manage") && <Link to="/public-content-management" className="quick-action-btn">📢 Kelola Konten Publik</Link>}
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
