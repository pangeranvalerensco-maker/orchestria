export interface OrganizationMember {
  id: number;
  authUserId: number;
  fullName: string;
  email: string;
  active: boolean;
}

export interface MemberAssignment {
  id: number;
  memberId: number;
  memberName: string;
  memberEmail: string;

  periodId: number;
  periodName: string;

  divisionId: number;
  divisionCode: string;
  divisionName: string;

  positionId: number;
  positionCode: string;
  positionName: string;

  status: string;
  active: boolean;
}

export interface CurrentMemberContext {
  member: OrganizationMember;
  activeAssignments: MemberAssignment[];
}