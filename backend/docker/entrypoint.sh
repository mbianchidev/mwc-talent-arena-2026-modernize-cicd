#!/bin/bash
# Entrypoint that fixes H2 data directory ownership before starting TomEE.
# Docker named volumes may retain root ownership from prior builds;
# this ensures the tomee user can always write to the DB path.
set -e

DB_DIR="/home/tomee/.telcorec/db"
mkdir -p "$DB_DIR"
chown -R tomee:tomee "$DB_DIR"

exec gosu tomee catalina.sh run
