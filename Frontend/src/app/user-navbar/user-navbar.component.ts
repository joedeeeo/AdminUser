import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-user-navbar',
  standalone: false,
  templateUrl: './user-navbar.component.html',
  styleUrl: './user-navbar.component.css'
})
export class UserNavbarComponent {

  constructor(private router: Router) {}
  logout(): void {
    localStorage.removeItem('jwt');
    localStorage.removeItem('email');
    this.router.navigate(['/login']);
  }
}
