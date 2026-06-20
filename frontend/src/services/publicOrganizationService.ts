import { apiRequest } from "../api/http";
import type { PublicOrganizationStructureResponse, PublicPeriodResponse } from "../types/publicOrganization";

export function getPublicStructureCurrent() {
    return apiRequest<PublicOrganizationStructureResponse>(
        `/api/organization/public/structure/current`,
        { method: "GET" },
        undefined // No token for public endpoints
    );
}

export function getPublicPeriods() {
    return apiRequest<PublicPeriodResponse[]>(
        `/api/organization/public/periods`,
        { method: "GET" },
        undefined // No token for public endpoints
    );
}
