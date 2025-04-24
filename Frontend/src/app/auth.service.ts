// src/app/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  jwt: string;
  role: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:8080'; // Update with your actual endpoint

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.baseUrl+'/login', credentials);
  }

  sendResetPasswordEmail(email: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/send-reset-password-mail/${email}`, { responseType: 'text' });
  }

  // Method to reset the password
  resetPassword(request: ResetPasswordRequest): Observable<boolean> {
    return this.http.post<boolean>(`${this.baseUrl}/reset-password`, request);
  }


}
