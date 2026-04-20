#!/bin/sh
set -e

echo "==> Starting Vault..."
docker compose up -d

echo "==> Waiting for vault-init to complete..."
until [ "$(docker inspect -f '{{.State.Status}}' vault-init)" = "exited" ]; do
  sleep 1
done

export $(docker logs vault-init 2>&1 | grep -E "^VAULT_(ROLE_ID|SECRET_ID)=")

echo ""
echo "export VAULT_ROLE_ID=$VAULT_ROLE_ID"
echo "export VAULT_SECRET_ID=$VAULT_SECRET_ID"
echo ""
