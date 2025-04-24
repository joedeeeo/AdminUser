import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService, ResetPasswordRequest } from '../auth.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  
  resetForm: FormGroup;
  token: string = '';
  submitted = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validator: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
    });
  }

  passwordMatchValidator(form: FormGroup) {
    return form.get('newPassword')?.value === form.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  get f() {
    return this.resetForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.resetForm.invalid) return;

    const request: ResetPasswordRequest = {
      token: this.token,
      newPassword: this.resetForm.get('newPassword')?.value
    };

    this.authService.resetPassword(request).subscribe({
      next: (success) => {
        if (success) {
          alert("Password reset successful!");
          this.router.navigate(['/login']);
        } else {
          alert("Password reset failed.");
        }
      },
      error: () => {
        alert("An error occurred. Try again.");
      }
    });
  }
}
