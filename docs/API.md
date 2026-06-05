# API Quick Reference

Gateway base URL: `http://localhost:8080`

## Auth

```http
POST /api/auth/register
```

```json
{
  "email": "customer@example.com",
  "fullName": "Demo Customer"
}
```

```http
POST /api/auth/login
```

```json
{
  "email": "customer@example.com",
  "password": "demo"
}
```

## Catalog

```http
GET /api/catalog/products
POST /api/catalog/products
```

## Orders

```http
POST /api/orders
GET /api/orders?customerEmail=customer@example.com
```

## Cart

```http
GET /api/cart?customerEmail=customer@example.com
POST /api/cart/items?customerEmail=customer@example.com
DELETE /api/cart/items/{productId}?customerEmail=customer@example.com
DELETE /api/cart?customerEmail=customer@example.com
```

## Payments

```http
POST /api/payments
GET /api/payments?customerEmail=customer@example.com
```

## Shipping

```http
GET /api/shipping?customerEmail=customer@example.com
POST /api/shipping/{orderId}/deliver
```

## Notifications

```http
GET /api/notifications?customerEmail=customer@example.com
```

## Analytics

```http
GET /api/analytics/dashboard
```

## AI

```http
POST /api/ai/chat
POST /api/ai/agent/plan
POST /api/ai/agent/analyze
POST /api/ai/rag/search
```

```json
{
  "message": "Recommend a product for learning microservices"
}
```
