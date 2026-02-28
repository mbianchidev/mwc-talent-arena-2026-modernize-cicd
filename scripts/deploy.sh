#!/usr/bin/env bash
# =============================================================================
#  TelcoRec Deployer v1.0  –  TelcoCorp Italia S.r.l.
#  Deploys the invoice-reconciliator WAR to a local Apache TomEE Plus instance.
# =============================================================================

set -euo pipefail

# ── Colour helpers ──────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'
ok()   { echo -e "${GREEN}[OK]${RESET}  $*"; }
err()  { echo -e "${RED}[ERR]${RESET} $*" >&2; }
warn() { echo -e "${YELLOW}[WARN]${RESET} $*"; }
info() { echo -e "${CYAN}[INFO]${RESET} $*"; }

# ── Default parameters ───────────────────────────────────────────────────────
SKIP_BUILD=false
SKIP_FRONTEND=false
APP_PORT=8080
FRONTEND_PORT=3000
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
TELCOREC_HOME="${HOME}/.telcorec"
LOG_DIR="${TELCOREC_HOME}/logs"
PID_DIR="${TELCOREC_HOME}/run"
LOG_FILE="${LOG_DIR}/deploy-$(date +%Y%m%d).log"
TOMEE_PID_FILE="${PID_DIR}/tomee.pid"
FRONTEND_PID_FILE="${PID_DIR}/frontend.pid"

# ── CLI argument parsing ─────────────────────────────────────────────────────
for arg in "$@"; do
  case "$arg" in
    --skip-build)    SKIP_BUILD=true ;;
    --skip-frontend) SKIP_FRONTEND=true ;;
    --port=*)        APP_PORT="${arg#*=}" ;;
    --help|-h)
      echo ""
      echo "  Usage: $0 [OPTIONS]"
      echo ""
      echo "  Options:"
      echo "    --skip-build      Skip mvn clean package step"
      echo "    --skip-frontend   Do not start the frontend dev server"
      echo "    --port=PORT       TomEE HTTP port (default: 8080)"
      echo "    --help            Show this help message"
      echo ""
      exit 0
      ;;
    *) warn "Unknown argument: $arg" ;;
  esac
done

# ── ASCII Banner ─────────────────────────────────────────────────────────────
print_banner() {
  echo ""
  echo -e "${CYAN}${BOLD}"
  echo "  ████████╗███████╗██╗      ██████╗ ██████╗ ██████╗ ███████╗ ██████╗ "
  echo "     ██╔══╝██╔════╝██║     ██╔════╝██╔═══██╗██╔══██╗██╔════╝██╔════╝ "
  echo "     ██║   █████╗  ██║     ██║     ██║   ██║██████╔╝█████╗  ██║      "
  echo "     ██║   ██╔══╝  ██║     ██║     ██║   ██║██╔══██╗██╔══╝  ██║      "
  echo "     ██║   ███████╗███████╗╚██████╗╚██████╔╝██║  ██║███████╗╚██████╗ "
  echo "     ╚═╝   ╚══════╝╚══════╝ ╚═════╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝ ╚═════╝"
  echo ""
  echo "                     TelcoRec Deployer  v1.0"
  echo "                  TelcoCorp Italia S.r.l. - 2025"
  echo -e "${RESET}"
}

# ── Setup logging ────────────────────────────────────────────────────────────
setup_workspace() {
  info "Setting up workspace directories..."
  mkdir -p "${LOG_DIR}" "${PID_DIR}"
  touch "${LOG_FILE}"
  info "Logs: ${LOG_FILE}"
  info "PIDs: ${PID_DIR}"
  ok "Workspace ready"
}

# ── Trap cleanup on exit ─────────────────────────────────────────────────────
cleanup() {
  warn "Signal received – cleaning up..."
  log "Deployment interrupted by signal."
}
trap cleanup SIGINT SIGTERM

# ── Logging to file ──────────────────────────────────────────────────────────
log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" >> "${LOG_FILE}"; }

# ── Prerequisite checks ──────────────────────────────────────────────────────
check_prerequisites() {
  info "Checking prerequisites..."
  log "Checking prerequisites"

  # Java >= 8
  if ! command -v java &>/dev/null; then
    err "java not found in PATH. Please install JDK 8+."
    exit 1
  fi
  JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
  JAVA_VER="${JAVA_VER#1.}"   # Normalise "1.8" -> "8"
  if [[ "$JAVA_VER" -lt 8 ]]; then
    err "Java 8+ is required. Found: Java ${JAVA_VER}"
    exit 1
  fi
  ok "Java ${JAVA_VER} found"

  # Maven >= 3.6
  if ! command -v mvn &>/dev/null; then
    err "mvn not found in PATH. Please install Maven 3.6+."
    exit 1
  fi
  MVN_VER=$(mvn --version 2>&1 | head -1 | awk '{print $3}')
  ok "Maven ${MVN_VER} found"

  # JAVA_HOME
  if [[ -z "${JAVA_HOME:-}" ]]; then
    warn "JAVA_HOME is not set. TomEE may not start correctly."
    log "WARNING: JAVA_HOME not set"
  else
    ok "JAVA_HOME=${JAVA_HOME}"
  fi
}

# ── OS detection ─────────────────────────────────────────────────────────────
validate_environment() {
  info "Detecting operating system..."
  OS_TYPE="$(uname -s)"
  case "$OS_TYPE" in
    Linux*)
      info "Linux detected – using lsof for port checks"
      PORT_CHECK_CMD="lsof"
      ;;
    Darwin*)
      info "macOS detected – using lsof for port checks"
      PORT_CHECK_CMD="lsof"
      ;;
    *)
      warn "Unknown OS: ${OS_TYPE} – assuming Linux-like environment"
      PORT_CHECK_CMD="lsof"
      ;;
  esac
  ok "Environment validated (${OS_TYPE})"
}

# ── Port management ───────────────────────────────────────────────────────────
check_port() {
  local port="$1"
  info "Checking if port ${port} is in use..."
  local pid=""

  if command -v lsof &>/dev/null; then
    pid=$(lsof -ti tcp:"${port}" 2>/dev/null | head -1 || true)
  elif command -v netstat &>/dev/null; then
    pid=$(netstat -tlnp 2>/dev/null | awk "/:${port} /{print \$7}" | cut -d/ -f1 | head -1 || true)
  fi

  if [[ -n "$pid" ]]; then
    warn "Port ${port} is occupied by PID ${pid}. Attempting to stop..."
    log "Killing PID ${pid} on port ${port}"
    kill -TERM "$pid" 2>/dev/null || true
    sleep 3
    if kill -0 "$pid" 2>/dev/null; then
      warn "Process did not stop gracefully – sending SIGKILL"
      kill -KILL "$pid" 2>/dev/null || true
    fi
    ok "Port ${port} freed"
  else
    ok "Port ${port} is available"
  fi
}

# ── H2 database workspace ────────────────────────────────────────────────────
start_database() {
  info "Preparing embedded H2 database workspace..."
  local db_dir="${TELCOREC_HOME}/db"
  mkdir -p "${db_dir}"

  cat > "${TELCOREC_HOME}/h2-connection.properties" << 'PROPS'
# H2 connection properties (for reference – DB is embedded in TomEE)
url=jdbc:h2:~/.telcorec/db/reconciliator;AUTO_SERVER=TRUE
user=telcorec
password=telcorec123
PROPS

  ok "H2 workspace ready at: ${db_dir}"
  info "  JDBC URL:  jdbc:h2:${db_dir}/reconciliator"
  info "  User:      telcorec"
  info "  Password:  telcorec123"
  log "H2 workspace prepared at ${db_dir}"
}

# ── Maven build ───────────────────────────────────────────────────────────────
build_application() {
  if [[ "$SKIP_BUILD" == "true" ]]; then
    warn "Build skipped (--skip-build flag)"
    log "Build skipped"
    return
  fi

  info "Building application with Maven..."
  log "Starting Maven build"
  cd "${PROJECT_ROOT}/backend"

  if mvn clean package -DskipTests >> "${LOG_FILE}" 2>&1; then
    ok "Maven build successful"
    log "Maven build successful"
  else
    err "Maven build FAILED. Check ${LOG_FILE} for details."
    log "Maven build FAILED"
    exit 1
  fi

  # Copy WAR to deploy staging directory
  local war_src
  war_src="$(find "${PROJECT_ROOT}/backend/target" -name "*.war" -type f | head -1)"
  if [[ -z "$war_src" ]]; then
    warn "No WAR file found in target/ – deployment may fail"
  else
    local deploy_dir="${TELCOREC_HOME}/deploy"
    mkdir -p "${deploy_dir}"
    cp "${war_src}" "${deploy_dir}/"
    ok "WAR staged: ${deploy_dir}/$(basename "${war_src}")"
  fi

  cd "${PROJECT_ROOT}"
}

# ── Deploy and start TomEE ────────────────────────────────────────────────────
deploy_application() {
  info "Starting application on port ${APP_PORT}..."
  log "Launching TomEE"

  check_port "${APP_PORT}"
  cd "${PROJECT_ROOT}/backend"

  nohup mvn tomee:run \
    -Dtomee-plugin.port="${APP_PORT}" \
    >> "${LOG_FILE}" 2>&1 &

  local tomee_pid=$!
  echo "${tomee_pid}" > "${TOMEE_PID_FILE}"
  info "TomEE started (PID ${tomee_pid})"
  log "TomEE PID ${tomee_pid}"

  # Health check loop – wait up to 120 seconds
  local elapsed=0
  local health_url="http://localhost:${APP_PORT}/reconciliator/api/customers"
  info "Waiting for application to become ready at ${health_url} ..."
  while [[ $elapsed -lt 120 ]]; do
    if curl -sf --max-time 3 "${health_url}" > /dev/null 2>&1; then
      ok "Application is UP after ${elapsed}s"
      log "Application ready after ${elapsed}s"
      break
    fi
    sleep 5
    elapsed=$((elapsed + 5))
    info "  ... still waiting (${elapsed}s)"
  done

  if [[ $elapsed -ge 120 ]]; then
    err "Timed out waiting for application to start after 120s."
    err "Check ${LOG_FILE} for TomEE output."
    log "Startup timeout"
    exit 1
  fi

  cd "${PROJECT_ROOT}"
}

# ── Frontend dev server ───────────────────────────────────────────────────────
setup_frontend() {
  if [[ "$SKIP_FRONTEND" == "true" ]]; then
    warn "Frontend server skipped (--skip-frontend flag)"
    return
  fi

  info "Setting up frontend..."

  # Patch API base URL if needed (e.g., non-default port)
  local appjs="${PROJECT_ROOT}/frontend/js/app.js"
  if [[ $APP_PORT -ne 8080 ]]; then
    info "Patching API base URL to port ${APP_PORT} in app.js..."
    sed -i.bak "s|localhost:8080|localhost:${APP_PORT}|g" "${appjs}"
    ok "app.js patched"
  fi

  # Staging copy
  local frontend_dir="${TELCOREC_HOME}/frontend"
  mkdir -p "${frontend_dir}"
  cp -r "${PROJECT_ROOT}/frontend/." "${frontend_dir}/"
  ok "Frontend files staged at: ${frontend_dir}"

  # Start a simple HTTP server
  check_port "${FRONTEND_PORT}"
  cd "${frontend_dir}"

  if command -v python3 &>/dev/null; then
    nohup python3 -m http.server "${FRONTEND_PORT}" \
      >> "${LOG_FILE}" 2>&1 &
    echo $! > "${FRONTEND_PID_FILE}"
    ok "Frontend server started at http://localhost:${FRONTEND_PORT}"
    ok "  (python3 -m http.server ${FRONTEND_PORT})"
    log "Frontend server PID $!"
  elif command -v python &>/dev/null; then
    nohup python -m SimpleHTTPServer "${FRONTEND_PORT}" \
      >> "${LOG_FILE}" 2>&1 &
    echo $! > "${FRONTEND_PID_FILE}"
    ok "Frontend server started at http://localhost:${FRONTEND_PORT}"
    log "Frontend server PID $!"
  else
    warn "Python not found – open the frontend manually:"
    info "  open ${frontend_dir}/index.html"
    log "No Python found for frontend server"
  fi

  cd "${PROJECT_ROOT}"
}

# ── Summary ───────────────────────────────────────────────────────────────────
print_summary() {
  echo ""
  echo -e "${BOLD}${GREEN}═══════════════════════════════════════════════════${RESET}"
  echo -e "${BOLD}   TelcoRec deployment complete!${RESET}"
  echo -e "${GREEN}═══════════════════════════════════════════════════${RESET}"
  echo ""
  echo -e "  ${BOLD}Backend API:${RESET}  http://localhost:${APP_PORT}/reconciliator/api/"
  if [[ "$SKIP_FRONTEND" != "true" ]]; then
    echo -e "  ${BOLD}Frontend:${RESET}     http://localhost:${FRONTEND_PORT}"
  fi
  echo -e "  ${BOLD}Log file:${RESET}     ${LOG_FILE}"
  echo -e "  ${BOLD}Stop all:${RESET}     ${SCRIPT_DIR}/stop-all.sh"
  echo ""
}

# ── Main ──────────────────────────────────────────────────────────────────────
main() {
  print_banner
  setup_workspace
  check_prerequisites
  validate_environment
  start_database
  build_application
  deploy_application
  setup_frontend
  print_summary
}

main "$@"
