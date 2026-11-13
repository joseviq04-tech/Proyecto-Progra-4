package com.soporte.controller;

import com.soporte.dao.TicketDAO;
import com.soporte.dao.UsuarioDAO;
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

@WebServlet({"/admin", "/admin/dashboard", "/admin/asignar"})
public class AdminServlet extends HttpServlet {

    private static final String MENSAJE = "adminMensaje";
    private static final String ERROR = "adminError";

    private final TicketDAO ticketDAO = new TicketDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final TicketService ticketService = new TicketService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Usuario admin = adminActual(request);
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        HttpSession session = request.getSession(false);
        moverMensaje(session, request, MENSAJE);
        moverMensaje(session, request, ERROR);
        
        List<Ticket> listaTickets = new ArrayList<>();
        List<Usuario> listaTecnicos = new ArrayList<>();
        try {
            listaTickets = ticketDAO.listarTodos();
            listaTecnicos = usuarioDAO.listarPorRol("TECNICO");
        } catch (SQLException e) {
            log("No se pudieron cargar los datos del panel de administración", e);
            request.setAttribute(ERROR, "No se pudieron cargar los datos. Intenta de nuevo más tarde.");
        }
        
        request.setAttribute("listaTickets", listaTickets);
        request.setAttribute("listaTecnicos", listaTecnicos);
        request.getRequestDispatcher("/WEB-INF/views/admin/panel.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Usuario admin = adminActual(request);
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        int idTicket = parseId(request.getParameter("idTicket"));
        int idTecnico = parseId(request.getParameter("idTecnico"));
        
        String error;
        if (idTicket < 0 || idTecnico < 0) {
            error = "Debes seleccionar un ticket y un técnico válidos.";
        } else {
            error = ticketService.asignarTecnico(idTicket, idTecnico, admin);
        }
        
        HttpSession session = request.getSession();
        if (error != null) {
            session.setAttribute(ERROR, error);
        } else {
            session.setAttribute(MENSAJE, "Ticket asignado correctamente.");
        }
        
        // Redirige de vuelta al panel de administración tras asignar
        response.sendRedirect(request.getContextPath() + "/admin");
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
        if (session == null) {
            return;
        }
        Object valor = session.getAttribute(clave);
        if (valor != null) {
            request.setAttribute(clave, valor);
            session.removeAttribute(clave);
        }
    }
}
