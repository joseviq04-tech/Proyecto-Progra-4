package com.soporte.controller;

import com.soporte.dao.TicketDAO;
import com.soporte.model.Ticket;
import com.soporte.model.Usuario;
import com.soporte.service.TicketService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/cliente/dashboard")
public class ClienteServlet extends HttpServlet {

    private static final String MENSAJE = "clienteMensaje";
    private static final String ERROR = "clienteError";

    private final TicketDAO ticketDAO = new TicketDAO();
    private final TicketService ticketService = new TicketService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Usuario usuario = (session == null) ? null : (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null || !"CLIENTE".equals(usuario.getRol())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        moverMensaje(session, request, MENSAJE);
        moverMensaje(session, request, ERROR);

        // Cargamos solo los tickets de este cliente
        List<Ticket> listaTickets = new ArrayList<>();
        try {
            listaTickets = ticketDAO.listarPorCliente(usuario.getId());
        } catch (SQLException e) {
            log("No se pudieron cargar los tickets del cliente " + usuario.getId(), e);
            request.setAttribute(ERROR, "No se pudieron cargar tus tickets. Intenta de nuevo más tarde.");
        }
        request.setAttribute("listaTickets", listaTickets);

        request.getRequestDispatcher("/WEB-INF/views/cliente/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Usuario usuario = (session == null) ? null : (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null || !"CLIENTE".equals(usuario.getRol())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String accion = request.getParameter("accion");
        String error;
        String exito = null;

        if ("aprobar".equals(accion)) {
            // El cliente aprueba la solución: el ticket Resuelto pasa a Cerrado
            int idTicket = parseId(request.getParameter("ticketId"));
            if (idTicket < 0) {
                error = "Identificador de ticket no válido.";
            } else {
                error = ticketService.aprobarSolucion(idTicket, usuario);
                exito = "Solución aprobada. El ticket quedó cerrado.";
            }
        } else {
            // Por defecto: crear un ticket nuevo
            error = ticketService.crearTicket(
                    request.getParameter("titulo"),
                    request.getParameter("descripcion"),
                    usuario);
            exito = "Ticket creado correctamente.";
        }

        if (error != null) {
            session.setAttribute(ERROR, error);
        } else {
            session.setAttribute(MENSAJE, exito);
        }

        // Redireccionamos de vuelta al dashboard del cliente
        response.sendRedirect(request.getContextPath() + "/cliente/dashboard");
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
