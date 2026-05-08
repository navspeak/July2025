#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────────
# PAN Encryption Demo
# Pre-requisites: encryption service on localhost:8081, Vault with transit key "my-key"
# Usage: bash scripts/pan_demo.sh
# ──────────────────────────────────────────────────────────────────────────────

set -euo pipefail

HOST="http://localhost:8081"
TRANSIT_KEY="my-key"
WORK_DIR="$(mktemp -d)"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
DIM='\033[2m'
RESET='\033[0m'

cleanup() { rm -rf "$WORK_DIR"; }
trap cleanup EXIT

PAN_FILE="$WORK_DIR/pan_records.csv"
ENC_FILE="$WORK_DIR/pan_records.enc"
DEC_FILE="$WORK_DIR/pan_records_decrypted.csv"

pause() { echo -e "\n${DIM}[ press Enter to continue ]${RESET}"; read -r; }

banner() {
  echo ""
  echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
  echo -e "  ${BOLD}$1${RESET}"
  echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
  echo ""
}

# ─────────────────────────────────────────────────────────────────────────────
banner "THE PROBLEM — What we are protecting"
# ─────────────────────────────────────────────────────────────────────────────

echo -e "  We have a file containing cardholder PAN data."
echo -e "  This is the kind of file that lives in batch jobs, reports,"
echo -e "  or data pipelines — and it is a high-value target for attackers."
echo ""
echo -e "  In its current form, anyone who gets this file can read every card number."
echo -e "  We are going to change that.\n"

cat > "$PAN_FILE" <<'CSV'
card_number,cardholder_name,expiry,cvv,credit_limit,account_status
4111111111111111,Alice Johnson,12/27,737,15000.00,ACTIVE
5500005555555559,Bob Martinez,08/26,192,8500.00,ACTIVE
378282246310005,Carol Smith,03/28,4829,25000.00,ACTIVE
6011111111111117,Dave Williams,11/25,341,5000.00,SUSPENDED
4012888888881881,Eve Brown,07/27,523,12000.00,ACTIVE
5105105105105100,Frank Davis,09/26,789,9000.00,ACTIVE
CSV

echo -e "${RED}  Plaintext — fully readable right now:${RESET}\n"
column -t -s',' "$PAN_FILE"

pause

# ─────────────────────────────────────────────────────────────────────────────
banner "THE SOLUTION — One API call, one protected file"
# ─────────────────────────────────────────────────────────────────────────────

echo -e "  We send this file to our encryption service."
echo -e "  The service does three things internally:"
echo ""
echo -e "    1. Generates a random AES-256 key (the DEK) for this file only"
echo -e "    2. Sends that key to HashiCorp Vault — Vault wraps it and hands back"
echo -e "       an opaque blob. The raw key never touches our disk."
echo -e "    3. Encrypts the file content with AES-256-GCM and streams back"
echo -e "       a single output file that bundles everything needed to decrypt later."
echo ""
echo -e "  Calling the service now...\n"

HTTP_STATUS=$(curl -s -o "$ENC_FILE" \
  -w "%{http_code}" \
  -X POST "$HOST/api/v1/file/encrypt" \
  -F "file=@$PAN_FILE" \
  -F 'request={"transitKey":"my-key","algorithm":"AES_256_GCM"};type=application/json')

if [ "$HTTP_STATUS" != "200" ]; then
  echo -e "${RED}  Encryption failed — HTTP $HTTP_STATUS. Is the service running?${RESET}"
  exit 1
fi

ORIG_SIZE=$(wc -c < "$PAN_FILE")
ENC_SIZE=$(wc -c < "$ENC_FILE")

echo -e "${GREEN}  Done.${RESET}"
echo -e "  Original  : $ORIG_SIZE bytes  →  Encrypted : $ENC_SIZE bytes"
echo -e "  The output file is self-contained — it carries its own decryption metadata."

pause

# ─────────────────────────────────────────────────────────────────────────────
banner "WHAT IS INSIDE THE ENCRYPTED FILE"
# ─────────────────────────────────────────────────────────────────────────────

echo -e "  The file has a simple layout:\n"
echo -e "    ┌──────────────────────────────────────────────────────────────────┐"
echo -e "    │  4 bytes     metadata length                                     │"
echo -e "    │  N bytes     metadata JSON  ← IV + wrapped DEK + algorithm name  │"
echo -e "    │  rest        AES-256-GCM ciphertext + 16-byte auth tag           │"
echo -e "    └──────────────────────────────────────────────────────────────────┘\n"

echo -e "  The metadata section is plain JSON — let us look at it:\n"

META_LEN=$(od -A n -t u4 -N 4 "$ENC_FILE" | tr -d ' ')
dd if="$ENC_FILE" bs=1 skip=4 count="$META_LEN" 2>/dev/null | python3 -m json.tool | sed 's/^/    /'

echo ""
echo -e "  Notice the ${YELLOW}wrappedDek${RESET} field — that is the AES key after Vault encrypted it."
echo -e "  It looks random. Without Vault you cannot turn it back into the original key."
echo -e "  The ${YELLOW}ivBase64${RESET} is the initialisation vector — unique per file, not a secret."

pause

# ─────────────────────────────────────────────────────────────────────────────
banner "WHAT AN ATTACKER SEES — The file is stolen"
# ─────────────────────────────────────────────────────────────────────────────

echo -e "  Imagine an attacker exfiltrates pan_records.enc from your storage."
echo -e "  They have the full file. Let us see what they can do with it.\n"

echo -e "${YELLOW}  Attempt 1 — search for card numbers directly:${RESET}\n"
echo -e "    grep '4111\\|5500\\|3782\\|6011' pan_records.enc\n"

if grep -qE "4111|5500|3782|6011" "$ENC_FILE" 2>/dev/null; then
  echo -e "  ${RED}  Found — data is exposed!${RESET}"
else
  echo -e "  ${GREEN}    No matches. The card numbers do not exist anywhere in the file.${RESET}"
fi

echo ""
echo -e "${YELLOW}  Attempt 2 — look for any readable strings:${RESET}\n"
READABLE=$(strings "$ENC_FILE" 2>/dev/null | grep -v '{' | grep -v '}' | grep -v '"' | head -3)
if [ -z "$READABLE" ]; then
  echo -e "    No human-readable strings in the ciphertext section."
else
  echo -e "    $READABLE"
fi

echo ""
echo -e "${YELLOW}  Attempt 3 — look at the raw bytes (first 32 bytes of ciphertext):${RESET}\n"
dd if="$ENC_FILE" bs=1 skip=$((4 + META_LEN)) count=32 2>/dev/null \
  | od -A x -t x1z | head -4 | sed 's/^/    /'

echo ""
echo -e "  ${RED}The attacker is stuck.${RESET}"
echo ""
echo -e "  Even with the full file they need TWO more things they do not have:"
echo ""
echo -e "    1. ${BOLD}Vault credentials${RESET} — to call the unwrap API"
echo -e "       Without these, the wrappedDek field is an opaque blob forever."
echo ""
echo -e "    2. ${BOLD}Permission on the transit key 'my-key'${RESET} inside Vault"
echo -e "       Even valid Vault credentials are not enough — the policy must"
echo -e "       explicitly grant decrypt permission on this specific key."
echo ""
echo -e "  The encryption key was generated in memory, used once, and discarded."
echo -e "  It never touched a config file, a database, or an environment variable."

pause

# ─────────────────────────────────────────────────────────────────────────────
banner "LEGITIMATE DECRYPTION — Business as usual"
# ─────────────────────────────────────────────────────────────────────────────

echo -e "  An authorised service with valid Vault credentials sends the"
echo -e "  ensemble file back to the decryption endpoint.\n"

HTTP_STATUS=$(curl -s -o "$DEC_FILE" \
  -w "%{http_code}" \
  -X POST "$HOST/api/v1/file/decrypt" \
  -F "file=@$ENC_FILE" \
  -F "transitKey=$TRANSIT_KEY")

if [ "$HTTP_STATUS" != "200" ]; then
  echo -e "${RED}  Decryption failed — HTTP $HTTP_STATUS${RESET}"
  exit 1
fi

echo -e "${GREEN}  Decrypted successfully. Original card data restored:${RESET}\n"
column -t -s',' "$DEC_FILE"

echo ""
if diff -q "$PAN_FILE" "$DEC_FILE" > /dev/null 2>&1; then
  echo -e "  ${GREEN}Integrity check passed — byte-for-byte identical to the original.${RESET}"
  echo -e "  AES-256-GCM includes an authentication tag, so any tampering with"
  echo -e "  the ciphertext would cause decryption to fail outright, not silently corrupt data."
else
  echo -e "  ${RED}Integrity check failed — files differ.${RESET}"
fi

pause

# ─────────────────────────────────────────────────────────────────────────────
banner "SUMMARY"
# ─────────────────────────────────────────────────────────────────────────────

echo -e "  What we demonstrated:\n"
echo -e "    ✓  One file in  →  one encrypted ensemble file out"
echo -e "    ✓  No key management in application code — Vault owns the keys"
echo -e "    ✓  Stolen file alone is cryptographically useless"
echo -e "    ✓  Decryption requires live Vault access — no offline attack possible"
echo -e "    ✓  AES-256-GCM guarantees both confidentiality and integrity"
echo -e "    ✓  Every operation is traced end-to-end with a unique traceId\n"

echo -e "  ${DIM}Compliance note: this pattern satisfies PCI-DSS requirement 3.5"
echo -e "  (protect stored cardholder data) when Vault is deployed in HA mode"
echo -e "  with audit logging enabled.${RESET}\n"
