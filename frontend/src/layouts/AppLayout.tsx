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
              {hasPermission("archive.manage") && (
                <NavLink to="/archive" className={navClass}>Arsip Dokumen</NavLink>
              )}
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
