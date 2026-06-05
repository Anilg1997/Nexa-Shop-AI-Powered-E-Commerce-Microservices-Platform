Write-Host "Starting infrastructure..."
docker compose up -d

Write-Host "Start backend services in separate terminals:"
Write-Host "  cd backend; mvn -pl service-registry spring-boot:run"
Write-Host "  cd backend; mvn -pl api-gateway spring-boot:run"
Write-Host "  cd backend; mvn -pl auth-service spring-boot:run"
Write-Host "  cd backend; mvn -pl catalog-service spring-boot:run"
Write-Host "  cd backend; mvn -pl order-service spring-boot:run"
Write-Host "  cd backend; mvn -pl ai-service spring-boot:run"

Write-Host "Start frontend:"
Write-Host "  cd frontend; npm install; npm start"
