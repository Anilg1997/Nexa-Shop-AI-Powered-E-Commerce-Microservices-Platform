Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Nexa Shop Microservices" -ForegroundColor Cyan
Write-Host "  Starting Local Development Environment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n[1/3] Starting infrastructure (PostgreSQL, MongoDB, Redis, Kafka)..." -ForegroundColor Yellow
docker compose up -d
Write-Host "  Infrastructure started." -ForegroundColor Green

Write-Host "`n[2/3] Build backend services..." -ForegroundColor Yellow
Write-Host "  Run in a separate terminal: cd backend; mvn clean package -DskipTests" -ForegroundColor Gray

Write-Host "`n[3/3] Start services in order (separate terminals):" -ForegroundColor Yellow
Write-Host "  1. cd backend; mvn -pl service-registry spring-boot:run" -ForegroundColor White
Write-Host "  2. cd backend; mvn -pl api-gateway spring-boot:run" -ForegroundColor White
Write-Host "  3. cd backend; mvn -pl auth-service spring-boot:run" -ForegroundColor White
Write-Host "  4. cd backend; mvn -pl catalog-service spring-boot:run" -ForegroundColor White
Write-Host "  5. cd backend; mvn -pl cart-service spring-boot:run" -ForegroundColor White
Write-Host "  6. cd backend; mvn -pl order-service spring-boot:run" -ForegroundColor White
Write-Host "  7. cd backend; mvn -pl payment-service spring-boot:run" -ForegroundColor White
Write-Host "  8. cd backend; mvn -pl shipping-service spring-boot:run" -ForegroundColor White
Write-Host "  9. cd backend; mvn -pl notification-service spring-boot:run" -ForegroundColor White
Write-Host "  10. cd backend; mvn -pl analytics-service spring-boot:run" -ForegroundColor White
Write-Host "  11. cd backend; mvn -pl ai-service spring-boot:run" -ForegroundColor White

Write-Host "`nStart frontend:" -ForegroundColor Yellow
Write-Host "  cd frontend; npm install; npm start" -ForegroundColor White

Write-Host "`nAccess points:" -ForegroundColor Cyan
Write-Host "  Frontend:      http://localhost:4200" -ForegroundColor Green
Write-Host "  API Gateway:   http://localhost:8080" -ForegroundColor Green
Write-Host "  Eureka:        http://localhost:8761" -ForegroundColor Green
Write-Host "  PgAdmin:       http://localhost:5050" -ForegroundColor Green
Write-Host "  Mongo Express: http://localhost:8098" -ForegroundColor Green
Write-Host "  Redpanda:      http://localhost:9644" -ForegroundColor Green

Write-Host "`nPrerequisites:" -ForegroundColor Magenta
Write-Host "  - Java 21+   (java -version)" -ForegroundColor Gray
Write-Host "  - Maven 3.9+ (mvn --version)" -ForegroundColor Gray
Write-Host "  - Node.js 20+(node --version)" -ForegroundColor Gray
Write-Host "  - Docker Desktop" -ForegroundColor Gray
Write-Host "  - Ollama with: ollama pull llama3; ollama serve" -ForegroundColor Gray

Write-Host "`n========================================" -ForegroundColor Cyan
