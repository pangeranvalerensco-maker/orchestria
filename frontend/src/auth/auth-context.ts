import { createContext } from "react";
import type {
  LoginPayload,
  User,
} from "../types/auth";

export interface AuthContextValue {
  token: string | null;
  user: User | null;
  loading: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  logout: () => void;
  hasPermission: (permission: string) => boolean;
  hasRole: (role: string) => boolean;
}

export const AuthContext =
  createContext<AuthContextValue | undefined>(undefined);