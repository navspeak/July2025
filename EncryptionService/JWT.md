# JWT Authentication — Encryption Service

## Overview

The encryption service is a protected resource. Every API call to `/api/v1/file/encrypt`
or `/api/v1/file/decrypt` requires a valid Bearer token issued by the company Identity
Provider (IdP).

The service validates tokens locally using the IdP's public keys — it never calls the
IdP on your behalf. Your application is responsible for obtaining a token before calling
the service.

---

## Interaction Flow

```
  Your Application                  IdP (OAuth2 Server)             Encryption Service
        |                                   |                                |
        |  POST /oauth2/token               |                                |
        |  grant_type=client_credentials    |                                |
        |  client_id=your-client-id         |                                |
        |  client_secret=your-secret        |                                |
        |  scope=<required-scope>           |                                |
        | --------------------------------> |                                |
        |                                   |                                |
        |  { access_token: "eyJ..." }       |                                |
        | <-------------------------------- |                                |
        |                                   |                                |
        |                                                                    |
        |  POST /api/v1/file/encrypt                                         |
        |  Authorization: Bearer eyJ...                                      |
        | -----------------------------------------------------------------> |
        |                                   |                                |
        |                                   |   verify signature using       |
        |                                   |   public keys from jwk-set-uri |
        |                                   |   (no IdP call — local check)  |
        |                                   |                                |
        |  200 OK  { encrypted file }                                        |
        | <----------------------------------------------------------------- |
```

---

## What You Need to Do

### Step 1 — Register your application with the IdP

If your application is not already registered, raise a request with the IdP team to get:

- `client-id` — identifies your application
- `client-secret` — your application's credential, treat it like a password

Store these in your application's secure configuration — not in source code.

### Step 2 — Obtain a Bearer token

Before calling the encryption service, fetch a token from the IdP using the
**Client Credentials** flow (machine-to-machine, no user login required):

```
POST https://<idp-host>/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=your-client-id
&client_secret=your-client-secret
&scope=<confirm-scope-with-idp-team>
```

Example curl:

```bash
TOKEN=$(curl -s -X POST "https://<idp-host>/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=your-client-id" \
  -d "client_secret=your-client-secret" \
  -d "scope=<required-scope>" \
  | jq -r '.access_token')
```

### Step 3 — Call the encryption service

Include the token in every request:

```bash
curl -X POST "https://<encryption-service-host>/api/v1/file/encrypt" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@your-file.csv" \
  -F 'request={"transitKey":"my-key","algorithm":"AES_256_GCM"};type=application/json'
```

### Step 4 — Cache the token

Tokens are short-lived (typically 5–60 minutes). Do not fetch a new token on every
request — fetch once, reuse until it expires, then refresh.

Most OAuth2 client libraries handle this automatically:

- **Spring** — `spring-boot-starter-oauth2-client` with `WebClient` handles token
  fetch and refresh transparently
- **Python** — `requests-oauthlib` or `authlib`
- **Node** — `client-oauth2` or `openid-client`

---

## What You Do Not Need from Us

The encryption service is already configured to trust your IdP. You do not need to
register with us or share any credentials. All you need is your own client-id and
secret from the IdP team.

---

## Confirm with the IdP Team

| Question | Why |
|---|---|
| What is the token endpoint URL? | Needed for the POST in Step 2 |
| What scope should I request? | Required by some IdP policies |
| What is the token TTL? | So you know how often to refresh |
| Is client-secret rotation required? | Operational — typically 90 days |

---

## Error Reference

| HTTP Status | Meaning |
|---|---|
| `401 Unauthorized` | No token sent, token expired, or signature invalid |
| `403 Forbidden` | Token is valid but does not have required scope/audience |
| `200 OK` | Token accepted, request processed |

---

## Notes

- The encryption service never sees your `client-id` or `client-secret` — those stay
  between your application and the IdP
- Token verification is done locally using the IdP's public keys — no IdP call per
  request, no added latency
- The IdP's public keys are fetched once at startup and cached — key rotation is
  handled automatically
