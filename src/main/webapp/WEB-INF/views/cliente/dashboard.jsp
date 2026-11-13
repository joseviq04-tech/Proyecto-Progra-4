<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Mi Panel - Soporte Técnico</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; }
        .container { max-width: 1000px; margin: auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
        .header-bar { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #eee; padding-bottom: 10px; margin-bottom: 20px; }
        h1 { color: #007bff; margin-top: 0; }
        .mensaje { padding: 10px 14px; border-radius: 4px; margin-bottom: 15px; }
        .mensaje.ok { background: #d4edda; color: #155724; }
        .mensaje.error { background: #f8d7da; color: #721c24; }
        .form-card { background: #f8f9fa; padding: 15px; border-radius: 6px; margin-bottom: 25px; border: 1px solid #e9ecef; }
        .form-group { margin-bottom: 12px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; font-size: 13px; color: #555; }
        input[type="text"], textarea { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        textarea { resize: vertical; height: 80px; }
        button { background-color: #28a745; color: white; padding: 10px 15px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; }
        button:hover { background-color: #218838; }
        button.aprobar { padding: 6px 12px; font-size: 13px; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; font-size: 14px; }
        th { background-color: #007bff; color: white; }
        .badge { padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; color: white; background-color: #6c757d; }
        .badge-abierto { background-color: #ffc107; color: #333; }
        .badge-asignado { background-color: #17a2b8; }
        .badge-proceso { background-color: #007bff; }
        .badge-resuelto { background-color: #28a745; }
        .badge-cerrado { background-color: #6c757d; }
        .btn-logout { background-color: #dc3545; color: white; padding: 6px 12px; border: none; border-radius: 4px; text-decoration: none; font-size: 13px; }
        .btn-logout:hover { background-color: #c82333; }
        .sin-accion { color: #999; }
    </style>
</head>
<body>

<div class="container">
    <div class="header-bar">
        <h1>Portal de Cliente</h1>
        <div>
            <span style="margin-right: 15px; font-weight: bold;">Hola, <c:out value="${sessionScope.usuarioLogueado.nombre}"/></span>
            <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Cerrar Sesión</a>
        </div>
    </div>

    <c:if test="${not empty clienteMensaje}">
        <div class="mensaje ok"><c:out value="${clienteMensaje}"/></div>
    </c:if>
    <c:if test="${not empty clienteError}">
        <div class="mensaje error"><c:out value="${clienteError}"/></div>
    </c:if>

    <!-- Formulario para crear un nuevo ticket -->
    <div class="form-card">
        <h3>Crear Nuevo Ticket de Soporte</h3>
        <form action="${pageContext.request.contextPath}/cliente/dashboard" method="post">
            <input type="hidden" name="accion" value="crear">
            <div class="form-group">
                <label for="titulo">Título del Problema:</label>
                <input type="text" id="titulo" name="titulo" maxlength="150" required placeholder="Ej. Error al conectar a la impresora">
            </div>
            <div class="form-group">
                <label for="descripcion">Descripción Detallada:</label>
                <textarea id="descripcion" name="descripcion" maxlength="2000" required placeholder="Explica detalladamente el inconveniente..."></textarea>
            </div>
            <button type="submit">Enviar Ticket</button>
        </form>
    </div>

    <h3>Mis Tickets Registrados</h3>
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Título</th>
                <th>Descripción</th>
                <th>Estado</th>
                <th>Fecha de Creación</th>
                <th>Acción</th>
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
                    <td><c:out value="${t.fechaCreacion}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${t.estado == 'Resuelto'}">
                                <form action="${pageContext.request.contextPath}/cliente/dashboard" method="post"
                                      onsubmit="return confirm('¿Confirmas que el problema quedó solucionado? El ticket se cerrará.');"
                                      style="margin:0;">
                                    <input type="hidden" name="accion" value="aprobar">
                                    <input type="hidden" name="ticketId" value="${t.id}">
                                    <button type="submit" class="aprobar">Aprobar y cerrar</button>
                                </form>
                            </c:when>
                            <c:otherwise><span class="sin-accion">—</span></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty listaTickets}">
                <tr>
                    <td colspan="6" style="text-align: center; color: #777;">No has creado ningún ticket todavía.</td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>

</body>
</html>
