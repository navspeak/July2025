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
   - Spring Security still uses Session (JSESSIONID)
   - User is authenticated (Basic / Form login)
   - You login. Browser has `Cookie: JSESSIONID=abc123`. You submit POST /product. Controller adds it. => __all good?__
     - While logged in the browser, you visit a malicious site that posts a vicious product:
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
2. Your browser has the cookie, so it attaches to the request. You app is deceieved into thinking it's from an authenticated user. And BOOM you got scammed!!!!
3. So, spring security out of box has `CSRF protection ENABLED by default`
4. **What Spring Security does OUT OF THE BOX**
   - On login / first request -> Spring generates a CSRF token -> Stores it in HTTP session
   - On every state-changing request (POST, PUT, DELETE):
     - Spring expects a CSRF token in header or request param.
     - CsrfFilter checks: `request token == session token ?`
     - If token is missing or invalid -> reject request
5. Replay the attack WITH CSRF enabled.Malicious site submits `<form action="http://localhost:8080/product" method="POST">`.
   - Browser sends `Cookie: JSESSIONID=abc123`. 
   - But attacker can't read & inject into requests (because of `Same-orgin-policy` enforced by browsers)
   - So Spring says `403 Forbidden - Invalid CSRF token`
6. When developers disable CSRF
    - In stateless APIs: No sessions, No cookies, Auth via headers (JWT / Bearer token)
    ```
    http
    .csrf(csrf -> csrf.disable())
    .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS));
    ```
    - Now: browser can’t auto-attach auth -> attacker can’t forge headers -> CSRF risk disappears
