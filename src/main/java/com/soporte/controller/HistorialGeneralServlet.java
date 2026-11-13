package com.soporte.controller;

import com.soporte.model.Usuario;
import com.soporte.service.HistorialService;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Historial general de cambios de estado de todos los tickets (solo administrador).
 * URL: /admin/historial
 */
@WebServlet("/admin/historial")
public class HistorialGeneralServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/admin/historial.jsp";

    private final HistorialService servicio = new HistorialService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Solo entra un usuario con sesión y rol ADMINISTRADOR
        HttpSession session = request.getSession(false);
        Usuario usuario = (session == null) ? null : (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMINISTRADOR".equals(usuario.getRol())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            request.setAttribute("historial", servicio.listarTodo());
        } catch (SQLException e) {
            throw new ServletException("No se pudo cargar el historial", e);
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
