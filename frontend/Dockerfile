# --- Build stage ---
FROM node:24-alpine AS build

WORKDIR /workspace

COPY package*.json ./
RUN npm ci

COPY . .

RUN npm run build

# --- Runtime stage ---
FROM nginxinc/nginx-unprivileged:1.29-alpine

COPY --from=build /workspace/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf


EXPOSE 8080
