import { apiRequest } from "../api/http";
import type {
  CurrentMemberContext,
  MemberResponse,
  DivisionResponse,
  PositionResponse,
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

export function getMembers(token: string) {
  return apiRequest<MemberResponse[]>("/api/organization/members", { method: "GET" }, token);
}

export function getDivisions(token: string) {
  return apiRequest<DivisionResponse[]>("/api/organization/divisions", { method: "GET" }, token);
}

export function getPositions(token: string) {
  return apiRequest<PositionResponse[]>("/api/organization/positions", { method: "GET" }, token);
}