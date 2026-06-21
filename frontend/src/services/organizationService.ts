import { apiRequest } from "../api/http";
import type {
  CurrentMemberContext,
  MemberResponse,
  DivisionResponse,
  PositionResponse,
  MemberRequest,
  DivisionRequest,
  PositionRequest,
  OrganizationPeriodRequest,
  OrganizationPeriodResponse,
  MemberAssignmentRequest,
  MemberAssignment,
} from "../types/organization";

export function getCurrentMemberContext(
  token: string,
) {
  return apiRequest<CurrentMemberContext>(
    "/api/organization/members/me/context",
    {
      method: "GET",
    },
    token,
  );
}

// MEMBERS
export function getMembers(token: string) {
  return apiRequest<MemberResponse[]>("/api/organization/members", { method: "GET" }, token);
}

export function createMember(token: string, data: MemberRequest) {
  return apiRequest<MemberResponse>("/api/organization/members", {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
}

export function updateMember(token: string, id: number, data: MemberRequest) {
  return apiRequest<MemberResponse>(`/api/organization/members/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  }, token);
}

export function deleteMember(token: string, id: number) {
  return apiRequest<void>(`/api/organization/members/${id}`, { method: "DELETE" }, token);
}

// DIVISIONS
export function getDivisions(token: string) {
  return apiRequest<DivisionResponse[]>("/api/organization/divisions", { method: "GET" }, token);
}

export function createDivision(token: string, data: DivisionRequest) {
  return apiRequest<DivisionResponse>("/api/organization/divisions", {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
}

export function updateDivision(token: string, id: number, data: DivisionRequest) {
  return apiRequest<DivisionResponse>(`/api/organization/divisions/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  }, token);
}

export function deleteDivision(token: string, id: number) {
  return apiRequest<void>(`/api/organization/divisions/${id}`, { method: "DELETE" }, token);
}

// POSITIONS
export function getPositions(token: string) {
  return apiRequest<PositionResponse[]>("/api/organization/positions", { method: "GET" }, token);
}

export function createPosition(token: string, data: PositionRequest) {
  return apiRequest<PositionResponse>("/api/organization/positions", {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
}

export function updatePosition(token: string, id: number, data: PositionRequest) {
  return apiRequest<PositionResponse>(`/api/organization/positions/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  }, token);
}

export function deletePosition(token: string, id: number) {
  return apiRequest<void>(`/api/organization/positions/${id}`, { method: "DELETE" }, token);
}

// PERIODS
export function getPeriods(token: string) {
  return apiRequest<OrganizationPeriodResponse[]>("/api/organization/periods", { method: "GET" }, token);
}

export function getCurrentPeriod(token: string) {
  return apiRequest<OrganizationPeriodResponse>("/api/organization/periods/current", { method: "GET" }, token);
}

export function createPeriod(token: string, data: OrganizationPeriodRequest) {
  return apiRequest<OrganizationPeriodResponse>("/api/organization/periods", {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
}

export function updatePeriod(token: string, id: number, data: OrganizationPeriodRequest) {
  return apiRequest<OrganizationPeriodResponse>(`/api/organization/periods/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  }, token);
}

export function deletePeriod(token: string, id: number) {
  return apiRequest<void>(`/api/organization/periods/${id}`, { method: "DELETE" }, token);
}

// MEMBER ASSIGNMENTS
export function getMemberAssignments(token: string) {
  return apiRequest<MemberAssignment[]>("/api/organization/member-assignments", { method: "GET" }, token);
}

export function getMemberAssignmentsByPeriod(token: string, periodId: number) {
  return apiRequest<MemberAssignment[]>(`/api/organization/member-assignments/period/${periodId}`, { method: "GET" }, token);
}

export function getMemberAssignmentsByPeriodAndDivision(token: string, periodId: number, divisionId: number) {
  return apiRequest<MemberAssignment[]>(`/api/organization/member-assignments/period/${periodId}/division/${divisionId}`, { method: "GET" }, token);
}

export function createMemberAssignment(token: string, data: MemberAssignmentRequest) {
  return apiRequest<MemberAssignment>("/api/organization/member-assignments", {
    method: "POST",
    body: JSON.stringify(data),
  }, token);
}

export function updateMemberAssignment(token: string, id: number, data: MemberAssignmentRequest) {
  return apiRequest<MemberAssignment>(`/api/organization/member-assignments/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  }, token);
}

export function deleteMemberAssignment(token: string, id: number) {
  return apiRequest<void>(`/api/organization/member-assignments/${id}`, { method: "DELETE" }, token);
}