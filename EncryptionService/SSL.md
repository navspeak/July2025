# SSL Setup — Encryption Service

## 1. Generate self-signed P12 keystore (dev/test)

```bash
keytool -genkeypair \
  -alias encryption-service \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore src/main/resources/keystore.p12 \
  -validity 365 \
  -storepass changeit \
  -dname "CN=localhost, OU=Dev, O=Example, L=City, ST=State, C=US"
```

## 2. Verify the keystore

```bash
keytool -list -v \
  -keystore src/main/resources/keystore.p12 \
  -storetype PKCS12 \
  -storepass changeit
```

## 3. Export certificate (share with clients for trust)

```bash
keytool -exportcert \
  -alias encryption-service \
  -keystore src/main/resources/keystore.p12 \
  -storetype PKCS12 \
  -storepass changeit \
  -rfc \
  -file encryption-service.crt
```

## 4. Import existing cert + private key into P12 (production)

If you have a CA-signed cert (`cert.pem`) and private key (`key.pem`):

```bash
openssl pkcs12 -export \
  -in cert.pem \
  -inkey key.pem \
  -out src/main/resources/keystore.p12 \
  -name encryption-service \
  -passout pass:changeit
```

## 5. Spring Boot properties

Add to `application.yml`:

```yaml
server:
  port: 8443
  ssl:
    key-store: classpath:keystore.p12
    key-store-type: PKCS12
    key-store-password: changeit
    key-alias: encryption-service
    enabled: true
```

For production — read password from env var, never hardcode:

```yaml
server:
  ssl:
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
```

## 6. Start the service

```bash
java -jar target/encryption-service-0.0.1-SNAPSHOT.jar
# now runs on https://localhost:8443
```

## 7. Test with curl

```bash
# self-signed — skip verify
curl -k https://localhost:8443/actuator/health

# with cert trust
curl --cacert encryption-service.crt https://localhost:8443/actuator/health
```

## 8. Enable HTTP and HTTPS simultaneously (optional)

Spring Boot only supports one connector in `application.yml`.
Add a second connector programmatically:

```java
@Bean
public ServletWebServerFactory servletContainer() {
    TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
    tomcat.addAdditionalTomcatConnectors(httpConnector());
    return tomcat;
}

private Connector httpConnector() {
    Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
    connector.setScheme("http");
    connector.setPort(8081);
    connector.setSecure(false);
    return connector;
}
```

## Notes

- Keep `keystore.p12` out of git — add to `.gitignore`
- Default keytool is bundled with JDK — no install needed
- For Let's Encrypt certs use step 4 (openssl import)
