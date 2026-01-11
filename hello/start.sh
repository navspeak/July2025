#!/bin/bash
export MINIKUBE_CMD="/c/ProgramData/chocolatey/bin/minikube.exe"
set -e

APP_NAME="demo-hello"
IMAGE_NAME="$APP_NAME:latest"
DEPLOYMENT_DIR="deployment"
CONTEXT=""

# 🔍 Parse optional --context argument
while [[ "$#" -gt 0 ]]; do
  case $1 in
    --context) CONTEXT="$2"; shift ;;
    *) echo "❌ Unknown parameter passed: $1"; exit 1 ;;
  esac
  shift
done

# 🧭 Determine context
if [[ -n "$CONTEXT" ]]; then
  echo "🔁 Switching to specified context: $CONTEXT"
  kubectl config use-context "$CONTEXT"
else
  CONTEXT=$(kubectl config current-context 2>/dev/null || echo "")
  if [[ "$CONTEXT" != "minikube" ]]; then
    echo "🚀 No context specified. Starting Minikube..."
    $MINIKUBE_CMD start --driver=docker
    CONTEXT="minikube"
    kubectl config use-context minikube
  fi
fi

echo "👉 Using context: $CONTEXT"

# 🔧 Configure Docker env
if [[ "$CONTEXT" == "minikube" ]]; then
  echo "🔁 Using Minikube Docker daemon"
  echo "$MINIKUBE_CMD" docker-env
  eval $($MINIKUBE_CMD docker-env --shell bash)
else
  echo "🐳 Using host Docker (e.g., Docker Desktop)"
fi

echo "🔧 Step 1: Building Spring Boot JAR..."
mvn clean package -DskipTests

echo "🐳 Step 2: Building Docker image..."
docker build -t $IMAGE_NAME .

echo "🚀 Step 3: Applying Kubernetes manifests..."
kubectl apply -f $DEPLOYMENT_DIR/

echo "🔁 Step 4: Restarting deployment if exists..."
if kubectl get deployment "${APP_NAME}-deployment" > /dev/null 2>&1; then
  kubectl rollout restart deployment "${APP_NAME}-deployment"
else
  echo "ℹ️ Deployment '${APP_NAME}-deployment' not found. Skipping restart."
fi

# ✅ Print service access info
if [[ "$CONTEXT" == "minikube" ]]; then
  echo "🌐 Minikube: Use the following to access your app:"
  $MINIKUBE_CMD service ${APP_NAME}-service
else
  echo "🌐 Docker Desktop: Access via NodePort or Ingress (e.g., localhost:30080)"
fi

echo "✅ Done!"
