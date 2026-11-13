package com.soporte.service;

import com.soporte.dao.TicketDAO;
import com.soporte.dao.UsuarioDAO;
import com.soporte.model.Ticket;
import com.soporte.model.Usuario;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Capa de lógica de negocio para los tickets.
 * Aplica las reglas (quién puede hacer qué), deja el registro en el historial
 * a través de TicketDAO y avisa por correo al cliente cuando cambia el estado.
 * Los métodos devuelven null si todo salió bien, o el mensaje de error
 * que se le mostrará al usuario.
 */
public class TicketService {

    private static final List<String> ESTADOS_TECNICO =
            Arrays.asList("Abierto", "Asignado", "En proceso", "Resuelto");

    private static final int TITULO_MAX = 150;
    private static final int DESCRIPCION_MAX = 2000;
    private static final String MENSAJE_ERROR_BD = "Error de base de datos. Intenta de nuevo.";

    private final TicketDAO ticketDAO = new TicketDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final NotificacionService notificacionService = new NotificacionService();

    /**
     * Un cliente crea un ticket nuevo (queda en estado 'Abierto').
     */
    public String crearTicket(String titulo, String descripcion, Usuario cliente) {
        if (titulo == null || titulo.trim().isEmpty()) {
            return "El título es obligatorio.";
        }
        if (titulo.trim().length() > TITULO_MAX) {
            return "El título no puede superar los " + TITULO_MAX + " caracteres.";
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return "La descripción es obligatoria.";
        }
        if (descripcion.trim().length() > DESCRIPCION_MAX) {
            return "La descripción no puede superar los " + DESCRIPCION_MAX + " caracteres.";
        }

        Ticket ticket = new Ticket();
        ticket.setTitulo(titulo.trim());
        ticket.setDescripcion(descripcion.trim());
        ticket.setClienteId(cliente.getId());

        try {
            ticketDAO.crearTicket(ticket);
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return "No se pudo crear el ticket por un problema del sistema. Intenta de nuevo más tarde.";
        }
    }

    /**
     * Un técnico cambia el estado de uno de SUS tickets.
     */
    public String cambiarEstadoTecnico(int ticketId, String estadoRecibido, Usuario tecnico) {
        String estado = normalizarEstado(estadoRecibido, ESTADOS_TECNICO);
        if (estado == null) {
            return "Estado no válido.";
        }

        try {
            Ticket ticket = ticketDAO.buscarPorId(ticketId);
            if (ticket == null) {
                return "El ticket no existe.";
            }
            if (ticket.getTecnicoId() == null || ticket.getTecnicoId() != tecnico.getId()) {
                return "No puedes modificar tickets que no tienes asignados.";
            }
            if ("Cerrado".equals(ticket.getEstado())) {
                return "El ticket está cerrado y ya no se puede modificar.";
            }

            String anterior = ticketDAO.cambiarEstado(
                    ticketId, estado, tecnico.getId(), tecnico.getId(),
                    "Estado actualizado por el técnico");
            if (anterior == null) {
                return "No se pudo actualizar: el ticket no te pertenece o no existe.";
            }

            // Solo se avisa al cliente si el estado realmente cambió
            if (!anterior.equals(estado)) {
                notificarCliente(ticket, anterior, estado);
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return MENSAJE_ERROR_BD;
        }
    }

    /**
     * El administrador asigna un ticket a un técnico. El ticket pasa a 'Asignado'.
     */
    public String asignarTecnico(int ticketId, int tecnicoId, Usuario admin) {
        try {
            Ticket ticket = ticketDAO.buscarPorId(ticketId);
            if (ticket == null) {
                return "El ticket no existe.";
            }
            if ("Cerrado".equals(ticket.getEstado())) {
                return "No se puede asignar un ticket cerrado.";
            }

            // Se comprueba que el usuario elegido exista y de verdad sea técnico
            Usuario tecnico = usuarioDAO.buscarPorId(tecnicoId);
            if (tecnico == null || !"TECNICO".equals(tecnico.getRol())) {
                return "El técnico seleccionado no es válido.";
            }

            String anterior = ticketDAO.asignarTecnico(
                    ticketId, tecnicoId, admin.getId(),
                    "Asignado al técnico " + tecnico.getNombre());
            if (anterior == null) {
                return "El ticket no existe.";
            }

            if (!anterior.equals("Asignado")) {
                notificarCliente(ticket, anterior, "Asignado");
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return MENSAJE_ERROR_BD;
        }
    }

    /**
     * El cliente aprueba la solución de uno de SUS tickets en estado 'Resuelto'.
     * El ticket pasa a 'Cerrado' automáticamente y queda en el historial.
     */
    public String aprobarSolucion(int ticketId, Usuario cliente) {
        try {
            Ticket ticket = ticketDAO.buscarPorId(ticketId);
            if (ticket == null) {
                return "El ticket no existe.";
            }
            if (ticket.getClienteId() != cliente.getId()) {
                return "Solo puedes aprobar tus propios tickets.";
            }
            if (!"Resuelto".equals(ticket.getEstado())) {
                return "Solo puedes aprobar tickets en estado Resuelto.";
            }

            // El DAO vuelve a comprobar dueño y estado dentro de la transacción
            boolean cerrado = ticketDAO.cerrarTicketResuelto(
                    ticketId, cliente.getId(), "Solución aprobada por el cliente");
            if (!cerrado) {
                return "No se pudo cerrar el ticket. Verifica que siga en estado Resuelto.";
            }

            notificarCliente(ticket, "Resuelto", "Cerrado");
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return MENSAJE_ERROR_BD;
        }
    }

    // Busca al cliente dueño del ticket y le envía el correo.
    private void notificarCliente(Ticket ticket, String estadoAnterior, String estadoNuevo) {
        try {
            Usuario cliente = usuarioDAO.buscarPorId(ticket.getClienteId());
            if (cliente != null) {
                notificacionService.notificarCambioEstado(ticket, cliente, estadoAnterior, estadoNuevo);
            }
        } catch (SQLException e) {
            System.err.println("[Correo] No se pudo buscar al cliente del ticket #" + ticket.getId());
            e.printStackTrace();
        }
    }

    private String normalizarEstado(String recibido, List<String> permitidos) {
        if (recibido == null) {
            return null;
        }
        String limpio = recibido.trim();
        for (String permitido : permitidos) {
            if (permitido.equalsIgnoreCase(limpio)) {
                return permitido;
            }
        }
        return null;
    }
}
