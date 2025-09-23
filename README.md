# MINIKUBE
- choco install minikube
- Start Minikube (Start with Docker as driver (as you may be already using Docker Desktop):
  - minikube start --driver=docker
    This runs Minikube as a container using Docker. Lightweight and fast
  - Minikube itself creates its own context in your kubeconfig called minikube.
This context points kubectl to the Minikube cluster running in Docker.

It does not create the docker-desktop context.
- Switch kubectl Contexts. Kubernetes uses contexts to decide which cluster to talk to.
  - kubectl config current-context
- List all contexts:
  - kubectl config get-contexts
- Switch between Minikube and Docker Desktop:
  - kubectl config use-context minikube
  - kubectl config use-context docker-desktop
    - Where docker-desktop comes from? 
      - Docker Desktop has an optional Kubernetes cluster built-in.
      - When you enable Kubernetes in Docker Desktop, it automatically creates a context called docker-desktop in your kubeconfig.

This is independent of Minikube, even though both can run in Docker.
- Use Local Docker Images in Minikube. Minikube runs a different Docker daemon, so to use your local images:
  - eval $(minikube docker-env)
  - docker build -t demo-hello:latest .
  - Now Kubernetes inside Minikube can see the image.
    - Don't forget to add: imagePullPolicy: Never in your deployment.yaml.
- Enable Ingress in Minikube
  - minikube addons enable ingress
  - Then apply your ingress.yaml.
- Simulate LoadBalancer Access. If your Service is type: LoadBalancer, run:
  - minikube tunnel
  - It creates a virtual external IP you can curl or open in browser.


