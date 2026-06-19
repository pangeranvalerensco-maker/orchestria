import { apiRequest } from "../api/http";
import type {
  ApprovalAction,
  CreateFundRequestPayload,
  CreateRequestItemPayload,
  FundRequest,
  PageResponse,
  ProcessApprovalPayload,
  RequestSettlement,
  SubmitSettlementPayload,
} from "../types/request";

export function getMyRequests(token: string, page = 0, size = 10) {
  const searchParams = new URLSearchParams({
    page: String(page),
    size: String(size),
    sortBy: "createdAt",
    sortDirection: "desc",
  });
  return apiRequest<PageResponse<FundRequest>>(`/api/requests/my?${searchParams.toString()}`, { method: "GET" }, token);
}

export function createFundRequest(token: string, payload: CreateFundRequestPayload) {
  return apiRequest<FundRequest>("/api/requests", { method: "POST", body: JSON.stringify(payload) }, token);
}

export function getMyRequestById(token: string, requestId: number) {
  return apiRequest<FundRequest>(`/api/requests/my/${requestId}`, { method: "GET" }, token);
}

export function addRequestItem(token: string, requestId: number, payload: CreateRequestItemPayload) {
  return apiRequest<FundRequest>(`/api/requests/${requestId}/items`, { method: "POST", body: JSON.stringify(payload) }, token);
}

export function submitFundRequest(token: string, requestId: number) {
  return apiRequest<FundRequest>(`/api/requests/${requestId}/submit`, { method: "POST" }, token);
}

export function confirmFundReceived(token: string, requestId: number) {
  return apiRequest<FundRequest>(`/api/requests/${requestId}/confirm-received`, { method: "POST" }, token);
}

export function submitSettlement(token: string, requestId: number, payload: SubmitSettlementPayload) {
  return apiRequest<RequestSettlement>(`/api/requests/${requestId}/settlement`, {
    method: "POST",
    body: JSON.stringify(payload),
  }, token);
}

export function resubmitSettlement(token: string, requestId: number, payload: SubmitSettlementPayload) {
  return apiRequest<RequestSettlement>(`/api/requests/${requestId}/settlement`, {
    method: "PUT",
    body: JSON.stringify(payload),
  }, token);
}

export function getSettlementDetail(token: string, requestId: number) {
  return apiRequest<RequestSettlement>(`/api/requests/${requestId}/settlement`, {
    method: "GET",
  }, token);
}

export function getPendingApprovals(token: string) {
  return apiRequest<FundRequest[]>("/api/requests/pending-approvals", { method: "GET" }, token);
}

export function processApproval(
  token: string,
  requestId: number,
  action: ApprovalAction,
  payload: ProcessApprovalPayload,
) {
  return apiRequest<FundRequest>(`/api/requests/${requestId}/approvals/${action}`, {
    method: "POST",
    body: JSON.stringify(payload),
  }, token);
}
