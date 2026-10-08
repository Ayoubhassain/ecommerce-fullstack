import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { API_URL } from '../config/api';
import { AuthResponse, AuthUser } from '../common/auth-user';

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  // current user, or null when logged out; components subscribe to it
  private userSubject = new BehaviorSubject<AuthUser | null>(this.restoreUser());
  readonly user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) { }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, { email, password })
      .pipe(tap(response => this.store(response)));
  }

  register(data: { email: string, password: string, firstName: string, lastName: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/register`, data)
      .pipe(tap(response => this.store(response)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.userSubject.next(null);
  }

  get token(): string | null {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token && this.isExpired(token)) {
      this.logout();
      return null;
    }
    return token;
  }

  get currentUser(): AuthUser | null {
    return this.token ? this.userSubject.value : null;
  }

  isLoggedIn(): boolean {
    return this.currentUser !== null;
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  private store(response: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(response.user));
    this.userSubject.next(response.user);
  }

  private restoreUser(): AuthUser | null {
    const token = localStorage.getItem(TOKEN_KEY);
    const user = localStorage.getItem(USER_KEY);
    if (!token || !user || this.isExpired(token)) {
      return null;
    }
    return JSON.parse(user) as AuthUser;
  }

  // reads the "exp" claim of the JWT (the role check itself is always done by the backend)
  private isExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return typeof payload.exp === 'number' && payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}
