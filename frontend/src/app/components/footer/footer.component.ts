import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterModule],
  template: `
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-section">
          <h4>Nexa Shop</h4>
          <p>AI-powered e-commerce platform built with Spring Boot microservices, Angular, Kafka, and RAG-based LLM agents.</p>
        </div>
        <div class="footer-section">
          <h4>Quick Links</h4>
          <a routerLink="/products">Products</a>
          <a routerLink="/cart">Cart</a>
          <a routerLink="/orders">Orders</a>
          <a routerLink="/ai-assistant">AI Assistant</a>
        </div>
        <div class="footer-section">
          <h4>Admin</h4>
          <a routerLink="/admin">Dashboard</a>
          <a routerLink="/admin/products">Manage Products</a>
          <a routerLink="/admin/orders">Manage Orders</a>
          <a routerLink="/admin/users">Manage Users</a>
        </div>
        <div class="footer-section">
          <h4>Tech Stack</h4>
          <span>Java 21 + Spring Boot 3.3</span>
          <span>Angular 18 + Signals</span>
          <span>Apache Kafka + Redpanda</span>
          <span>Ollama + LangChain4j + RAG</span>
          <span>PostgreSQL + MongoDB + Redis</span>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2026 Nexa Shop Microservices. Built with ❤️ for the AI era.</p>
      </div>
    </footer>
  `,
  styles: [`
    .footer { background: #172033; color: #b9c6d4; margin-top: 40px; }
    .footer-inner { display: grid; grid-template-columns: repeat(4, 1fr); gap: 30px; max-width: 1280px; margin: 0 auto; padding: 40px 24px; }
    .footer-section { display: flex; flex-direction: column; gap: 8px; }
    .footer-section h4 { color: white; font-size: 16px; margin: 0 0 8px; }
    .footer-section p { font-size: 13px; line-height: 1.6; margin: 0; }
    .footer-section a { color: #b9c6d4; text-decoration: none; font-size: 13px; }
    .footer-section a:hover { color: white; }
    .footer-section span { font-size: 13px; }
    .footer-bottom { border-top: 1px solid #263447; padding: 16px 24px; text-align: center; }
    .footer-bottom p { margin: 0; font-size: 13px; }
    @media (max-width: 768px) { .footer-inner { grid-template-columns: repeat(2, 1fr); } }
  `]
})
export class FooterComponent {}
