import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TableComponent } from './table/table.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './login/login.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { AuthGuard } from './auth.guard';
import { AddUserComponent } from './add-user/add-user.component';
import { UserDashboardComponent } from './user-dashboard/user-dashboard.component';
import { AddBulkUserComponent } from './add-bulk-user/add-bulk-user.component';

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full'},
  { path: 'table', component: TableComponent, canActivate: [AuthGuard]},
  { path: 'dashboard/:email', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'add-user',component: AddUserComponent},
  { path: 'add-bulk-user', component: AddBulkUserComponent },
  { path: 'user-dashboard', component: UserDashboardComponent },
  {
    path: 'aZ1xR7nVf4GqL9dPbWmYcT6jHsEUkO38iJNtK5BAzvCeMyXoFDQrlpSghuZwNbRXatqVYEo7cdkpLwzTmUAgKHnFJbsiQ93vGRyLmCXeWtuqOASBxMkPnfhaVjTcLdUboqNRYmgtZKXeWJVCHQyxLiDGnUfoP2vM9zsekl8BpLtRYKhN',
    component: ResetPasswordComponent
  }
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
