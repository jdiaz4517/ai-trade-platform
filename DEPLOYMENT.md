# 🚀 AI Trade Platform Deployment Guide

This guide covers deploying the AI Trade Platform with dual AI engine support (Ollama/Groq) to your Hetzner K3s server.

## 🎯 Quick Overview

Your application now supports:
- **Dual AI Engines**: Switch between Ollama (offline) and Groq (fast cloud AI) with one property
- **Docker Ready**: Complete containerization with multi-stage builds
- **K3s Optimized**: Production-ready Kubernetes manifests
- **Environment Flexible**: Easy configuration for different environments

## 🔧 Configuration

### AI Engine Selection

Switch between AI engines by setting the `ACTIVE_AI_ENGINE` environment variable:

```bash
# Use Groq (fast, cloud-based)
ACTIVE_AI_ENGINE=groq

# Use Ollama (offline, local)
ACTIVE_AI_ENGINE=ollama
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `ACTIVE_AI_ENGINE` | AI engine to use: "ollama" or "groq" | `ollama` |
| `GROQ_API_KEY` | Your Groq API key | - |
| `GROQ_MODEL` | Groq model to use | `llama3-8b-8192` |
| `OLLAMA_BASE_URL` | Ollama server URL | `http://localhost:11434` |
| `OLLAMA_MODEL` | Ollama model to use | `mistral` |

## 📦 Local Development

### 1. Using Groq (Recommended)

```bash
# Copy environment template
cp .env.example .env

# Edit .env and add your Groq API key
ACTIVE_AI_ENGINE=groq
GROQ_API_KEY=your_groq_api_key_here

# Run with Docker Compose
docker-compose up ai-trade-platform
```

### 2. Using Local Ollama

```bash
# Start Ollama and your app
docker-compose --profile ollama up

# Or just the offline version
docker-compose up ai-trade-platform-offline
```

## 🏗️ Hetzner K3s Deployment

### Prerequisites

1. **K3s Running**: Ensure K3s is installed and running on your Hetzner server
2. **kubectl**: Configured to connect to your K3s cluster
3. **Docker**: For building images locally
4. **Groq API Key**: Sign up at https://console.groq.com

### Deploy to K3s

```bash
# 1. Clone and navigate to your project
cd ai-trade-platform

# 2. Run the deployment script
./k8s/deploy.sh

# The script will:
# - Build the Docker image
# - Create namespace, secrets, configmaps
# - Deploy the application
# - Set up services and ingress
```

### Manual Deployment

If you prefer manual deployment:

```bash
# Build image
docker build -t ai-trade-platform:latest .

# Apply manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml

# Create secret with your Groq API key
kubectl create secret generic ai-trade-platform-secrets \
  --namespace=ai-trade-platform \
  --from-literal=groq-api-key="YOUR_GROQ_API_KEY"

kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
```

## 🌐 Accessing Your Application

After deployment, your app will be available at:

1. **NodePort**: `http://YOUR_SERVER_IP:30081`
2. **Port Forward**: `kubectl port-forward service/ai-trade-platform-service 8081:80 -n ai-trade-platform`
3. **Ingress**: Configure DNS to point to your server (see ingress.yaml)

## 🔄 Updates

To deploy updates:

```bash
# Quick update (rebuilds and redeploys)
./k8s/update.sh

# Or manual rollout
kubectl rollout restart deployment/ai-trade-platform -n ai-trade-platform
```

## 📊 Monitoring

### Health Checks

- Health endpoint: `/actuator/health`
- Metrics: `/actuator/metrics`
- Info: `/actuator/info`

### Useful Commands

```bash
# View logs
kubectl logs -f deployment/ai-trade-platform -n ai-trade-platform

# Scale deployment
kubectl scale deployment ai-trade-platform --replicas=3 -n ai-trade-platform

# Check pod status
kubectl get pods -n ai-trade-platform

# Delete deployment
kubectl delete namespace ai-trade-platform
```

## 🛠️ Troubleshooting

### Common Issues

1. **Groq API Key Missing**
   ```bash
   kubectl edit secret ai-trade-platform-secrets -n ai-trade-platform
   ```

2. **Image Pull Issues**
   ```bash
   # Build locally and check
   docker build -t ai-trade-platform:latest .
   kubectl describe pod -n ai-trade-platform
   ```

3. **AI Engine Not Working**
   ```bash
   # Check logs for configuration
   kubectl logs deployment/ai-trade-platform -n ai-trade-platform | grep -i "ai\|groq\|ollama"
   ```

## 🔒 Security Considerations

- Secrets are stored in Kubernetes secrets
- Non-root container user
- Health check endpoints exposed
- Resource limits configured
- Network policies can be added if needed

## 📝 Testing Your Deployment

Once deployed, test with:

```bash
# Health check
curl http://YOUR_SERVER_IP:30081/actuator/health

# Chat endpoint
curl -X POST http://YOUR_SERVER_IP:30081/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "message": "I need a plumber for a kitchen sink leak",
    "sessionId": "test-session"
  }'
```

Your son should now be able to access the application and run tests against the deployed version! 🎉