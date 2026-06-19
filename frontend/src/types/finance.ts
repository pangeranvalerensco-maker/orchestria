import type { PageResponse } from "./request";

export type DisbursementMethod = "CASH" | "BANK_TRANSFER" | "E_WALLET";
export type DisbursementStatus = "DISBURSED" | "CANCELLED";
export type RequestSyncStatus = "PENDING" | "SYNCED" | "FAILED";

export interface FundDisbursement {
  id: number;
  fundRequestId: number;
  requestTitle: string;
  divisionId: number;
  divisionName: string;
  requesterName: string;
  amount: number;
  method: DisbursementMethod;
  status: DisbursementStatus;
  requestSyncStatus: RequestSyncStatus;
  requestSyncError: string | null;
  requestSyncedAt: string | null;
  requestSyncAttempts: number;
  receiverName: string;
  receiverNote: string | null;
  proofUrl: string | null;
  note: string | null;
  disbursedByEmail: string;
  disbursedAt: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDisbursementPayload {
  fundRequestId: number;
  method: DisbursementMethod;
  receiverName: string;
  receiverNote?: string;
  proofUrl?: string;
  note?: string;
}

export type DisbursementPage = PageResponse<FundDisbursement>;
