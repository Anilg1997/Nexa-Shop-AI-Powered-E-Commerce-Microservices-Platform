import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <h1>{{ isLogin ? 'Welcome Back' : 'Create Account' }}</h1>
          <p>{{ isLogin ? 'Sign in to your Nexa Shop account' : 'Join the future of AI-powered shopping' }}</p>
        </div>
        <div class="auth-tabs">
          <button [class.active]="isLogin" (click)="isLogin = true">Login</button>
          <button [class.active]="!isLogin" (click)="isLogin = false">Register</button>
        </div>
        <form (ngSubmit)="isLogin ? doLogin() : doRegister()" class="auth-form">
          <div class="form-group" *ngIf="!isLogin">
            <label>Full Name</label>
            <input [(ngModel)]="fullName" name="fullName" placeholder="Enter your full name" required>
          </div>
          <div class="form-group">
            <label>Email Address</label>
            <input [(ngModel)]="email" name="email" type="email" placeholder="Enter your email" required>
          </div>
          <div class="form-group">
            <label>Password</label>
            <input [(ngModel)]="password" name="password" type="password" placeholder="Enter password" required>
          </div>
          <button type="submit" class="btn-submit">{{ isLogin ? 'Login' : 'Create Account' }}</button>
        </form>
        <p class="auth-error" *ngIf="error">{{ error }}</p>
        <div class="auth-info">
          <p><strong>Demo Credentials:</strong></p>
          <p>Email: <code>admin&#64;example.com</code> / Password: <code>admin</code> (Admin)</p>
          <p>Email: <code>customer&#64;example.com</code> / Password: <code>demo</code> (Customer)</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-container {
      display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 160px); padding: 20px;
    }
    .auth-card {
      background: white; border-radius: 16px; padding: 40px; width: 100%; max-width: 440px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.08);
    }
    .auth-header { text-align: center; margin-bottom: 24px; }
    .auth-header h1 { font-size: 28px; margin: 0 0 8px; color: #212121; }
    .auth-header p { color: #666; margin: 0; font-size: 14px; }
    .auth-tabs { display: flex; border-radius: 8px; overflow: hidden; border: 1px solid #e0e0e0; margin-bottom: 24px; }
    .auth-tabs button {
      flex: 1; padding: 12px; border: none; background: #f5f5f5; cursor: pointer;
      font-weight: 600; font-size: 14px; transition: all 0.2s;
    }
    .auth-tabs button.active { background: #2874f0; color: white; }
    .auth-form { display: flex; flex-direction: column; gap: 16px; }
    .form-group { display: flex; flex-direction: column; gap: 6px; }
    .form-group label { font-size: 13px; font-weight: 600; color: #333; }
    .form-group input {
      padding: 12px 14px; border: 1px solid #e0e0e0; border-radius: 8px;
      font-size: 14px; transition: border-color 0.2s;
    }
    .form-group input:focus { outline: none; border-color: #2874f0; }
    .btn-submit {
      padding: 14px; background: #fb641b; color: white; border: none; border-radius: 8px;
      font-size: 16px; font-weight: 600; cursor: pointer; transition: background 0.2s;
    }
    .btn-submit:hover { background: #e55a16; }
    .auth-error { color: #d32f2f; text-align: center; margin: 12px 0 0; font-size: 14px; }
    .auth-info {
      margin-top: 24px; padding: 16px; background: #f5f7fa; border-radius: 8px;
      font-size: 12px; color: #666; line-height: 1.6;
    }
    .auth-info p { margin: 4px 0; }
    .auth-info code { background: #e8eaf6; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
  `]
})
export class AuthComponent {
  isLogin = true;
  email = 'customer@example.com';
  password = 'demo';
  fullName = 'Demo Customer';
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  doLogin() {
    this.error = '';
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => this.error = 'Login failed. Make sure auth-service is running.'
    });
  }

  doRegister() {
    this.error = '';
    this.authService.register({ email: this.email, fullName: this.fullName, password: this.password }).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => this.error = 'Registration failed. Make sure auth-service is running.'
    });
  }
}
