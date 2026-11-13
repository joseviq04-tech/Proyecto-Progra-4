package com.soporte.dao;

import com.soporte.model.HistorialCambio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HistorialDAO {

    // Máximo de filas que se muestran en el historial general
    private static final int LIMITE_GENERAL = 500;

    /**
     * Registra un cambio de estado.
     * Recibe la conexión de quien la llama (TicketDAO) para que el cambio del ticket
     * y su registro en el historial queden en la MISMA transacción: o se guardan los dos, o ninguno.
     */
    public void registrar(Connection conn, int ticketId, int usuarioId,
                          String estadoAnterior, String estadoNuevo, String comentario) throws SQLException {
        String sql = "INSERT INTO historial_cambios " +
                     "(ticket_id, usuario_id, estado_anterior, estado_nuevo, comentario) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            stmt.setInt(2, usuarioId);
            stmt.setString(3, estadoAnterior);
            stmt.setString(4, estadoNuevo);
            stmt.setString(5, comentario);
            stmt.executeUpdate();
        }
    }

    // Historial completo de un ticket, del cambio más antiguo al más reciente
    public List<HistorialCambio> listarPorTicket(int ticketId) throws SQLException {
        List<HistorialCambio> lista = new ArrayList<>();
        String sql = "SELECT h.id, h.ticket_id, h.usuario_id, u.nombre AS usuario_nombre, " +
                     "h.estado_anterior, h.estado_nuevo, h.comentario, h.fecha_cambio " +
                     "FROM historial_cambios h " +
                     "JOIN usuarios u ON h.usuario_id = u.id " +
                     "WHERE h.ticket_id = ? " +
                     "ORDER BY h.fecha_cambio, h.id";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HistorialCambio h = new HistorialCambio();
                    h.setId(rs.getInt("id"));
                    h.setTicketId(rs.getInt("ticket_id"));
                    h.setUsuarioId(rs.getInt("usuario_id"));
                    h.setUsuarioNombre(rs.getString("usuario_nombre"));
                    h.setEstadoAnterior(rs.getString("estado_anterior"));
                    h.setEstadoNuevo(rs.getString("estado_nuevo"));
                    h.setComentario(rs.getString("comentario"));
                    h.setFechaCambio(rs.getString("fecha_cambio"));
                    lista.add(h);
                }
            }
        }
        return lista;
    }

    // Todos los cambios de todos los tickets, del más reciente al más antiguo 
    public List<HistorialCambio> listarTodos() throws SQLException {
        List<HistorialCambio> lista = new ArrayList<>();
        String sql = "SELECT h.id, h.ticket_id, t.titulo AS ticket_titulo, h.usuario_id, " +
                     "u.nombre AS usuario_nombre, h.estado_anterior, h.estado_nuevo, " +
                     "h.comentario, h.fecha_cambio " +
                     "FROM historial_cambios h " +
                     "JOIN tickets t ON h.ticket_id = t.id " +
                     "JOIN usuarios u ON h.usuario_id = u.id " +
                     "ORDER BY h.fecha_cambio DESC, h.id DESC " +
                     "LIMIT " + LIMITE_GENERAL;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                HistorialCambio h = new HistorialCambio();
                h.setId(rs.getInt("id"));
                h.setTicketId(rs.getInt("ticket_id"));
                h.setTicketTitulo(rs.getString("ticket_titulo"));
                h.setUsuarioId(rs.getInt("usuario_id"));
                h.setUsuarioNombre(rs.getString("usuario_nombre"));
                h.setEstadoAnterior(rs.getString("estado_anterior"));
                h.setEstadoNuevo(rs.getString("estado_nuevo"));
                h.setComentario(rs.getString("comentario"));
                h.setFechaCambio(rs.getString("fecha_cambio"));
                lista.add(h);
            }
        }
        return lista;
    }
}
