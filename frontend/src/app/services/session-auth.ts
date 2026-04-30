import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, tap } from 'rxjs/operators';

import { AuthSession, ChangePasswordPayload, RegisterAuthPayload, UserRole, VetRegisterPayload } from '../models/app.models';

@Injectable({ providedIn: 'root' })
export class SessionAuthService {
  private readonly baseUrl = 'http://localhost:8087/auth';
  private readonly storageKey = 'pamper-paws-session';

  private readonly sessionState = signal<AuthSession | null>(this.readStoredSession());

  readonly session = this.sessionState.asReadonly();
  readonly isAuthenticated = computed(() => this.sessionState() !== null);
  readonly role = computed(() => this.sessionState()?.role ?? null);

  constructor(private http: HttpClient) {}

  login(credentials: { username: string; password: string }) {
    return this.http
      .post(`${this.baseUrl}/login`, credentials, { responseType: 'text' })
      .pipe(
        map((token) => this.buildSession(token)),
        tap((session) => this.persistSession(session)),
      );
  }

  register(payload: RegisterAuthPayload) {
    return this.http.post(`${this.baseUrl}/register`, payload, {
      responseType: 'text',
    });
  }

  registerVet(payload: VetRegisterPayload) {
    return this.http.post(`${this.baseUrl}/register/vet`, payload, {
      responseType: 'text',
    });
  }

  changePassword(payload: ChangePasswordPayload) {
    return this.http.put(`${this.baseUrl}/password`, payload, {
      responseType: 'text',
    });
  }

  logout() {
    sessionStorage.removeItem(this.storageKey);
    this.sessionState.set(null);
  }

  landingPathForRole(role: UserRole | null | undefined): string {
    switch (role) {
      case 'CUSTOMER':
        return '/customer';
      case 'VET':
        return '/vet';
      case 'ADMIN':
        return '/admin';
      default:
        return '/login';
    }
  }

  getToken(): string | null {
    return this.sessionState()?.token ?? null;
  }

  private persistSession(session: AuthSession) {
    sessionStorage.setItem(this.storageKey, JSON.stringify(session));
    this.sessionState.set(session);
  }

  private readStoredSession(): AuthSession | null {
    const raw = sessionStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }

    try {
      const parsed = JSON.parse(raw) as AuthSession;
      if (!parsed.token || !parsed.username || !parsed.role) {
        sessionStorage.removeItem(this.storageKey);
        return null;
      }
      return parsed;
    } catch {
      sessionStorage.removeItem(this.storageKey);
      return null;
    }
  }

  private buildSession(token: string): AuthSession {
    const payload = this.decodeJwtPayload(token);
    const username = String(payload['sub'] ?? '');
    const role = String(payload['role'] ?? '').toUpperCase() as UserRole;

    if (!username || !role) {
      throw new Error('Invalid authentication token');
    }

    return { token, username, role };
  }

  private decodeJwtPayload(token: string): Record<string, unknown> {
    const [, payload] = token.split('.');
    if (!payload) {
      throw new Error('Missing token payload');
    }

    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
    return JSON.parse(atob(padded)) as Record<string, unknown>;
  }
}
