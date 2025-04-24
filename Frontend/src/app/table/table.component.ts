import { Component, OnInit } from '@angular/core';
import { UserService } from '../user.service';
import { User, UserPageResponse } from '../user.service'; 
import { Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit {
  users: User[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  searchQuery: string = '';

  constructor(private router : Router, private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  onSearch(): void {
  this.page = 0; // reset to first page
  this.loadUsers();
}

  loadUsers(): void {
    this.userService.getUserTable(this.page, this.size, this.searchQuery).subscribe({
      next: (res: UserPageResponse) => {
        this.users = res.content;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
      },
      error: (err) => console.error('Error loading users:', err)
    });
  }
  
  isValidBase64(image: string): boolean {
    const base64Pattern = /^data:image\/(png|jpeg|jpg|gif);base64,/;
    return base64Pattern.test(image);
  }

  viewProfile(user: User): void {
    this.router.navigate(['/dashboard', user.email]);
  }

  deleteUser(id: number): void {
    if (confirm('Are you sure you want to delete this user?')) {
      this.userService.deleteUser(id).subscribe({
        next: (res) => {
          if (res) {
            alert('User deleted successfully.');
            this.loadUsers(); // refresh the table
          } else {
            alert('Failed to delete user.');
          }
        },
        error: (err) => {
          console.error('Error deleting user:', err);
          alert('An error occurred while deleting the user.');
        }
      });
    }
  }

  getUserInitials(name: string): string {
    if (!name) return '';
    const parts = name.trim().split(' ');
    return parts.map(p => p[0]).join('').toUpperCase().slice(0, 2);
  }

  goToPage(pageNum: number): void {
    this.page = pageNum;
    this.loadUsers();
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.loadUsers();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadUsers();
    }
  }

  onPageSizeChange(): void {
    this.page = 0;
    this.loadUsers();
  }
}
