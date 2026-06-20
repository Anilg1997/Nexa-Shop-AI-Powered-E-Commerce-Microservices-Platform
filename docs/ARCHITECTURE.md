# Architecture Documentation

## High-Level Design (HLD)

### System Overview

Nexa Shop is a **cloud-native, event-driven microservices platform** for AI-powered e-commerce. It combines traditional e-commerce functionality with RAG-based LLM agents, real-time event streaming, and vector search capabilities.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                                 │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              Angular 18 SPA (Standalone Components)          │   │
│  │  ┌─────────┐ ┌────────┐ ┌──────────┐ ┌────────────────┐    │   │
│  │  │  Home   │ │Products│ │ AI       │ │ Admin Panel    │    │   │
│  │  │  Page   │ │ Catalog│ │Assistant │ │ (Dashboard,    │    │   │
│  │  │         │ │+Filter │ │(RAG Chat)│ │ Products,      │    │   │
│  │  │         │ │        │ │          │ │ Orders, Users) │    │   │
│  │  └─────────┘ └────────┘ └──────────┘ └────────────────┘    │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────────────┘
                           │ HTTP / JWT
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (Spring Cloud Gateway)              │
│                  Port 8080 · Route & Auth Forwarding                 │
└──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┘
       │      │      │      │      │      │      │      │      │
       ▼      ▼      ▼      ▼      ▼      ▼      ▼      ▼      ▼
┌─────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌──────┐
│Auth │ │Cat │ │Cart│ │Ord │ │Pay │ │Ship│ │Ntf │ │Anly│ │ AI   │
│8081 │ │8082│ │8085│ │8083│ │8086│ │8087│ │8088│ │8089│ │8084  │
└──┬──┘ └──┬─┘ └──┬─┘ └──┬─┘ └──┬─┘ └──┬─┘ └──┬─┘ └──┬─┘ └──┬───┘
   │       │       │       │       │       │       │       │
   ▼       ▼       ▼       ▼       ▼       ▼       ▼       ▼
┌─────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌──────┐
│PG DB│ │MDB │ │Rdis│ │PG  │ │PG  │ │PG  │ │PG  │ │PG  │ │PG    │
│auth │ │cat │ │cart│ │ord │ │pay │ │ship│ │ntf │ │anal│ │vect  │
└─────┘ └────┘ └────┘ └────┘ └────┘ └────┘ └────┘ └────┘ └──────┘
   │       │       │       │       │       │       │       │
   └───────┴───────┴───────┴───────┴───────┴───────┴───────┴──────┘
                           │
                           ▼
              ┌────────────────────────┐
              │   KAFKA (commerce.events) │
              │   Event Bus             │
              └────────────────────────┘
```

### Technology Stack

| Layer        | Technology                                      |
|-------------|-------------------------------------------------|
| Frontend    | Angular 18, TypeScript, Signals, RxJS           |
| Backend     | Java 21, Spring Boot 3.3.5, Spring Cloud        |
| Auth        | JWT (jjwt 0.12), Role-Based Access Control      |
| Database    | PostgreSQL 16 (pgvector), MongoDB 7, Redis 7    |
| Event Bus   | Redpanda Kafka 24 (Kafka-compatible)            |
| AI          | Spring AI 1.0, Ollama, LangChain4j 0.35         |
| Vector DB   | pgvector (PostgreSQL extension)                 |
| Service Disc| Netflix Eureka                                  |
| API Gateway | Spring Cloud Gateway                            |
| Infra       | Docker, Docker Compose                          |

### Service Architecture

| Service             | Port | DB         | Responsibility                                 |
|--------------------|------|-----------|-------------------------------------------------|
| service-registry   | 8761 | —         | Eureka service discovery                       |
| api-gateway        | 8080 | —         | Route all API requests via service discovery   |
| auth-service       | 8081 | PostgreSQL| JWT auth, RBAC (CUSTOMER/ADMIN), user mgmt     |
| catalog-service    | 8082 | MongoDB   | Products, categories, reviews                   |
| cart-service       | 8085 | Redis     | Live shopping cart (per-email)                 |
| order-service      | 8083 | PostgreSQL| Order lifecycle management                     |
| payment-service    | 8086 | PostgreSQL| Payment processing & event publishing          |
| shipping-service   | 8087 | PostgreSQL| Shipment creation, tracking, delivery          |
| notification-service| 8088| PostgreSQL| Demo email notifications from lifecycle events |
| analytics-service  | 8089 | PostgreSQL| Event consumption, funnel metrics, dashboard   |
| ai-service         | 8084 | PostgreSQL| RAG, LangChain4j agents, MCP tools, Ollama     |

---

## Low-Level Design (LLD)

### 1. Event-Driven Architecture

The entire platform is connected via the `commerce.events` Kafka topic:

```
USER_REGISTERED → USER_LOGGED_IN → CART_ITEM_ADDED → CART_ITEM_REMOVED
→ ORDER_CREATED → PAYMENT_COMPLETED → ORDER_SHIPPED → ORDER_DELIVERED
```

Each event is a `DomainEvent` record with:
- `eventId` (UUID)
- `type` (event type name)
- `aggregateId` (domain entity identifier)
- `actor` (who performed the action)
- `payload` (Map<String, Object> with event data)
- `occurredAt` (Instant timestamp)

### 2. Frontend Component Architecture (Angular 18)

```
app/
├── components/
│   ├── header/          → Navigation, search, user menu
│   ├── footer/          → Site footer with links
│   └── product-card/    → Reusable product card
├── guards/
│   ├── auth.guard.ts    → Requires JWT authentication
│   └── admin.guard.ts   → Requires ADMIN role
├── interceptors/
│   └── jwt.interceptor.ts → Attaches Bearer token to requests
├── models/
│   └── models.ts        → All TypeScript interfaces
├── pages/
│   ├── auth/            → Login/Register with JWT
│   ├── home/            → Hero banner, categories, featured products
│   ├── products/        → Product catalog with filters & search
│   ├── product-detail/  → Product view, reviews, add to cart
│   ├── cart/            → Shopping cart with quantity management
│   ├── checkout/        → Address form and order placement
│   ├── orders/          → Order history, shipments, notifications
│   ├── ai-assistant/    → RAG chat, MCP tools, agent plan
│   └── admin/
│       ├── dashboard/   → Funnel metrics, charts, event stream
│       ├── products/    → CRUD product management
│       ├── orders/      → Order status management
│       └── users/       → User role management
├── services/
│   ├── auth.service.ts  → JWT token management, login/register
│   └── api.service.ts   → All HTTP API calls
└── app.routes.ts        → Route definitions with guards
```

### 3. AI Service Architecture (RAG + Agentic)

```
┌──────────────────────────────────────────────────────────────┐
│                       AI SERVICE (8084)                       │
│                                                              │
│  ┌─────────────┐   ┌──────────────┐   ┌──────────────────┐  │
│  │  Chat Endpoint │   │  Agent Endpoint│   │  MCP Endpoint     │  │
│  │  POST /chat   │   │  POST /agent/  │   │  POST /mcp/execute │
│  └──────┬──────┘   │  analyze, plan │   └────────┬─────────┘  │
│         │          └──────┬───────┘              │            │
│         ▼                 ▼                      │            │
│  ┌────────────────────────────────────────────┐  │            │
│  │         Spring AI ChatClient               │  │            │
│  │         + Ollama (llama3)                  │  │            │
│  └─────────────────────┬──────────────────────┘  │            │
│                        │                         │            │
│  ┌─────────────────────▼──────────────────────────▼──────────┐ │
│  │              Knowledge Base (RAG)                        │ │
│  │  ┌────────────────────────────────────────────────────┐  │ │
│  │  │  PostgreSQL + pgvector                             │  │ │
│  │  │  - Knowledge documents with embeddings             │  │ │
│  │  │  - Term-based and vector search                    │  │ │
│  │  └────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                              │
│  External Calls:                                             │
│  ┌────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ Analytics  │  │ Catalog      │  │ Ollama (localhost:   │  │
│  │ Service    │  │ Service      │  │ 11434)               │  │
│  └────────────┘  └──────────────┘  └──────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### 4. Authentication Flow

```
┌──────────┐          ┌──────────┐          ┌──────────────┐
│  Client  │          │ Gateway  │          │ Auth Service │
└────┬─────┘          └────┬─────┘          └──────┬───────┘
     │  POST /api/auth/     │                      │
     │  login {email,pass}  │                      │
     │─────────────────────►│─────────────────────►│
     │                      │                      │ Verify user
     │                      │                      │ Generate JWT
     │                      │                      │ (email+role)
     │                      │                      │ Publish event
     │  {token,email,role}  │                      │
     │◄─────────────────────│◄─────────────────────│
     │                      │                      │
     │  [Store JWT in       │                      │
     │   localStorage]      │                      │
     │                      │                      │
     │  GET /api/orders     │                      │
     │  Authorization:      │                      │
     │  Bearer <token>      │                      │
     │─────────────────────►│ (JWT Interceptor)    │
     │                      │ Forward to order-svc │
     │◄─────────────────────│                      │
```

### 5. Checkout & Payment Flow

```
Cart → Checkout → Order Created → Payment → Auto-Shipping → Delivered
  │        │            │            │            │              │
  │        │            │            │            │              │
  ▼        ▼            ▼            ▼            ▼              ▼
Redis   Address      Kafka       Kafka        Kafka          Kafka
Cart     Form      ORDER_      PAYMENT_     ORDER_         ORDER_
Items            CREATED     COMPLETED     SHIPPED        DELIVERED
```

### 6. Database Schema

**PostgreSQL Databases (one per service):**
- `authdb`: user_accounts (id, email, full_name, password, role)
- `orderdb`: purchase_orders (id, customer_email, total, status, created_at)
- `paymentdb`: payments (id, order_id, customer_email, amount, status)
- `shippingdb`: shipments (id, order_id, customer_email, status, tracking_number)
- `notificationdb`: notifications (id, customer_email, subject, body, status)
- `analyticsdb`: event_records (event_id, type, aggregate_id, actor, payload)
- `vectordb`: knowledge_docs (id, title, category, content, embedding VECTOR)

**MongoDB Collections:**
- `catalogdb.products`: Product documents with categories, ratings, features
- `catalogdb.reviews`: Review documents linked to products

**Redis Keys:**
- `cart:<email>`: List of CartItem objects

### 7. MCP (Model Context Protocol) Tools

The AI service exposes MCP-compatible tools:

| Tool                | Description                                    |
|--------------------|------------------------------------------------|
| dashboard_analysis | Fetches and analyzes real-time analytics data  |
| product_search     | Searches the product catalog                   |
| system_knowledge   | Retrieves RAG knowledge base documents         |

### 8. Admin Panel Features

| Page               | Features                                          |
|-------------------|--------------------------------------------------|
| Admin Dashboard   | Funnel metrics, conversion rates, event stream    |
| Manage Products   | CRUD operations, categories, stock management     |
| Manage Orders     | Order lifecycle, status transitions, filtering    |
| Manage Users      | Promote/demote roles, user listing                |

---

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Docker Compose                          │
│                                                         │
│  ┌──────────┐ ┌──────┐ ┌─────────┐ ┌──────┐ ┌───────┐  │
│  │PostgreSQL │ │Mongo │ │Redpanda │ │Redis │ │PgAdmin│  │
│  │ +pgvector │ │  DB  │ │ (Kafka) │ │      │ │+Mongo │  │
│  └──────────┘ └──────┘ └─────────┘ └──────┘ │Express│  │
│                                              └───────┘  │
└─────────────────────────────────────────────────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
      ┌──────────────┐          ┌──────────────┐
      │  Eureka SR   │          │  API Gateway  │
      │  (Port 8761) │          │  (Port 8080)  │
      └──────────────┘          └──────────────┘
              │                         │
              └────────────┬────────────┘
                           ▼
              ┌────────────────────────┐
              │  11 Spring Boot       │
              │  Microservices        │
              │  (Ports 8081-8089)    │
              └────────────────────────┘
```

### Port Summary

| Service             | Port |
|--------------------|------|
| Frontend (Angular) | 4200 |
| API Gateway        | 8080 |
| Auth Service       | 8081 |
| Catalog Service    | 8082 |
| Order Service      | 8083 |
| AI Service         | 8084 |
| Cart Service       | 8085 |
| Payment Service    | 8086 |
| Shipping Service   | 8087 |
| Notification Svc   | 8088 |
| Analytics Service  | 8089 |
| Service Registry   | 8761 |
| PgAdmin            | 5050 |
| Mongo Express      | 8098 |
| Redpanda Console   | 9644 |
| Ollama             | 11434|

---

## AI & RAG Architecture Details

### RAG Pipeline

```
User Question
    │
    ▼
┌─────────────────────────────────────────┐
│ 1. Query Understanding                  │
│    - Extract intent, entities           │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│ 2. Knowledge Retrieval                  │
│    - Term-based search in KnowledgeBase │
│    - pgvector similarity search         │
│    - Combine & rank top 3-5 docs        │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│ 3. Context Assembly                     │
│    - System prompt + retrieved docs     │
│    - User question + chat history       │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│ 4. LLM Generation (Ollama + llama3)     │
│    - Spring AI ChatClient               │
│    - LangChain4j chaining               │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│ 5. Response Delivery                    │
│    - Answer text + retrieved context    │
│    - Citations to source documents      │
└─────────────────────────────────────────┘
```

### Agentic AI Architecture

```
┌─────────────────────────────────────────────────────┐
│                  AI Agent Pipeline                    │
│                                                      │
│  User Request                                        │
│      │                                               │
│      ▼                                               │
│  ┌─────────────────────────────────────────────┐     │
│  │  1. Agent Planning (LangChain4j)            │     │
│  │     - Understand intent                      │     │
│  │     - Decompose into steps                   │     │
│  │     - Select tools needed                    │     │
│  └─────────────────┬───────────────────────────┘     │
│                    │                                 │
│      ┌─────────────┼─────────────┐                   │
│      ▼             ▼             ▼                   │
│  ┌────────┐  ┌──────────┐  ┌──────────┐            │
│  │ RAG    │  │ MCP Tools │  │ Catalog  │            │
│  │ Search │  │(Dashboard,│  │ Search   │            │
│  │        │  │ Products) │  │          │            │
│  └────────┘  └──────────┘  └──────────┘            │
│                    │                                 │
│                    ▼                                 │
│  ┌─────────────────────────────────────────────┐     │
│  │  3. Response Synthesis                       │     │
│  │     - Aggregate tool results                 │     │
│  │     - Generate human-readable answer         │     │
│  │     - Provide actionable insights            │     │
│  └─────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────┘
```

### Vector Search with pgvector

```
┌──────────────────────────────────────┐
│          pgvector (PostgreSQL)        │
│                                       │
│  knowledge_docs table:                │
│  ┌────┬───────┬────────┬──────────┐  │
│  │ id │ title │content │embedding │  │
│  ├────┼───────┼────────┼──────────┤  │
│  │ 1  │Arch   │The...  │[0.1,...] │  │
│  │ 2  │Catalog│Prod... │[0.3,...] │  │
│  └────┴───────┴────────┴──────────┘  │
│                                       │
│  Query: SELECT * FROM knowledge_docs │
│  ORDER BY embedding <-> query_vec    │
│  LIMIT 5;                            │
└──────────────────────────────────────┘
```

---

## Getting Started

See [README.md](../README.md) for complete setup instructions.
