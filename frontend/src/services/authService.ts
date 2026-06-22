import { apiRequest } from "../api/http";
import type {
  AuthData,
  LoginPayload,
  LoginResult,
  OtpVerifyPayload,
  OtpResendPayload,
  OtpResendResponse,
  ForgotPasswordPayload,
  ForgotPasswordVerifyPayload,
  PasswordResetPayload,
  SecuritySettings,
  User,
} from "../types/auth";

export function loginRequest(payload: LoginPayload) {
  return apiRequest<LoginResult>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function verifyLoginOtp(payload: OtpVerifyPayload) {
  return apiRequest<AuthData>("/api/auth/login/verify", {
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
  return apiRequest<string>("/api/auth/password/forgot", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function verifyForgotPassword(payload: ForgotPasswordVerifyPayload) {
  return apiRequest<any>("/api/auth/password/forgot/verify", {
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
  return apiRequest<string>("/api/auth/security/2fa/enable", { method: "POST" }, token);
}

export function confirmEnableTwoFactor(token: string, payload: OtpVerifyPayload) {
  return apiRequest<string>("/api/auth/security/2fa/enable/confirm", {
    method: "POST",
    body: JSON.stringify(payload),
  }, token);
}

export function requestDisableTwoFactor(token: string) {
  return apiRequest<string>("/api/auth/security/2fa/disable", { method: "POST" }, token);
}

export function confirmDisableTwoFactor(token: string, payload: OtpVerifyPayload) {
  return apiRequest<string>("/api/auth/security/2fa/disable/confirm", {
    method: "POST",
    body: JSON.stringify(payload),
  }, token);
}