#!/bin/bash
export MINIKUBE_CMD="/c/ProgramData/chocolatey/bin/minikube.exe"
set -e

APP_NAME="demo-hello"
IMAGE_NAME="$APP_NAME:latest"
DEPLOYMENT_DIR="deployment"

echo "🧭 Detecting active Kubernetes context..."
CONTEXT=$(kubectl config current-context)

echo "👉 Current context: $CONTEXT"

if [[ "$CONTEXT" == "minikube" ]]; then
  echo "🔁 Configuring Docker to use Minikube’s Docker daemon..."
  eval $($MINIKUBE_CMD docker-env)
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

if [[ "$CONTEXT" == "minikube" ]]; then
  echo "🌐 Minikube detected. You can access your app via:"
  $MINIKUBE_CMD service ${APP_NAME}-service
else
  echo "🌐 Docker Desktop detected. Access via NodePort or Ingress."
fi

echo "✅ Done!"
