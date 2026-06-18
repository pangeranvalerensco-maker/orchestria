import { apiRequest } from "../api/http";
import type {
  CurrentMemberContext,
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