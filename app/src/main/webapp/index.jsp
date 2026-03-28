<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>java-war-demo</title>
    <style>
        body { font-family: system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif; margin: 2rem; }
        code { background: #f3f4f6; padding: 0.15rem 0.3rem; border-radius: 0.25rem; }
        .card { border: 1px solid #e5e7eb; border-radius: 0.75rem; padding: 1rem 1.25rem; max-width: 720px; }
        a { color: #2563eb; text-decoration: none; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>
<div class="card">
    <h1>java-war-demo</h1>
    <p>WAR-deployed Java app for Kubernetes assessment.</p>
    <p>Try:</p>
    <ul>
        <li><a href="api/ping?name=assessment">/api/ping?name=assessment</a> (writes a log line)</li>
    </ul>
    <p>Pod hostname (if running in K8s): <code><%= System.getenv("HOSTNAME") %></code></p>
</div>
</body>
</html>

