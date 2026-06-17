#!/usr/bin/env bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# --- Config file ---
CONFIG="${ENC_CONFIG:-$PROJECT_DIR/cli.properties}"
if [ ! -f "$CONFIG" ]; then
  echo "ERROR: config file not found: $CONFIG" >&2
  echo "Set ENC_CONFIG=/path/to/cli.properties or place cli.properties in $PROJECT_DIR" >&2
  exit 1
fi

# --- JAR ---
JAR="${ENC_JAR:-$(ls "$PROJECT_DIR"/target/encryption-service-*-cli.jar 2>/dev/null | head -1)}"
if [ ! -f "$JAR" ]; then
  echo "ERROR: CLI JAR not found. Run: mvn package -DskipTests" >&2
  exit 1
fi

exec java \
  -Dspring.config.additional-location="file:$CONFIG" \
  ${JAVA_OPTS:-} \
  -jar "$JAR" \
  "$@"
