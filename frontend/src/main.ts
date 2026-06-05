import { HttpClient, provideHttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Component, Injectable, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { bootstrapApplication } from '@angular/platform-browser';

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

interface CartItem {
  productId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  imageUrl: string;
}

interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
}

interface PurchaseOrder {
  id: number;
  customerEmail: string;
  total: number;
  status: string;
  createdAt: string;
  items: OrderItem[];
}

interface Payment {
  id: number;
  orderId: number;
  customerEmail: string;
  amount: number;
  provider: string;
  status: string;
  paidAt: string;
}

interface Shipment {
  id: number;
  orderId: number;
  customerEmail: string;
  status: string;
  trackingNumber: string;
  updatedAt: string;
}

interface NotificationMessage {
  id: number;
  subject: string;
  body: string;
  channel: string;
  status: string;
  createdAt: string;
}

interface EventRecord {
  eventId: string;
  type: string;
  actor: string;
  payload: string;
  occurredAt: string;
}

interface Dashboard {
  registeredUsers: number;
  cartAdds: number;
  orders: number;
  payments: number;
  shipments: number;
  deliveries: number;
  recentEvents: EventRecord[];
}

@Injectable({ providedIn: 'root' })
class CommerceApi {
  private readonly baseUrl = 'http://localhost:8080';

  constructor(private readonly http: HttpClient) {}

  register(email: string, fullName: string) {
    return this.http.post<ApiResponse<unknown>>(`${this.baseUrl}/api/auth/register`, { email, fullName });
  }

  login(email: string, password: string) {
    return this.http.post<ApiResponse<{ token: string }>>(`${this.baseUrl}/api/auth/login`, { email, password });
  }

  products() {
    return this.http.get<ApiResponse<Product[]>>(`${this.baseUrl}/api/catalog/products`);
  }

  createProduct(product: Product) {
    return this.http.post<ApiResponse<Product>>(`${this.baseUrl}/api/catalog/products`, product);
  }

  cart(customerEmail: string) {
    return this.http.get<ApiResponse<CartItem[]>>(`${this.baseUrl}/api/cart?customerEmail=${encodeURIComponent(customerEmail)}`);
  }

  addCartItem(customerEmail: string, product: Product) {
    return this.http.post<ApiResponse<CartItem[]>>(`${this.baseUrl}/api/cart/items?customerEmail=${encodeURIComponent(customerEmail)}`, {
      productId: product.id,
      productName: product.name,
      unitPrice: product.price,
      quantity: 1,
      imageUrl: product.imageUrl
    });
  }

  deleteCartItem(customerEmail: string, productId: string) {
    return this.http.delete<ApiResponse<CartItem[]>>(`${this.baseUrl}/api/cart/items/${productId}?customerEmail=${encodeURIComponent(customerEmail)}`);
  }

  clearCart(customerEmail: string) {
    return this.http.delete<ApiResponse<CartItem[]>>(`${this.baseUrl}/api/cart?customerEmail=${encodeURIComponent(customerEmail)}`);
  }

  createOrder(customerEmail: string, items: CartItem[]) {
    return this.http.post<ApiResponse<PurchaseOrder>>(`${this.baseUrl}/api/orders`, {
      customerEmail,
      items: items.map(item => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        unitPrice: item.unitPrice
      }))
    });
  }

  orders(customerEmail: string) {
    return this.http.get<ApiResponse<PurchaseOrder[]>>(`${this.baseUrl}/api/orders?customerEmail=${encodeURIComponent(customerEmail)}`);
  }

  pay(order: PurchaseOrder) {
    return this.http.post<ApiResponse<Payment>>(`${this.baseUrl}/api/payments`, {
      orderId: order.id,
      customerEmail: order.customerEmail,
      amount: order.total,
      provider: 'demo-card'
    });
  }

  shipments(customerEmail: string) {
    return this.http.get<ApiResponse<Shipment[]>>(`${this.baseUrl}/api/shipping?customerEmail=${encodeURIComponent(customerEmail)}`);
  }

  deliver(orderId: number) {
    return this.http.post<ApiResponse<Shipment>>(`${this.baseUrl}/api/shipping/${orderId}/deliver`, {});
  }

  notifications(customerEmail: string) {
    return this.http.get<ApiResponse<NotificationMessage[]>>(`${this.baseUrl}/api/notifications?customerEmail=${encodeURIComponent(customerEmail)}`);
  }

  dashboard() {
    return this.http.get<ApiResponse<Dashboard>>(`${this.baseUrl}/api/analytics/dashboard`);
  }

  ask(message: string) {
    return this.http.post<ApiResponse<{ answer: string; retrievedContext: string }>>(`${this.baseUrl}/api/ai/chat`, { message });
  }

  analyze(message: string) {
    return this.http.post<ApiResponse<{ answer: string; retrievedContext: string }>>(`${this.baseUrl}/api/ai/agent/analyze`, { message });
  }
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="app-shell">
      <aside class="rail">
        <div class="brand">
          <span>AC</span>
          <div>
            <strong>AI Commerce</strong>
            <small>Agentic ecommerce platform</small>
          </div>
        </div>
        <button *ngFor="let tab of tabs" type="button" [class.active]="activeTab() === tab.id" (click)="activeTab.set(tab.id)">
          {{ tab.label }}
        </button>
      </aside>

      <section class="main">
        <header class="topbar">
          <div>
            <p class="eyebrow">Real-time event driven ecommerce</p>
            <h1>{{ currentTitle() }}</h1>
          </div>
          <div class="identity">
            <span>{{ customerEmail }}</span>
            <button type="button" (click)="refreshAll()">Refresh</button>
          </div>
        </header>

        <section class="metrics">
          <article><span>Users</span><strong>{{ dashboard()?.registeredUsers || 0 }}</strong></article>
          <article><span>Cart Adds</span><strong>{{ dashboard()?.cartAdds || 0 }}</strong></article>
          <article><span>Orders</span><strong>{{ dashboard()?.orders || 0 }}</strong></article>
          <article><span>Payments</span><strong>{{ dashboard()?.payments || 0 }}</strong></article>
          <article><span>Delivered</span><strong>{{ dashboard()?.deliveries || 0 }}</strong></article>
        </section>

        <section class="view" *ngIf="activeTab() === 'auth'">
          <div class="form-grid">
            <div class="panel">
              <h2>Register</h2>
              <input [(ngModel)]="fullName" placeholder="Full name">
              <input [(ngModel)]="customerEmail" placeholder="Email">
              <button type="button" (click)="register()">Create account</button>
            </div>
            <div class="panel">
              <h2>Login</h2>
              <input [(ngModel)]="customerEmail" placeholder="Email">
              <input [(ngModel)]="password" placeholder="Password" type="password">
              <button type="button" (click)="login()">Login</button>
            </div>
          </div>
        </section>

        <section class="view" *ngIf="activeTab() === 'shop'">
          <div class="catalog-tools panel">
            <h2>Add product</h2>
            <input [(ngModel)]="newProduct.name" placeholder="Product name">
            <input [(ngModel)]="newProduct.description" placeholder="Description">
            <input [(ngModel)]="newProduct.price" placeholder="Price" type="number">
            <input [(ngModel)]="newProduct.stock" placeholder="Stock" type="number">
            <input [(ngModel)]="newProduct.imageUrl" placeholder="Image URL">
            <button type="button" (click)="createProduct()">Save product</button>
          </div>
          <div class="product-grid">
            <article class="product" *ngFor="let product of products()">
              <img [src]="product.imageUrl" [alt]="product.name">
              <div>
                <h3>{{ product.name }}</h3>
                <p>{{ product.description }}</p>
                <div class="row">
                  <strong>{{ product.price | currency }}</strong>
                  <button type="button" (click)="addToCart(product)">Add to cart</button>
                </div>
              </div>
            </article>
          </div>
        </section>

        <section class="view split" *ngIf="activeTab() === 'cart'">
          <div class="panel">
            <h2>Live Redis Cart</h2>
            <div class="line-item" *ngFor="let item of cart()">
              <img [src]="item.imageUrl" [alt]="item.productName">
              <div>
                <strong>{{ item.productName }}</strong>
                <span>{{ item.quantity }} x {{ item.unitPrice | currency }}</span>
              </div>
              <button type="button" class="ghost" (click)="deleteCartItem(item.productId)">Delete</button>
            </div>
            <p class="total">Cart total {{ total() | currency }}</p>
            <button type="button" (click)="checkout()" [disabled]="!cart().length">Create order</button>
            <button type="button" class="ghost" (click)="clearCart()" [disabled]="!cart().length">Clear cart</button>
          </div>
          <div class="panel">
            <h2>Latest order</h2>
            <pre>{{ latestOrder() | json }}</pre>
            <button type="button" (click)="payLatest()" [disabled]="!latestOrder()">Pay latest order</button>
          </div>
        </section>

        <section class="view split" *ngIf="activeTab() === 'orders'">
          <div class="panel">
            <h2>Orders</h2>
            <div class="record" *ngFor="let order of orders()">
              <strong>#{{ order.id }} {{ order.total | currency }}</strong>
              <span>{{ order.status }} - {{ order.createdAt | date:'short' }}</span>
              <button type="button" (click)="pay(order)">Pay</button>
            </div>
          </div>
          <div class="panel">
            <h2>Shipping</h2>
            <div class="record" *ngFor="let shipment of shipments()">
              <strong>#{{ shipment.orderId }} {{ shipment.status }}</strong>
              <span>{{ shipment.trackingNumber }}</span>
              <button type="button" (click)="deliver(shipment.orderId)" [disabled]="shipment.status === 'DELIVERED'">Mark delivered</button>
            </div>
          </div>
        </section>

        <section class="view split" *ngIf="activeTab() === 'ai'">
          <div class="panel">
            <h2>RAG shopping assistant</h2>
            <textarea [(ngModel)]="aiQuestion"></textarea>
            <button type="button" (click)="askAi()">Ask assistant</button>
            <p class="answer">{{ aiAnswer() }}</p>
          </div>
          <div class="panel">
            <h2>Agentic analysis</h2>
            <textarea [(ngModel)]="analysisQuestion"></textarea>
            <button type="button" (click)="analyze()">Analyze funnel</button>
            <p class="answer">{{ analysisAnswer() }}</p>
          </div>
        </section>

        <section class="view split" *ngIf="activeTab() === 'dashboard'">
          <div class="panel">
            <h2>Event stream</h2>
            <div class="event" *ngFor="let event of dashboard()?.recentEvents || []">
              <strong>{{ event.type }}</strong>
              <span>{{ event.actor }} - {{ event.occurredAt | date:'short' }}</span>
              <small>{{ event.payload }}</small>
            </div>
          </div>
          <div class="panel">
            <h2>Email notifications</h2>
            <div class="record" *ngFor="let notification of notifications()">
              <strong>{{ notification.subject }}</strong>
              <span>{{ notification.status }} - {{ notification.createdAt | date:'short' }}</span>
              <small>{{ notification.body }}</small>
            </div>
          </div>
        </section>

        <p class="toast">{{ status() }}</p>
      </section>
    </main>
  `
})
class AppComponent implements OnInit {
  tabs = [
    { id: 'auth', label: 'Auth' },
    { id: 'shop', label: 'Shop' },
    { id: 'cart', label: 'Cart' },
    { id: 'orders', label: 'Orders' },
    { id: 'ai', label: 'AI Agent' },
    { id: 'dashboard', label: 'Dashboard' }
  ];
  activeTab = signal('shop');
  products = signal<Product[]>([]);
  cart = signal<CartItem[]>([]);
  orders = signal<PurchaseOrder[]>([]);
  shipments = signal<Shipment[]>([]);
  notifications = signal<NotificationMessage[]>([]);
  dashboard = signal<Dashboard | null>(null);
  status = signal('Ready');
  aiAnswer = signal('');
  analysisAnswer = signal('');
  customerEmail = 'customer@example.com';
  fullName = 'Demo Customer';
  password = 'demo';
  aiQuestion = 'Find the best product for learning Java microservices, Kafka, and AI agents.';
  analysisQuestion = 'Analyze registration to delivery conversion and recommend next action.';
  newProduct: Product = {
    name: 'Agentic Commerce Bundle',
    description: 'End-to-end ecommerce learning bundle with RAG, Kafka events, and analytics.',
    price: 249,
    stock: 25,
    imageUrl: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80'
  };
  total = computed(() => this.cart().reduce((sum, item) => sum + item.unitPrice * item.quantity, 0));
  latestOrder = computed(() => this.orders()[0] || null);
  currentTitle = computed(() => this.tabs.find(tab => tab.id === this.activeTab())?.label || 'AI Commerce');

  constructor(private readonly api: CommerceApi) {}

  ngOnInit() {
    this.loadProducts();
    this.refreshAll();
  }

  register() {
    this.api.register(this.customerEmail, this.fullName).subscribe({
      next: () => this.setStatus('Registered user and emitted USER_REGISTERED.'),
      error: () => this.setStatus('Start auth-service to register.')
    });
  }

  login() {
    this.api.login(this.customerEmail, this.password).subscribe({
      next: () => this.setStatus('Logged in and emitted USER_LOGGED_IN.'),
      error: () => this.setStatus('Start auth-service to login.')
    });
  }

  loadProducts() {
    this.api.products().subscribe({
      next: response => this.products.set(response.data.length ? response.data : this.demoProducts()),
      error: () => this.products.set(this.demoProducts())
    });
  }

  createProduct() {
    this.api.createProduct(this.newProduct).subscribe({
      next: response => {
        this.products.set([response.data, ...this.products()]);
        this.setStatus('Product saved and PRODUCT_CREATED emitted.');
      },
      error: () => this.setStatus('Start catalog-service to save products.')
    });
  }

  addToCart(product: Product) {
    if (!product.id) {
      product.id = product.name.toLowerCase().replace(/\W+/g, '-');
    }
    this.api.addCartItem(this.customerEmail, product).subscribe({
      next: response => {
        this.cart.set(response.data);
        this.activeTab.set('cart');
        this.setStatus('Cart updated in Redis and CART_ITEM_ADDED emitted.');
      },
      error: () => this.setStatus('Start cart-service and Redis to use live cart.')
    });
  }

  deleteCartItem(productId: string) {
    this.api.deleteCartItem(this.customerEmail, productId).subscribe({
      next: response => {
        this.cart.set(response.data);
        this.setStatus('Item deleted and CART_ITEM_REMOVED emitted.');
      },
      error: () => this.setStatus('Start cart-service to delete cart items.')
    });
  }

  clearCart() {
    this.api.clearCart(this.customerEmail).subscribe({
      next: response => this.cart.set(response.data),
      error: () => this.setStatus('Start cart-service to clear cart.')
    });
  }

  checkout() {
    this.api.createOrder(this.customerEmail, this.cart()).subscribe({
      next: response => {
        this.orders.set([response.data, ...this.orders()]);
        this.clearCart();
        this.activeTab.set('orders');
        this.setStatus('Order created and ORDER_CREATED emitted.');
      },
      error: () => this.setStatus('Start order-service before checkout.')
    });
  }

  payLatest() {
    const order = this.latestOrder();
    if (order) {
      this.pay(order);
    }
  }

  pay(order: PurchaseOrder) {
    this.api.pay(order).subscribe({
      next: () => {
        this.setStatus('Payment completed; shipping will be triggered by Kafka.');
        setTimeout(() => this.refreshAll(), 1000);
      },
      error: () => this.setStatus('Start payment-service to process payment.')
    });
  }

  deliver(orderId: number) {
    this.api.deliver(orderId).subscribe({
      next: () => {
        this.setStatus('Order delivered and notification event emitted.');
        this.refreshAll();
      },
      error: () => this.setStatus('Start shipping-service to deliver orders.')
    });
  }

  askAi() {
    this.api.ask(this.aiQuestion).subscribe({
      next: response => this.aiAnswer.set(response.data.answer),
      error: () => this.aiAnswer.set('Start ai-service and Ollama to use RAG assistant.')
    });
  }

  analyze() {
    this.api.analyze(this.analysisQuestion).subscribe({
      next: response => this.analysisAnswer.set(response.data.answer),
      error: () => this.analysisAnswer.set('Start ai-service, analytics-service, and Ollama for agent analysis.')
    });
  }

  refreshAll() {
    this.api.cart(this.customerEmail).subscribe(response => this.cart.set(response.data));
    this.api.orders(this.customerEmail).subscribe(response => this.orders.set(response.data));
    this.api.shipments(this.customerEmail).subscribe(response => this.shipments.set(response.data));
    this.api.notifications(this.customerEmail).subscribe(response => this.notifications.set(response.data));
    this.api.dashboard().subscribe(response => this.dashboard.set(response.data));
  }

  private setStatus(message: string) {
    this.status.set(message);
    this.refreshAll();
  }

  private demoProducts(): Product[] {
    return [
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
    ];
  }
}

bootstrapApplication(AppComponent, {
  providers: [provideHttpClient()]
}).catch(error => console.error(error));
