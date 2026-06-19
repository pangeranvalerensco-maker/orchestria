import { Navigate, Outlet } from "react-router";

import { useAuth } from "../auth/useAuth";

interface PermissionRouteProps {
  anyOf: string[];
}

export function PermissionRoute({ anyOf }: PermissionRouteProps) {
  const { hasPermission } = useAuth();

  if (!anyOf.some((permission) => hasPermission(permission))) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
