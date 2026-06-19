export interface RoleSummary {
  id: number;
  name: string;
  description: string;
  active: boolean;
  permissions: string[];
}

export interface RegisterUserPayload {
  fullName: string;
  email: string;
  password: string;
}
