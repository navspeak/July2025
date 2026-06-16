#!/usr/bin/env bash

TOKEN_URI="http://localhost:8080/realms/encryption/protocol/openid-connect/token"
CLIENT_ID="test-consumer"
CLIENT_SECRET="test-consumer-secret"

response=$(curl -s -X POST "$TOKEN_URI" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}")

JWT_TOKEN=$(echo "$response" | sed 's/.*"access_token":"\([^"]*\)".*/\1/')

if [ -z "$JWT_TOKEN" ] || [ "$JWT_TOKEN" = "$response" ]; then
  echo "ERROR: failed to get token. Response: $response" >&2
  return 1 2>/dev/null || exit 1
fi

export JWT_TOKEN
expires_in=$(echo "$response" | sed 's/.*"expires_in":\([^,}]*\).*/\1/')
echo "JWT_TOKEN exported (expires in ${expires_in}s)"
