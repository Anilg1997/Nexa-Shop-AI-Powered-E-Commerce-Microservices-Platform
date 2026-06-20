@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-24
set POSTGRES_PASSWORD=root
set POSTGRES_USER=postgres
set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
set BACKEND=C:\Users\Lenovo\ai-commerce-microservices\backend
set LOGDIR=%TEMP%

cd /d "%BACKEND%"

echo Starting service-registry (8761)...
start "sr" cmd /c ""%JAVA_HOME%\bin\java" -jar "service-registry\target\service-registry-0.1.0-SNAPSHOT.jar" > "%LOGDIR%\sr.log" 2>&1"
timeout /t 15 /nobreak > nul

echo Starting api-gateway (8080)...
start "gw" cmd /c ""%JAVA_HOME%\bin\java" -jar "api-gateway\target\api-gateway-0.1.0-SNAPSHOT.jar" > "%LOGDIR%\gw.log" 2>&1"
timeout /t 10 /nobreak > nul

echo Starting auth-service (8081)...
start "auth" cmd /c ""%JAVA_HOME%\bin\java" -jar "auth-service\target\auth-service-0.1.0-SNAPSHOT.jar" > "%LOGDIR%\auth.log" 2>&1"
timeout /t 10 /nobreak > nul

echo Starting catalog-service (8082)...
start "catalog" cmd /c ""%JAVA_HOME%\bin\java" -jar "catalog-service\target\catalog-service-0.1.0-SNAPSHOT.jar" > "%LOGDIR%\catalog.log" 2>&1"
timeout /t 10 /nobreak > nul

echo Starting order-service (8083)...
start "order" cmd /c ""%JAVA_HOME%\bin\java" -jar "order-service\target\order-service-0.1.0-SNAPSHOT.jar" > "%LOGDIR%\order.log" 2>&1"
timeout /t 10 /nobreak > nul

echo Starting ai-service (8084)...
start "ai" cmd /c ""%JAVA_HOME%\bin\java" -jar "ai-service\target\ai-service-0.1.0-SNAPSHOT.jar" > "%LOGDIR%\ai.log" 2>&1"
timeout /t 10 /nobreak > nul

echo Starting cart-service (8085)...
start "cart" cmd /c ""%JAVA_HOME%\bin\java" -jar "cart-service\target\cart-service-0.1.0-SNAPSHOT.jar" > "%LOGDIR%\cart.log" 2>&1"
timeout /t 10 /nobreak > nul

echo Starting payment-service (8086)...
start "payment" cmd /c ""%JAVA_HOME%\bin\java" -jar "payment-service\target\payment-service-0.1.0-SNAPSHOT.jar" > "%LOGDIR%\payment.log" 2>&1"

echo All services launched. Waiting 30s for startup...
timeout /t 30 /nobreak > nul

echo ======== CHECKING HEALTH ========
powershell -Command "& { $ports = @{8761='sr';8080='gw';8081='auth';8082='catalog';8083='order';8084='ai';8085='cart';8086='payment'}; foreach ($p in $ports.Keys) { $c = netstat -ano | Select-String \":$p \" | Select-String 'LISTENING'; if ($c -and $c.Count -gt 0) { Write-Host \"$($ports[$p]) ($p): UP\" } else { Write-Host \"$($ports[$p]) ($p): waiting...\" } } }"
pause
