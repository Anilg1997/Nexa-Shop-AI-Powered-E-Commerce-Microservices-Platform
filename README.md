# AI Commerce Microservices

Full-stack ecommerce starter for Java, Spring Boot microservices, Angular, Kafka, PostgreSQL, MongoDB, Spring AI, LangChain4j, Ollama, and AWS deployment.

## Modules

- `frontend`: Angular ecommerce UI with catalog, cart, checkout, and AI assistant.
- `backend/service-registry`: Eureka discovery server.
- `backend/api-gateway`: Spring Cloud Gateway entry point.
- `backend/auth-service`: demo login/register profile API backed by PostgreSQL.
- `backend/catalog-service`: product catalog backed by MongoDB.
- `backend/cart-service`: Redis-backed live cart with add/delete/clear APIs.
- `backend/order-service`: order workflow backed by PostgreSQL and Kafka.
- `backend/payment-service`: demo payment processing and payment events.
- `backend/shipping-service`: shipment tracking, delivery updates, and payment-triggered shipping.
- `backend/notification-service`: demo email notifications generated from lifecycle events.
- `backend/analytics-service`: Kafka event consumer and real-time dashboard API.
- `backend/ai-service`: Spring AI + Ollama assistant endpoints for RAG and agent-style ecommerce help.
- `infra/aws`: Terraform starter for AWS.

## Local Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 20+
- Docker Desktop
- Ollama with a Llama 3 model, for example:

```powershell
ollama pull llama3
ollama serve
```

## Start Local Infrastructure

```powershell
docker compose up -d
```

This starts PostgreSQL, MongoDB, Redis, Redpanda Kafka, and admin tools.

## Build Backend

```powershell
cd backend
mvn clean package
```

Run services in this order:

```powershell
mvn -pl service-registry spring-boot:run
mvn -pl api-gateway spring-boot:run
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

Gateway: `http://localhost:8080`

Admin tools:

- PgAdmin: `http://localhost:5050`
- Mongo Express: `http://localhost:8098`

## Run Frontend

```powershell
cd frontend
npm install
npm start
```

Angular app: `http://localhost:4200`

## Real-Time Flow

The platform emits all lifecycle events to Kafka topic `commerce.events`:

`USER_REGISTERED -> USER_LOGGED_IN -> CART_ITEM_ADDED -> CART_ITEM_REMOVED -> ORDER_CREATED -> PAYMENT_COMPLETED -> ORDER_SHIPPED -> ORDER_DELIVERED`

Analytics and notification services consume this topic. The AI service can use the analytics dashboard as context for agentic analysis.

## GitHub Setup

```powershell
git add .
git commit -m "Initial AI ecommerce microservices project"
git branch -M main
git remote add origin https://github.com/Anilg1997/ai-commerce-microservices.git
git push -u origin main
```

## AWS Free-Tier Direction

Start small:

- Use one `t3.micro` or `t4g.micro` EC2 instance for a Docker Compose deployment while learning.
- Keep PostgreSQL/Mongo/Kafka local or containerized for demos; managed databases and MSK can exceed free limits quickly.
- Move to ECS/RDS/DocumentDB/MSK only when you are ready for paid production infrastructure.

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md).
