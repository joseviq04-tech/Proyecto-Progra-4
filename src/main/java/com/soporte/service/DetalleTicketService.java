package com.soporte.service;

import com.soporte.dao.HistorialDAO;
import com.soporte.dao.TicketDAO;
import com.soporte.model.HistorialCambio;
import com.soporte.model.Ticket;
import com.soporte.model.Usuario;
import java.sql.SQLException;
import java.util.List;

/**
 * Capa de lógica de negocio para consultar el detalle de un ticket.
 * Decide quién puede ver cada ticket según su rol.
 */
public class DetalleTicketService {

    private final TicketDAO ticketDAO = new TicketDAO();
    private final HistorialDAO historialDAO = new HistorialDAO();

    /**
     * Devuelve el ticket solo si el usuario tiene permiso para verlo:
     * el administrador ve todos, el técnico solo los asignados a él y el cliente solo los suyos.
     * Devuelve null si el ticket no existe o si el usuario no puede verlo.
     */
    public Ticket buscarVisible(int ticketId, Usuario usuario) throws SQLException {
        Ticket ticket = ticketDAO.buscarPorId(ticketId);
        if (ticket == null) {
            return null;
        }

        switch (usuario.getRol()) {
            case "ADMINISTRADOR":
                return ticket;
            case "TECNICO":
                boolean esSuyo = ticket.getTecnicoId() != null && ticket.getTecnicoId() == usuario.getId();
                return esSuyo ? ticket : null;
            case "CLIENTE":
                return (ticket.getClienteId() == usuario.getId()) ? ticket : null;
            default:
                return null;
        }
    }

    public List<HistorialCambio> historial(int ticketId) throws SQLException {
        return historialDAO.listarPorTicket(ticketId);
    }
}
