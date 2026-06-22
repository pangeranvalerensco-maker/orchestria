export interface SessionDemoLoginPayload {
    email: string;
    password: string;
}

export interface SessionDemoUser {
    id: number;
    fullName: string;
    email: string;
    roles: string[];
}

export interface SessionDemoResponse {
    authenticated: boolean;
    authenticationMode: string;
    user?: SessionDemoUser | null;
    createdAt?: string | null;
    lastAccessedAt?: string | null;
    expiresInSeconds?: number;
    message: string;
}
