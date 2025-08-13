#!/bin/bash

# AI Trade Platform K3s Update Script
set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 Updating AI Trade Platform deployment${NC}"

# Build new Docker image
echo -e "${YELLOW}📦 Building new Docker image...${NC}"
docker build -t ai-trade-platform:latest .

# Update ConfigMap and Deployment
echo -e "${YELLOW}🔧 Updating Kubernetes resources...${NC}"
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml

# Restart deployment to pick up new image
echo -e "${YELLOW}🔄 Restarting deployment...${NC}"
kubectl rollout restart deployment/ai-trade-platform -n ai-trade-platform

# Wait for rollout to complete
echo -e "${YELLOW}⏳ Waiting for rollout to complete...${NC}"
kubectl rollout status deployment/ai-trade-platform -n ai-trade-platform

echo -e "${GREEN}🎉 Update completed successfully!${NC}"

# Show current status
kubectl get pods -n ai-trade-platform