export interface User {
  id: number;
  fullName: string;
  email: string;
  active: boolean;
  roles: string[];
  permissions: string[];
  twoFactorEnabled?: boolean;
  twoFactorRequired?: boolean;
}

export interface AuthData {
  tokenType: string;
  accessToken: string;
  user: User;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export type LoginStatus = 'AUTHENTICATED' | 'OTP_REQUIRED';

export interface LoginResult {
  status: LoginStatus;
  authData?: AuthData;
  challengeId?: string;
  maskedEmail?: string;
  expiresInSeconds?: number;
  resendAfterSeconds?: number;
}

export interface OtpVerifyPayload {
  challengeId: string;
  code: string;
  rememberDevice?: boolean;
  deviceName?: string;
}

export interface OtpResendPayload {
  challengeId: string;
}

export interface OtpResendResponse {
  resendAfterSeconds?: number;
}

export interface ForgotPasswordPayload {
  email: string;
}

export interface ForgotPasswordVerifyPayload {
  challengeId: string;
  code: string;
}

export interface PasswordResetPayload {
  resetToken: string;
  newPassword: string;
  confirmPassword: string;
}

export interface SecuritySettings {
  twoFactorEnabled: boolean;
  twoFactorRequired: boolean;
  mandatoryByRole: boolean;
  trustedDeviceCount: number;
}

export interface TrustedDeviceResponse {
  id: string;
  deviceName: string;
  userAgent: string;
  lastIpAddress: string;
  createdAt: string;
  lastUsedAt: string;
  expiresAt: string;
}