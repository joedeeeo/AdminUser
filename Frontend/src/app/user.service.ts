import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface User {
  id: number;
  name: string;
  email: string;
  dob: string;
  gender: string;
  pinCode: number;
  profileImage: string;
  contactNumber: string;
  address: string;
  role: string;
  formattedCreatedDate: string;
  formattedModifiedDate: string;
}

export interface UserPageResponse {
  content: User[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  role: string;
}

export interface CheckEmailResp { exists: boolean; id?: number; email?: string }

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getUserData(email: string): Observable<User> {
    return this.http.get<any>(`${this.apiUrl}/get-admin-data/${email}`).pipe(
      map(response => response.data as User)
    );
  }

  getUserTable(page: number, size: number, search?: string, role?: string): Observable<UserPageResponse> {
    let params = new HttpParams();

    if (search && search.trim() !== '') {
      params = params.set('search', search);
    }

    const actualRole = role || 'USER';
    const url = `${this.apiUrl}/get-user-table/${page}/${size}/${actualRole}`;

    return this.http.get<any>(url, { params }).pipe(
      map(response => response.data as UserPageResponse)
    );
  }

  deleteUser(id: number): Observable<boolean> {
    return this.http.delete<any>(`${this.apiUrl}/delete-user/${id}`).pipe(
      map(response => response.data as boolean)
    );
  }

  updateUserData(adminUser: any, imageFile?: File): Observable<boolean> {
    const formData = new FormData();
    formData.append('updatedAdminUserProxy', new Blob([JSON.stringify(adminUser)], { type: 'application/json' }));

    if (imageFile) {
      formData.append('image', imageFile);
    }

    return this.http.put<any>(`${this.apiUrl}/update-user-data`, formData).pipe(
      map(response => response.data as boolean)
    );
  }

  downloadUserExcel(): Observable<Blob> {
    // This returns raw file data so no need to unwrap
    return this.http.get(`${this.apiUrl}/download-users-excel`, { responseType: 'blob' });
  }

  addUser(newUser: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/adduser`, newUser).pipe(
      map(response => response) // Adjust this based on the response structure from the backend
    );
  }

  checkEmail(email: string): Observable<CheckEmailResp> {
    return this.http.get<CheckEmailResp>(
      `${this.apiUrl}/check-email`, { params: new HttpParams().set('email', email) }
    );
  }

  downloadTemplate(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download-template`, { responseType: 'blob' });
  }

  uploadFile(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.apiUrl}/upload`, formData);
  }
  
}
