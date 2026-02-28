#!/usr/bin/env bash
# =============================================================================
#  TelcoRec stop-all.sh
#  Stops all running TelcoRec processes (TomEE + frontend HTTP server).
# =============================================================================

set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; RESET='\033[0m'
ok()   { echo -e "${GREEN}[OK]${RESET}  $*"; }
err()  { echo -e "${RED}[ERR]${RESET} $*" >&2; }
warn() { echo -e "${YELLOW}[WARN]${RESET} $*"; }
info() { echo -e "${CYAN}[INFO]${RESET} $*"; }

TELCOREC_HOME="${HOME}/.telcorec"
PID_DIR="${TELCOREC_HOME}/run"
TOMEE_PID_FILE="${PID_DIR}/tomee.pid"
FRONTEND_PID_FILE="${PID_DIR}/frontend.pid"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

stop_process() {
  local label="$1"
  local pid_file="$2"

  if [[ ! -f "$pid_file" ]]; then
    warn "${label}: no PID file found at ${pid_file}"
    return
  fi

  local pid
  pid="$(cat "${pid_file}")"

  if [[ -z "$pid" ]]; then
    warn "${label}: PID file is empty"
    rm -f "${pid_file}"
    return
  fi

  info "Stopping ${label} (PID ${pid})..."
  if kill -0 "${pid}" 2>/dev/null; then
    kill -TERM "${pid}" 2>/dev/null || true
    local waited=0
    while kill -0 "${pid}" 2>/dev/null && [[ $waited -lt 20 ]]; do
      sleep 1
      waited=$((waited + 1))
    done
    if kill -0 "${pid}" 2>/dev/null; then
      warn "${label} did not stop gracefully – using SIGKILL"
      kill -KILL "${pid}" 2>/dev/null || true
    fi
    ok "${label} stopped"
  else
    warn "${label}: PID ${pid} is not running"
  fi

  rm -f "${pid_file}"
}

mvn_tomee_stop() {
  info "Attempting mvn tomee:stop as fallback..."
  if [[ -d "${PROJECT_ROOT}/backend" ]]; then
    (cd "${PROJECT_ROOT}/backend" && mvn tomee:stop >> /dev/null 2>&1) || true
    ok "mvn tomee:stop completed"
  else
    warn "backend/ directory not found – skipping mvn tomee:stop"
  fi
}

main() {
  echo ""
  echo "  Stopping TelcoRec services..."
  echo ""

  stop_process "TomEE"              "${TOMEE_PID_FILE}"
  stop_process "Frontend HTTP server" "${FRONTEND_PID_FILE}"

  # mvn tomee:stop as fallback in case the PID file is stale
  mvn_tomee_stop

  # Clean up staging directories
  info "Cleaning up temporary staging directories..."
  rm -rf "${TELCOREC_HOME}/frontend" || true
  ok "Cleanup complete"

  echo ""
  ok "All TelcoRec services have been stopped."
  echo ""
}

main "$@"
