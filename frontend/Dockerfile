# ===========================================================================
#  Frontend Dockerfile — TelcoRec SPA
#  Serves static files via nginx and proxies API calls to the backend.
# ===========================================================================

FROM nginx:alpine

# Remove default nginx site
RUN rm /etc/nginx/conf.d/default.conf

# Copy nginx configuration (reverse proxy + static serving)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy frontend static files
COPY index.html /usr/share/nginx/html/
COPY css/       /usr/share/nginx/html/css/
COPY js/        /usr/share/nginx/html/js/

# Patch API base URL to use relative path (proxied through nginx)
# so all API calls go through the nginx reverse proxy instead of
# directly to the backend on port 8080.
RUN sed -i "s|http://localhost:8080/reconciliator/api|/reconciliator/api|g" \
    /usr/share/nginx/html/js/app.js

EXPOSE 3000

HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=3 \
    CMD wget -q --spider http://localhost:3000/ || exit 1
