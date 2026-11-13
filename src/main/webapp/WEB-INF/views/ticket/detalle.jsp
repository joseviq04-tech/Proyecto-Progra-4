<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ticket #<c:out value="${ticket.id}"/> - Sistema de Soporte</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; color: #333; }
        .container { max-width: 900px; margin: auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
        .header-bar { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #eee; padding-bottom: 10px; margin-bottom: 20px; }
        h1 { color: #007bff; margin: 0; font-size: 24px; }
        h3 { margin-top: 30px; }
        .btn { color: white; padding: 8px 14px; border-radius: 4px; text-decoration: none; font-size: 14px; font-weight: bold; margin-left: 6px; }
        .btn-volver { background-color: #6c757d; }
        .btn-logout { background-color: #dc3545; }
        table { width: 100%; border-collapse: collapse; }
        .info th { width: 180px; background: #f8f9fa; color: #555; text-align: left; }
        th, td { padding: 10px; text-align: left; border: 1px solid #ddd; font-size: 14px; vertical-align: top; }
        .historial th { background-color: #007bff; color: white; }
        .descripcion { white-space: pre-wrap; }
        .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; color: white; background-color: #6c757d; }
        .badge-abierto { background-color: #ffc107; color: #333; }
        .badge-asignado { background-color: #17a2b8; }
        .badge-proceso { background-color: #007bff; }
        .badge-resuelto { background-color: #28a745; }
        .badge-cerrado { background-color: #6c757d; }
        .sin-asignar { color: #888; font-style: italic; }
        .vacio { text-align: center; color: #777; padding: 20px; }
    </style>
</head>
<body>
<div class="container">

    <div class="header-bar">
        <h1>Ticket #<c:out value="${ticket.id}"/></h1>
        <div>
            <a href="${volverUrl}" class="btn btn-volver">Volver</a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-logout">Cerrar Sesión</a>
        </div>
    </div>

    <table class="info">
        <tr>
            <th>Título</th>
            <td><c:out value="${ticket.titulo}"/></td>
        </tr>
        <tr>
            <th>Descripción</th>
            <td class="descripcion"><c:out value="${ticket.descripcion}"/></td>
        </tr>
        <tr>
            <th>Estado</th>
            <td>
                <c:choose>
                    <c:when test="${ticket.estado == 'Abierto'}"><span class="badge badge-abierto">Abierto</span></c:when>
                    <c:when test="${ticket.estado == 'Asignado'}"><span class="badge badge-asignado">Asignado</span></c:when>
                    <c:when test="${ticket.estado == 'En proceso'}"><span class="badge badge-proceso">En proceso</span></c:when>
                    <c:when test="${ticket.estado == 'Resuelto'}"><span class="badge badge-resuelto">Resuelto</span></c:when>
                    <c:when test="${ticket.estado == 'Cerrado'}"><span class="badge badge-cerrado">Cerrado</span></c:when>
                    <c:otherwise><span class="badge"><c:out value="${ticket.estado}"/></span></c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <th>Cliente</th>
            <td><c:out value="${ticket.clienteNombre}"/></td>
        </tr>
        <tr>
            <th>Técnico asignado</th>
            <td>
                <c:choose>
                    <c:when test="${not empty ticket.tecnicoNombre}"><c:out value="${ticket.tecnicoNombre}"/></c:when>
                    <c:otherwise><span class="sin-asignar">Sin asignar</span></c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <th>Fecha de creación</th>
            <td><c:out value="${fn:substring(ticket.fechaCreacion, 0, 19)}"/></td>
        </tr>
    </table>

    <h3>Historial de cambios de estado</h3>
    <table class="historial">
        <thead>
            <tr>
                <th>Fecha</th>
                <th>Realizado por</th>
                <th>Cambio</th>
                <th>Comentario</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="h" items="${historial}">
                <tr>
                    <td><c:out value="${fn:substring(h.fechaCambio, 0, 19)}"/></td>
                    <td><c:out value="${h.usuarioNombre}"/></td>
                    <td><c:out value="${h.estadoAnterior}"/> &rarr; <strong><c:out value="${h.estadoNuevo}"/></strong></td>
                    <td><c:out value="${h.comentario}"/></td>
                </tr>
            </c:forEach>
            <c:if test="${empty historial}">
                <tr>
                    <td colspan="4" class="vacio">Este ticket todavía no tiene cambios de estado.</td>
                </tr>
            </c:if>
        </tbody>
    </table>

</div>
</body>
</html>
