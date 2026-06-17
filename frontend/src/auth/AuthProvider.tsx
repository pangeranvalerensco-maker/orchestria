import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";

import { ApiError } from "../api/http";
import {
  getCurrentUser,
  loginRequest,
} from "../services/authService";
import type {
  LoginPayload,
  User,
} from "../types/auth";

import { AuthContext } from "./auth-context";
import {
  getAccessToken,
  removeAccessToken,
  saveAccessToken,
} from "./storage";

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({
  children,
}: AuthProviderProps) {
  const [token, setToken] = useState<string | null>(
    () => getAccessToken(),
  );

  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const logout = useCallback(() => {
    removeAccessToken();
    setToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    async function restoreSession() {
      if (!token) {
        setLoading(false);
        return;
      }

      try {
        const response = await getCurrentUser(token);
        setUser(response.data);
      } catch (error) {
        if (
          error instanceof ApiError &&
          (error.status === 401 || error.status === 403)
        ) {
          logout();
        }
      } finally {
        setLoading(false);
      }
    }

    void restoreSession();
  }, [token, logout]);

  const login = useCallback(
    async (payload: LoginPayload) => {
      const response = await loginRequest(payload);
      const authData = response.data;

      saveAccessToken(authData.accessToken);
      setToken(authData.accessToken);
      setUser(authData.user);
    },
    [],
  );

  const hasPermission = useCallback(
    (permission: string) =>
      Boolean(user?.permissions.includes(permission)),
    [user],
  );

  const hasRole = useCallback(
    (role: string) =>
      Boolean(user?.roles.includes(role)),
    [user],
  );

  const value = useMemo(
    () => ({
      token,
      user,
      loading,
      login,
      logout,
      hasPermission,
      hasRole,
    }),
    [
      token,
      user,
      loading,
      login,
      logout,
      hasPermission,
      hasRole,
    ],
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}