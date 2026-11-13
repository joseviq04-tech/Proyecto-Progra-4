<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel de Administración - Sistema de Soporte</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; color: #333; }
        .container { max-width: 1100px; margin: auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 2px solid #eee; padding-bottom: 15px; }
        .header h2 { margin: 0; color: #2c3e50; }
        .btn-logout { background-color: #dc3545; color: white; padding: 8px 16px; text-decoration: none; border-radius: 4px; font-weight: bold; }
        .btn-logout:hover { background-color: #c82333; }
        .btn-usuarios { display: inline-block; background: #007bff; color: white; padding: 8px 16px; border-radius: 4px; text-decoration: none; font-weight: bold; }
        .mensaje { padding: 10px 14px; border-radius: 4px; margin-bottom: 15px; }
        .mensaje.ok { background: #d4edda; color: #155724; }
        .mensaje.error { background: #f8d7da; color: #721c24; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; border: 1px solid #ddd; text-align: left; }
        th { background-color: #007bff; color: white; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        select, button { padding: 6px 10px; border-radius: 4px; border: 1px solid #ccc; font-size: 14px; }
        button[type="submit"] { background-color: #28a745; color: white; font-weight: bold; cursor: pointer; border: none; }
        button[type="submit"]:hover { background-color: #218838; }
        .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; color: white; background-color: #6c757d; }
        .badge-abierto { background-color: #ffc107; color: #333; }
        .badge-asignado { background-color: #17a2b8; }
        .badge-proceso { background-color: #007bff; }
        .badge-resuelto { background-color: #28a745; }
        .badge-cerrado { background-color: #6c757d; }
        .badge-unassigned { color: #888; font-style: italic; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>Panel de Administración - Sistema de Soporte</h2>
            <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Cerrar Sesión</a>
        </div>

        <p>Bienvenido, Administrador. Desde aquí puedes supervisar las incidencias y asignarlas de manera eficiente a los técnicos disponibles.</p>
        <p><a href="${pageContext.request.contextPath}/admin/usuarios" class="btn-usuarios">Gestionar usuarios</a> <a href="${pageContext.request.contextPath}/admin/historial" class="btn-usuarios">Historial general</a></p>

        <c:if test="${not empty adminMensaje}">
            <div class="mensaje ok"><c:out value="${adminMensaje}"/></div>
        </c:if>
        <c:if test="${not empty adminError}">
            <div class="mensaje error"><c:out value="${adminError}"/></div>
        </c:if>

        <h3>Listado General de Tickets</h3>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Título</th>
                    <th>Cliente</th>
                    <th>Estado</th>
                    <th>Técnico Asignado</th>
                    <th>Acción (Asignación)</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="ticket" items="${listaTickets}">
                    <tr>
                        <td><c:out value="${ticket.id}"/></td>
                        <td><a href="${pageContext.request.contextPath}/ticket?id=${ticket.id}"><c:out value="${ticket.titulo}"/></a></td>
                        <td><c:out value="${ticket.clienteNombre}"/></td>
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
                        <td>
                            <c:choose>
                                <c:when test="${not empty ticket.tecnicoNombre}"><c:out value="${ticket.tecnicoNombre}"/></c:when>
                                <c:otherwise><span class="badge-unassigned">Sin asignar</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${ticket.estado == 'Cerrado'}">
                                    <span class="badge-unassigned">Ticket cerrado</span>
                                </c:when>
                                <c:otherwise>
                                    <form action="${pageContext.request.contextPath}/admin/asignar" method="POST" style="display: flex; gap: 5px; align-items: center; margin: 0;">
                                        <input type="hidden" name="idTicket" value="${ticket.id}">
                                        <select name="idTecnico" required>
                                            <option value="">-- Seleccionar Técnico --</option>
                                            <c:forEach var="tec" items="${listaTecnicos}">
                                                <option value="${tec.id}" ${tec.id == ticket.tecnicoId ? 'selected' : ''}><c:out value="${tec.nombre}"/></option>
                                            </c:forEach>
                                        </select>
                                        <button type="submit">Asignar</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty listaTickets}">
                    <tr>
                        <td colspan="6" style="text-align: center; color: #666; padding: 20px;">No hay tickets registrados en este momento.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</body>
</html>
