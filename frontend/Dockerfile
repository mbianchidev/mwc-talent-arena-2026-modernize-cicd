# ---------------------------------------------------------------------------
#  TelcoRec Frontend — Nginx serving static files with API reverse proxy
# ---------------------------------------------------------------------------
FROM nginx:1.27-alpine

# Remove default nginx content
RUN rm -rf /usr/share/nginx/html/*

# Copy static frontend files
COPY index.html /usr/share/nginx/html/
COPY css/ /usr/share/nginx/html/css/
COPY js/ /usr/share/nginx/html/js/

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 3000

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO /dev/null http://localhost:3000/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
