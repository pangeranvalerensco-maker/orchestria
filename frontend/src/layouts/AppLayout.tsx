import {
  NavLink,
  Outlet,
  useNavigate,
} from "react-router";

import { useAuth } from "../auth/useAuth";

export function AppLayout() {
  const navigate = useNavigate();

  const {
    user,
    logout,
    hasPermission,
  } = useAuth();

  function handleLogout() {
    logout();
    navigate("/login", {
      replace: true,
    });
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="sidebar-logo">O</div>

          <div>
            <strong>Orchestria</strong>
            <small>Organization OS</small>
          </div>
        </div>

        <nav className="sidebar-nav">
          <NavLink
            to="/dashboard"
            className={({ isActive }) =>
              isActive
                ? "nav-item active"
                : "nav-item"
            }
          >
            Dashboard
          </NavLink>

          <NavLink
            to="/requests"
            className={({ isActive }) =>
                isActive
                ? "nav-item active"
                : "nav-item"
            }
            >
            Pengajuan Dana
            </NavLink>

          {hasPermission("request.approve.division")
              || hasPermission("request.approve.pub")
              || hasPermission("request.approve.pembina") ? (
              <NavLink
                to="/approvals"
                className={({ isActive }) =>
                  isActive
                    ? "nav-item active"
                    : "nav-item"
                }
              >
                Approval
              </NavLink>
            ) : (
              <span className="nav-item disabled">
                Approval
              </span>
            )}

          <span className="nav-item disabled">
            Keuangan
          </span>
        </nav>

        <div className="sidebar-user">
          <div className="avatar">
            {user?.fullName
              ?.charAt(0)
              .toUpperCase()}
          </div>

          <div className="sidebar-user-info">
            <strong>{user?.fullName}</strong>
            <small>{user?.email}</small>
          </div>

          <button
            className="logout-button"
            type="button"
            onClick={handleLogout}
          >
            Keluar
          </button>
        </div>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <div>
            <p className="eyebrow">ORCHESTRIA</p>
            <strong>Sistem Operasional Organisasi</strong>
          </div>

          <span className="status-badge">
            Akun aktif
          </span>
        </header>

        <Outlet />
      </div>
    </div>
  );
}