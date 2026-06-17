#!/usr/bin/env bash

usage() {
  cat <<'EOF'
Usage: enc.sh <command> [options]

Commands:
  encrypt-text   Encrypt a plaintext string, prints vault:v1:... to stdout
  encrypt-file   Encrypt a single file
  encrypt-dir    Encrypt matching files in a directory
  encrypt-zip    Encrypt matching entries inside a ZIP archive
  encrypt-list   Encrypt files listed in a text file (one path per line)

encrypt-text:
  enc.sh encrypt-text --plaintext <value> [--transit-key <key>]

encrypt-file:
  enc.sh encrypt-file --file <path> --out <path>
                      [--algorithm AES_256_GCM|CHACHA20_POLY1305] [--transit-key <key>]

encrypt-dir:
  enc.sh encrypt-dir --dir <path> --out <path> [--recursive] [--same-dek]
                     [--algorithm AES_256_GCM|CHACHA20_POLY1305] [--transit-key <key>]

  --recursive   Descend into subdirectories
  --same-dek    Share one DEK across all files (one key-wrap call); default is per-file DEK

encrypt-zip:
  enc.sh encrypt-zip --zip <path> --out <path> [--same-dek]
                     [--algorithm AES_256_GCM|CHACHA20_POLY1305] [--transit-key <key>]

  --same-dek    Share one DEK across all ZIP entries; default is per-entry DEK

encrypt-list:
  enc.sh encrypt-list --list <path> --out <path> [--same-dek]
                      [--algorithm AES_256_GCM|CHACHA20_POLY1305] [--transit-key <key>]

  List file format — one file path per line, blank lines and # comments ignored:
    /data/file1.csv
    /data/file2.txt
    # this line is ignored

  Failed encryptions are logged to failed_encryption.txt in cli.log-dir (see config).

Config:
  Reads cli.properties from current directory by default.
  Override: ENC_CONFIG=/path/to/cli.properties enc.sh ...

JAR:
  Looks for encryption-service-*-cli.jar in current directory.
  Override: ENC_JAR=/path/to/cli.jar enc.sh ...

JVM flags:
  JAVA_OPTS="-Xmx512m" enc.sh ...
EOF
}

# --- Show usage for --help / -h with no Spring Boot noise
for arg in "$@"; do
  if [ "$arg" = "--help" ] || [ "$arg" = "-h" ]; then
    usage
    exit 0
  fi
done

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
