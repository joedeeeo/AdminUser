// src/app/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

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

  // login(credentials: LoginRequest): Observable<LoginResponse> {
  //   return this.http.post<LoginResponse>(this.baseUrl+'/login', credentials);
  // }
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<any>(this.baseUrl + '/login', credentials).pipe(
      map(response => response.data as LoginResponse)
    );
  }

  // Send Reset Password Email - returns token or success message
sendResetPasswordEmail(email: string): Observable<string> {
  return this.http.get<any>(`${this.baseUrl}/send-reset-password-mail/${email}`).pipe(
    map(response => response.data as string)
  );
}

// Reset Password - returns boolean
resetPassword(request: ResetPasswordRequest): Observable<boolean> {
  return this.http.post<any>(`${this.baseUrl}/reset-password`, request).pipe(
    map(response => response.data as boolean)
  );
}


}
