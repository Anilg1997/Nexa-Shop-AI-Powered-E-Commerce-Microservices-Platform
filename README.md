# Nexa Shop — AI-Powered E-Commerce Microservices Platform

[![Java 21](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)](https://spring.io/projects/spring-boot)
[![Angular 18](https://img.shields.io/badge/Angular-18-red)](https://angular.dev/)
[![Kafka](https://img.shields.io/badge/Kafka-Redpanda-black)](https://redpanda.com/)
[![Ollama](https://img.shields.io/badge/Ollama-Llama3-yellow)](https://ollama.ai/)
[![pgvector](https://img.shields.io/badge/pgvector-RAG-blueviolet)](https://github.com/pgvector/pgvector)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

> **A production-grade, open-source e-commerce platform powered by RAG-based LLM agents, real-time event streaming, and microservices architecture. Built with Spring Boot 3.3, Angular 18, Apache Kafka, and LangChain4j — with AI features that would impress any 2026 tech reviewer.**

---

## ✨ AI-Era Features

| Feature | Description |
|---------|-------------|
| **🤖 RAG Shopping Assistant** | Ollama + LangChain4j-powered AI agent answers product questions, compares options, and provides personalized recommendations using retrieved knowledge |
| **📊 Agentic Funnel Analysis** | AI analyzes real-time e-commerce analytics and suggests optimizations across the registration-to-delivery funnel |
| **🔧 MCP Tools** | Model Context Protocol tools allow the AI to query dashboards, search products, and access system knowledge dynamically |
| **🧠 pgvector RAG** | PostgreSQL vector database stores knowledge embeddings for semantic similarity search |
| **📈 Real-Time Analytics** | Kafka event-driven dashboard shows live funnel metrics, conversion rates, and event streams |
| **🔐 JWT + RBAC** | Real JWT authentication with CUSTOMER and ADMIN roles for secure access control |
| **🛍️ Admin Panel** | Full admin dashboard: product CRUD, order management, user role management, funnel analytics |
| **⚡ Event-Driven** | All lifecycle events flow through Kafka: register → cart → order → payment → shipping → delivery |
| **🔄 Reactive UI** | Angular 18 Signals architecture with real-time state management and responsive Flipkart-like design |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Angular 18 SPA                     │
│  Home │ Products │ AI Assistant │ Cart │ Admin       │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP + JWT
┌──────────────────────▼──────────────────────────────┐
│           API Gateway (Spring Cloud Gateway)         │
└──┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬────┘
   │   │   │   │   │   │   │   │   │   │   │   │
   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼
 Auth Cat Cart Ord Pay Ship Notif Anal AI   Eureka
(8081)(8082)(8085)(8083)(8086)(8087)(8088)(8089)(8084)(8761)
   │   │   │   │   │   │   │   │   │
   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼   ▼
  PG   MDB Redis PG  PG  PG  PG  PG  PG+Vec
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for complete HLD & LLD.

---

## 🚀 Tech Stack

### Frontend
- **Angular 18** — Standalone components, Signals, Router, HttpClient
- **TypeScript** — Full type safety across all models and services
- **CSS3** — Flipkart-inspired design system with responsive grid

### Backend
- **Java 21** — Virtual threads, records, sealed classes
- **Spring Boot 3.3.5** — Auto-configuration, Actuator, Validation
- **Spring Cloud 2023.0.3** — Gateway, Eureka, Load Balancing
- **Spring AI 1.0.0-M3** — Ollama integration, ChatClient
- **LangChain4j 0.35.0** — AI agent chaining
- **JJWT 0.12.6** — JWT token generation and validation

### Data & Events
- **PostgreSQL 16** — Relational data (auth, orders, payments, shipping, analytics)
- **pgvector** — Vector embeddings for RAG similarity search
- **MongoDB 7** — Product catalog and reviews
- **Redis 7** — Live shopping cart
- **Redpanda (Kafka-compatible)** — Event bus for all domain events

### AI & ML
- **Ollama** — Local LLM inference (llama3, mistral, etc.)
- **RAG** — Retrieval Augmented Generation with knowledge base
- **MCP** — Model Context Protocol for tool-calling
- **Agentic AI** — Autonomous planning and analysis agents

### DevOps
- **Docker Compose** — One-command infrastructure setup
- **Maven** — Multi-module build system
- **Terraform** — AWS deployment (infra/aws)

---

## 📂 Project Structure

```
ai-commerce-microservices/
├── frontend/                 ← Angular 18 SPA
│   └── src/
│       ├── app/
│       │   ├── components/   ← Header, Footer, ProductCard
│       │   ├── guards/       ← AuthGuard, AdminGuard
│       │   ├── interceptors/ ← JWT interceptor
│       │   ├── models/       ← TypeScript interfaces
│       │   ├── pages/        ← All page components
│       │   │   ├── auth/     ← Login/Register
│       │   │   ├── home/     ← Hero, Categories, Featured
│       │   │   ├── products/ ← Catalog + Filters
│       │   │   ├── cart/     ← Shopping cart
│       │   │   ├── checkout/ ← Address + Payment
│       │   │   ├── orders/   ← Orders + Shipments
│       │   │   ├── ai-assistant/ ← RAG Chat + MCP
│       │   │   └── admin/    ← Dashboard, Products, Orders, Users
│       │   └── services/     ← AuthService, ApiService
│       └── main.ts           ← App bootstrap
├── backend/                  ← Java Spring Boot microservices
│   ├── common-lib/           ← Shared DTOs (ApiResponse, DomainEvent)
│   ├── service-registry/     ← Eureka server
│   ├── api-gateway/          ← Spring Cloud Gateway
│   ├── auth-service/         ← JWT auth, RBAC
│   ├── catalog-service/      ← Products, Reviews, Categories
│   ├── cart-service/         ← Redis cart
│   ├── order-service/        ← Order lifecycle
│   ├── payment-service/      ← Payment processing
│   ├── shipping-service/     ← Shipment tracking
│   ├── notification-service/ ← Email notifications
│   ├── analytics-service/    ← Event analytics
│   └── ai-service/           ← RAG, Agents, MCP
├── docker/
│   └── postgres/init.sql     ← Database setup with pgvector
├── docs/
│   ├── ARCHITECTURE.md       ← HLD & LLD documentation
│   ├── API.md                ← API reference
│   └── DEPLOYMENT.md         ← AWS deployment guide
├── scripts/
│   ├── start-local.ps1       ← Local dev startup
│   └── seed-products.ps1     ← Seed 10 demo products
├── screenshots/              ← App screenshots
└── docker-compose.yml        ← Infrastructure services
```

---

## ⚡ Quick Start (5 Minutes)

### Prerequisites

- [Java 21+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/download.cgi)
- [Node.js 20+](https://nodejs.org/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Ollama](https://ollama.ai/) with a model:

```powershell
ollama pull llama3
ollama serve
```

### 1. Start Infrastructure

```powershell
docker compose up -d
```

This starts PostgreSQL (with pgvector), MongoDB, Redpanda (Kafka), Redis, PgAdmin, and Mongo Express.

### 2. Build Backend

```powershell
cd backend
mvn clean package -DskipTests
```

### 3. Start Backend Services (in order)

```powershell
# Terminal 1
mvn -pl service-registry spring-boot:run

# Terminal 2
mvn -pl api-gateway spring-boot:run

# Then in any order:
mvn -pl auth-service spring-boot:run
mvn -pl catalog-service spring-boot:run
mvn -pl cart-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl payment-service spring-boot:run
mvn -pl shipping-service spring-boot:run
mvn -pl notification-service spring-boot:run
mvn -pl analytics-service spring-boot:run
mvn -pl ai-service spring-boot:run
```

### 4. Seed Demo Products

```powershell
.\scripts\seed-products.ps1
```

### 5. Start Frontend

```powershell
cd frontend
npm install
npm start
```

### 6. Open App

Navigate to **http://localhost:4200** 🎉

---

## 🔗 Access Points

| Service           | URL                          |
|------------------|------------------------------|
| Frontend App     | http://localhost:4200         |
| API Gateway      | http://localhost:8080         |
| Eureka Dashboard | http://localhost:8761         |
| PgAdmin          | http://localhost:5050         |
| Mongo Express    | http://localhost:8098         |
| Redpanda Console | http://localhost:9644         |
| Ollama API       | http://localhost:11434        |

---

## 🛒 Demo Credentials

| Role     | Email                  | Password |
|----------|------------------------|----------|
| Admin    | admin@example.com      | admin    |
| Customer | customer@example.com   | demo     |

---

## 📊 Real-Time Event Flow

```
User Registration → Add to Cart → Create Order → Payment → Shipping → Delivery
     │                │              │             │          │           │
     ▼                ▼              ▼             ▼          ▼           ▼
  Kafka ─────────── Kafka ──────── Kafka ──────── Kafka ──── Kafka ───── Kafka
     │                │              │             │          │           │
     └────────────────┴──────────────┴─────────────┴──────────┴───────────┘
                                        │
                                   Analytics
                                   Dashboard
                                   (Live updates)
```

---

## 🤖 AI Features Deep Dive

### RAG Shopping Assistant
1. User asks a question (e.g., "Find the best laptop for Java development")
2. AI service searches KnowledgeBase for relevant documents
3. Retrieved context is injected into the LLM prompt
4. Ollama generates a contextual, accurate response
5. Response includes the retrieved knowledge references

### Agentic Funnel Analysis
1. User requests analysis ("Analyze conversion funnel")
2. Agent fetches real-time dashboard data from analytics-service
3. Knowledge base provides context about e-commerce metrics
4. LangChain4j chains the analysis steps
5. LLM returns actionable insights with recommendations

### MCP (Model Context Protocol) Tools
- **dashboard_analysis**: Real-time analytics data retrieval
- **product_search**: Product catalog search
- **system_knowledge**: RAG knowledge base queries

### pgvector RAG
- Knowledge documents stored in PostgreSQL with vector embeddings
- Semantic similarity search for better context retrieval
- Auto-seeded with 10 system knowledge documents

---

## 👑 Admin Panel

| Page               | Description                                    |
|-------------------|------------------------------------------------|
| **Dashboard**     | Funnel metrics, conversion rates, event stream  |
| **Products**      | Add/edit/delete products, manage stock & prices |
| **Orders**        | View all orders, update status (Pay/Ship/Deliver) |
| **Users**         | List users, promote/demote admin roles          |

Access at `http://localhost:4200/admin` (login with admin@example.com)

---

## 🔐 Security

- **JWT Authentication** — Real tokens with HS256 signing (not demo tokens)
- **Role-Based Access** — CUSTOMER and ADMIN roles
- **JWT Interceptor** — Automatic Bearer token attachment
- **Route Guards** — AuthGuard protects authenticated routes, AdminGuard protects admin routes
- **Token Storage** — Securely stored in localStorage with automatic expiration

---

## 🧪 Testing

```powershell
# Frontend tests
cd frontend
npm test

# Backend tests
cd backend
mvn test
```

---

## ☁️ AWS Deployment

```powershell
cd infra/aws
terraform init
terraform plan
terraform apply
```

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for detailed deployment instructions.

---

## 📚 Documentation

- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** — Complete HLD & LLD with diagrams
- **[API.md](docs/API.md)** — Full API reference
- **[DEPLOYMENT.md](docs/DEPLOYMENT.md)** — AWS and production deployment guide
- **[COMMON.md](docs/COMMON.md)** — Getting started FAQ

---

## 🏆 Why This Project Stands Out

### For Recruiters & Tech Leads

1. **Modern Stack** — Java 21 + Spring Boot 3.3 + Angular 18 — the cutting edge of enterprise development
2. **AI-Native** — Not just bolted-on AI, but deeply integrated RAG, agentic AI, and MCP tooling
3. **Production Architecture** — Microservices, event-driven, CQRS patterns, service discovery, API gateway
4. **Real-Time** — Kafka-powered event bus with live analytics dashboard
5. **Full-Stack** — Complete e-commerce flow from registration to delivery with admin panel
6. **Vector Search** — pgvector integration for semantic RAG — the standard for production AI apps
7. **Clean Code** — Standalone Angular components, Java records, signals-based state management
8. **DevOps Ready** — Docker Compose, Terraform, one-command startup

### For Developers

- **Learn by example**: See how Spring AI, LangChain4j, and Ollama work together in a real app
- **Extensible**: Add new services, AI agents, or MCP tools easily
- **Well-documented**: Architecture docs, API references, and README
- **Open source**: MIT licensed — fork, modify, and deploy

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

MIT — See [LICENSE](LICENSE) for details.

---

## 🙏 Acknowledgments

- Spring AI team for the Ollama integration
- LangChain4j project for Java AI agents
- pgvector for PostgreSQL vector search
- Redpanda for the Kafka-compatible event streaming

---

**Built for the AI era.** If you like this project, give it a ⭐!
