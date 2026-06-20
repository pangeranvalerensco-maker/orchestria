export interface PublicOrganizationStructureResponse {
    period: PublicPeriodResponse;
    structure: PublicMemberAssignmentResponse[];
}

export interface PublicPeriodResponse {
    id: number;
    name: string;
    startDate: string;
    endDate: string | null;
    currentPeriod: boolean;
    publicVisible: boolean;
    active: boolean;
    createdAt: string;
    updatedAt: string | null;
}

export interface PublicMemberAssignmentResponse {
    memberId: number;
    memberName: string;
    cohort: string | null;
    profilePhotoUrl: string | null;
    major: string | null;
    periodId: number;
    periodName: string;
    divisionId: number;
    divisionCode: string;
    divisionName: string;
    positionId: number;
    positionCode: string;
    positionName: string;
    positionLevelOrder: number;
}
