#!/bin/bash
# ═══════════════════════════════════════════════════════════════
# BookVault — Production Deploy Script
# ═══════════════════════════════════════════════════════════════
set -e
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'

echo -e "${GREEN}╔══════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║     BookVault Production Deploy          ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════╝${NC}"

# Guard: must have .env
if [ ! -f .env ]; then
  echo -e "${RED}ERROR: .env file not found!${NC}"
  echo "Copy .env.example to .env and fill in production values."
  exit 1
fi

# Validate critical secrets
source .env
if [ "$JWT_SECRET" = "Replace_This_With_A_Very_Long_Random_Secret_Key_At_Least_64_Characters_Long_For_Security" ]; then
  echo -e "${RED}ERROR: Please change JWT_SECRET in .env before deploying to production!${NC}"
  exit 1
fi
if [ -z "$ADMIN_PASSWORD" ] || [ "$ADMIN_PASSWORD" = "Admin@SecurePassword123!" ]; then
  echo -e "${YELLOW}WARNING: Please set a strong ADMIN_PASSWORD in .env${NC}"
fi

echo -e "${GREEN}Building Docker images…${NC}"
docker compose build --no-cache

echo -e "${GREEN}Starting all services (including Nginx)…${NC}"
docker compose --profile production up -d

echo -e "${YELLOW}Waiting for app health check…${NC}"
ATTEMPTS=0
until curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1 || [ $ATTEMPTS -ge 30 ]; do
  printf '.'
  sleep 3
  ATTEMPTS=$((ATTEMPTS+1))
done
echo ""

if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
  echo -e "${GREEN}✓ BookVault is running!${NC}"
  echo -e "${GREEN}  → Application: http://localhost:8080${NC}"
  echo -e "${GREEN}  → Via Nginx:   http://localhost:80${NC}"
else
  echo -e "${RED}⚠  Application did not become healthy. Check logs:${NC}"
  echo "  docker compose logs app"
fi
