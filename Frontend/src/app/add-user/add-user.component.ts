import { Component } from '@angular/core';
import { NgForm } from '@angular/forms';
import { CheckEmailResp, UserService } from '../user.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-add-user',
  standalone: false,
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css']
})
export class AddUserComponent {

  stage: 'email' | 'choice' | 'single' | 'bulk' = 'email';

  emailToCheck = '';
  checkError   = '';

  newUser: any = {
    name: '',
    email: '',
    dob: '',
    gender: '',
    pinCode: null,
    contactNumber: '',
    address: '',
    password: '',
    role: 'USER',
    isActive: true
  };

  selectedFile: File | null = null;

  uploadResult: { savedUsersCount: number; errors: string[] } | null = null;

  bulkOption: 'upload' = 'upload';

  constructor(private userService: UserService, private router: Router) {}

  onFileSelected(evt: Event) {
    const input = evt.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedFile = input.files[0];
      this.uploadResult = null;
    }
  }

  removeFile() {
    this.selectedFile = null;
    this.uploadResult = null;
  }

  downloadTemplate() {
    this.userService.downloadTemplate()
      .subscribe(blob => {
        const url = window.URL.createObjectURL(blob);
        const a   = document.createElement('a');
        a.href    = url;
        a.download = 'User_Creation_Template.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
      });
  }

  uploadFile() {
    if (!this.selectedFile) { return; }

    this.uploadResult = null;
    this.userService.uploadFile(this.selectedFile)
      .subscribe({
        next: (res) => this.uploadResult = res,
        error: (err) => {
          console.error(err);
          this.uploadResult = { savedUsersCount: 0, errors: ['Upload failed.'] };
        }
      });
  }

  onCheckEmail() {
    this.checkError = '';
    if (!this.emailToCheck) {
      this.checkError = 'Email is required';
      return;
    }
    this.userService.checkEmail(this.emailToCheck)
      .subscribe({
        next: (res: CheckEmailResp) => {
          if (res.exists) {
            // redirect to existing dashboard
            this.router.navigate(['/dashboard', res.email]);
          } else {
            // show choice card
            this.stage = 'choice';
          }
        },
        error: () => {
          this.checkError = 'Server error—please try again';
        }
      });
  }

  chooseSingle() { this.stage = 'single'; }
  chooseBulk()   { this.stage = 'bulk';   }

  onSubmitSingle(form: NgForm): void {
    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    this.userService.addUser(this.newUser).subscribe({
      next: (response) => {
        alert('User added successfully!');
        this.router.navigate(['/table']);
      },
      error: (err) => {
        console.error('Error adding user:', err);
        alert('Failed to add user.');
      }
    });
  }
}
