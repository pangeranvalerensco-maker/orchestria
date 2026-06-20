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
  authUserId: number;
  fullName: string;
  email: string;
  studentNumber: string;
  phoneNumber: string;
  cohort: string;
  profilePhotoUrl: string;
  major: string;
  campusClass: string;
  publicVisible: boolean;
  displayOrder: number;
  status: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DivisionResponse {
  id: number;
  code: string;
  name: string;
  description: string;
  displayOrder: number;
  publicVisible: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PositionResponse {
  id: number;
  code: string;
  name: string;
  description: string;
  levelOrder: number;
  publicVisible: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}