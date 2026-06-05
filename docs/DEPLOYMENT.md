# Deployment Guide

## Local First

Run the platform locally before AWS:

```powershell
docker compose up -d
cd backend
mvn clean package
```

Start services in this order:

1. `service-registry`
2. `api-gateway`
3. `auth-service`
4. `catalog-service`
5. `cart-service`
6. `order-service`
7. `payment-service`
8. `shipping-service`
9. `notification-service`
10. `analytics-service`
11. `ai-service`
12. Angular frontend

## AWS Free-Tier Friendly Path

The cheapest learning deployment is one small EC2 instance running Docker Compose.

1. Create a free-tier eligible EC2 instance.
2. Install Docker and Docker Compose.
3. Clone your GitHub repository.
4. Run infrastructure containers with `docker compose up -d`.
5. Build backend jars and frontend assets.
6. Use Nginx or Caddy as the public reverse proxy.

Avoid these for a free project until you are ready to pay:

- Amazon MSK for Kafka.
- DocumentDB for MongoDB.
- Large RDS instances.
- Multi-node ECS/EKS clusters.

## Production Evolution

When the app is ready for paid production:

- ECS Fargate for Spring services.
- RDS PostgreSQL for auth/order data.
- DocumentDB or MongoDB Atlas for catalog.
- ElastiCache Redis for carts and hot product cache.
- MSK or Redpanda Cloud for Kafka.
- S3 + CloudFront for Angular static files.
- Secrets Manager for passwords and tokens.
- CloudWatch logs and alarms.
