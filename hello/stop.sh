#!/bin/bash

set -e

APP_NAME="demo-hello"
DEPLOYMENT_DIR="deployment"
IMAGE_NAME="$APP_NAME:latest"

echo "🗑️  Step 1: Delete Kubernetes resources..."
kubectl delete -f $DEPLOYMENT_DIR/ --ignore-not-found

echo "🐳 Step 2: Remove Docker image..."
docker image rm $IMAGE_NAME --force || echo "⚠️ Image $IMAGE_NAME not found or already deleted."

echo "✅ All cleaned up."
#kubectl get all | grep demo-hello
#docker images | grep demo-hello