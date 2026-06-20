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

export interface MemberResponse {
  id: number;
  authUserId: number | null;
  fullName: string;
  email: string;
  studentNumber: string | null;
  phoneNumber: string | null;
  cohort: string | null;
  profilePhotoUrl: string | null;
  major: string | null;
  campusClass: string | null;
  publicVisible: boolean;
  displayOrder: number | null;
  status: string;
  active: boolean;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface DivisionResponse {
  id: number;
  code: string;
  name: string;
  description: string | null;
  displayOrder: number | null;
  publicVisible: boolean;
  active: boolean;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface PositionResponse {
  id: number;
  code: string;
  name: string;
  description: string | null;
  levelOrder: number | null;
  publicVisible: boolean;
  active: boolean;
  createdAt: string | null;
  updatedAt: string | null;
}