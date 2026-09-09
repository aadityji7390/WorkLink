# WorkLink

WorkLink is a Java HttpServer + HTML/CSS/JavaScript web application.

## Local

From the `backend` directory:

```powershell
javac *.java
java Server
```

Open `http://localhost:8080/`.

## Render

This repository includes a Dockerfile. Deploy it as a Render Web Service. Render supplies the `PORT` environment variable; the server uses it automatically.
