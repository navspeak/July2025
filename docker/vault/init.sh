#!/bin/sh

vault secrets enable transit          2>&1 || true
vault write -f transit/keys/my-key type=aes256-gcm96 2>&1 || true
vault secrets enable -path=secret kv-v2 2>&1 || true
vault auth enable approle             2>&1 || true

vault policy write encryption-policy - <<EOF
path "transit/encrypt/*" { capabilities = ["update"] }
path "transit/decrypt/*" { capabilities = ["update"] }
path "transit/keys/*"    { capabilities = ["read"] }
path "secret/data/*"     { capabilities = ["create", "read", "update", "delete"] }
path "secret/metadata/*" { capabilities = ["read", "list", "delete"] }
EOF

vault write auth/approle/role/encryption-service \
  token_policies="encryption-policy" \
  token_ttl=1h \
  token_max_ttl=4h

ROLE_ID=$(vault read -field=role_id auth/approle/role/encryption-service/role-id)
SECRET_ID=$(vault write -force -field=secret_id auth/approle/role/encryption-service/secret-id)

echo "VAULT_ROLE_ID=$ROLE_ID"
echo "VAULT_SECRET_ID=$SECRET_ID"
