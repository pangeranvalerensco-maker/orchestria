import { apiRequest } from "../api/http";
import type {
  AuthData,
  LoginPayload,
  LoginResult,
  OtpVerifyPayload,
  OtpResendPayload,
  OtpResendResponse,
  ForgotPasswordPayload,
  ForgotPasswordStartResponse,
  ForgotPasswordVerifyPayload,
  ForgotPasswordVerifyResponse,
  PasswordResetPayload,
  SecuritySettings,
  User,
  TrustedDeviceResponse,
} from "../types/auth";

export function loginRequest(payload: LoginPayload) {
  return apiRequest<LoginResult>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function verifyOtp(payload: OtpVerifyPayload) {
  return apiRequest<AuthData>("/api/auth/otp/verify", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function resendOtp(payload: OtpResendPayload) {
  return apiRequest<OtpResendResponse>("/api/auth/otp/resend", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function forgotPassword(payload: ForgotPasswordPayload) {
  return apiRequest<ForgotPasswordStartResponse>("/api/auth/password/forgot", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function verifyForgotPassword(payload: ForgotPasswordVerifyPayload) {
  return apiRequest<ForgotPasswordVerifyResponse>("/api/auth/password/forgot/verify", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function resetPassword(payload: PasswordResetPayload) {
  return apiRequest<string>("/api/auth/password/reset", {
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

export function getSecuritySettings(token: string) {
  return apiRequest<SecuritySettings>("/api/auth/security", { method: "GET" }, token);
}

export function requestEnableTwoFactor(token: string) {
  return apiRequest<string>("/api/auth/2fa/enable/request", { method: "POST" }, token);
}

export function confirmEnableTwoFactor(token: string, payload: OtpVerifyPayload) {
  return apiRequest<string>("/api/auth/2fa/enable/confirm", {
    method: "POST",
    body: JSON.stringify(payload),
  }, token);
}

export function requestDisableTwoFactor(token: string) {
  return apiRequest<string>("/api/auth/2fa/disable/request", { method: "POST" }, token);
}

export function confirmDisableTwoFactor(token: string, payload: OtpVerifyPayload) {
  return apiRequest<string>("/api/auth/2fa/disable/confirm", {
    method: "POST",
    body: JSON.stringify(payload),
  }, token);
}

export function getTrustedDevices(token: string) {
  return apiRequest<TrustedDeviceResponse[]>("/api/auth/trusted-devices", { method: "GET" }, token);
}

export function revokeTrustedDevice(token: string, id: string) {
  return apiRequest<string>(`/api/auth/trusted-devices/${id}`, { method: "DELETE" }, token);
}

export function revokeAllTrustedDevices(token: string) {
  return apiRequest<string>("/api/auth/trusted-devices", { method: "DELETE" }, token);
}

export function logoutRequest() {
  return apiRequest<string>("/api/auth/logout", { method: "POST" });
}