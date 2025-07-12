# Pokemon Go Friends - Deployment Guide

This directory contains all deployment-related configurations for the Pokemon Go Friends backend application.

## Directory Structure

```
deployment/
├── docker/
│   ├── Dockerfile                 # Multi-stage Docker build configuration
│   └── compose/
│       ├── compose.yaml          # Docker Compose for local development
│       └── docker-compose.override.yml  # Local overrides
└── kubernetes/
    ├── base/                     # Base Kubernetes manifests
    │   ├── backend.yaml          # Backend deployment and service
    │   ├── postgres.yaml         # PostgreSQL database
    │   ├── redis.yaml            # Redis cache
    │   ├── secrets.yaml          # Secrets configuration
    │   ├── ingress.yaml          # Ingress configuration
    │   └── kustomization.yaml    # Base kustomization
    └── overlays/                 # Environment-specific overlays
        ├── development/
        │   ├── kustomization.yaml
        │   └── backend-patch.yaml
        └── production/
            ├── kustomization.yaml
            └── backend-patch.yaml
```

## Quick Start

### Local Development with Docker Compose

```bash
# Start all services locally
cd deployment/docker/compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Kubernetes Deployment

#### Prerequisites
- `kubectl` configured for your cluster
- `kustomize` (optional, kubectl has built-in support)

#### Deploy to Development
```bash
kubectl apply -k deployment/kubernetes/overlays/development
```

#### Deploy to Production
```bash
kubectl apply -k deployment/kubernetes/overlays/production
```

#### Check Deployment Status
```bash
# Check pods
kubectl get pods -l app=poke-go-friends

# Check services
kubectl get services -l app=poke-go-friends

# Check deployments
kubectl get deployments -l app=poke-go-friends
```

## Environment Configuration

### Development Environment
- **Namespace**: Uses `dev-` prefix for all resources
- **Replicas**: 1 instance of each service
- **Resources**: Lower CPU/memory limits
- **Database**: `poke_go_friends`
- **Profile**: `dev`

### Production Environment
- **Namespace**: Uses `prod-` prefix for all resources
- **Replicas**: 2 backend instances, 1 database instance
- **Resources**: Higher CPU/memory limits
- **Database**: `poke_go_friends_prod`
- **Profile**: `prod`

## CI/CD Pipeline

The project uses GitHub Actions for automated testing and deployment:

### Workflow Triggers
- **Push** to `main`, `develop`, or `dockerize_k8s` branches
- **Pull Requests** to `main` or `develop`

### Pipeline Stages

1. **Test**: Runs unit and integration tests with PostgreSQL and Redis services
2. **Build**: Builds the application JAR and Docker image
3. **Security Scan**: Scans for vulnerabilities using Trivy
4. **Deploy Dev**: Auto-deploys to development environment on `develop` branch
5. **Deploy Prod**: Auto-deploys to production environment on `main` branch

### Environment Protection
- **Development**: Automatic deployment
- **Production**: Requires manual approval (configure in GitHub repository settings)

## Docker Configuration

### Dockerfile
- **Multi-stage build** for optimized image size
- **Security best practices** (non-root user, minimal base image)
- **Build caching** for faster builds

### Key Features
- Base image: `eclipse-temurin:21-jre`
- Non-root user execution
- Health check endpoint
- Optimized layer caching

## Kubernetes Manifests

### Base Configuration
Contains shared configurations for all environments:
- **Secrets**: Database passwords and API keys
- **Services**: ClusterIP services for internal communication
- **Deployments**: Application pods with resource limits
- **Ingress**: HTTP routing configuration

### Kustomize Overlays
Environment-specific customizations:
- **Name prefixes** for resource isolation
- **Replica counts** based on environment needs
- **Resource limits** tuned for each environment
- **Environment variables** (Spring profiles, database names)

## Monitoring and Observability

### Health Checks
- **Readiness Probe**: `/actuator/health` (port 8080)
- **Liveness Probe**: `/actuator/health` (port 8080)
- **Startup time**: 30 seconds initial delay

### Logging
- **Structured logging** with JSON format in production
- **Log levels** configurable per environment
- **Centralized collection** ready (configure log drivers)

### Metrics
- **Spring Boot Actuator** endpoints exposed
- **Prometheus metrics** available at `/actuator/prometheus`
- **Health indicators** for database and Redis

## Security Considerations

### Secrets Management
- **Kubernetes Secrets** for sensitive data
- **Never commit** secrets to version control
- **Environment-specific** secret configuration

### Container Security
- **Non-root user** execution
- **Minimal base image** (JRE only)
- **Regular vulnerability scanning** with Trivy
- **No sensitive data** in Docker images

### Network Security
- **ClusterIP services** for internal communication
- **Ingress** for controlled external access
- **Network policies** ready for implementation

## Troubleshooting

### Common Issues

#### Pod Not Starting
```bash
# Check pod status
kubectl describe pod <pod-name>

# Check logs
kubectl logs <pod-name>

# Check events
kubectl get events --sort-by=.metadata.creationTimestamp
```

#### Database Connection Issues
```bash
# Check if PostgreSQL is running
kubectl get pods -l app=postgres

# Check PostgreSQL logs
kubectl logs deployment/postgres

# Test database connectivity
kubectl exec -it <backend-pod> -- pg_isready -h postgres -p 5432
```

#### Image Pull Issues
```bash
# Check image name and tag
kubectl describe pod <pod-name>

# Verify image exists
docker images | grep poke-go-backend
```

### Useful Commands

```bash
# Port forward to local machine
kubectl port-forward service/poke-go-backend 8080:8080

# Access backend logs
kubectl logs -f deployment/poke-go-backend

# Execute commands in container
kubectl exec -it deployment/poke-go-backend -- /bin/bash

# Delete all resources for an environment
kubectl delete -k deployment/kubernetes/overlays/development
```

## Contributing

When making changes to deployment configurations:

1. **Test locally** with Docker Compose first
2. **Validate** Kubernetes manifests: `kubectl apply --dry-run=client -k deployment/kubernetes/overlays/development`
3. **Test** in development environment before production
4. **Update documentation** if needed
5. **Follow** the established naming conventions

## Support

For deployment-related issues:
1. Check the troubleshooting section above
2. Review GitHub Actions workflow logs
3. Check Kubernetes cluster status and logs
4. Contact the development team
