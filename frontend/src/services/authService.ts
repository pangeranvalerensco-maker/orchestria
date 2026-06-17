import { apiRequest } from "../api/http";
import type {
  AuthData,
  LoginPayload,
  User,
} from "../types/auth";

export function loginRequest(payload: LoginPayload) {
  return apiRequest<AuthData>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function getCurrentUser(token: string) {
  return apiRequest<User>(
    "/api/auth/me",
    {
      method: "GET",
    },
    token,
  );
}