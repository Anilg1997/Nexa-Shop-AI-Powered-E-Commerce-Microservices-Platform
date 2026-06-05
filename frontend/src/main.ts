import { HttpClient, provideHttpClient } from '@angular/common/http';
import { Component, Injectable, computed, signal } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

interface Product {
  id?: string;
  name: string;
  description: string;
  price: number;
  stock: number;
  imageUrl: string;
}

interface CartItem extends Product {
  quantity: number;
}

@Injectable({ providedIn: 'root' })
class CommerceApi {
  private readonly baseUrl = 'http://localhost:8080';

  constructor(private readonly http: HttpClient) {}

  products() {
    return this.http.get<ApiResponse<Product[]>>(`${this.baseUrl}/api/catalog/products`);
  }

  createOrder(customerEmail: string, items: CartItem[]) {
    return this.http.post<ApiResponse<unknown>>(`${this.baseUrl}/api/orders`, {
      customerEmail,
      items: items.map(item => ({
        productId: item.id,
        productName: item.name,
        quantity: item.quantity,
        unitPrice: item.price
      }))
    });
  }

  askAssistant(message: string) {
    return this.http.post<ApiResponse<{ answer: string; retrievedContext: string }>>(`${this.baseUrl}/api/ai/chat`, { message });
  }
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="shell">
      <section class="topbar">
        <div>
          <p class="eyebrow">AI Commerce</p>
          <h1>Microservices ecommerce with an AI shopping agent</h1>
        </div>
        <button type="button" (click)="loadProducts()">Refresh</button>
      </section>

      <section class="workspace">
        <div class="panel catalog">
          <div class="panel-header">
            <h2>Catalog</h2>
            <span>{{ products().length }} products</span>
          </div>
          <div class="grid">
            <article class="product" *ngFor="let product of products()">
              <img [src]="product.imageUrl" [alt]="product.name">
              <div>
                <h3>{{ product.name }}</h3>
                <p>{{ product.description }}</p>
                <div class="row">
                  <strong>{{ product.price | currency }}</strong>
                  <button type="button" (click)="addToCart(product)">Add</button>
                </div>
              </div>
            </article>
          </div>
        </div>

        <aside class="panel side">
          <h2>Cart</h2>
          <div class="cart-line" *ngFor="let item of cart()">
            <span>{{ item.name }}</span>
            <strong>x{{ item.quantity }}</strong>
          </div>
          <p class="total">Total {{ total() | currency }}</p>
          <input [(ngModel)]="customerEmail" placeholder="customer@example.com">
          <button type="button" (click)="checkout()" [disabled]="!cart().length">Checkout</button>
          <p class="status">{{ orderStatus() }}</p>

          <hr>

          <h2>AI Assistant</h2>
          <textarea [(ngModel)]="question" placeholder="Ask about products, orders, or checkout"></textarea>
          <button type="button" (click)="ask()">Ask AI</button>
          <p class="answer">{{ answer() }}</p>
        </aside>
      </section>
    </main>
  `
})
class AppComponent {
  products = signal<Product[]>([
    {
      id: 'demo-1',
      name: 'AI Starter Laptop',
      description: 'Developer laptop for Java, Angular, Docker, and local AI demos.',
      price: 899,
      stock: 12,
      imageUrl: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80'
    },
    {
      id: 'demo-2',
      name: 'Kafka Event Kit',
      description: 'A learning bundle for event-driven ecommerce workflows.',
      price: 149,
      stock: 40,
      imageUrl: 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=900&q=80'
    }
  ]);
  cart = signal<CartItem[]>([]);
  customerEmail = 'customer@example.com';
  question = 'Recommend a product for learning microservices and AI agents.';
  answer = signal('');
  orderStatus = signal('');
  total = computed(() => this.cart().reduce((sum, item) => sum + item.price * item.quantity, 0));

  constructor(private readonly api: CommerceApi) {}

  loadProducts() {
    this.api.products().subscribe({
      next: response => this.products.set(response.data.length ? response.data : this.products()),
      error: () => this.orderStatus.set('Backend not running yet; showing demo products.')
    });
  }

  addToCart(product: Product) {
    const existing = this.cart().find(item => item.id === product.id);
    if (existing) {
      existing.quantity += 1;
      this.cart.set([...this.cart()]);
      return;
    }
    this.cart.set([...this.cart(), { ...product, quantity: 1 }]);
  }

  checkout() {
    this.api.createOrder(this.customerEmail, this.cart()).subscribe({
      next: () => {
        this.orderStatus.set('Order created.');
        this.cart.set([]);
      },
      error: () => this.orderStatus.set('Start backend services before checkout.')
    });
  }

  ask() {
    this.api.askAssistant(this.question).subscribe({
      next: response => this.answer.set(response.data.answer),
      error: () => this.answer.set('Start ai-service and Ollama to use the assistant.')
    });
  }
}

bootstrapApplication(AppComponent, {
  providers: [provideHttpClient()]
}).catch(error => console.error(error));
