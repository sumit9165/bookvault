#!/bin/bash
# ═══════════════════════════════════════════════════════════════
# BookVault — Development Quick-Start Script
# ═══════════════════════════════════════════════════════════════
set -e
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'

echo -e "${GREEN}╔══════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║       BookVault Dev Environment          ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════╝${NC}"

# Check prerequisites
command -v docker  >/dev/null 2>&1 || { echo -e "${RED}Docker not found. Please install Docker Desktop.${NC}"; exit 1; }
command -v java    >/dev/null 2>&1 || { echo -e "${RED}Java 21+ not found.${NC}"; exit 1; }

# Copy .env if missing
if [ ! -f .env ]; then
  echo -e "${YELLOW}Creating .env from .env.example …${NC}"
  cp .env.example .env
  echo -e "${YELLOW}⚠  Please review .env and change secrets before continuing.${NC}"
fi

# Start MySQL + Redis only (app runs locally)
echo -e "${GREEN}Starting MySQL and Redis via Docker Compose…${NC}"
docker compose up -d mysql redis

echo -e "${YELLOW}Waiting for MySQL to be healthy…${NC}"
until docker compose exec mysql mysqladmin ping -h localhost --silent 2>/dev/null; do
  printf '.'
  sleep 2
done
echo ""
echo -e "${GREEN}✓ MySQL is ready${NC}"

# Build and run
echo -e "${GREEN}Building application…${NC}"
./gradlew bootRun --args='--spring.profiles.active=dev' &

APP_PID=$!
echo -e "${GREEN}✓ Application started (PID: $APP_PID)${NC}"
echo -e "${GREEN}  → http://localhost:8080${NC}"
echo -e "${YELLOW}  Press Ctrl+C to stop${NC}"

trap "echo -e '${YELLOW}Stopping…${NC}'; kill $APP_PID 2>/dev/null; docker compose stop mysql redis" INT
wait $APP_PID
