#!/bin/bash

# AI Trade Platform K3s Deployment Script
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Deploying AI Trade Platform to K3s${NC}"

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}❌ kubectl is not installed or not in PATH${NC}"
    exit 1
fi

# Check if we can connect to the cluster
if ! kubectl cluster-info &> /dev/null; then
    echo -e "${RED}❌ Cannot connect to Kubernetes cluster${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Connected to Kubernetes cluster${NC}"

# Build and tag Docker image (adjust this based on your registry)
echo -e "${YELLOW}📦 Building Docker image...${NC}"
docker build -t ai-trade-platform:latest .

# If using a remote registry (uncomment and adjust):
# docker tag ai-trade-platform:latest your-registry/ai-trade-platform:latest
# docker push your-registry/ai-trade-platform:latest

# Deploy to K3s
echo -e "${YELLOW}🔧 Applying Kubernetes manifests...${NC}"

# Apply manifests in order
kubectl apply -f k8s/namespace.yaml
echo -e "${GREEN}✅ Namespace created${NC}"

kubectl apply -f k8s/configmap.yaml
echo -e "${GREEN}✅ ConfigMap applied${NC}"

# Check if secret exists and prompt for Groq API key
if ! kubectl get secret ai-trade-platform-secrets -n ai-trade-platform &> /dev/null; then
    echo -e "${YELLOW}🔑 Setting up secrets...${NC}"
    echo -e "${BLUE}Please provide your Groq API key:${NC}"
    read -s GROQ_API_KEY
    
    # Create secret with provided API key
    kubectl create secret generic ai-trade-platform-secrets \
        --namespace=ai-trade-platform \
        --from-literal=groq-api-key="$GROQ_API_KEY" \
        --from-literal=database-url="jdbc:h2:mem:testdb" \
        --from-literal=database-username="sa" \
        --from-literal=database-password=""
    echo -e "${GREEN}✅ Secrets created${NC}"
else
    echo -e "${GREEN}✅ Secrets already exist${NC}"
fi

kubectl apply -f k8s/deployment.yaml
echo -e "${GREEN}✅ Deployment applied${NC}"

kubectl apply -f k8s/service.yaml
echo -e "${GREEN}✅ Services applied${NC}"

kubectl apply -f k8s/ingress.yaml
echo -e "${GREEN}✅ Ingress applied${NC}"

# Wait for deployment to be ready
echo -e "${YELLOW}⏳ Waiting for deployment to be ready...${NC}"
kubectl wait --for=condition=available --timeout=300s deployment/ai-trade-platform -n ai-trade-platform

# Get deployment status
echo -e "${BLUE}📊 Deployment Status:${NC}"
kubectl get pods -n ai-trade-platform
kubectl get services -n ai-trade-platform

# Get access information
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
NODE_PORT=$(kubectl get service ai-trade-platform-nodeport -n ai-trade-platform -o jsonpath='{.spec.ports[0].nodePort}')

echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"
echo -e "${BLUE}📋 Access Information:${NC}"
echo -e "   NodePort: http://${NODE_IP}:${NODE_PORT}"
echo -e "   ClusterIP: kubectl port-forward service/ai-trade-platform-service 8081:80 -n ai-trade-platform"
echo -e "   Ingress: http://ai-trade.your-domain.com (after DNS configuration)"

echo -e "${YELLOW}💡 Useful commands:${NC}"
echo -e "   View logs: kubectl logs -f deployment/ai-trade-platform -n ai-trade-platform"
echo -e "   Scale up: kubectl scale deployment ai-trade-platform --replicas=3 -n ai-trade-platform"
echo -e "   Update: ./k8s/update.sh"
echo -e "   Delete: kubectl delete namespace ai-trade-platform"