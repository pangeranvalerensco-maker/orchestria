import type { ApiResponse } from "../types/auth";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8000";

interface BackendError {
  message?: string;
  errors?: unknown;
}

export class ApiError extends Error {
  status: number;
  errors?: unknown;

  constructor(
    message: string,
    status: number,
    errors?: unknown,
  ) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.errors = errors;
  }
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
  token?: string | null,
): Promise<ApiResponse<T>> {
  const headers = new Headers(options.headers);

  headers.set("Accept", "application/json");

  if (options.body && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  const responseText = await response.text();

  let responseBody: unknown = null;

  if (responseText) {
    try {
      responseBody = JSON.parse(responseText);
    } catch {
      responseBody = null;
    }
  }

  if (!response.ok) {
    const backendError = responseBody as BackendError | null;

    throw new ApiError(
      backendError?.message ??
        `Request gagal dengan status ${response.status}`,
      response.status,
      backendError?.errors,
    );
  }

  return responseBody as ApiResponse<T>;
}