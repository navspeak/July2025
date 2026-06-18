#!/usr/bin/env bash

# --- Config file ---
# 1. ENC_CONFIG env var
# 2. cli.properties in current directory
CONFIG="${ENC_CONFIG:-$PWD/cli.properties}"
if [ ! -f "$CONFIG" ]; then
  echo "ERROR: config file not found: $CONFIG" >&2
  echo "Place cli.properties in the current directory or set ENC_CONFIG=/path/to/cli.properties" >&2
  exit 1
fi

# --- JAR ---
# 1. ENC_JAR env var
# 2. Any encryption-service-*-cli.jar in current directory
JAR="${ENC_JAR:-$(ls "$PWD"/encryption-service-*-cli.jar 2>/dev/null | head -1)}"
if [ ! -f "$JAR" ]; then
  echo "ERROR: CLI JAR not found in $PWD" >&2
  echo "Copy encryption-service-*-cli.jar here or set ENC_JAR=/path/to/cli.jar" >&2
  exit 1
fi

# Convert Git Bash /c/Users/... paths to C:/Users/... for Spring on Windows
to_native() {
  if command -v cygpath &>/dev/null; then
    cygpath -w "$1"
  else
    echo "$1"
  fi
}

CONFIG_NATIVE="$(to_native "$CONFIG")"
JAR_NATIVE="$(to_native "$JAR")"

exec java \
  -Dspring.config.additional-location="file:$CONFIG_NATIVE" \
  ${JAVA_OPTS:-} \
  -jar "$JAR_NATIVE" \
  "$@"
