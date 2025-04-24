import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../user.service'; 
import { User } from '../user.service'; 

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  standalone: false
})
export class DashboardComponent implements OnInit {
  userData: User | null = null;
  originalData: User | null = null;
  selectedImageFile: File | null = null;
  editMode = false;
  private isBrowser: boolean;
  email: string | null = null;

  constructor(
    private userService: UserService,
    private route: ActivatedRoute,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit(): void {
    if (!this.isBrowser) return;

    this.route.paramMap.subscribe(params => {
      this.email = params.get('email');
      if (this.email) {
        this.loadUserData(this.email);
      } else {
        this.email = localStorage.getItem('email')
        this.loadUserData(this.email!);
      }
    });

  }

  private loadUserData(email: string): void {
    this.userService.getUserData(email).subscribe({
      next: (data) => {
        this.userData = { ...data };
        this.originalData = { ...data };
      },
      error: (err) => console.error('Error fetching user data:', err)
    });
  }

  getUserInitials(name: string): string {
    if (!name) return '';
    const names = name.trim().split(' ');
    return names.map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedImageFile = input.files[0];
    } else {
      this.selectedImageFile = null;
    }
  }

  saveUserData() {
    if (!this.userData) return;

    this.userService.updateUserData(this.userData, this.selectedImageFile!).subscribe({
      next: () => {
        this.editMode = false;
        this.selectedImageFile = null;
        this.loadUserData(this.email!); // Reload updated data
      },
      error: (err) => console.error('Failed to update user data:', err)
    });
  }

  cancelEdit() {
    this.editMode = false;
    this.selectedImageFile = null;
    this.userData = { ...this.originalData! };
  }
}
