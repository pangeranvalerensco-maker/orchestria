import { apiRequest } from "../api/http";
import type { User } from "../types/auth";
import type {
  RegisterUserPayload,
  RoleSummary,
} from "../types/admin";

export function getAdminUsers(token: string) {
  return apiRequest<User[]>(
    "/api/auth/admin/users",
    { method: "GET" },
    token,
  );
}

export function getAdminRoles(token: string) {
  return apiRequest<RoleSummary[]>(
    "/api/auth/admin/roles",
    { method: "GET" },
    token,
  );
}

export function registerUser(
  token: string,
  payload: RegisterUserPayload,
) {
  return apiRequest<User>(
    "/api/auth/register",
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    token,
  );
}

export function assignUserRole(
  token: string,
  userId: number,
  roleName: string,
) {
  return apiRequest<User>(
    `/api/auth/admin/users/${userId}/roles/${encodeURIComponent(roleName)}`,
    { method: "POST" },
    token,
  );
}

export function removeUserRole(
  token: string,
  userId: number,
  roleName: string,
) {
  return apiRequest<User>(
    `/api/auth/admin/users/${userId}/roles/${encodeURIComponent(roleName)}`,
    { method: "DELETE" },
    token,
  );
}
