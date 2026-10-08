export interface AuthUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'USER' | 'ADMIN';
}

export interface AuthResponse {
  token: string;
  user: AuthUser;
}
