import { Injectable, effect, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = 'http://localhost:8080';
  private readonly TOKEN_KEY = 'nexa_shop_token';
  private readonly USER_KEY = 'nexa_shop_user';

  isLoggedIn = signal(false);
  currentUser = signal<{ email: string; fullName: string; role: string } | null>(null);

  constructor(private readonly http: HttpClient) {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const user = localStorage.getItem(this.USER_KEY);
    if (token && user) {
      this.isLoggedIn.set(true);
      this.currentUser.set(JSON.parse(user));
    }
  }

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.baseUrl}/api/auth/login`, request)
      .pipe(tap(res => {
        if (res.success && res.data) {
          localStorage.setItem(this.TOKEN_KEY, res.data.token);
          localStorage.setItem(this.USER_KEY, JSON.stringify({
            email: res.data.email,
            fullName: res.data.fullName,
            role: res.data.role
          }));
          this.isLoggedIn.set(true);
          this.currentUser.set({ email: res.data.email, fullName: res.data.fullName, role: res.data.role });
        }
      }));
  }

  register(request: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.baseUrl}/api/auth/register`, request)
      .pipe(tap(res => {
        if (res.success && res.data) {
          localStorage.setItem(this.TOKEN_KEY, res.data.token);
          localStorage.setItem(this.USER_KEY, JSON.stringify({
            email: res.data.email,
            fullName: res.data.fullName,
            role: res.data.role
          }));
          this.isLoggedIn.set(true);
          this.currentUser.set({ email: res.data.email, fullName: res.data.fullName, role: res.data.role });
        }
      }));
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.isLoggedIn.set(false);
    this.currentUser.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isAdmin(): boolean {
    return this.currentUser()?.role === 'ADMIN';
  }
}
