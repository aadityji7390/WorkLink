# WorkLink v2

WorkLink is a Java + HTML/CSS/JavaScript college project that connects workers with employers.

## Included features
- Worker and Boss registration/login
- Password hashing with PBKDF2 (legacy plain-text accounts are migrated after successful login)
- Basic session-token authentication for write actions
- Role-aware worker/employer search
- Worker filters by skill/category
- Worker and Boss profiles with editing
- Worker availability UI
- Two-way ratings and reviews with server-side validation
- Boss can post work opportunities
- Worker can search, filter, sort and view work details
- Input length/format validation and basic HTTP security headers
- Render-friendly Java server using the PORT environment variable

## Run locally
From the `backend` directory:

```bash
javac *.java
java Server
```

Open:
`http://localhost:8080`

If 8080 is busy, use another port, for example:

```powershell
$env:PORT=8090
java Server
```

## Render
The included Dockerfile uses Java 17, listens on the Render `PORT`, and serves the frontend from the Java server.

> Note: the file-based data store is suitable for a college demonstration. For production use, replace it with a managed database and HTTPS/session infrastructure.
