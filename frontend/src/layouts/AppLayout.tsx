import { NavLink, Outlet, useNavigate } from "react-router";

import { useAuth } from "../auth/useAuth";

function navClass({ isActive }: { isActive: boolean }) {
  return isActive ? "nav-item active" : "nav-item";
}

export function AppLayout() {
  const navigate = useNavigate();
  const { user, logout, hasPermission } = useAuth();

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  const canApprove = hasPermission("request.approve.division")
    || hasPermission("request.approve.pub")
    || hasPermission("request.approve.pembina");
  const canDisburse = hasPermission("finance.disburse");
  const canVerifySettlement = hasPermission("finance.settlement.verify");

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="sidebar-logo">O</div>
          <div><strong>Orchestria</strong><small>Organization OS</small></div>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-group">
            <span className="nav-group-title">Dashboard</span>
            <NavLink to="/dashboard" className={navClass}>Dashboard</NavLink>
          </div>

          <div className="nav-group">
            <span className="nav-group-title">Keuangan</span>
            <NavLink to="/requests" className={navClass}>Pengajuan Dana</NavLink>
            {canApprove && <NavLink to="/approvals" className={navClass}>Approval</NavLink>}
            {canDisburse && <NavLink to="/finance/disbursements" className={navClass}>Pencairan</NavLink>}
            {canVerifySettlement && <NavLink to="/finance/settlements" className={navClass}>Verifikasi Laporan</NavLink>}
          </div>

          {hasPermission("organization.read") && (
            <div className="nav-group">
              <span className="nav-group-title">Organisasi</span>
              <NavLink to="/organization" className={navClass}>Direktori Organisasi</NavLink>
            </div>
          )}

          {hasPermission("division.task.read") && (
            <div className="nav-group">
              <span className="nav-group-title">Aktivitas Divisi</span>
              <NavLink to="/division-tasks" className={navClass}>Tugas Divisi</NavLink>
            </div>
          )}

          {hasPermission("asset.read") && (
            <div className="nav-group">
              <span className="nav-group-title">Aset Organisasi</span>
              <NavLink to="/assets" className={navClass}>Katalog Aset</NavLink>
              {hasPermission("asset.borrow.read.own") && (
                <NavLink to="/my-borrowings" className={navClass}>Peminjaman Saya</NavLink>
              )}
              {hasPermission("asset.borrow.read.all") && (
                <NavLink to="/asset-operations" className={navClass}>Operasional Aset</NavLink>
              )}
            </div>
          )}

          {hasPermission("cleanliness.schedule.read") && (
            <div className="nav-group">
              <span className="nav-group-title">Kebersihan</span>
              <NavLink to="/picket-schedules" className={navClass}>Jadwal Piket</NavLink>
              {hasPermission("cleanliness.report.read") && (
                <NavLink to="/picket-reports" className={navClass}>Laporan Piket</NavLink>
              )}
            </div>
          )}

          {(hasPermission("english.activity.read") || hasPermission("english.deposit.read.own")) && (
            <div className="nav-group">
              <span className="nav-group-title">English Activity</span>
              <NavLink to="/english-activities" className={navClass}>English Portal</NavLink>
              {(hasPermission("english.activity.manage") || hasPermission("english.deposit.verify") || hasPermission("english.deposit.read.all")) && (
                <NavLink to="/english-management" className={navClass}>Management</NavLink>
              )}
            </div>
          )}

          {hasPermission("archive.manage") && (
            <div className="nav-group">
              <span className="nav-group-title">Administrasi</span>
              <NavLink to="/archive" className={navClass}>Arsip Dokumen</NavLink>
            </div>
          )}

          {hasPermission("public.content.manage") && (
            <div className="nav-group">
              <span className="nav-group-title">Humas</span>
              <NavLink to="/public-content-management" className={navClass}>Kelola Konten Publik</NavLink>
            </div>
          )}

          {hasPermission("organization.manage") && (
            <div className="nav-group">
              <span className="nav-group-title">Manajemen</span>
              <NavLink to="/admin/organization" className={navClass}>Kelola Organisasi</NavLink>
            </div>
          )}

          {hasPermission("request.read.all") && (
            <div className="nav-group">
              <span className="nav-group-title">Laporan</span>
              <NavLink to="/reports" className={navClass}>Laporan Pengajuan</NavLink>
            </div>
          )}
        </nav>

        <div className="sidebar-user">
          <div className="avatar">{user?.fullName?.charAt(0).toUpperCase()}</div>
          <div className="sidebar-user-info"><strong>{user?.fullName}</strong><small>{user?.email}</small></div>
          <button className="logout-button" type="button" onClick={handleLogout}>Keluar</button>
        </div>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <div><p className="eyebrow">ORCHESTRIA</p><strong>Sistem Operasional Organisasi</strong></div>
          <span className="status-badge">Akun aktif</span>
        </header>
        <Outlet />
      </div>
    </div>
  );
}

