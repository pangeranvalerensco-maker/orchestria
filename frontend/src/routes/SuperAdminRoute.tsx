import { Navigate, Outlet } from "react-router";

import { useAuth } from "../auth/useAuth";

export function SuperAdminRoute() {
  const { hasRole } = useAuth();

  if (!hasRole("SUPER_ADMIN")) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
