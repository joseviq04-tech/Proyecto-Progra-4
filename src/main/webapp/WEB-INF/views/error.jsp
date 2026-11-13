<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Error - Sistema de Soporte</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f6f9; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .card { background: white; padding: 35px 40px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); max-width: 460px; text-align: center; }
        .codigo { font-size: 56px; font-weight: bold; color: #dc3545; margin: 0; }
        h2 { color: #333; margin: 10px 0; }
        p { color: #666; line-height: 1.5; }
        a { display: inline-block; margin-top: 15px; background: #007bff; color: white; padding: 10px 20px; border-radius: 4px; text-decoration: none; font-weight: bold; }
    </style>
</head>
<body>
    <c:set var="codigo" value="${pageContext.errorData.statusCode}"/>
    <div class="card">
        <p class="codigo"><c:out value="${codigo}"/></p>
        <c:choose>
            <c:when test="${codigo == 400}">
                <h2>Solicitud no válida</h2>
                <p>Los datos enviados no son correctos. Vuelve atrás e inténtalo de nuevo.</p>
            </c:when>
            <c:when test="${codigo == 404}">
                <h2>No encontrado</h2>
                <p>La página o el ticket que buscas no existe, o no tienes permiso para verlo.</p>
            </c:when>
            <c:otherwise>
                <h2>Ocurrió un problema</h2>
                <p>No pudimos completar la operación. Intenta de nuevo más tarde; si el problema continúa, avisa al administrador.</p>
            </c:otherwise>
        </c:choose>
        <a href="${pageContext.request.contextPath}/">Volver al inicio</a>
    </div>
</body>
</html>
