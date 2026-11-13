package com.soporte.controller;

import com.soporte.model.Usuario;
import com.soporte.service.UsuarioService;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Gestión de usuarios (solo administrador): listar, crear, editar y eliminar.
 * La validación y las reglas están en UsuarioService.
 */
@WebServlet("/admin/usuarios")
public class UsuarioServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/admin/usuarios.jsp";
    private static final String MENSAJE = "usuariosMensaje";
    private static final String ERROR = "usuariosError";

    private final UsuarioService servicio = new UsuarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario admin = adminActual(request);
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            // Si viene ?editar=ID, se carga ese usuario en el formulario de edición
            int idEditar = parseId(request.getParameter("editar"));
            if (idEditar > 0) {
                Usuario aEditar = servicio.buscarPorId(idEditar);
                if (aEditar == null) {
                    request.setAttribute(ERROR, "El usuario que quieres editar no existe.");
                } else {
                    request.setAttribute("usuarioEditar", aEditar);
                }
            }
            request.setAttribute("listaUsuarios", servicio.listar());
        } catch (SQLException e) {
            throw new ServletException("No se pudo cargar la lista de usuarios", e);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            moverMensaje(session, request, MENSAJE);
            moverMensaje(session, request, ERROR);
        }

        request.setAttribute("adminId", admin.getId());
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario admin = adminActual(request);
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String accion = request.getParameter("accion");
        String error;
        String exito = null;

        if ("crear".equals(accion)) {
            error = servicio.crear(
                    request.getParameter("nombre"),
                    request.getParameter("correo"),
                    request.getParameter("password"),
                    request.getParameter("rol"));
            exito = "Usuario creado correctamente.";

        } else if ("actualizar".equals(accion)) {
            int id = parseId(request.getParameter("id"));
            if (id < 0) {
                error = "Identificador de usuario no válido.";
            } else {
                error = servicio.actualizar(
                        id,
                        request.getParameter("nombre"),
                        request.getParameter("correo"),
                        request.getParameter("password"),
                        request.getParameter("rol"),
                        admin.getId());
                exito = "Usuario actualizado correctamente.";
            }

        } else if ("eliminar".equals(accion)) {
            int id = parseId(request.getParameter("id"));
            if (id < 0) {
                error = "Identificador de usuario no válido.";
            } else {
                error = servicio.eliminar(id, admin.getId());
                exito = "Usuario eliminado correctamente.";
            }

        } else {
            error = "Acción no válida.";
        }

        HttpSession session = request.getSession();
        if (error != null) {
            session.setAttribute(ERROR, error);
        } else {
            session.setAttribute(MENSAJE, exito);
        }

        // Patrón Post/Redirect/Get: evita reenviar el formulario al recargar la página
        response.sendRedirect(request.getContextPath() + "/admin/usuarios");
    }

    // Devuelve el usuario de la sesión solo si es administrador; si no, null
    private Usuario adminActual(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMINISTRADOR".equals(usuario.getRol())) {
            return null;
        }
        return usuario;
    }

    // Convierte el texto a número; devuelve -1 si no es un entero válido
    private int parseId(String texto) {
        if (texto == null) {
            return -1;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void moverMensaje(HttpSession session, HttpServletRequest request, String clave) {
        Object valor = session.getAttribute(clave);
        if (valor != null) {
            request.setAttribute(clave, valor);
            session.removeAttribute(clave);
        }
    }
}
