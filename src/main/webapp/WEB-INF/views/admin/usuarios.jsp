<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestión de Usuarios - Sistema de Soporte</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; color: #333; }
        .container { max-width: 1100px; margin: auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 2px solid #eee; padding-bottom: 15px; }
        .header h2 { margin: 0; color: #2c3e50; }
        .nav a { text-decoration: none; padding: 8px 16px; border-radius: 4px; font-weight: bold; color: white; margin-left: 8px; }
        .btn-volver { background-color: #6c757d; }
        .btn-logout { background-color: #dc3545; }
        .mensaje { padding: 10px 14px; border-radius: 4px; margin-bottom: 15px; }
        .mensaje.ok { background: #d4edda; color: #155724; }
        .mensaje.error { background: #f8d7da; color: #721c24; }
        .form-card { background: #f8f9fa; padding: 15px 20px; border-radius: 6px; border: 1px solid #e9ecef; margin-bottom: 25px; }
        .form-card h3 { margin-top: 0; }
        .fila { display: flex; gap: 12px; flex-wrap: wrap; align-items: flex-end; }
        .campo { display: flex; flex-direction: column; flex: 1; min-width: 180px; }
        .campo label { font-size: 13px; font-weight: bold; color: #555; margin-bottom: 4px; }
        input[type="text"], input[type="email"], input[type="password"], select { padding: 8px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }
        button { padding: 8px 16px; border: none; border-radius: 4px; color: white; font-weight: bold; cursor: pointer; font-size: 14px; }
        .btn-guardar { background-color: #28a745; }
        .btn-eliminar { background-color: #dc3545; padding: 6px 12px; }
        .btn-cancelar { background-color: #6c757d; text-decoration: none; padding: 8px 16px; border-radius: 4px; color: white; font-weight: bold; font-size: 14px; }
        .btn-editar { background-color: #007bff; text-decoration: none; padding: 6px 12px; border-radius: 4px; color: white; font-weight: bold; font-size: 14px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; border: 1px solid #ddd; text-align: left; }
        th { background-color: #007bff; color: white; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        .acciones { display: flex; gap: 6px; align-items: center; }
        .tu { color: #888; font-style: italic; font-size: 13px; }
    </style>
</head>
<body>
<div class="container">

    <div class="header">
        <h2>Gestión de Usuarios</h2>
        <div class="nav">
            <a href="${pageContext.request.contextPath}/admin" class="btn-volver">Volver a tickets</a>
            <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Cerrar Sesión</a>
        </div>
    </div>

    <c:if test="${not empty usuariosMensaje}">
        <div class="mensaje ok"><c:out value="${usuariosMensaje}"/></div>
    </c:if>
    <c:if test="${not empty usuariosError}">
        <div class="mensaje error"><c:out value="${usuariosError}"/></div>
    </c:if>

    <!-- Formulario: si hay un usuario para editar se muestra la edición; si no, la creación -->
    <div class="form-card">
        <c:choose>
            <c:when test="${not empty usuarioEditar}">
                <h3>Editar usuario #<c:out value="${usuarioEditar.id}"/></h3>
            </c:when>
            <c:otherwise>
                <h3>Crear nuevo usuario</h3>
            </c:otherwise>
        </c:choose>

        <form action="${pageContext.request.contextPath}/admin/usuarios" method="POST">
            <c:choose>
                <c:when test="${not empty usuarioEditar}">
                    <input type="hidden" name="accion" value="actualizar">
                    <input type="hidden" name="id" value="${usuarioEditar.id}">
                </c:when>
                <c:otherwise>
                    <input type="hidden" name="accion" value="crear">
                </c:otherwise>
            </c:choose>

            <div class="fila">
                <div class="campo">
                    <label for="nombre">Nombre</label>
                    <input type="text" id="nombre" name="nombre" maxlength="100" required
                           value="<c:out value='${usuarioEditar.nombre}'/>">
                </div>
                <div class="campo">
                    <label for="correo">Correo</label>
                    <input type="email" id="correo" name="correo" maxlength="100" required
                           value="<c:out value='${usuarioEditar.correo}'/>">
                </div>
                <div class="campo">
                    <label for="password">Contraseña</label>
                    <c:choose>
                        <c:when test="${not empty usuarioEditar}">
                            <input type="password" id="password" name="password" minlength="6"
                                   placeholder="Vacío = no cambiar">
                        </c:when>
                        <c:otherwise>
                            <input type="password" id="password" name="password" minlength="6" required
                                   placeholder="Mínimo 6 caracteres">
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="campo">
                    <label for="rol">Rol</label>
                    <select id="rol" name="rol" required>
                        <option value="CLIENTE" ${usuarioEditar.rol == 'CLIENTE' ? 'selected' : ''}>Cliente</option>
                        <option value="TECNICO" ${usuarioEditar.rol == 'TECNICO' ? 'selected' : ''}>Técnico</option>
                        <option value="ADMINISTRADOR" ${usuarioEditar.rol == 'ADMINISTRADOR' ? 'selected' : ''}>Administrador</option>
                    </select>
                </div>
                <div class="acciones">
                    <button type="submit" class="btn-guardar">
                        <c:choose>
                            <c:when test="${not empty usuarioEditar}">Guardar cambios</c:when>
                            <c:otherwise>Crear usuario</c:otherwise>
                        </c:choose>
                    </button>
                    <c:if test="${not empty usuarioEditar}">
                        <a href="${pageContext.request.contextPath}/admin/usuarios" class="btn-cancelar">Cancelar</a>
                    </c:if>
                </div>
            </div>
        </form>
    </div>

    <!-- Tabla de usuarios -->
    <h3>Usuarios registrados</h3>
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Rol</th>
                <th>Acciones</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="u" items="${listaUsuarios}">
                <tr>
                    <td><c:out value="${u.id}"/></td>
                    <td><c:out value="${u.nombre}"/></td>
                    <td><c:out value="${u.correo}"/></td>
                    <td><c:out value="${u.rol}"/></td>
                    <td>
                        <div class="acciones">
                            <a href="${pageContext.request.contextPath}/admin/usuarios?editar=${u.id}" class="btn-editar">Editar</a>
                            <c:choose>
                                <c:when test="${u.id == adminId}">
                                    <span class="tu">(tu cuenta)</span>
                                </c:when>
                                <c:otherwise>
                                    <form action="${pageContext.request.contextPath}/admin/usuarios" method="POST"
                                          onsubmit="return confirm('¿Eliminar este usuario?');" style="margin:0;">
                                        <input type="hidden" name="accion" value="eliminar">
                                        <input type="hidden" name="id" value="${u.id}">
                                        <button type="submit" class="btn-eliminar">Eliminar</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty listaUsuarios}">
                <tr>
                    <td colspan="5" style="text-align:center; color:#666; padding:20px;">No hay usuarios registrados.</td>
                </tr>
            </c:if>
        </tbody>
    </table>

</div>
</body>
</html>
