<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel de Técnico - Soporte</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; }
        .container { max-width: 1000px; margin: auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
        .header-bar { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #eee; padding-bottom: 10px; margin-bottom: 20px; }
        h1 { color: #007bff; margin-top: 0; }
        .mensaje { padding: 10px 14px; border-radius: 4px; margin-bottom: 15px; }
        .mensaje.error { background: #f8d7da; color: #721c24; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; font-size: 14px; }
        th { background-color: #007bff; color: white; }
        tr:hover { background-color: #f1f1f1; }
        .badge { padding: 5px 10px; border-radius: 4px; font-size: 12px; font-weight: bold; color: white; background-color: #6c757d; }
        .badge-abierto { background-color: #ffc107; color: #333; }
        .badge-asignado { background-color: #17a2b8; }
        .badge-proceso { background-color: #007bff; }
        .badge-resuelto { background-color: #28a745; }
        .badge-cerrado { background-color: #6c757d; }
        select { padding: 5px; border-radius: 4px; border: 1px solid #ccc; }
        button { background-color: #17a2b8; color: white; padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
        button:hover { background-color: #138496; }
        .btn-logout { background-color: #dc3545; color: white; padding: 8px 15px; border: none; border-radius: 4px; text-decoration: none; font-size: 14px; }
        .btn-logout:hover { background-color: #c82333; }
        .sin-accion { color: #888; font-style: italic; }
    </style>
</head>
<body>

<div class="container">
    <div class="header-bar">
        <h1>Panel de Técnico</h1>
        <div>
            <span style="margin-right: 15px; font-weight: bold;">Técnico: <c:out value="${sessionScope.usuarioLogueado.nombre}"/></span>
            <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Cerrar Sesión</a>
        </div>
    </div>

    <c:if test="${not empty errorMsg}">
        <div class="mensaje error"><c:out value="${errorMsg}"/></div>
    </c:if>

    <h3>Gestión y Seguimiento de Tickets</h3>
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Título</th>
                <th>Descripción</th>
                <th>Estado Actual</th>
                <th>Actualizar Estado</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="t" items="${listaTickets}">
                <tr>
                    <td><c:out value="${t.id}"/></td>
                    <td><a href="${pageContext.request.contextPath}/ticket?id=${t.id}"><c:out value="${t.titulo}"/></a></td>
                    <td><c:out value="${t.descripcion}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${t.estado == 'Abierto'}"><span class="badge badge-abierto">Abierto</span></c:when>
                            <c:when test="${t.estado == 'Asignado'}"><span class="badge badge-asignado">Asignado</span></c:when>
                            <c:when test="${t.estado == 'En proceso'}"><span class="badge badge-proceso">En proceso</span></c:when>
                            <c:when test="${t.estado == 'Resuelto'}"><span class="badge badge-resuelto">Resuelto</span></c:when>
                            <c:when test="${t.estado == 'Cerrado'}"><span class="badge badge-cerrado">Cerrado</span></c:when>
                            <c:otherwise><span class="badge"><c:out value="${t.estado}"/></span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${t.estado == 'Cerrado'}">
                                <span class="sin-accion">Ticket cerrado</span>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/tecnico/dashboard" method="post" style="display: flex; gap: 8px; align-items: center; margin: 0;">
                                    <input type="hidden" name="ticketId" value="${t.id}">
                                    <select name="estado">
                                        <option value="Abierto" ${t.estado == 'Abierto' ? 'selected' : ''}>Abierto</option>
                                        <option value="Asignado" ${t.estado == 'Asignado' ? 'selected' : ''}>Asignado</option>
                                        <option value="En proceso" ${t.estado == 'En proceso' ? 'selected' : ''}>En proceso</option>
                                        <option value="Resuelto" ${t.estado == 'Resuelto' ? 'selected' : ''}>Resuelto</option>
                                    </select>
                                    <button type="submit">Guardar</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty listaTickets}">
                <tr>
                    <td colspan="5" style="text-align: center; color: #777;">No tienes tickets asignados en este momento.</td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>

</body>
</html>
