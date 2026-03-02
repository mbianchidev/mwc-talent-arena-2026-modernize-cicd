# ===========================================================================
#  Backend Dockerfile — TelcoRec Invoice Reconciliator
#  Multi-stage build: Maven build → TomEE Plus 8.0.16 runtime
# ===========================================================================

# ---- Stage 1: Build the WAR with Maven ----
FROM maven:3.9-eclipse-temurin-11 AS builder

WORKDIR /build

# Copy POM files first for better Docker layer caching
COPY pom.xml ./pom.xml
COPY backend/pom.xml ./backend/pom.xml

# Pre-download dependencies (cached unless POMs change)
RUN cd backend && mvn dependency:go-offline -B 2>/dev/null || true

# Copy source and build
COPY backend/src ./backend/src
RUN cd backend && mvn clean package -DskipTests -B

# ---- Stage 2: TomEE Plus runtime ----
FROM eclipse-temurin:11-jre

ENV TOMEE_VERSION=8.0.16 \
    CATALINA_HOME=/opt/tomee \
    PATH="/opt/tomee/bin:${PATH}"

# Install curl (health checks) and download TomEE Plus
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p ${CATALINA_HOME} && \
    curl -fsSL "https://archive.apache.org/dist/tomee/tomee-${TOMEE_VERSION}/apache-tomee-${TOMEE_VERSION}-plus.tar.gz" \
      | tar xzC ${CATALINA_HOME} --strip-components=1 && \
    rm -rf ${CATALINA_HOME}/webapps/*

# Create non-root user and data directory for H2 persistence
RUN groupadd -r tomee && \
    useradd -r -g tomee -d /home/tomee -m -s /bin/bash tomee && \
    chown -R tomee:tomee ${CATALINA_HOME} && \
    mkdir -p /home/tomee/.telcorec/db && \
    chown -R tomee:tomee /home/tomee

# Install gosu for dropping privileges in entrypoint
RUN apt-get update && \
    apt-get install -y --no-install-recommends gosu && \
    rm -rf /var/lib/apt/lists/*

# Deploy the WAR
COPY --from=builder /build/backend/target/reconciliator.war ${CATALINA_HOME}/webapps/reconciliator.war
RUN chown tomee:tomee ${CATALINA_HOME}/webapps/reconciliator.war

# Entrypoint: fix volume permissions at runtime, then start TomEE as tomee user
COPY backend/docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

WORKDIR ${CATALINA_HOME}

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD curl -sf http://localhost:8080/reconciliator/api/customers > /dev/null || exit 1

ENTRYPOINT ["/entrypoint.sh"]
