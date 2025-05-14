import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-add-dummy-user',
  standalone:false,
  templateUrl: './add-bulk-user.component.html',
  styleUrls: ['./add-bulk-user.component.css']
})
export class AddBulkUserComponent {
  count: number = 0;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  addDummyUsers() {
    if (this.count <= 0) {
      alert('Please enter a count greater than 0');
      return;
    }

    this.http.get<any>(`http://localhost:8080/add-dummy-data/${this.count}`)
      .subscribe({
        next: () => {
          alert('Dummy users added successfully');
          this.router.navigate(['/dashboard/userlist']);
        },
        error: (err) => {
          console.error(err);
          alert('Failed to add dummy users');
        }
      });
  }
}
