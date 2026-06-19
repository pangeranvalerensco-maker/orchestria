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
  const canUseFinance = hasPermission("finance.disburse")
    || hasPermission("finance.report.read");
  const canVerifySettlement = hasPermission("finance.settlement.verify");

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="sidebar-logo">O</div>
          <div><strong>Orchestria</strong><small>Organization OS</small></div>
        </div>

        <nav className="sidebar-nav">
          <NavLink to="/dashboard" className={navClass}>Dashboard</NavLink>
          <NavLink to="/requests" className={navClass}>Pengajuan Dana</NavLink>
          {canApprove ? <NavLink to="/approvals" className={navClass}>Approval</NavLink> : <span className="nav-item disabled">Approval</span>}
          {canUseFinance ? <NavLink to="/finance/disbursements" className={navClass}>Pencairan</NavLink> : <span className="nav-item disabled">Pencairan</span>}
          {canVerifySettlement ? <NavLink to="/finance/settlements" className={navClass}>Settlement</NavLink> : <span className="nav-item disabled">Settlement</span>}
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
