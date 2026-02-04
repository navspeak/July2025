## CSRF
```bash
# GET Works
http -a user:ff0dca13-3534-4570-9242-9e6741c04ef8 GET http://localhost:8080/product
# POST 401 = WHY?
http -a user:ff0dca13-3534-4570-9242-9e6741c04ef8 POST http://localhost:8080/product \
   Accept:application/json \
   productId:=3 \
   productName="iPad" \
   price:=1500
```
1. Assume: CSRF is disabled (whatever it is!)
    2. Spring Security still uses Session (JSESSIONID)
    3. User is authenticated (Basic / Form login)
    3. You login. Browser has `Cookie: JSESSIONID=abc123`. You submit POST /product. Controller adds it. => __all good?__
    4. While logged in the browser, you visit a malicious site that posts a vicious product:
  ```html
  <form action="http://localhost:8080/product" method="POST">
    <input name="productId" value="999">
    <input name="productName" value="HackedProduct">
    <input name="price" value="0">
  </form>
  
  <script>
    document.forms[0].submit();
  </script>
  ```
5. Your browser has the cookie, so it attaches to the request. You app is deceieved into thinking it's from an authenticated user. And BOOM you got scammed!!!!

2. So, spring security out of box has `CSRF protection ENABLED by default`
3. **What Spring Security does OUT OF THE BOX**
    1. On login / first request -> Spring generates a CSRF token -> Stores it in HTTP session
    2. On every state-changing request (POST, PUT, DELETE):
        3. Spring expects a CSRF token in header or request param.
        4. CsrfFilter checks: `request token == session token ?`
    3. If token is missing or invalid -> reject request
4. Replay the attack WITH CSRF enabled
    5. Malicious site submits `<form action="http://localhost:8080/product" method="POST">`
    6. Browser sends `Cookie: JSESSIONID=abc123`. But attacker can't read & inject into requests (because of `Same-orgin-policy` enforced by browsers)
    7. So Spring says `403 Forbidden - Invalid CSRF token`
5. When developers disable CSRF
    6. In stateless APIs: No sessions, No cookies, Auth via headers (JWT / Bearer token)
    ```
    http
    .csrf(csrf -> csrf.disable())
    .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS));
    ```
    7. Now: browser can’t auto-attach auth -> attacker can’t forge headers -> CSRF risk disappears
