#!/bin/bash

set -eo pipefail  # Exit on error, even in piped commands

PORT=8080
HEALTH_ENDPOINT="http://localhost:$PORT/actuator/health"
TEST_ENDPOINT="http://localhost:$PORT/hello"
JAR_PATH="target/*.jar"

echo "🚀 Building the Spring Boot project..."
mvn clean package -DskipTests

echo "✅ Build complete. Starting the app..."
java -jar $JAR_PATH &
APP_PID=$!

# Ensure the app is killed on exit
cleanup() {
  echo "🧼 Stopping the app (PID: $APP_PID)..."
  kill $APP_PID
}
trap cleanup EXIT

# Wait for actuator to become healthy
echo "🔄 Waiting for app to become healthy..."

for i in {1..30}; do
  RAW=$(curl -s "$HEALTH_ENDPOINT" || echo "")

  if [[ -z "$RAW" ]]; then
    STATUS="NO_RESPONSE"
  else
    STATUS=$(echo "$RAW" | grep -o '"status":"[^"]*"' | head -n 1 | cut -d':' -f2 | tr -d '"')
    STATUS=${STATUS:-UNKNOWN}
  fi

  echo "⏳ Attempt $i: Health status = $STATUS"

  if [ "$STATUS" = "UP" ]; then
    echo "✅ App is healthy!"
    break
  fi

  sleep 1
done

if [ "$STATUS" != "UP" ]; then
  echo "❌ App did not become healthy in time."
  echo "Last health check response:"
  curl -s "$HEALTH_ENDPOINT" || echo "(no response)"
  exit 1
fi

# Test the main endpoint
echo "🔍 Testing $TEST_ENDPOINT"
curl -i "$TEST_ENDPOINT"
