# Auth Service - Production-ready Starter

This repository contains a production-oriented starter for an Authentication microservice:
- Java 25, Spring Boot 4.0.0-SNAPSHOT, Spring Security 7
- JWT access tokens + refresh tokens (rotating refresh tokens)
- PostgreSQL persistence (via docker-compose)
- Endpoints: /api/auth/register, /api/auth/login, /api/auth/refresh, /api/auth/revoke, /actuator/health

## Run locally (quick)
1. Start Postgres:
   docker compose up -d

2. Build & run:
   mvn clean package
   java -jar target/auth-service-1.0.0.jar

3. Default config:
   src/main/resources/application.yml

## Production checklist (non-exhaustive)
- Use secure, long random `jwt.secret` stored in a secrets manager (Vault/KMS).
- Use HTTPS/TLS for all traffic; terminate TLS at ingress/load balancer.
- Consider issuing short-lived access tokens and use refresh token rotation with strict revocation.
- Use audited, centralized logging and monitoring (ELK/Prometheus/Grafana).
- Add rate limiting, WAF protection and account lockouts/brute-force protection.
- Use database backups and secrets rotation.
- Add integration tests and vulnerability scanning.

