export type RequestPriority =
  | "LOW"
  | "MEDIUM"
  | "HIGH"
  | "URGENT";

export type FundRequestStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "DIVISION_APPROVED"
  | "PUB_APPROVED"
  | "PEMBINA_APPROVED"
  | "REVISION_REQUESTED"
  | "REJECTED"
  | "READY_FOR_DISBURSEMENT"
  | "DISBURSED"
  | "FUND_RECEIVED"
  | "SETTLEMENT_SUBMITTED"
  | "SETTLEMENT_APPROVED"
  | "COMPLETED"
  | "CANCELLED";

export interface RequestItem {
  id: number;
  itemName: string;
  description: string | null;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface FundRequest {
  id: number;
  divisionId: number;
  divisionName: string;
  requesterMemberId: number;
  requesterName: string;
  requesterAuthUserId: number;
  title: string;
  description: string | null;
  activityDate: string | null;
  priority: RequestPriority;
  status: FundRequestStatus;
  totalAmount: number;
  submittedAt: string | null;
  completedAt: string | null;
  active: boolean;
  createdByEmail: string;
  updatedByEmail: string | null;
  createdAt: string;
  updatedAt: string;
  items: RequestItem[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface CreateFundRequestPayload {
  divisionId: number;
  title: string;
  description?: string;
  activityDate?: string;
  priority: RequestPriority;
}