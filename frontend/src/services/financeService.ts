import { apiRequest } from "../api/http";
import type { FundRequest, PageResponse } from "../types/request";
import type {
  CreateDisbursementPayload,
  DisbursementPage,
  FundDisbursement,
} from "../types/finance";

export function getReadyForDisbursement(token: string) {
  const params = new URLSearchParams({
    status: "READY_FOR_DISBURSEMENT",
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
