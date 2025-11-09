import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  email = '';
  password = '';
  fullName = '';
  mobile = '';
  role = 'USER';
  error = '';

  constructor(private readonly authService: AuthService) {}

  register(): void {
    this.authService.register(this.email, this.password, this.fullName, this.mobile, this.role).subscribe({
      next: (res) => {
        localStorage.setItem('token', res.token);
        this.error = '';
        // Redirect to dashboard or reload
      },
      error: () => {
        this.error = 'Registration failed';
      }
    });
  }
}
