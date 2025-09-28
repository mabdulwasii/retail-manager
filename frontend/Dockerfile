# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Build args for environment variables with Kubernetes defaults
ARG VITE_API_BASE_URL=https://api.shop-manager.local/api
ARG VITE_KEYCLOAK_URL=https://auth.shop-manager.local
ARG VITE_KEYCLOAK_REALM=shop-manager
ARG VITE_KEYCLOAK_CLIENT_ID=shop-manager-frontend

# Set environment variables
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL}
ENV VITE_KEYCLOAK_REALM=${VITE_KEYCLOAK_REALM}
ENV VITE_KEYCLOAK_CLIENT_ID=${VITE_KEYCLOAK_CLIENT_ID}

# Copy package files
COPY package.json ./

# Install dependencies using npm
RUN npm install

# Copy source code
COPY . .

# Copy environment file for Kubernetes
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