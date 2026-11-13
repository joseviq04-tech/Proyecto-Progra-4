<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Historial General - Sistema de Soporte</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; color: #333; }
        .container { max-width: 1100px; margin: auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 2px solid #eee; padding-bottom: 15px; }
        .header h2 { margin: 0; color: #2c3e50; }
        .nav a { text-decoration: none; padding: 8px 16px; border-radius: 4px; font-weight: bold; color: white; margin-left: 8px; }
        .btn-volver { background-color: #6c757d; }
        .btn-logout { background-color: #dc3545; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 10px; border: 1px solid #ddd; text-align: left; font-size: 14px; vertical-align: top; }
        th { background-color: #007bff; color: white; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        .nota { color: #777; font-size: 13px; }
        .vacio { text-align: center; color: #777; padding: 20px; }
    </style>
</head>
<body>
<div class="container">

    <div class="header">
        <h2>Historial General de Cambios</h2>
        <div class="nav">
            <a href="${pageContext.request.contextPath}/admin" class="btn-volver">Volver al panel</a>
            <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Cerrar Sesión</a>
        </div>
    </div>

    <p class="nota">Todos los cambios de estado de todos los tickets, del más reciente al más antiguo.
        Haz clic en un ticket para ver su detalle.</p>

    <table>
        <thead>
            <tr>
                <th>Fecha</th>
                <th>Ticket</th>
                <th>Realizado por</th>
                <th>Cambio</th>
                <th>Comentario</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="h" items="${historial}">
                <tr>
                    <td><c:out value="${fn:substring(h.fechaCambio, 0, 19)}"/></td>
                    <td>
                        <a href="${pageContext.request.contextPath}/ticket?id=${h.ticketId}">
                            #<c:out value="${h.ticketId}"/> - <c:out value="${h.ticketTitulo}"/>
                        </a>
                    </td>
                    <td><c:out value="${h.usuarioNombre}"/></td>
                    <td><c:out value="${h.estadoAnterior}"/> &rarr; <strong><c:out value="${h.estadoNuevo}"/></strong></td>
                    <td><c:out value="${h.comentario}"/></td>
                </tr>
            </c:forEach>
            <c:if test="${empty historial}">
                <tr>
                    <td colspan="5" class="vacio">Todavía no hay cambios de estado registrados.</td>
                </tr>
            </c:if>
        </tbody>
    </table>

</div>
</body>
</html>
