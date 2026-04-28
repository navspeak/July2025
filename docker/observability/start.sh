#!/bin/sh
set -e

echo "==> Starting observability stack..."
docker compose up -d

echo "==> Waiting for services..."
until curl -s http://localhost:9090/-/ready > /dev/null 2>&1; do sleep 2; done
echo "  Prometheus ready"

until curl -s http://localhost:3000/api/health > /dev/null 2>&1; do sleep 2; done
echo "  Grafana ready"

until curl -s http://localhost:9411/health > /dev/null 2>&1; do sleep 2; done
echo "  Zipkin ready"

echo ""
echo "  Prometheus : http://localhost:9090"
echo "  Grafana    : http://localhost:3000  (admin / admin)"
echo "  Zipkin     : http://localhost:9411"
echo ""
