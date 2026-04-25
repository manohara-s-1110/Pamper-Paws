import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:8087/auth'; // gateway URL

  constructor(private http: HttpClient) {}

  // ✅ REGISTER
  register(data: any) {
    return this.http.post(`${this.baseUrl}/register`, data, {
      responseType: 'text'   // because backend returns string
    });
  }

  // ✅ LOGIN
  login(data: any) {
    return this.http.post(`${this.baseUrl}/login`, data, {
      responseType: 'text'   // JWT token comes as string
    });
  }
}