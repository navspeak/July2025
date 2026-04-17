# Encryption Service

A Spring Boot service for file and text encryption using AES-256-GCM or ChaCha20-Poly1305, backed by HashiCorp Vault for key management.

---

## Prerequisites

- Java 17
- HashiCorp Vault running locally on `http://localhost:8200`
- Vault transit engine enabled with a key
- Vault KV v2 engine enabled

### Vault Setup

```bash
# Enable transit engine
vault secrets enable transit
vault write -f transit/keys/my-key

# Enable KV v2
vault secrets enable -version=2 secret

# Enable AppRole auth
vault auth enable approle
vault write auth/approle/role/encryption-service \
    token_policies="default" \
    token_ttl=1h

# Get role-id and secret-id
vault read auth/approle/role/encryption-service/role-id
vault write -f auth/approle/role/encryption-service/secret-id
```

---

## Vault URL Resolution

Spring Vault does **not** embed the namespace in the URL path. Instead it sends
`X-Vault-Namespace` as a request header and all mount paths are relative within that namespace.

| Config | Value | Role |
|--------|-------|------|
| `app.vault.url` | `https://vault.host:8200` | Base host and port |
| `app.vault.namespace` | `harness/myproj` | Sent as `X-Vault-Namespace` header |
| `app.vault.mount-path` | `transit` | Mount path relative to namespace |
| `app.vault.key-name` | `mykey` | Transit key name |

**What Spring Vault sends:**
```
POST https://vault.host:8200/v1/transit/encrypt/mykey
X-Vault-Namespace: harness/myproj
```

**What Vault resolves internally:**
```
/v1/harness/myproj/transit/encrypt/mykey
```

The same applies to KV — `opsForVersionedKeyValue("secret")` resolves to
`harness/myproj/secret/data/{path}` automatically via the namespace header.

---

## Configuration

Set the following environment variables before running:

```bash
export VAULT_ROLE_ID=your-role-id
export VAULT_SECRET_ID=your-secret-id
```

Or override in `application.yml`:

```yaml
spring:
  vault:
    uri: http://localhost:8200
    app-role:
      role-id: your-role-id
      secret-id: your-secret-id
vault:
  transit-key: my-key
```

---

## Running

```bash
mvn spring-boot:run
```

Service starts on `http://localhost:8081`.

---

## Endpoints

### File Encryption — `POST /api/v1/encrypt`

Encrypts an uploaded file using AES-256-GCM or ChaCha20-Poly1305. DEK is generated per file and wrapped via Vault transit. Returns a binary stream.

**Request**

| Field | Type | Location | Required | Default | Description |
|-------|------|----------|----------|---------|-------------|
| `file` | Binary | `multipart/form-data` | Yes | — | File to encrypt (e.g. CSV containing PAN) |
| `request.algorithm` | String | `multipart/form-data` | No | `AES_256_GCM` | `AES_256_GCM` or `CHACHA20_POLY1305` |
| `request.keyId` | String | `multipart/form-data` | No | `my-key` | Vault transit key name |
| `request.fileName` | String | `multipart/form-data` | No | Original filename | Override output filename in metadata |

**Response**

| Field | Value |
|-------|-------|
| Status | `200 OK` |
| Content-Type | `application/octet-stream` |
| Content-Disposition | `attachment; filename=encrypted.enc` |
| Transfer-Encoding | chunked (streamed) |

**Encrypted file format**

```
┌──────────────────────────────────────┐
│  4 bytes  │  metadata JSON length    │
├──────────────────────────────────────┤
│  N bytes  │  metadata JSON           │
│           │  (fileName, ivBase64,    │
│           │   wrappedDek, algorithm) │
├──────────────────────────────────────┤
│  M bytes  │  encrypted file content  │
└──────────────────────────────────────┘
```

**Example**

```bash
# Encrypt with defaults (AES-256-GCM)
curl -X POST http://localhost:8081/api/v1/encrypt \
  -F "file=@/path/to/file.csv" \
  --output encrypted.enc

# Encrypt with ChaCha20
curl -X POST http://localhost:8081/api/v1/encrypt \
  -F "file=@/path/to/file.csv" \
  -F 'request={"algorithm":"CHACHA20_POLY1305","keyId":"my-key"};type=application/json' \
  --output encrypted.enc
```

---

### File Decryption — `POST /api/v1/decrypt`

Decrypts a file previously encrypted by this service. Algorithm and wrapped DEK are read from the file's metadata header — no need to specify them.

**Request**

| Field | Type | Location | Required | Description |
|-------|------|----------|----------|-------------|
| `file` | Binary | `multipart/form-data` | Yes | Encrypted `.enc` file |
| `keyId` | String | Query param | No | Vault transit key name. Defaults to `my-key` |

**Response**

| Field | Value |
|-------|-------|
| Status | `200 OK` |
| Content-Type | `application/octet-stream` |
| Content-Disposition | `attachment; filename=decrypted` |

**Example**

```bash
curl -X POST http://localhost:8081/api/v1/decrypt \
  -F "file=@encrypted.enc" \
  --output decrypted.csv

# With explicit keyId
curl -X POST "http://localhost:8081/api/v1/decrypt?keyId=my-key" \
  -F "file=@encrypted.enc" \
  --output decrypted.csv
```

---

### Text Encryption — `POST /api/v1/text/encrypt`

Encrypts a plaintext string using Vault transit. Useful for encrypting individual field values such as PAN, SSN, or API keys.

**Request Body**

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `plaintext` | String | Yes | — | Text to encrypt |
| `keyId` | String | No | `my-key` | Vault transit key name |

**Response Body**

| Field | Type | Description |
|-------|------|-------------|
| `ciphertext` | String | Vault ciphertext token (`vault:v1:...`) |

**Example**

```bash
curl -X POST http://localhost:8081/api/v1/text/encrypt \
  -H "Content-Type: application/json" \
  -d '{"plaintext":"4111111111111111","keyId":"my-key"}'

# Response
{"ciphertext":"vault:v1:ABCxyz..."}
```

---

### Text Decryption — `POST /api/v1/text/decrypt`

Decrypts a Vault ciphertext token back to plaintext.

**Request Body**

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `ciphertext` | String | Yes | — | Vault ciphertext token (`vault:v1:...`) |
| `keyId` | String | No | `my-key` | Vault transit key name |

**Response Body**

| Field | Type | Description |
|-------|------|-------------|
| `plaintext` | String | Decrypted original value |

**Example**

```bash
curl -X POST http://localhost:8081/api/v1/text/decrypt \
  -H "Content-Type: application/json" \
  -d '{"ciphertext":"vault:v1:ABCxyz...","keyId":"my-key"}'

# Response
{"plaintext":"4111111111111111"}
```

---

### Write Secret — `POST /api/v1/secrets/{path}`

Stores a key-value secret in Vault KV v2 at the given path.

**Path Variable**

| Variable | Description |
|----------|-------------|
| `path` | Secret path under `secret/` (e.g. `myapp/config`) |

**Request Body** — arbitrary JSON object

**Example**

```bash
curl -X POST http://localhost:8081/api/v1/secrets/myapp/config \
  -H "Content-Type: application/json" \
  -d '{"apiKey":"abc123","region":"us-east-1"}'

# Response: 204 No Content
```

---

### Read Secret — `GET /api/v1/secrets/{path}`

Reads all key-value pairs at the given Vault KV path.

**Example**

```bash
curl http://localhost:8081/api/v1/secrets/myapp/config

# Response
{"apiKey":"abc123","region":"us-east-1"}
```

---

### Read Secret Key — `GET /api/v1/secrets/{path}/{key}`

Reads a single key from a Vault KV secret.

**Example**

```bash
curl http://localhost:8081/api/v1/secrets/myapp/config/apiKey

# Response
{"apiKey":"abc123"}
```

---

### Delete Secret — `DELETE /api/v1/secrets/{path}`

Soft-deletes the latest version of a secret at the given path.

**Example**

```bash
curl -X DELETE http://localhost:8081/api/v1/secrets/myapp/config

# Response: 204 No Content
```

---

## Error Responses

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | Missing required field |
| `404 Not Found` | Secret path not found |
| `413 Payload Too Large` | File exceeds 500MB limit |
| `500 Internal Server Error` | Vault unreachable or encryption failure |

---

## Sequence Diagram

```
Client → Encryption Service → Vault (AppRole login → client_token)
                            → Vault Transit (wrap/unwrap DEK)
                            → Vault KV (read/write secrets)
```

---

## Architecture Notes

### File Size Limit

Maximum file size is **1MB**, enforced at two levels:

| Level | Setting | Effect |
|-------|---------|--------|
| Servlet (Spring multipart) | `max-file-size: 1MB` | Rejects oversized uploads immediately with `413` |
| Multipart threshold | `file-size-threshold: 1MB` | Files under 1MB stay in memory — no temp files written by Tomcat |

Because `file-size-threshold` matches `max-file-size`, the multipart resolver **never writes to disk**. All temp file management is handled explicitly by `FileProcessor`.

---

### Staging Files

Each encrypt/decrypt request writes two files to `${staging.dir}`:

```
/tmp/encryption-staging/
  {traceId}-input-{filename}     ← uploaded file
  {traceId}-output.enc / .dec    ← processed result
```

Files are named with the **Micrometer traceId** so they can be correlated with logs and traces.

**Normal cleanup** — the `StreamingResponseBody` lambda deletes both files in a `finally` block after streaming completes:

```
Request → write staging files → stream output → finally: delete staging files
```

**Orphan cleanup** — if the JVM crashes mid-stream, staging files are left behind. `StagingCleanupJob` runs every hour and deletes any file older than 60 minutes:

| Config | Default | Description |
|--------|---------|-------------|
| `staging.cleanup.max-age-minutes` | `60` | Files older than this are eligible for deletion |
| `staging.cleanup.interval-ms` | `3600000` | How often the cleanup job runs (ms) |

---

### Multipart Temp Files (Tomcat)

Spring Boot's embedded Tomcat writes multipart parts larger than `file-size-threshold` to a temp directory (`java.io.tmpdir`). With `file-size-threshold: 1MB` matching the max upload limit:

- Files ≤ 1MB → held in memory, **no Tomcat temp file created**
- Files > 1MB → rejected by `max-file-size` before reaching the threshold check

Result: **Tomcat never writes multipart temp files** for this service.

---

### Tracing

Micrometer Brave tracing is enabled with `sampling.probability: 1.0` (100% of requests traced).

- **TraceId** is propagated into `FileProcessor` and used as the staging filename prefix — correlating files on disk with distributed traces
- Traces are exported to Zipkin at `http://localhost:9411` if running
- Actuator endpoints exposed: `health`, `info`, `metrics`, `traces`

```bash
# Start Zipkin locally
docker run -d -p 9411:9411 openzipkin/zipkin
```

---

### Encryption Flow

```
POST /encrypt
  │
  ├── FileProcessor       → write input to staging (named by traceId)
  ├── EncryptionProcessor → generate DEK (AES-256 or ChaCha20 key + 12-byte IV)
  ├── VaultTransitService → wrap DEK via Vault transit
  ├── EncryptionProcessor → build FileEncryptionMetadata (fileName, ivBase64, wrappedDek, algorithm)
  ├── FileProcessor       → write [4-byte len][metadata JSON][CipherOutputStream encrypted bytes]
  └── StreamingResponseBody → stream output → finally: cleanup staging files
```

```
POST /decrypt
  │
  ├── FileProcessor       → write encrypted file to staging
  ├── Read 4 bytes        → metadata length
  ├── Read N bytes        → parse FileEncryptionMetadata (algorithm, ivBase64, wrappedDek)
  ├── VaultTransitService → unwrap DEK via Vault transit
  ├── EncryptionProcessor → init decrypt cipher (algorithm + DEK + IV from metadata)
  ├── CipherInputStream   → decrypt remaining bytes to staging output
  └── StreamingResponseBody → stream output → finally: cleanup staging files
```
