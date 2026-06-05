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

## AI

```http
POST /api/ai/chat
POST /api/ai/agent/plan
```

```json
{
  "message": "Recommend a product for learning microservices"
}
```
