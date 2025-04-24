import { Component, NgZone, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService, LoginRequest } from '../auth.service';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ForgotPasswordDialogComponent } from '../forgot-password-dialog/forgot-password-dialog.component';

declare global {
  interface Window {
    onCaptchaSuccess: (token: string) => void;
    onCaptchaExpired: () => void;
  }
}

declare var hcaptcha: any;

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit{
  loginForm: FormGroup;
  submitted = false;
  loading = false;
  captchaToken: string | null = null; // ⬅️ store the captcha token

  constructor(
    private dialog: MatDialog,
    private router: Router,
    private fb: FormBuilder,
    private authService: AuthService,
    private zone: NgZone
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
    

    // Set up global callback functions
    window.onCaptchaSuccess = (token: string) => {
      this.zone.run(() => {
        this.captchaToken = token;
        console.log('Captcha success:', token);
      });
    };

    window.onCaptchaExpired = () => {
      this.zone.run(() => {
        this.captchaToken = null;
        console.log('Captcha expired');
      });
    };
  }
  ngOnInit(): void {
    this.renderCaptcha();
  }

  get f() {
    return this.loginForm.controls;
  }

  renderCaptcha() {
    const interval = setInterval(() => {
      if (typeof hcaptcha !== 'undefined' && hcaptcha.render) {
        clearInterval(interval);

        hcaptcha.render('hcaptcha-container', {
          sitekey: 'a3e81826-2e6c-479a-99ba-38fc03f06bbe',
          callback: 'onCaptchaSuccess',
          'expired-callback': 'onCaptchaExpired'
        });
      }
    }, 200);
  }

  onForgotPassword(): void {
    if (!this.captchaToken) {
      alert("Please complete the CAPTCHA first.");
      return;
    }
  
    const email = this.loginForm.get('email')?.value || '';
  
    const dialogRef = this.dialog.open(ForgotPasswordDialogComponent, {
      width: '400px',
      data: { email }
    });
  
    dialogRef.afterClosed().subscribe((result: string | undefined) => {
      if (result) {
        this.authService.sendResetPasswordEmail(result).subscribe({
          next: () => {
            alert("Password reset email sent. Please check your inbox.");
          },
          error: () => {
            alert("Failed to send password reset email. Try again.");
          }
        });
      }
    });
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.loginForm.invalid) return;

    if (!this.captchaToken) {
      alert("Please complete the CAPTCHA before submitting.");
      return;
    }

    this.loading = true;

    const loginData: LoginRequest = this.loginForm.value;
    this.authService.login(loginData).subscribe({
      next: (response) => {
        console.log('Login success:', response);
        localStorage.setItem('jwt', response.jwt);
        localStorage.setItem('email', loginData.email);
        this.router.navigate(['/dashboard']);
        this.loading = false;
      },
      error: (error) => {
        alert("Login Failed");
        console.error('Login error:', error);
        this.loading = false;
      }
    });
  }
}
