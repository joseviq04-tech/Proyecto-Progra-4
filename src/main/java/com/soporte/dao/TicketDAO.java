package com.soporte.dao;

import com.soporte.model.Ticket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los tickets (JDBC con consultas parametrizadas).
 * Todos los métodos lanzan SQLException: la capa que los llama decide qué mensaje mostrar.
 */
public class TicketDAO {

    private final HistorialDAO historialDAO = new HistorialDAO();

    // 1. Listar todos los tickets (con nombres de cliente y técnico, para el Admin)
    public List<Ticket> listarTodos() throws SQLException {
        List<Ticket> lista = new ArrayList<>();
        String sql = "SELECT t.id, t.titulo, t.descripcion, t.estado, t.cliente_id, t.tecnico_id, t.fecha_creacion, " +
                     "c.nombre AS cliente_nombre, tec.nombre AS tecnico_nombre " +
                     "FROM tickets t " +
                     "JOIN usuarios c ON t.cliente_id = c.id " +
                     "LEFT JOIN usuarios tec ON t.tecnico_id = tec.id " +
                     "ORDER BY t.id";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearConNombres(rs));
            }
        }
        return lista;
    }

    // 2. Listar tickets asignados a un técnico específico
    public List<Ticket> listarPorTecnico(int tecnicoId) throws SQLException {
        List<Ticket> lista = new ArrayList<>();
        String sql = "SELECT id, titulo, descripcion, estado, cliente_id, tecnico_id, fecha_creacion " +
                     "FROM tickets WHERE tecnico_id = ? ORDER BY id";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tecnicoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearBasico(rs));
                }
            }
        }
        return lista;
    }

    // 3. Listar tickets creados por un cliente específico
    public List<Ticket> listarPorCliente(int clienteId) throws SQLException {
        List<Ticket> lista = new ArrayList<>();
        String sql = "SELECT id, titulo, descripcion, estado, cliente_id, tecnico_id, fecha_creacion " +
                     "FROM tickets WHERE cliente_id = ? ORDER BY id";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearBasico(rs));
                }
            }
        }
        return lista;
    }

    // 4. Crear un nuevo ticket (queda en estado 'Abierto')
    public void crearTicket(Ticket ticket) throws SQLException {
        String sql = "INSERT INTO tickets (titulo, descripcion, estado, cliente_id) VALUES (?, ?, 'Abierto', ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticket.getTitulo());
            stmt.setString(2, ticket.getDescripcion());
            stmt.setInt(3, ticket.getClienteId());
            stmt.executeUpdate();
        }
    }

    // 5. Buscar un ticket por su id (con los nombres de cliente y técnico). Devuelve null si no existe.
    public Ticket buscarPorId(int ticketId) throws SQLException {
        String sql = "SELECT t.id, t.titulo, t.descripcion, t.estado, t.cliente_id, t.tecnico_id, t.fecha_creacion, " +
                     "c.nombre AS cliente_nombre, tec.nombre AS tecnico_nombre " +
                     "FROM tickets t " +
                     "JOIN usuarios c ON t.cliente_id = c.id " +
                     "LEFT JOIN usuarios tec ON t.tecnico_id = tec.id " +
                     "WHERE t.id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearConNombres(rs);
                }
            }
        }
        return null;
    }

    /**
     * 6. Cambia el estado de un ticket y lo registra en el historial, todo en una transacción.
     *
     * @param tecnicoRequerido si no es null, el cambio solo se hace si el ticket está asignado
     *                         a ese técnico (así un técnico no puede tocar tickets ajenos).
     * @return el estado anterior del ticket, o null si el ticket no existe o no cumple
     *         la condición del técnico. Si el estado anterior es igual al nuevo, no se cambia nada.
     */
    public String cambiarEstado(int ticketId, String estadoNuevo, int usuarioId,
                                Integer tecnicoRequerido, String comentario) throws SQLException {

        String sqlBuscar = "SELECT estado FROM tickets WHERE id = ?"
                + (tecnicoRequerido != null ? " AND tecnico_id = ?" : "")
                + " FOR UPDATE";
        String sqlActualizar = "UPDATE tickets SET estado = ? WHERE id = ?";

        try (Connection conn = Conexion.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String estadoAnterior;

                // Se bloquea la fila mientras dura la transacción para que dos cambios simultáneos no se pisen
                try (PreparedStatement stmt = conn.prepareStatement(sqlBuscar)) {
                    stmt.setInt(1, ticketId);
                    if (tecnicoRequerido != null) {
                        stmt.setInt(2, tecnicoRequerido);
                    }
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return null;
                        }
                        estadoAnterior = rs.getString("estado");
                    }
                }

                if (estadoAnterior.equals(estadoNuevo)) {
                    conn.rollback();
                    return estadoAnterior;
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlActualizar)) {
                    stmt.setString(1, estadoNuevo);
                    stmt.setInt(2, ticketId);
                    stmt.executeUpdate();
                }

                historialDAO.registrar(conn, ticketId, usuarioId, estadoAnterior, estadoNuevo, comentario);

                conn.commit();
                return estadoAnterior;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * 7. Asigna un técnico a un ticket (el ticket pasa a 'Asignado') y lo registra en el historial.
     *
     * @return el estado anterior del ticket, o null si el ticket no existe.
     */
    public String asignarTecnico(int ticketId, int tecnicoId, int usuarioId, String comentario) throws SQLException {
        String sqlBuscar = "SELECT estado FROM tickets WHERE id = ? FOR UPDATE";
        String sqlActualizar = "UPDATE tickets SET tecnico_id = ?, estado = 'Asignado' WHERE id = ?";

        try (Connection conn = Conexion.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String estadoAnterior;

                try (PreparedStatement stmt = conn.prepareStatement(sqlBuscar)) {
                    stmt.setInt(1, ticketId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return null;
                        }
                        estadoAnterior = rs.getString("estado");
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlActualizar)) {
                    stmt.setInt(1, tecnicoId);
                    stmt.setInt(2, ticketId);
                    stmt.executeUpdate();
                }

                historialDAO.registrar(conn, ticketId, usuarioId, estadoAnterior, "Asignado", comentario);

                conn.commit();
                return estadoAnterior;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * 8. El cliente aprueba la solución: un ticket suyo en estado 'Resuelto' pasa a 'Cerrado'
     * y el cambio queda en el historial, todo en una transacción.
     *
     * @return true si se cerró; false si el ticket no existe, no es del cliente o no está 'Resuelto'.
     */
    public boolean cerrarTicketResuelto(int ticketId, int clienteId, String comentario) throws SQLException {
        String sqlBuscar = "SELECT estado FROM tickets " +
                           "WHERE id = ? AND cliente_id = ? AND estado = 'Resuelto' FOR UPDATE";
        String sqlActualizar = "UPDATE tickets SET estado = 'Cerrado' WHERE id = ?";

        try (Connection conn = Conexion.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlBuscar)) {
                    stmt.setInt(1, ticketId);
                    stmt.setInt(2, clienteId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlActualizar)) {
                    stmt.setInt(1, ticketId);
                    stmt.executeUpdate();
                }

                // El usuario que hace el cambio es el propio cliente
                historialDAO.registrar(conn, ticketId, clienteId, "Resuelto", "Cerrado", comentario);

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }


    private Ticket mapearBasico(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getInt("id"));
        ticket.setTitulo(rs.getString("titulo"));
        ticket.setDescripcion(rs.getString("descripcion"));
        ticket.setEstado(rs.getString("estado"));
        ticket.setClienteId(rs.getInt("cliente_id"));
        ticket.setTecnicoId((Integer) rs.getObject("tecnico_id"));
        ticket.setFechaCreacion(rs.getString("fecha_creacion"));
        return ticket;
    }

    private Ticket mapearConNombres(ResultSet rs) throws SQLException {
        Ticket ticket = mapearBasico(rs);
        ticket.setClienteNombre(rs.getString("cliente_nombre"));
        ticket.setTecnicoNombre(rs.getString("tecnico_nombre"));
        return ticket;
    }
}
