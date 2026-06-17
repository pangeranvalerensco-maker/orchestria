import { apiRequest } from "../api/http";
import type {
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