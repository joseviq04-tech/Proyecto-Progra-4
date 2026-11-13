package com.soporte.ws;

import com.soporte.dao.TicketDAO;
import com.soporte.model.Ticket;
import java.sql.SQLException;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Servicio web SOAP que permite consultar el estado de un ticket.
 * WSDL: http://localhost:8080/sistema-tickets/ws/ticketService?wsdl
 */
@WebService(serviceName = "TicketService")
public class TicketWebService {

    private final TicketDAO ticketDAO = new TicketDAO();

    @WebMethod(operationName = "consultarEstadoTicket")
    public String consultarEstadoTicket(@WebParam(name = "ticketId") int ticketId) {
        // JAX-WS convierte un valor no numérico (por ejemplo "abc") en 0, así que
        // cualquier ID menor o igual a 0 se rechaza como no válido
        if (ticketId <= 0) {
            return "El ID del ticket no es válido. Debe ser un número entero positivo.";
        }

        try {
            Ticket ticket = ticketDAO.buscarPorId(ticketId);
            if (ticket == null) {
                return "Ticket con ID " + ticketId + " no encontrado en el sistema.";
            }
            return "El ticket #" + ticket.getId() + " (" + ticket.getTitulo()
                    + ") se encuentra en estado: " + ticket.getEstado();
        } catch (SQLException e) {
            e.printStackTrace();
            return "No se pudo consultar el ticket. Intenta de nuevo.";
        }
    }
}
