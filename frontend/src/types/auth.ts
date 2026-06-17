export interface User {
  id: number;
  fullName: string;
  email: string;
  active: boolean;
  roles: string[];
  permissions: string[];
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