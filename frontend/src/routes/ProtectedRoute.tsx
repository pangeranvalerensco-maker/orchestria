import {
  Navigate,
  Outlet,
} from "react-router";

import { useAuth } from "../auth/useAuth";

export function ProtectedRoute() {
  const {
    token,
    user,
    loading,
  } = useAuth();

  if (loading) {
    return (
      <main className="center-screen">
        <div className="loading-card">
          <div className="spinner" />
          <p>Memuat sesi Orchestria...</p>
        </div>
      </main>
    );
  }

  if (!token || !user) {
    return (
      <Navigate
        to="/login"
        replace
      />
    );
  }

  return <Outlet />;
}