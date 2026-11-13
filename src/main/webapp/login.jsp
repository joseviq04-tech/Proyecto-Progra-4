<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Inicio de Sesión - Soporte Técnico</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f6f9; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .login-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); width: 320px; }
        h2 { text-align: center; color: #333; margin-bottom: 20px; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; color: #666; font-size: 14px; }
        input[type="email"], input[type="password"] { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        button { width: 100%; padding: 10px; background-color: #007bff; border: none; color: white; border-radius: 4px; font-weight: bold; cursor: pointer; }
        button:hover { background-color: #0056b3; }
        .error-msg { color: #d9534f; background: #f9dedc; padding: 10px; border-radius: 4px; font-size: 13px; margin-bottom: 15px; text-align: center; }
    </style>
</head>
<body>

<div class="login-card">
    <h2>Sistema de Soporte</h2>

    <% if (request.getAttribute("error") != null) { %>
        <div class="error-msg"><%= request.getAttribute("error") %></div>
    <% } %>

    <form action="login" method="post">
        <div class="form-group">
            <label for="correo">Correo Electrónico:</label>
            <input type="email" id="correo" name="correo" required placeholder="ejemplo@soporte.com">
        </div>
        <div class="form-group">
            <label for="password">Contraseña:</label>
            <input type="password" id="password" name="password" required placeholder="••••••••">
        </div>
        <button type="submit">Ingresar</button>
    </form>
</div>

</body>
</html>
