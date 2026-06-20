FROM node:24-alpine AS build

WORKDIR /workspace

COPY package*.json ./
RUN npm ci

COPY index.html .
COPY tsconfig*.json ./
COPY vite.config.ts .
COPY public ./public
COPY src ./src

RUN npm run build

FROM nginx:1.29-alpine

RUN set -eux; \
    { \
      echo 'server {'; \
      echo '  listen 80;'; \
      echo '  server_name _;'; \
      echo '  root /usr/share/nginx/html;'; \
      echo '  index index.html;'; \
      echo ''; \
      echo '  location /api {'; \
      echo '    proxy_pass http://backend:8080;'; \
      echo '    proxy_http_version 1.1;'; \
      echo '    proxy_set_header Host $host;'; \
      echo '    proxy_set_header X-Real-IP $remote_addr;'; \
      echo '    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;'; \
      echo '    proxy_set_header X-Forwarded-Proto $scheme;'; \
      echo '  }'; \
      echo ''; \
      echo '  location / {'; \
      echo '    try_files $uri $uri/ /index.html;'; \
      echo '  }'; \
      echo '}'; \
    } > /etc/nginx/conf.d/default.conf

COPY --from=build /workspace/dist /usr/share/nginx/html

EXPOSE 80
