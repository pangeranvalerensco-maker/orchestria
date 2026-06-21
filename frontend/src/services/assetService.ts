import { apiRequest } from "../api/http";
import type {
  Asset,
  AssetRequest,
  AssetConditionUpdateRequest,
  ConditionHistory,
  Borrowing,
  BorrowingCreateRequest,
  BorrowingDecisionRequest,
  AssetHandoverRequest,
  AssetReturnRequest,
  AssetReturnVerificationRequest,
  AssetStatus,
  AssetCondition,
  BorrowingStatus,
} from "../types/asset";

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

const ASSET_BASE_URL = "/api/organization/assets";
const BORROWING_BASE_URL = "/api/organization/asset-borrowings";

export const getAssets = async (
  token: string,
  search?: string,
  status?: AssetStatus,
  condition?: AssetCondition,
  page = 0,
  size = 10
): Promise<PaginatedResponse<Asset>> => {
  const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
  if (search) params.append("search", search);
  if (status) params.append("status", status);
  if (condition) params.append("condition", condition);

  const response = await apiRequest<PaginatedResponse<Asset>>(`${ASSET_BASE_URL}?${params.toString()}`, {
    method: "GET",
  }, token);
  return response.data;
};

export const getAssetById = async (token: string, id: string): Promise<Asset> => {
  const response = await apiRequest<Asset>(`${ASSET_BASE_URL}/${id}`, {
    method: "GET",
  }, token);
  return response.data;
};

export const createAsset = async (token: string, data: AssetRequest): Promise<Asset> => {
  const response = await apiRequest<Asset>(ASSET_BASE_URL, {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
  return response.data;
};

export const updateAsset = async (token: string, id: string, data: AssetRequest): Promise<Asset> => {
  const response = await apiRequest<Asset>(`${ASSET_BASE_URL}/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  }, token);
  return response.data;
};

export const deleteAsset = async (token: string, id: string): Promise<void> => {
  await apiRequest<void>(`${ASSET_BASE_URL}/${id}`, {
    method: "DELETE",
  }, token);
};

export const updateAssetCondition = async (token: string, id: string, data: AssetConditionUpdateRequest): Promise<Asset> => {
  const response = await apiRequest<Asset>(`${ASSET_BASE_URL}/${id}/condition`, {
    method: "PATCH",
    body: JSON.stringify(data),
  }, token);
  return response.data;
};

export const getConditionHistories = async (token: string, id: string): Promise<ConditionHistory[]> => {
  const response = await apiRequest<ConditionHistory[]>(`${ASSET_BASE_URL}/${id}/condition-histories`, {
    method: "GET",
  }, token);
  return response.data;
};

export const createBorrowing = async (token: string, data: BorrowingCreateRequest): Promise<Borrowing> => {
  const response = await apiRequest<Borrowing>(BORROWING_BASE_URL, {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
  return response.data;
};

export const getMyBorrowings = async (
  token: string,
  status?: BorrowingStatus,
  page = 0,
  size = 10
): Promise<PaginatedResponse<Borrowing>> => {
  const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
  if (status) params.append("status", status);

  const response = await apiRequest<PaginatedResponse<Borrowing>>(`${BORROWING_BASE_URL}/my?${params.toString()}`, {
    method: "GET",
  }, token);
  return response.data;
};

export const getAllBorrowings = async (
  token: string,
  status?: BorrowingStatus,
  assetId?: string,
  borrowerName?: string,
  page = 0,
  size = 10
): Promise<PaginatedResponse<Borrowing>> => {
  const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
  if (status) params.append("status", status);
  if (assetId) params.append("assetId", assetId);
  if (borrowerName) params.append("borrowerName", borrowerName);

  const response = await apiRequest<PaginatedResponse<Borrowing>>(`${BORROWING_BASE_URL}?${params.toString()}`, {
    method: "GET",
  }, token);
  return response.data;
};

export const getBorrowingById = async (token: string, id: string): Promise<Borrowing> => {
  const response = await apiRequest<Borrowing>(`${BORROWING_BASE_URL}/${id}`, {
    method: "GET",
  }, token);
  return response.data;
};

export const approveBorrowing = async (token: string, id: string): Promise<Borrowing> => {
  const response = await apiRequest<Borrowing>(`${BORROWING_BASE_URL}/${id}/approve`, {
    method: "POST",
  }, token);
  return response.data;
};

export const rejectBorrowing = async (token: string, id: string, data: BorrowingDecisionRequest): Promise<Borrowing> => {
  const response = await apiRequest<Borrowing>(`${BORROWING_BASE_URL}/${id}/reject`, {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
  return response.data;
};

export const cancelBorrowing = async (token: string, id: string, data: BorrowingDecisionRequest): Promise<Borrowing> => {
  const response = await apiRequest<Borrowing>(`${BORROWING_BASE_URL}/${id}/cancel`, {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
  return response.data;
};

export const cancelApprovedBorrowing = async (token: string, id: string, data: BorrowingDecisionRequest): Promise<Borrowing> => {
  const response = await apiRequest<Borrowing>(`${BORROWING_BASE_URL}/${id}/cancel-approved`, {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
  return response.data;
};

export const handoverAsset = async (token: string, id: string, data: AssetHandoverRequest): Promise<Borrowing> => {
  const response = await apiRequest<Borrowing>(`${BORROWING_BASE_URL}/${id}/handover`, {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
  return response.data;
};

export const requestReturnAsset = async (token: string, id: string, data: AssetReturnRequest): Promise<Borrowing> => {
  const response = await apiRequest<Borrowing>(`${BORROWING_BASE_URL}/${id}/return`, {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
  return response.data;
};

export const verifyReturnAsset = async (token: string, id: string, data: AssetReturnVerificationRequest): Promise<Borrowing> => {
  const response = await apiRequest<Borrowing>(`${BORROWING_BASE_URL}/${id}/verify-return`, {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
  return response.data;
};
