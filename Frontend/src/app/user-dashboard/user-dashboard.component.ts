import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { User, UserService } from '../user.service';
import { ActivatedRoute } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-user-dashboard',
  standalone: false,
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})
export class UserDashboardComponent implements OnInit {
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
    console.log('Saving user data', this.userData, this.selectedImageFile);
  
    // Ensure userData exists before proceeding
    if (!this.userData) {
      console.error('No user data to save');
      return;
    }
  
    // Call updateUserData with image file if it exists
    this.userService.updateUserData(this.userData, this.selectedImageFile || undefined).subscribe({
      next: () => {
        this.editMode = false; // Exit edit mode
        this.selectedImageFile = null; // Clear selected image after saving
        this.loadUserData(this.email!); // Reload updated user data
      },
      error: (err) => {
        console.error('Failed to update user data:', err);
      }
    });
  }
  

  cancelEdit() {
    this.editMode = false;
    this.selectedImageFile = null;
    this.userData = { ...this.originalData! };
  }
}
