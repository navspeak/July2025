# Encryption Service Client

Java client that calls the running Encryption Service at `http://localhost:8081`.

---

## Running the Java Client

The client is activated via the `client` Spring profile. The service must already be running on port 8081.

**Step 1 — create a sample file:**
```bash
echo "card_number,name\n4111111111111111,John Doe" > /tmp/sample.csv
```

**Step 2 — build and run with the client profile (from `EncryptionService/` folder):**
```bash
mvn install && mvn spring-boot:run -Dspring-boot.run.profiles=client
```

This will:
1. Encrypt `/tmp/sample.csv` with AES-256-GCM → `/tmp/sample.csv.enc`
2. Decrypt `/tmp/sample.csv.enc` → `/tmp/sample.csv.decrypted`
3. Encrypt `/tmp/sample.csv` with ChaCha20-Poly1305 → `/tmp/sample.csv.enc`

---

## Equivalent curl Commands

### Encrypt a file (AES-256-GCM default)
```bash
curl -X POST http://localhost:8081/api/v1/file/encrypt \
  -F "file=@/tmp/sample.csv" \
  --output /tmp/sample.csv.enc
```

### Encrypt with explicit algorithm and key
```bash
curl -X POST http://localhost:8081/api/v1/file/encrypt \
  -F "file=@/tmp/sample.csv" \
  -F 'request={"algorithm":"AES_256_GCM","transitKey":"my-key"};type=application/json' \
  --output /tmp/sample.csv.enc
```

### Encrypt with ChaCha20-Poly1305
```bash
curl -X POST http://localhost:8081/api/v1/file/encrypt \
  -F "file=@/tmp/sample.csv" \
  -F 'request={"algorithm":"CHACHA20_POLY1305","transitKey":"my-key"};type=application/json' \
  --output /tmp/sample.csv.enc
```

### Decrypt a file
```bash
curl -X POST http://localhost:8081/api/v1/file/decrypt \
  -F "file=@/tmp/sample.csv.enc" \
  --output /tmp/sample.csv.decrypted
```

### Decrypt with explicit transit key
```bash
curl -X POST "http://localhost:8081/api/v1/file/decrypt?transitKey=my-key" \
  -F "file=@/tmp/sample.csv.enc" \
  --output /tmp/sample.csv.decrypted
```

### Encrypt text
```bash
curl -X POST http://localhost:8081/api/v1/text/encrypt \
  -H "Content-Type: application/json" \
  -d '{"plaintext":"4111111111111111"}'

# With explicit key
curl -X POST http://localhost:8081/api/v1/text/encrypt \
  -H "Content-Type: application/json" \
  -d '{"plaintext":"4111111111111111","transitKey":"my-key"}'
```

### Decrypt text
```bash
curl -X POST http://localhost:8081/api/v1/text/decrypt \
  -H "Content-Type: application/json" \
  -d '{"ciphertext":"vault:v1:ABCxyz..."}'
```

### Write a secret
```bash
curl -X POST http://localhost:8081/api/v1/secrets/myapp/config \
  -H "Content-Type: application/json" \
  -d '{"apiKey":"abc123","region":"us-east-1"}'
```

### Read all keys at a path
```bash
curl http://localhost:8081/api/v1/secrets/myapp/config
```

### Read a single key
```bash
curl http://localhost:8081/api/v1/secrets/myapp/config/apiKey
```

### Delete a secret
```bash
curl -X DELETE http://localhost:8081/api/v1/secrets/myapp/config
```

---

## Metrics

### Vault round-trip latency (p50, p95, p99)
```bash
curl "http://localhost:8081/actuator/metrics/vault.ops.latency?tag=percentile:0.99"
```

### API endpoint latency per endpoint
```bash
curl "http://localhost:8081/actuator/metrics/http.server.requests?tag=uri:/api/v1/file/encrypt&tag=percentile:0.99"
```

### Filter by status code (e.g. count of 400s)
```bash
curl "http://localhost:8081/actuator/metrics/http.server.requests?tag=uri:/api/v1/file/encrypt&tag=status:400"
```
