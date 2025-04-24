// user.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

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
}

export interface UserPageResponse {
  content: User[];
  totalPages: number;
  totalElements: number;
  number: number; // current page number
  size: number;   // size of the page
}

@Injectable({
  providedIn: 'root'
})

export class UserService {
  private apiUrl = 'http://localhost:8080/get-admin-data';

  constructor(private http: HttpClient) {}

  getUserData(email: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${email}`);
  }

  getUserTable(page: number, size: number, search?: string): Observable<UserPageResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
  
    if (search && search.trim() !== '') {
      params = params.set('search', search);
    }
  
    return this.http.get<UserPageResponse>('http://localhost:8080/get-user-table/' + page + '/' + size, { params });
  }

  deleteUser(id: number): Observable<boolean> {
    return this.http.delete<boolean>(`http://localhost:8080/delete-user/${id}`);
  }

  updateUserData(adminUser: any, imageFile: File): Observable<boolean> {
    
    const formData = new FormData();
    formData.append('updatedAdminUserProxy', new Blob(
      [JSON.stringify(adminUser)],
      { type: 'application/json' }
    ));
    formData.append('image', imageFile);
  
    return this.http.put<boolean>('http://localhost:8080/update-user-data', formData);
  }

}
