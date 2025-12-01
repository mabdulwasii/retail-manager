# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# NOTE: We use runtime configuration (window.RUNTIME_CONFIG) from ConfigMap
# No build-time environment variables needed - all config is loaded at runtime

# Copy package files first for better layer caching
# This allows Docker to cache the dependency layer separately from source code
COPY package.json package-lock.json* ./

# Install dependencies
# Use npm install since project has incomplete Yarn 4 setup
# Layer is cached unless package files change
RUN npm install

# Copy source code (this layer is cached unless source changes)
COPY . .

# Copy environment file for Kubernetes (contains empty values - runtime config takes precedence)
COPY .env.k8s .env

# Build the application without TypeScript checking
RUN npm run build-no-tsc

# Production stage
FROM nginx:alpine AS production

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom nginx configuration
COPY nginx-simple.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:80/ || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]