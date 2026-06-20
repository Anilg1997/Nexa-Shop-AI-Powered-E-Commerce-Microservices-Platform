import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <header class="header">
      <div class="header-inner">
        <a routerLink="/" class="logo">
          <span class="logo-icon">AI</span>
          <span class="logo-text">Nexa Shop</span>
        </a>
        <div class="search-bar">
          <input [(ngModel)]="searchQuery" (keydown.enter)="search()" placeholder="Search products..." />
          <button (click)="search()">🔍</button>
        </div>
        <nav class="nav-links">
          <a routerLink="/products" routerLinkActive="active">Products</a>
          <a routerLink="/ai-assistant" routerLinkActive="active">AI Assistant</a>
          <a routerLink="/cart" routerLinkActive="active" class="cart-link">🛒 Cart</a>
        </nav>
        <div class="user-section">
          <ng-container *ngIf="auth.isLoggedIn(); else loginBtn">
            <div class="user-menu" (click)="showMenu = !showMenu">
              <span class="user-avatar">{{ auth.currentUser()?.fullName?.charAt(0) }}</span>
              <span class="user-name">{{ auth.currentUser()?.fullName }}</span>
            </div>
            <div class="dropdown" *ngIf="showMenu" (mouseleave)="showMenu = false">
              <a routerLink="/orders">My Orders</a>
              <a routerLink="/ai-assistant">AI Assistant</a>
              <a *ngIf="auth.isAdmin()" routerLink="/admin" class="admin-link">Admin Panel</a>
              <button (click)="logout()">Logout</button>
            </div>
          </ng-container>
          <ng-template #loginBtn>
            <a routerLink="/auth" class="btn-login">Login</a>
          </ng-template>
        </div>
      </div>
      <div class="category-nav">
        <a routerLink="/products" [queryParams]="{category: 'electronics'}">Electronics</a>
        <a routerLink="/products" [queryParams]="{category: 'laptops'}">Laptops</a>
        <a routerLink="/products" [queryParams]="{category: 'accessories'}">Accessories</a>
        <a routerLink="/products" [queryParams]="{category: 'books'}">Books</a>
        <a routerLink="/products">All Products</a>
      </div>
    </header>
  `,
  styles: [`
    .header { background: #2874f0; color: white; position: sticky; top: 0; z-index: 100; box-shadow: 0 2px 8px rgba(0,0,0,0.15); }
    .header-inner { display: flex; align-items: center; gap: 20px; max-width: 1280px; margin: 0 auto; padding: 10px 24px; }
    .logo { display: flex; align-items: center; gap: 8px; text-decoration: none; color: white; }
    .logo-icon { background: white; color: #2874f0; padding: 4px 8px; border-radius: 4px; font-weight: 800; font-size: 14px; }
    .logo-text { font-size: 18px; font-weight: 700; }
    .search-bar { flex: 1; max-width: 500px; display: flex; }
    .search-bar input { flex: 1; padding: 10px 14px; border: none; border-radius: 4px 0 0 4px; font-size: 14px; outline: none; }
    .search-bar button { padding: 10px 16px; border: none; background: #f5f5f5; border-radius: 0 4px 4px 0; cursor: pointer; font-size: 14px; }
    .nav-links { display: flex; gap: 16px; }
    .nav-links a { color: white; text-decoration: none; font-size: 14px; font-weight: 500; padding: 4px 0; }
    .nav-links a:hover, .nav-links a.active { border-bottom: 2px solid white; }
    .cart-link { font-size: 18px !important; }
    .user-section { position: relative; }
    .user-menu { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 8px; border-radius: 4px; }
    .user-menu:hover { background: rgba(255,255,255,0.1); }
    .user-avatar { width: 32px; height: 32px; border-radius: 50%; background: #ffd54f; color: #333; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; }
    .user-name { font-size: 14px; }
    .dropdown { position: absolute; top: 100%; right: 0; background: white; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.15); min-width: 180px; padding: 8px; z-index: 200; margin-top: 4px; }
    .dropdown a, .dropdown button { display: block; width: 100%; padding: 10px 14px; text-decoration: none; color: #333; font-size: 14px; border: none; background: none; text-align: left; cursor: pointer; border-radius: 4px; }
    .dropdown a:hover, .dropdown button:hover { background: #f5f5f5; }
    .admin-link { color: #fb641b !important; font-weight: 700; }
    .btn-login { padding: 8px 20px; background: white; color: #2874f0; border-radius: 4px; text-decoration: none; font-weight: 700; font-size: 14px; }
    .category-nav { display: flex; gap: 0; background: #1a5fc7; padding: 0 24px; max-width: 1280px; margin: 0 auto; }
    .category-nav a { color: rgba(255,255,255,0.9); text-decoration: none; padding: 10px 16px; font-size: 13px; white-space: nowrap; }
    .category-nav a:hover { background: rgba(255,255,255,0.1); color: white; }
    @media (max-width: 768px) { .search-bar { display: none; } .user-name { display: none; } .nav-links a:not(.cart-link) { display: none; } }
  `]
})
export class HeaderComponent {
  showMenu = false;
  searchQuery = '';

  constructor(public auth: AuthService) {}

  search() {
    if (this.searchQuery.trim()) {
      window.location.href = `/products?q=${encodeURIComponent(this.searchQuery)}`;
    }
  }

  logout() {
    this.auth.logout();
    window.location.href = '/';
  }
}
