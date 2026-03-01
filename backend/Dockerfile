# ---------------------------------------------------------------------------
#  TelcoRec Backend — Multi-stage Docker build
#  Build context: repository root (set via docker-compose.yml)
#  Stage 1: Build WAR with Maven
#  Stage 2: Deploy WAR into Apache TomEE Plus 8
# ---------------------------------------------------------------------------

# ── Build stage ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Import Maven Central TLS certificate into Java trust store
RUN openssl s_client -connect repo.maven.apache.org:443 -showcerts </dev/null 2>/dev/null \
        | openssl x509 -outform PEM > /tmp/maven-central.pem \
    && keytool -importcert -trustcacerts -cacerts -storepass changeit -noprompt \
         -alias maven-central -file /tmp/maven-central.pem \
    && rm /tmp/maven-central.pem

# Copy POMs first for dependency caching
COPY pom.xml /build/pom.xml
COPY backend/pom.xml /build/backend/pom.xml

# Resolve dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -f /build/backend/pom.xml -B -q || true

# Copy backend source and build
COPY backend/src /build/backend/src
RUN mvn clean package -DskipTests -B -q -f /build/backend/pom.xml

# ── Runtime stage ───────────────────────────────────────────────────────────
FROM tomee:8-jre11-plus AS runtime

# Remove default sample apps
RUN rm -rf /usr/local/tomee/webapps/ROOT /usr/local/tomee/webapps/docs \
           /usr/local/tomee/webapps/examples /usr/local/tomee/webapps/host-manager \
           /usr/local/tomee/webapps/manager

# Copy WAR from build stage
COPY --from=build /build/backend/target/reconciliator.war /usr/local/tomee/webapps/reconciliator.war

# Copy H2 JDBC driver from Maven cache to TomEE lib for server-level datasource
COPY --from=build /root/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar /usr/local/tomee/lib/

# Configure H2 datasource in TomEE — uses /data/db for Docker volume mount
COPY backend/docker/tomee.xml /usr/local/tomee/conf/tomee.xml

# Create data directory for H2
RUN mkdir -p /data/db

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=4 \
    CMD curl -sf http://localhost:8080/reconciliator/api/customers > /dev/null || exit 1

CMD ["catalina.sh", "run"]
