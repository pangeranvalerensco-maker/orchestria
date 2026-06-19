import { apiRequest } from "../api/http";
import type {
  FundRequest,
  PageResponse,
  RequestSettlement,
} from "../types/request";
import type {
  CreateDisbursementPayload,
  DisbursementPage,
  FundDisbursement,
} from "../types/finance";

function getRequestsByStatus(token: string, status: string) {
  const params = new URLSearchParams({
    status,
    page: "0",
    size: "100",
    sortBy: "createdAt",
    sortDirection: "asc",
  });

  return apiRequest<PageResponse<FundRequest>>(
    `/api/requests?${params.toString()}`,
    { method: "GET" },
    token,
  );
}

export function getReadyForDisbursement(token: string) {
  return getRequestsByStatus(token, "READY_FOR_DISBURSEMENT");
}

export function getPendingSettlements(token: string) {
  return getRequestsByStatus(token, "SETTLEMENT_SUBMITTED");
}

export function getDisbursements(token: string) {
  return apiRequest<DisbursementPage>(
    "/api/finance/disbursements?page=0&size=100&sortBy=disbursedAt&sortDirection=desc",
    { method: "GET" },
    token,
  );
}

export function createDisbursement(
  token: string,
  payload: CreateDisbursementPayload,
) {
  return apiRequest<FundDisbursement>(
    "/api/finance/disbursements",
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    token,
  );
}

export function approveSettlement(token: string, requestId: number) {
  return apiRequest<RequestSettlement>(
    `/api/requests/${requestId}/settlement/approve`,
    { method: "POST" },
    token,
  );
}

export function requestSettlementRevision(
  token: string,
  requestId: number,
  revisionNote: string,
) {
  return apiRequest<RequestSettlement>(
    `/api/requests/${requestId}/settlement/request-revision`,
    {
      method: "POST",
      body: JSON.stringify({ revisionNote }),
    },
    token,
  );
}
