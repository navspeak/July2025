1. Create a token service where you login with basic auth and that return JWT token
         GET /token: Basic Auth => JWT token
2. GET /orders Bearer jwt-token => result

https://www.youtube.com/watch?v=KYNR5js2cXE
https://github.com/danvega/jwt/blob/master/src/main/java/dev/danvega/jwt/config/SecurityConfig.java
https://github.com/danvega/jwt/blob/master/src/main/java/dev/danvega/jwt/service/TokenService.java