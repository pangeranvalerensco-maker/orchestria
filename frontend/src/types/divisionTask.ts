export const TaskStatus = {
  TODO: 'TODO',
  IN_PROGRESS: 'IN_PROGRESS',
  SUBMITTED: 'SUBMITTED',
  DONE: 'DONE',
  CANCELLED: 'CANCELLED'
} as const;

export type TaskStatus = typeof TaskStatus[keyof typeof TaskStatus];

export const TaskPriority = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH'
} as const;

export type TaskPriority = typeof TaskPriority[keyof typeof TaskPriority];

export const EvidenceType = {
  NOTE: 'NOTE',
  LINK: 'LINK',
  PHOTO: 'PHOTO',
  DOCUMENT: 'DOCUMENT'
} as const;

export type EvidenceType = typeof EvidenceType[keyof typeof EvidenceType];

export interface DivisionTask {
  id: number;
  divisionId: number;
  divisionCode: string;
  divisionName: string;
  assignedMemberId: number | null;
  assignedMemberName: string | null;
  assignedMemberEmail: string | null;
  title: string;
  description: string | null;
  dueDate: string | null; // ISO Date
  status: TaskStatus;
  priority: TaskPriority;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DivisionTaskRequest {
  divisionId: number;
  assignedMemberId: number | null;
  title: string;
  description: string | null;
  dueDate: string | null; // ISO Date YYYY-MM-DD
  status: TaskStatus;
  priority: TaskPriority;
}

export interface DivisionTaskEvidence {
  id: number;
  taskId: number;
  taskTitle: string;
  type: EvidenceType;
  title: string;
  description: string | null;
  fileUrl: string | null;
  externalLink: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  submittedByMemberId: number | null;
}

export interface DivisionTaskEvidenceRequest {
  taskId: number;
  type: EvidenceType;
  title: string;
  description: string | null;
  fileUrl: string | null;
  externalLink: string | null;
}
