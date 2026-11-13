package com.soporte.controller;

import com.soporte.model.Ticket;
import com.soporte.model.Usuario;
import com.soporte.service.DetalleTicketService;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Muestra el detalle de un ticket y su historial de cambios de estado.
 * URL: /ticket?id=NUMERO  (disponible para cualquier usuario con sesión, según permisos del servicio)
 */
@WebServlet("/ticket")
public class TicketDetalleServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/ticket/detalle.jsp";

    private final DetalleTicketService servicio = new DetalleTicketService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Usuario usuario = (session == null) ? null : (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int id = parseId(request.getParameter("id"));
        if (id < 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Identificador de ticket no válido.");
            return;
        }

        try {
            Ticket ticket = servicio.buscarVisible(id, usuario);
            if (ticket == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "El ticket no existe o no tienes permiso para verlo.");
                return;
            }
            request.setAttribute("ticket", ticket);
            request.setAttribute("historial", servicio.historial(id));
        } catch (SQLException e) {
            throw new ServletException("No se pudo cargar el ticket", e);
        }

        request.setAttribute("volverUrl", request.getContextPath() + paginaInicio(usuario.getRol()));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    // Panel al que vuelve cada rol con el botón "Volver"
    private String paginaInicio(String rol) {
        if ("ADMINISTRADOR".equals(rol)) {
            return "/admin";
        }
        if ("TECNICO".equals(rol)) {
            return "/tecnico/dashboard";
        }
        return "/cliente/dashboard";
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
}
