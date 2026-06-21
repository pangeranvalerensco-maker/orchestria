export type AssetStatus = "AVAILABLE" | "RESERVED" | "BORROWED" | "MAINTENANCE" | "LOST" | "INACTIVE";

export type AssetCondition = "GOOD" | "MINOR_DAMAGE" | "DAMAGED" | "UNKNOWN";

export type BorrowingStatus = "REQUESTED" | "APPROVED" | "REJECTED" | "BORROWED" | "RETURN_REQUESTED" | "RETURN_VERIFIED" | "CANCELLED";

export interface Asset {
  id: string;
  assetCode: string;
  assetName: string;
  category: string;
  description?: string;
  currentStatus: AssetStatus;
  currentCondition: AssetCondition;
  location?: string;
  responsibleMemberId?: number;
  imageUrl?: string;
  active: boolean;
  available: boolean;
  activeBorrowingId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AssetRequest {
  assetCode: string;
  assetName: string;
  category: string;
  description?: string;
  currentCondition: AssetCondition;
  location?: string;
  responsibleMemberId?: number;
  imageUrl?: string;
}

export interface AssetConditionUpdateRequest {
  newCondition: AssetCondition;
  newStatus: AssetStatus;
  note?: string;
}

export interface ConditionHistory {
  id: string;
  assetId: string;
  borrowingId?: string;
  oldCondition: AssetCondition;
  newCondition: AssetCondition;
  checkedByEmail: string;
  note?: string;
  checkedAt: string;
  createdAt: string;
}

export interface Borrowing {
  id: string;
  asset: Asset;
  borrowerMemberId: number;
  borrowerAuthUserId?: number;
  borrowerName: string;
  borrowerEmail: string;
  purpose: string;
  borrowDate: string;
  expectedReturnDate: string;
  actualReturnDate?: string;
  status: BorrowingStatus;
  overdue: boolean;
  rejectionReason?: string;
  cancellationReason?: string;
  approvedByEmail?: string;
  approvedAt?: string;
  handedOverByEmail?: string;
  handedOverAt?: string;
  returnRequestedAt?: string;
  returnVerifiedByEmail?: string;
  returnVerifiedAt?: string;
  conditionBefore?: AssetCondition;
  conditionAfter?: AssetCondition;
  handoverProofUrl?: string;
  returnProofUrl?: string;
  note?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BorrowingCreateRequest {
  assetId: string;
  purpose: string;
  borrowDate: string;
  expectedReturnDate: string;
}

export interface BorrowingDecisionRequest {
  reason: string;
}

export interface AssetHandoverRequest {
  conditionBefore: AssetCondition;
  handoverProofUrl: string;
  note?: string;
}

export interface AssetReturnRequest {
  returnProofUrl: string;
  note?: string;
}

export interface AssetReturnVerificationRequest {
  conditionAfter: AssetCondition;
  note?: string;
}
