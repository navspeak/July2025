#!/bin/bash

set -e  # Exit on any error

APP_NAME="demo-hello"
IMAGE_NAME="$APP_NAME:latest"
DEPLOYMENT_DIR="deployment"

echo "🔧 Step 1: Maven build..."
mvn clean package -DskipTests

echo "🐳 Step 2: Docker build..."
docker build -t $IMAGE_NAME .

echo "🚀 Step 3: Apply Kubernetes YAMLs..."
kubectl apply -f $DEPLOYMENT_DIR/

# Check if deployment exists and roll out restart
if kubectl get deployment "$APP_NAME-deployment" >/dev/null 2>&1; then
  echo "🔁 Step 4: Restarting deployment..."
  kubectl rollout restart deployment "$APP_NAME-deployment"
else
  echo "ℹ️ Deployment '$APP_NAME-deployment' not found. Skipping restart."
fi

echo "✅ Done! Service should be available shortly."
