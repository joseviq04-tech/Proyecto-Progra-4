package com.soporte.controller;

import com.soporte.dao.TicketDAO;
import com.soporte.model.Ticket;
import com.soporte.model.Usuario;
import com.soporte.service.TicketService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet({"/tecnico", "/tecnico/dashboard"})
public class TecnicoServlet extends HttpServlet {

    private final TicketDAO ticketDAO = new TicketDAO();
    private final TicketService ticketService = new TicketService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Validar sesión activa y rol TECNICO
        Usuario usuario = (session == null) ? null : (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"TECNICO".equals(usuario.getRol())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Object errorMsg = session.getAttribute("errorMsg");
        if (errorMsg != null) {
            request.setAttribute("errorMsg", errorMsg);
            session.removeAttribute("errorMsg");
        }
        
        // Cargar exclusivamente los tickets de este técnico
        List<Ticket> listaTickets = new ArrayList<>();
        try {
            listaTickets = ticketDAO.listarPorTecnico(usuario.getId());
        } catch (SQLException e) {
            log("No se pudieron cargar los tickets del técnico " + usuario.getId(), e);
            request.setAttribute("errorMsg", "No se pudieron cargar tus tickets. Intenta de nuevo más tarde.");
        }
        request.setAttribute("listaTickets", listaTickets);
        
        request.getRequestDispatcher("/WEB-INF/views/tecnico/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        Usuario usuario = (session == null) ? null : (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"TECNICO".equals(usuario.getRol())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        int idTicket = parseId(request.getParameter("ticketId"));
        String estado = request.getParameter("estado");
        
        String error;
        if (idTicket < 0 || estado == null) {
            error = "Datos no válidos.";
        } else {
            // Las reglas (dueño del ticket, estados permitidos, historial, correo) están en el servicio
            error = ticketService.cambiarEstadoTecnico(idTicket, estado, usuario);
        }
        
        if (error != null) {
            session.setAttribute("errorMsg", error);
        }
        
        response.sendRedirect(request.getContextPath() + "/tecnico/dashboard");
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
