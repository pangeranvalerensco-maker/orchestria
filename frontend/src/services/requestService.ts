import { apiRequest } from "../api/http";
import type {
  CreateFundRequestPayload,
  CreateRequestItemPayload,
  FundRequest,
  PageResponse,
} from "../types/request";

export function getMyRequests(
  token: string,
  page = 0,
  size = 10,
) {
  const searchParams = new URLSearchParams({
    page: String(page),
    size: String(size),
    sortBy: "createdAt",
    sortDirection: "desc",
  });

  return apiRequest<PageResponse<FundRequest>>(
    `/api/requests/my?${searchParams.toString()}`,
    {
      method: "GET",
    },
    token,
  );
}

export function createFundRequest(
  token: string,
  payload: CreateFundRequestPayload,
) {
  return apiRequest<FundRequest>(
    "/api/requests",
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    token,
  );
}

export function getMyRequestById(
  token: string,
  requestId: number,
) {
  return apiRequest<FundRequest>(
    `/api/requests/my/${requestId}`,
    {
      method: "GET",
    },
    token,
  );
}

export function addRequestItem(
  token: string,
  requestId: number,
  payload: CreateRequestItemPayload,
) {
  return apiRequest<FundRequest>(
    `/api/requests/${requestId}/items`,
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    token,
  );
}

export function submitFundRequest(
  token: string,
  requestId: number,
) {
  return apiRequest<FundRequest>(
    `/api/requests/${requestId}/submit`,
    {
      method: "POST",
    },
    token,
  );
}